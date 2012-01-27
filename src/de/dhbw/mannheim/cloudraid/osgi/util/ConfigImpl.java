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

package de.dhbw.mannheim.cloudraid.osgi.util;

import java.io.Console;
import java.io.IOException;

import de.dhbw.mannheim.cloudraid.util.Config;

public class ConfigImpl implements ConfigService {

	private Config config = null;

	public ConfigImpl(String password) {
		this.config = Config.getInstance();
		this.config.init(password);
		System.out.println(this.config);
	}

	@Override
	public Config getConfig() {
		return this.config;
	}

	public static String readPassword() throws IOException {
		Console console = System.console();
		String password = new String("");
		if (console == null) {
			System.out
					.print("Please enter the password in order to start the server: ");
			byte pw[] = new byte[255];
			int len = System.in.read(pw);
			password = new String(pw, 0, len);
			for (int i = 0; i < pw.length; i++) {
				pw[i] = 0;
			}
		} else {
			password = new String(
					console.readPassword("Please enter the password in order to start the server: "));
		}
		return password;
	}

}
