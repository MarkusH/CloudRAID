/*
 * Copyright 2011 by the CloudRAID Team, see AUTHORS for more details.
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

package de.dhbw.mannheim.cloudraid.api.resources;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.UriInfo;

import de.dhbw.mannheim.cloudraid.api.JSONResource;

/**
 * @author Markus Holtermann
 */
@Path("/")
public class RootResource {

	// Allows to insert contextual objects into the class,
	// e.g. ServletContext, Request, Response, UriInfo
	@Context
	UriInfo uriInfo;
	@Context
	Request request;

	@DELETE
	@Path("delete")
	@Produces(MediaType.APPLICATION_JSON)
	public String delete() {
		JSONResource jr = new JSONResource();
		jr.addPayload("buz");
		return jr.write();
	}

	@GET
	@Path("get")
	@Produces(MediaType.APPLICATION_JSON)
	public String get() {
		JSONResource jr = new JSONResource();
		jr.addPayload("foo");
		return jr.write();
	}

	@GET
	@Produces(MediaType.TEXT_HTML)
	public String index() {
		StringBuffer sb = new StringBuffer();
		sb.append("<form method=\"GET\" enctype=\"application/x-www-form-urlencoded\" action=\"get\">GET: <input type=\"text\" name=\"value\" value=\"get test text\"><input type=\"submit\"></form>");
		sb.append("<form method=\"POST\" enctype=\"application/x-www-form-urlencoded\" action=\"post\">POST: <input type=\"text\" name=\"value\" value=\"post test text\"><input type=\"submit\"></form>");
		return sb.toString();
	}

	@POST
	@Path("post")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public String post(@FormParam("value") String value) {
		JSONResource jr = new JSONResource();
		jr.addPayload(value);
		return jr.write();
	}

	@PUT
	@Path("put")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_OCTET_STREAM)
	public String put(String content) {
		System.out.println(content);
		JSONResource jr = new JSONResource();
		jr.addPayload(content);
		return jr.write();
	}

}
