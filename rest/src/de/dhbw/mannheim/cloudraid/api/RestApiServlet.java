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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.security.InvalidKeyException;
import java.util.ArrayList;
import java.util.Enumeration;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import de.dhbw.mannheim.cloudraid.api.RestApiUrlMapping.MatchResult;
import de.dhbw.mannheim.cloudraid.api.responses.JsonApiResponse;
import de.dhbw.mannheim.cloudraid.api.responses.PlainApiResponse;
import de.dhbw.mannheim.cloudraid.api.responses.RestApiResponse;
import de.dhbw.mannheim.cloudraid.persistence.IDatabaseConnector;
import de.dhbw.mannheim.cloudraid.util.Config;

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
	 * 
	 */
	private IDatabaseConnector database = null;

	/**
	 * @param database
	 * @throws IllegalArgumentException
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 */
	public RestApiServlet(IDatabaseConnector database)
			throws IllegalArgumentException, SecurityException,
			NoSuchMethodException {
		mappings.add(new RestApiUrlMapping("^/file/([^/]+)/$", "GET",
				RestApiServlet.class, "fileinfo"));
		mappings.add(new RestApiUrlMapping("^/file/new/([^/]+)/$",
				RestApiServlet.class, "fileNew"));
		mappings.add(new RestApiUrlMapping("^/filelist/$", "GET",
				RestApiServlet.class, "filelist"));
		mappings.add(new RestApiUrlMapping("^/user/add/$", "POST",
				RestApiServlet.class, "userAdd"));
		mappings.add(new RestApiUrlMapping("^/user/auth/$", "POST",
				RestApiServlet.class, "auth"));
		mappings.add(new RestApiUrlMapping("^/user/auth/logout/$", "GET",
				RestApiServlet.class, "authLogout"));
		this.database = database;
	}

	/**
	 * @param req
	 * @param resp
	 * @throws ServletException
	 * @throws IOException
	 */
	protected void doRequest(HttpServletRequest req, HttpServletResponse resp)
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

	@Override
	protected void doDelete(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		this.doRequest(req, resp);
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		this.doRequest(req, resp);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		this.doRequest(req, resp);
	}

	@Override
	protected void doPut(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		this.doRequest(req, resp);
	}

	/**
	 * @param req
	 * @param resp
	 * @param args
	 * @throws IOException
	 * @throws BadPaddingException
	 * @throws IllegalBlockSizeException
	 * @throws InvalidKeyException
	 */
	public void fileNew(HttpServletRequest req, RestApiResponse resp,
			ArrayList<String> args) throws IOException, InvalidKeyException,
			IllegalBlockSizeException, BadPaddingException {
		int i = 0;
		for (String arg : args) {
			resp.addField("" + i++, arg);
		}
		Enumeration<String> e = req.getHeaderNames();
		while (e.hasMoreElements()) {
			String header = e.nextElement();
			resp.addField(header, req.getHeader(header));
		}
		resp.addField("method", req.getMethod());

		int bufsize = Math.min(1024, req.getContentLength());
		String filename = args.get(0);

		BufferedInputStream bis = new BufferedInputStream(req.getInputStream(),
				bufsize);

		File f = new File(Config.getInstance().getString("split.input.dir",
				null)
				+ filename);
		f.getParentFile().mkdirs();

		BufferedOutputStream bos = new BufferedOutputStream(
				new FileOutputStream(f), bufsize);

		byte[] inputBytes = new byte[bufsize];
		int readLength;
		while ((readLength = bis.read(inputBytes)) >= 0) {
			bos.write(inputBytes, 0, readLength);
		}

		resp.addPayload(f.getAbsolutePath());
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
		if (!this.validateSession(req, resp)) {
			return;
		}
		HttpSession session = req.getSession();
		Enumeration<String> e = session.getAttributeNames();
		while (e.hasMoreElements()) {
			String attr = e.nextElement();
			resp.addField(attr, session.getAttribute(attr).toString());
		}
		resp.addPayload("X" + session.getCreationTime() + "Y"
				+ session.getLastAccessedTime() + "Z");
	}

	/**
	 * @param req
	 * @param resp
	 * @param args
	 */
	public void userAdd(HttpServletRequest req, RestApiResponse resp,
			ArrayList<String> args) {
		if (this.validateSession(req, resp)) {
			resp.setStatusCode(403);
			resp.addPayload("Already logged in!");
			return;
		}
		String username = req.getHeader("X-Username");
		String password = req.getHeader("X-Password");
		if (username == null || password == null) {
			resp.setStatusCode(401);
			resp.addPayload("Username or password missing!");
			return;
		}
		if (database.addUser(username, password)) {
			resp.setStatusCode(200);
			resp.addPayload("User created");
		} else {
			resp.setStatusCode(500);
			resp.addPayload("An error occured");
		}
	}

	/**
	 * @param req
	 * @param resp
	 * @param args
	 */
	public void auth(HttpServletRequest req, RestApiResponse resp,
			ArrayList<String> args) {
		String username = req.getHeader("X-Username");
		String password = req.getHeader("X-Password");
		password = password + "";
		HttpSession session = req.getSession(false);
		if (session != null) {
			resp.setStatusCode(301);
			resp.addPayload("Session already exists!");
			return;
		}
		session = req.getSession(true);
		if (session == null) {
			resp.setStatusCode(500);
			resp.addPayload("Session could ne be created!");
			return;
		}
		int id = database.authUser(username, password);
		if (id > -1) {
			session.setAttribute("auth", true);
			session.setAttribute("username", username);
			session.setAttribute("id", id);
		} else {
			session.invalidate();
			resp.setStatusCode(403);
			resp.addPayload("Credentials invalid!");
			return;
		}
	}

	/**
	 * @param req
	 * @param resp
	 * @param args
	 */
	public void authLogout(HttpServletRequest req, RestApiResponse resp,
			ArrayList<String> args) {
		if (!this.validateSession(req, resp)) {
			return;
		}
		req.getSession().invalidate();
	}

	/**
	 * @param req
	 * @param resp
	 * @return True if and only if all of the following points are true:
	 *         <ul>
	 *         <li>There is an existing session</li>
	 *         <li>The session id is taken from a cookie</li>
	 *         <li>The value of the session attribute <code>auth</code> is
	 *         <code>true</code></li>
	 *         </ul>
	 */
	private boolean validateSession(HttpServletRequest req, RestApiResponse resp) {
		HttpSession session = req.getSession(false);
		if (session == null) {
			resp.setStatusCode(301);
			resp.addPayload("Session does not exist!");
			return false;
		}
		if (!req.isRequestedSessionIdFromCookie()) {
			resp.setStatusCode(405);
			resp.addPayload("Session not submitted via Cookie!");
			return false;
		}
		if (!((Boolean) session.getAttribute("auth"))) {
			resp.setStatusCode(403);
			resp.addPayload("Not logged in!");
			return false;
		}
		return true;
	}

}