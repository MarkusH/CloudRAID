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

package de.dhbw.mannheim.cloudraid.core.impl;

import java.io.File;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import de.dhbw.mannheim.cloudraid.config.ICloudRAIDConfig;
import de.dhbw.mannheim.cloudraid.core.impl.fs.FileManager;
import de.dhbw.mannheim.cloudraid.core.impl.fs.RecursiveFileSystemWatcher;
import de.dhbw.mannheim.cloudraid.metadatamgr.IMetadataManager;
import de.dhbw.mannheim.cloudraid.passwordmgr.IPasswordManager;

/**
 * @author Markus Holtermann
 * 
 */
public class CloudRAIDService {

	/**
	 * 
	 */
	private ICloudRAIDConfig config = null;

	private FileManager[] fileManagers = null;

	/**
	 * 
	 */
	private IMetadataManager metadata = null;

	/**
	 * 
	 */
	private IPasswordManager pwdmngr = null;

	private RecursiveFileSystemWatcher recursiveFileSystemWatcher = null;

	/**
	 * @param config
	 */
	protected void setConfig(ICloudRAIDConfig config) {
		System.out.println("CloudRAIDService: setConfig: begin");
		this.config = config;
		System.out.println("CloudRAIDService: setConfig: " + this.config);
		System.out.println("CloudRAIDService: setConfig: end");
	}

	/**
	 * @param metadataService
	 */
	protected void setMetadataMgr(IMetadataManager metadataService) {
		System.out.println("CloudRAIDService: setMetadataMgr: begin");
		this.metadata = metadataService;
		System.out
				.println("CloudRAIDService: setMetadataMgr: " + this.metadata);
		System.out.println("CloudRAIDService: setMetadataMgr: end");
	}

	/**
	 * 
	 */
	protected void shutdown() {
		System.out.println("CloudRAIDService: shutdown: begin");
		for (int i = 0; i < fileManagers.length; i++) {
			fileManagers[i].interrupt();
		}
		recursiveFileSystemWatcher.interrupt();
		metadata.disconnect();
		config.save();
		System.out.println("CloudRAIDService: shutdown: end");
	}

	/**
	 * During the {@link BundleActivator#start(BundleContext) startup} this
	 * {@link BundleActivator} starts and initializes the following services and
	 * components (in order):
	 * <ul>
	 * <li>A {@link IPasswordManager} to handle passwords for the configuration</li>
	 * <li>A {@link ICloudRAIDConfig} storing the I/O paths for RAID, the
	 * metadata name, number of threads, etc.</li>
	 * <li>A {@link IMetadataManager} that represents the underlying metadata
	 * used to store all file information</li>
	 * <li>A {@link RecursiveFileSystemWatcher} that permanently watches the
	 * split input directory for new files</li>
	 * <li>Multiple {@link FileManager} that handle new files, e.g. split and
	 * upload them</li>
	 * </ul>
	 * 
	 * @param context
	 * @throws Exception
	 */
	protected void startup(BundleContext context) throws Exception {
		System.out.println("CloudRAIDService: startup: begin");
		// Initialize the password manager
		ServiceReference<IPasswordManager> passwordServiceReference = context
				.getServiceReference(IPasswordManager.class);
		pwdmngr = context.getService(passwordServiceReference);

		// Initialize the configuration using the password from the password
		// manager
		ServiceReference<ICloudRAIDConfig> configServiceReference = context
				.getServiceReference(ICloudRAIDConfig.class);
		config = context.getService(configServiceReference);
		config.init(pwdmngr.getCredentials());

		// Connect to the metadata
		ServiceReference<IMetadataManager> metadataServiceReference = context
				.getServiceReference(IMetadataManager.class);
		metadata = context.getService(metadataServiceReference);
		// String databasename = config.getString("metadata.name", null);
		// metadata.connect(databasename);
		// metadata.initialize();

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
			fileManagers[i] = new FileManager(config, i);
			fileManagers[i].start();
		}
		System.out.println("CloudRAIDService: startup: end");
	}

	/**
	 * @param config
	 */
	protected void unsetConfig(ICloudRAIDConfig config) {
		System.out.println("CloudRAIDService: unsetConfig: begin");
		System.out.println("CloudRAIDService: unsetConfig: " + config);
		this.config = null;
		System.out.println("CloudRAIDService: unsetConfig: " + this.config);
		System.out.println("CloudRAIDService: unsetConfig: end");
	}

	/**
	 * @param metadataService
	 */
	protected void unsetMetadataMgr(IMetadataManager metadataService) {
		System.out.println("CloudRAIDService: unsetConfig: begin");
		System.out.println("CloudRAIDService: unsetConfig: " + metadataService);
		this.metadata = null;
		System.out.println("CloudRAIDService: unsetConfig: " + this.metadata);
		System.out.println("CloudRAIDService: unsetConfig: end");
	}

}
