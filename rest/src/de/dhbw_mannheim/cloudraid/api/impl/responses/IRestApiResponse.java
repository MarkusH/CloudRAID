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

package de.dhbw_mannheim.cloudraid.api.impl.responses;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

/**
 * @author Markus Holtermann
 * 
 */
public interface IRestApiResponse {

	/**
	 * 
	 */
	public static String MIMETYPE = "text/plain";

	/**
	 * @param name
	 * @param payload
	 */
	public void addField(String name, String payload);

	/**
	 * @param payload
	 *            the content to set
	 */
	public void addPayload(String payload);

	/**
	 * @param map
	 */
	public void addRow(Map<String, Object> map);

	/**
	 * @throws IOException
	 * 
	 */
	public void send() throws IOException;

	/**
	 * @param name
	 * @param value
	 */
	public void setHeader(String name, String value);

	/**
	 * @param resp
	 */
	public void setResponseObject(HttpServletResponse resp);

	/**
	 * @param sc
	 */
	public void setStatusCode(int sc);

	public OutputStream getOutputStream() throws IOException;
}
