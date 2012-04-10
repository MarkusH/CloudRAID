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

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.dhbw.mannheim.cloudraid.api.RestApiUrlMapping.MatchResult;
import de.dhbw.mannheim.cloudraid.api.responses.JsonApiResponse;
import de.dhbw.mannheim.cloudraid.api.responses.PlainApiResponse;
import de.dhbw.mannheim.cloudraid.api.responses.RestApiResponse;

/**
 * @author Markus Holtermann
 * 
 */
public class RestApiServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1967811240645738359L;

	/**
	 * 
	 */
	private static ArrayList<RestApiUrlMapping> mappings = new ArrayList<RestApiUrlMapping>();

	/**
	 * @throws IllegalArgumentException
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 */
	public RestApiServlet() throws IllegalArgumentException, SecurityException,
			NoSuchMethodException {
		mappings.add(new RestApiUrlMapping("^/file/([^/]+)/$", "GET",
				RestApiServlet.class.getMethod("fileinfo",
						HttpServletRequest.class, RestApiResponse.class,
						ArrayList.class)));
		mappings.add(new RestApiUrlMapping("^/filelist/$", "GET",
				RestApiServlet.class.getMethod("filelist",
						HttpServletRequest.class, RestApiResponse.class,
						ArrayList.class)));
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		String mime = req.getHeader("Accept");
		RestApiResponse r;
		if (JsonApiResponse.MIMETYPE.equals(mime)) {
			r = new JsonApiResponse();
		} else if (PlainApiResponse.MIMETYPE.equals(mime)) {
			r = new PlainApiResponse();
		} else {
			r = new PlainApiResponse();
		}
		r.setResponseObject(resp);

		MatchResult mr = null;
		for (RestApiUrlMapping mapping : mappings) {
			System.out.println(mapping);
			mr = mapping.match(req);
			if (null != mr) {
				try {
					mr.getFunction().invoke(this, req, r, mr.getArgs());
				} catch (IllegalArgumentException e) {
					RestApiComponent.error500(req, r, e.getMessage());
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					RestApiComponent.error500(req, r, e.getMessage());
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					RestApiComponent.error500(req, r, e.getMessage());
					e.printStackTrace();
				}
				break;
			}
		}
		if (null == mr) { // nothing matched
			RestApiComponent.error404(req, r);
		}

		r.send();
	}

	/**
	 * @param req
	 * @param resp
	 * @param args
	 */
	public void fileinfo(HttpServletRequest req, RestApiResponse resp,
			ArrayList<String> args) {
		int i = 0;
		for (String arg : args) {
			resp.addField("" + i++, arg);
		}
		resp.addPayload(req.toString());
	}

	/**
	 * @param req
	 * @param resp
	 * @param args
	 */
	public void filelist(HttpServletRequest req, RestApiResponse resp,
			ArrayList<String> args) {
		int i = 0;
		for (String arg : args) {
			resp.addField("" + i++, arg);
		}
		resp.addPayload(req.toString());
	}

}