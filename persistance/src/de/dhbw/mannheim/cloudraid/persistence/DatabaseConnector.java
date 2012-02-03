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

package de.dhbw.mannheim.cloudraid.persistence;

/**
 * An abstract class for defining an interface to access various database types.
 * 
 * @author Florian Bausch, Markus Holtermann
 * 
 */
public abstract class DatabaseConnector implements IDatabaseConnector {

	/**
	 * Creates a DatabaseConnector for a specific database type via reflection.
	 * 
	 * @param className
	 *            The full class name (with packages) of the class that extends
	 *            this abstract class and implements the database type specific
	 *            things.
	 * @return An instance of the DatabaseConnector or, if there was an error
	 *         creating the instance, an {@link HSQLDatabaseConnector} instance
	 *         as fall-back.
	 * @throws ClassNotFoundException
	 *             if the class cannot be found
	 * @throws IllegalAccessException
	 *             if the class or its nullary constructor is not accessible.
	 * @throws InstantiationException
	 *             if this Class represents an abstract class, an interface, an
	 *             array class, a primitive type, or void; or if the class has
	 *             no nullary constructor; or if the instantiation fails for
	 *             some other reason.
	 * @throws ClassCastException
	 *             if the given class is not of type {@link IDatabaseConnector}
	 */
	public static IDatabaseConnector getDatabaseConnector(String className)
			throws InstantiationException, IllegalAccessException,
			ClassNotFoundException, ClassCastException {
		return (IDatabaseConnector) Class.forName(className).newInstance();
	}

}
