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
public class PlainApiResponse implements IRestApiResponse {

	public static String DEFAULT_MIMETYPE = "text/plain; charset=utf-8";
	private String mime = null;

	private HttpServletResponse resp = null;

	@Override
	public void writeField(String name, String value) {
		writeLine(name + ":" + value);
	}

	@Override
	public void writeLine(String line) {
		if (this.resp != null) {
			String s = line + "\n";
			try {
				this.resp.getOutputStream().write(s.getBytes());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public OutputStream getOutputStream() throws IOException {
		if (this.resp != null) {
			return this.resp.getOutputStream();
		}
		return null;
	}

	@Override
	public void send() throws IOException {
		if (this.resp != null) {
			this.resp.setContentType(this.mime);
			this.resp.getOutputStream().flush();
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
		this.mime = DEFAULT_MIMETYPE;
	}

	@Override
	public void setStatusCode(int sc) {
		if (this.resp != null) {
			this.resp.setStatus(sc);
		}
	}

	@Override
	public void addRow(Map<String, Object> map) {
		StringBuffer table = new StringBuffer();
		for (Map.Entry<String, Object> e : map.entrySet()) {
			table.append("\""
					+ e.getValue().toString().replace("\\", "\\\\")
							.replace("\"", "\\\"") + "\",");
		}
		table.setLength(table.length() - 1);
		table.append("\n");
		try {
			this.resp.getOutputStream().write(table.toString().getBytes());
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	@Override
	public void setContentLength(int len) {
		if (this.resp != null) {
			this.resp.setContentLength(len);
		}
	}

	@Override
	public void setContentType(String type) {
		if (this.resp != null) {
			this.mime = type;
		}
	}

	@Override
	public void flush() throws IOException {
		if (this.resp != null) {
			this.resp.getOutputStream().flush();
		}
	}

	@Override
	public void write(String content) {
		if (this.resp != null) {
			try {
				this.resp.getOutputStream().write(content.getBytes());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
