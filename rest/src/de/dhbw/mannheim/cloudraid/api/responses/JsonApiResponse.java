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
import java.util.HashMap;

import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Markus Holtermann
 * 
 */
public class JsonApiResponse implements IRestApiResponse {

	/**
	 * 
	 */
	public static String MIMETYPE = "application/json; charset=utf-8";

	/**
	 * 
	 */
	private HttpServletResponse resp = null;

	/**
	 * 
	 */
	private JSONArray table = new JSONArray();

	/**
	 * 
	 */
	private JSONObject jo = new JSONObject();

	@Override
	public void addField(String name, String payload) {
		try {
			this.jo.append(name, payload);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void addPayload(String payload) {
		this.addField("payload", payload);
	}

	@Override
	public void send() throws IOException {
		if (this.resp != null) {
			this.resp.setContentType(MIMETYPE);

			try {
				this.jo.append("payload", this.table);
			} catch (JSONException e) {
				e.printStackTrace();
			}

			this.resp.getWriter().write(this.jo.toString());
		}
	}

	@Override
	public void setHeader(String name, String value) {
		if (this.resp != null) {
			this.resp.setHeader(name, value);
		}
	}

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

	@Override
	public void addRow(HashMap<String, Object> map) {
		this.table.put(map);
	}

}
