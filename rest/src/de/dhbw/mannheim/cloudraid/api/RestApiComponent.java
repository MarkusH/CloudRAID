/*
 * Copyright 2011 - 2012 by the CloudRAID Team
 * see AUTHORS for more details
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at

 * http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package de.dhbw.mannheim.cloudraid.api;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;

import de.dhbw.mannheim.cloudraid.api.responses.IRestApiResponse;
import de.dhbw.mannheim.cloudraid.config.ICloudRAIDConfig;
import de.dhbw.mannheim.cloudraid.metadatamgr.IMetadataManager;

/**
 * @author Markus Holtermann
 * 
 */
public class RestApiComponent {

	/**
	 * Main path for the REST API
	 */
	private static final String SERVLET_ALIAS = "/";

	/**
	 * @param req
	 * @param resp
	 */
	public static void error404(HttpServletRequest req, IRestApiResponse resp) {
		resp.setStatusCode(404);
		resp.addPayload("No page matching " + req.getPathInfo());
	}

	/**
	 * @param req
	 * @param resp
	 * @param msg
	 */
	public static void error500(HttpServletRequest req, IRestApiResponse resp,
			String msg) {
		resp.setStatusCode(500);
		resp.addPayload("Servererror for page " + req.getPathInfo());
		resp.addField("msg", msg);
	}

	/**
	 * 
	 */
	private ICloudRAIDConfig config = null;

	/**
	 * 
	 */
	private IMetadataManager database = null;

	/**
	 * Service that handles all the request. Injected by the component.xml
	 */
	private HttpService httpService = null;

	/**
	 * @param configService
	 */
	public void setConfig(ICloudRAIDConfig configService) {
		this.config = configService;
	}

	/**
	 * @param configService
	 */
	public void unsetConfig(ICloudRAIDConfig configService) {
		httpService.unregister(SERVLET_ALIAS);
		this.config = null;
	}

	/**
	 * @param metadataService
	 */
	public void setMetadataMgr(IMetadataManager metadataService) {
		this.database = metadataService;
	}

	/**
	 * @param metadataService
	 */
	public void unsetMetadataMgr(IMetadataManager metadataService) {
		httpService.unregister(SERVLET_ALIAS);
		this.database = null;
	}

	/**
	 * @param httpService
	 */
	public void setHttpService(HttpService httpService) {
		this.httpService = httpService;
	}

	/**
	 * @param httpService
	 */
	public void unsetHttpService(HttpService httpService) {
		httpService.unregister(SERVLET_ALIAS);
		this.httpService = null;
	}

	/**
	 * Unregister the service.
	 */
	protected void shutdown() {
		httpService.unregister(SERVLET_ALIAS);
	}

	/**
	 * Initialize and start the service.
	 * 
	 * @param context
	 * 
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 * @throws IllegalArgumentException
	 */
	protected void startup(BundleContext context)
			throws IllegalArgumentException, SecurityException,
			NoSuchMethodException {
		try {
			ServiceReference<IMetadataManager> databaseServiceReference = context
					.getServiceReference(IMetadataManager.class);
			database = context.getService(databaseServiceReference);
			System.out.println("Staring up sevlet at " + SERVLET_ALIAS);
			RestApiServlet servlet = new RestApiServlet(database, config);
			httpService.registerServlet(SERVLET_ALIAS, servlet, null, null);
		} catch (ServletException e) {
			e.printStackTrace();
		} catch (NamespaceException e) {
			e.printStackTrace();
		}
	}

}