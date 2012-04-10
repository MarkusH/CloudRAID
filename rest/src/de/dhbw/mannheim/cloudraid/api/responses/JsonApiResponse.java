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

package de.dhbw.mannheim.cloudraid.api.responses;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Markus Holtermann
 * 
 */
public class JsonApiResponse implements RestApiResponse {

	/**
	 * 
	 */
	public static String MIMETYPE = "application/json";

	/**
	 * 
	 */
	private HttpServletResponse resp = null;

	/**
	 * 
	 */
	private JSONObject jo = new JSONObject();

	/*
	 * @see
	 * de.dhbw.mannheim.cloudraid.api.IRestApiResponse#addField(java.lang.String
	 * , java.lang.String)
	 */
	@Override
	public void addField(String name, String payload) {
		try {
			this.jo.append(name, payload);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	/*
	 * @see
	 * de.dhbw.mannheim.cloudraid.api.IRestApiResponse#addPayload(java.lang.
	 * String)
	 */
	@Override
	public void addPayload(String payload) {
		this.addField("payload", payload);
	}

	/*
	 * @see de.dhbw.mannheim.cloudraid.api.IRestApiResponse#send()
	 */
	@Override
	public void send() throws IOException {
		if (this.resp != null) {
			resp.setContentType(MIMETYPE);
			resp.getWriter().write(this.jo.toString());
		}
	}

	@Override
	public void setHeader(String name, String value) {
		if (this.resp != null) {
			this.resp.setHeader(name, value);
		}
	}

	/*
	 * @see
	 * de.dhbw.mannheim.cloudraid.api.IRestApiResponse#setResponseObject(javax
	 * .servlet.http.HttpServletResponse)
	 */
	@Override
	public void setResponseObject(HttpServletResponse resp) {
		this.resp = resp;
	}

	@Override
	public void setStatusCode(int sc) {
		if (this.resp != null) {
			this.resp.setStatus(sc);
		}
	}

}
