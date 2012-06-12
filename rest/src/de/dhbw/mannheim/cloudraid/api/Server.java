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

import com.sun.net.httpserver.HttpServer;

import de.dhbw.mannheim.cloudraid.util.Config;
import de.dhbw.mannheim.cloudraid.util.exceptions.InvalidConfigValueException;
import de.dhbw.mannheim.cloudraid.util.exceptions.MissingConfigValueException;

/**
 * @author Markus Holtermann
 */
public class Server {

	public static void main(String[] args) {
		Config config = Config.getInstance();
		config.init("");
		try {
			String host = Config.getInstance().getString("api.server.host", "");
			if (host.equals("")) {
				throw new InvalidConfigValueException();
			}
			int port = Config.getInstance().getInt("api.server.port", -1);
			if (port == -1) {
				throw new InvalidConfigValueException();
			}
			Server server = new Server(host, port);
			server.start();
		} catch (MissingConfigValueException e) {
			e.printStackTrace();
		} catch (InvalidConfigValueException e) {
			e.printStackTrace();
		}
	}

	private HttpServer server;

	/**
	 * @param host
	 *            The listening address
	 * @param port
	 *            The port to bind the service to
	 */
	public Server(String host, int port) {
		try {
			this.server = HttpServerFactory.create("http://" + host + ":"
					+ port + "/", new PackagesResourceConfig(
					"de.dhbw.mannheim.cloudraid.api.resources"));
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void start() {
		this.server.start();
	}
}
