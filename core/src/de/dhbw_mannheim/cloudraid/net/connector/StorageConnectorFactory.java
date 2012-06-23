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

package de.dhbw_mannheim.cloudraid.net.connector;

import java.util.HashMap;

/**
 * @author Markus Holtermann
 */
public class StorageConnectorFactory {

	/**
	 * This factory loads the class <i>name</i> and passes <i>parameter</i> to
	 * the class creation
	 * 
	 * @param name
	 *            The class to load
	 * @param parameter
	 *            Initial parameters, such as user name / password or API
	 *            information
	 * @return An instance of the {@link IStorageConnector} or null in case an
	 *         error occured
	 */
	public static IStorageConnector create(String name,
			HashMap<String, String> parameter) {
		try {
			Class<?> klass = Class.forName(name);
			IStorageConnector connector = (IStorageConnector) klass
					.newInstance();
			connector.create(parameter);
			return connector;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return null;
	}

}
