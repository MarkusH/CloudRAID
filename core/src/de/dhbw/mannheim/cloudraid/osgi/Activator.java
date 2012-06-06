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
import org.osgi.framework.ServiceReference;

import de.dhbw.mannheim.cloudraid.persistence.IDatabaseConnector;
import de.dhbw.mannheim.cloudraid.util.Config;
import de.dhbw.mannheim.cloudraid.util.IPasswordManager;

/**
 * @author Markus Holtermann
 * 
 */
public class Activator implements BundleActivator {

	/**
	 * 
	 */
	private IPasswordManager pwdmngr = null;

	/**
	 * 
	 */
	private IDatabaseConnector database = null;

	/**
	 * 
	 */
	private Config config;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext
	 * )
	 */
	@Override
	public void start(BundleContext context) throws Exception {
		ServiceReference<IPasswordManager> passwordServiceReference = context
				.getServiceReference(IPasswordManager.class);
		pwdmngr = context.getService(passwordServiceReference);
		config = Config.getInstance();
		config.init(pwdmngr.getCredentials());

		ServiceReference<IDatabaseConnector> databaseServiceReference = context
				.getServiceReference(IDatabaseConnector.class);
		database = context.getService(databaseServiceReference);

		String databasename = config.getString("database.name", null);
		database.connect(databasename);
		database.initialize();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	@Override
	public void stop(BundleContext context) throws Exception {
		database.disconnect();
	}

}
