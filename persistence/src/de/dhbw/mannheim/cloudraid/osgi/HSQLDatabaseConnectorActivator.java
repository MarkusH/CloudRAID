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

package de.dhbw.mannheim.cloudraid.osgi;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import de.dhbw.mannheim.cloudraid.persistence.HSQLDatabaseConnector;
import de.dhbw.mannheim.cloudraid.persistence.IDatabaseConnector;

/**
 * @author Markus Holtermann
 * 
 */
public class HSQLDatabaseConnectorActivator implements BundleActivator {

	/**
	 * 
	 */
	private ServiceRegistration<?> databaseService;

	@Override
	public void start(BundleContext context) throws Exception {
		IDatabaseConnector databaseConnector = new HSQLDatabaseConnector();
		databaseService = context.registerService(
				IDatabaseConnector.class.getName(), databaseConnector, null);
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		databaseService.unregister();
	}

}
