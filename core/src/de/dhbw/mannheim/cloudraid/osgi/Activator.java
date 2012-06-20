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

import java.io.File;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import de.dhbw.mannheim.cloudraid.fs.FileManager;
import de.dhbw.mannheim.cloudraid.fs.RecursiveFileSystemWatcher;
import de.dhbw.mannheim.cloudraid.metadatamgr.IMetadataManager;
import de.dhbw.mannheim.cloudraid.passwordmgr.IPasswordManager;
import de.dhbw.mannheim.cloudraid.util.Config;

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
	private IMetadataManager database = null;

	/**
	 * 
	 */
	private Config config;

	private RecursiveFileSystemWatcher recursiveFileSystemWatcher = null;

	private FileManager[] fileManagers = null;

	/**
	 * During the {@link BundleActivator#start(BundleContext) startup} this
	 * {@link BundleActivator} starts and initializes the following services and
	 * components (in order):
	 * <ul>
	 * <li>A {@link IPasswordManager} to handle passwords for the configuration</li>
	 * <li>A {@link Config} storing the I/O paths for RAID, the database name,
	 * number of threads, etc.</li>
	 * <li>A {@link IMetadataManager} that represents the underlying database
	 * used to store all file information</li>
	 * <li>A {@link RecursiveFileSystemWatcher} that permanently watches the
	 * split input directory for new files</li>
	 * <li>Multiple {@link FileManager} that handle new files, e.g. split and
	 * upload them</li>
	 * </ul>
	 */
	@Override
	public void start(BundleContext context) throws Exception {
		// Initialize the password manager
		ServiceReference<IPasswordManager> passwordServiceReference = context
				.getServiceReference(IPasswordManager.class);
		pwdmngr = context.getService(passwordServiceReference);

		// Initialize the configuration using the password from the password
		// manager
		config = Config.getInstance();
		config.init(pwdmngr.getCredentials());

		// Connect to the database
		ServiceReference<IMetadataManager> databaseServiceReference = context
				.getServiceReference(IMetadataManager.class);
		database = context.getService(databaseServiceReference);
		String databasename = config.getString("database.name", null);
		database.connect(databasename);
		database.initialize();

		String mergeInputDir = config.getString("merge.input.dir", null);
		String mergeOutputDir = config.getString("merge.output.dir", null);
		String splitInputDir = config.getString("split.input.dir", null);
		String splitOutputDir = config.getString("split.output.dir", null);

		new File(mergeInputDir).mkdirs();
		new File(mergeOutputDir).mkdirs();
		new File(splitInputDir).mkdirs();
		new File(splitOutputDir).mkdirs();

		recursiveFileSystemWatcher = new RecursiveFileSystemWatcher(
				config.getString("split.input.dir", null), config.getInt(
						"filemanagement.intervall", 60000));
		recursiveFileSystemWatcher.start();

		int proc = config.getInt("filemanagement.count", 1);
		System.out.println("Number FileManagers: " + proc);
		fileManagers = new FileManager[proc];
		for (int i = 0; i < proc; i++) {
			fileManagers[i] = new FileManager(i);
			fileManagers[i].start();
		}
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	@Override
	public void stop(BundleContext context) throws Exception {
		for (int i = 0; i < fileManagers.length; i++) {
			fileManagers[i].interrupt();
		}
		recursiveFileSystemWatcher.interrupt();
		database.disconnect();
		config.save();
	}

}
