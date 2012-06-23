/*
 * Copyright 2011 - 2012 by the CloudRAID Team
 * see AUTHORS for more details.
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

package de.dhbw_mannheim.cloudraid.config.cli;

import java.io.Console;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;

import de.dhbw_mannheim.cloudraid.config.impl.Config;
import de.dhbw_mannheim.cloudraid.config.exceptions.ConfigException;

/**
 * @author Markus Holtermann
 * 
 */
public class Configurator {

	private static Config config;

	/**
	 * @param args
	 *            the arguments
	 */
	public static void main(String[] args) {
		config = new Config();
		Console cons = System.console();
		if (args.length == 0 || args.length > 4) {
			System.err.println("Invalid number of arguments!");
			Configurator.help();
			System.exit(1);
		} else {
			if (!(args[0].equals("get") || args[0].equals("list") || args[0]
					.equals("set"))) {
				System.err.println("Invalid argument!");
				Configurator.help();
				System.exit(1);
			}
			if ((args[0].equals("get") && args.length != 2)
					|| (args[0].equals("list") && args.length != 1)
					|| (args[0].equals("set") && args.length != 3 && args.length != 4)) {
				System.err.println("Invalid number of arguments!");
				Configurator.help();
				System.exit(1);
			}
		}
		if (cons != null) {
			char[] password = cons
					.readPassword("Please enter the password to access the configuration file: ");
			config.init(new String(password));
			System.out.println();

			if (args[0].equals("get")) {
				get(args[1]);
			} else if (args[0].equals("list")) {
				list();
			} else if (args[0].equals("set")) {
				if (args[1].equals("-e") || args[1].equals("--encrypted")) {
					set(true, args[2], args[3]);
				} else {
					set(false, args[1], args[2]);
				}
				config.save();
			}
		} else {
			System.err.println("Cannot get console for password input.");
			System.exit(1);
		}
	}

	private static void help() {
		System.out
				.println("Usage: java de.dhbw_mannheim.cloudraid.cli.Configurator COMMAND");
		System.out.println("    COMMAND:");
		System.out.println("        get KEY");
		System.out.println("        list");
		System.out.println("        set [ -e | --encrypted ] KEY VALUE");
	}

	private static void get(String key) {
		try {
			System.out.println(config.getString(key, null));
		} catch (ConfigException e) {
			System.out.println("Key \"" + key + "\" not found.");
		}
	}

	private static void list() {
		Set<String> s = config.getDefaultData().keySet();
		s.addAll(config.keySet());
		ArrayList<String> list = new ArrayList<String>(s);
		Collections.sort(list);
		for (String key : list) {
			try {
				if (!config.keyExists(key)) {
					System.out.println("* " + key + " = "
							+ config.getString(key, null));
				} else {
					System.out.println("  " + key + " = "
							+ config.getString(key, null));
				}
			} catch (ConfigException e) {
				System.out.println("Key \"" + key + "\" not found.");
			}
		}
		System.out.println();
		System.out.println("Elements marked with a * are default values!");
	}

	private static void set(boolean encrypted, String key, String value) {
		config.put(key, value, encrypted);
	}

}
