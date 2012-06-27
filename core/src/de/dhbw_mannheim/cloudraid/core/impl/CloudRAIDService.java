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

package de.dhbw_mannheim.cloudraid.core.impl;

import java.io.File;
import java.util.NoSuchElementException;

import javax.management.ServiceNotFoundException;
import javax.naming.directory.InvalidAttributeValueException;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

import de.dhbw_mannheim.cloudraid.config.ICloudRAIDConfig;
import de.dhbw_mannheim.cloudraid.config.exceptions.ConfigException;
import de.dhbw_mannheim.cloudraid.config.exceptions.MissingConfigValueException;
import de.dhbw_mannheim.cloudraid.core.ICloudRAIDService;
import de.dhbw_mannheim.cloudraid.core.impl.fs.FileManager;
import de.dhbw_mannheim.cloudraid.core.impl.fs.RecursiveFileSystemWatcher;
import de.dhbw_mannheim.cloudraid.core.net.connector.IStorageConnector;

/**
 * @author Markus Holtermann
 * 
 */
public class CloudRAIDService implements ICloudRAIDService {

	private ICloudRAIDConfig config = null;

	private FileManager[] fileManagers = null;

	private RecursiveFileSystemWatcher recursiveFileSystemWatcher = null;

	private IStorageConnector[] storageConnectors = { null, null, null };
	private String[] storageConnectorClassnames = { null, null, null };

	private String splitInputDir;

	private String mergeInputDir;

	private String mergeOutputDir;

	private String splitOutputDir;

	/**
	 * @param config
	 *            The running instance of the {@link ICloudRAIDConfig
	 *            configuration service}.
	 */
	protected synchronized void setConfig(ICloudRAIDConfig config) {
		System.out.println("CloudRAIDService: setConfig: begin");
		this.config = config;
		System.out.println("CloudRAIDService: setConfig: " + this.config);
		System.out.println("CloudRAIDService: setConfig: end");
	}

	/**
	 * This function is called upon shutdown of this service. All
	 * {@link FileManager}s are stopped and the
	 * {@link RecursiveFileSystemWatcher} is stopped too. The instance of
	 * {@link ICloudRAIDConfig} will be saved.
	 */
	protected void shutdown() {
		System.out.println("CloudRAIDService: shutdown: begin");
		for (int i = 0; i < fileManagers.length; i++) {
			fileManagers[i].interrupt();
		}
		recursiveFileSystemWatcher.interrupt();
		config.save();
		System.out.println("CloudRAIDService: shutdown: end");
	}

	/**
	 * During the {@link #startup(BundleContext) startup} this service starts
	 * and initializes the following threads and components (in order):
	 * <ul>
	 * <li>A {@link RecursiveFileSystemWatcher} that permanently watches the
	 * split input directory for new files</li>
	 * <li>Multiple {@link FileManager} that handle new files, e.g. split and
	 * upload them</li>
	 * </ul>
	 * 
	 * @param context
	 *            The {@link BundleContext} for this bundle
	 * @throws NoSuchElementException
	 *             Thrown, if a configuration value is not found
	 * @throws ConfigException
	 *             Thrown, if a config value is invalid
	 * @throws InvalidAttributeValueException
	 *             Thrown, if the watching directory for the
	 *             {@link RecursiveFileSystemWatcher} is invalid.
	 * @throws InvalidSyntaxException
	 * @throws ClassNotFoundException
	 * @throws BundleException
	 * @throws ServiceNotFoundException
	 */
	protected void startup(BundleContext context) throws ConfigException,
			InvalidAttributeValueException, ClassNotFoundException {
		System.out.println("CloudRAIDService: startup: begin");

		initPaths();

		String classname = null;
		for (int i = 0; i < 3; i++) {
			classname = this.config.getString(String.format("connector.%d", i));
			if (classname == null || classname.isEmpty()) {
			} else {
				this.storageConnectorClassnames[i] = classname;
			}
		}

		int connectedServices = 0;

		System.out
				.println("Trying to resolve and start the required bundles and services ...");
		Bundle[] bundles = context.getBundles();
		for (Bundle b : bundles) {
			for (int i = 0; i < 3 && connectedServices < 3; i++) {

				try {
					b.loadClass(this.storageConnectorClassnames[i]);
					Thread.sleep(2500);
					if (b.getState() != Bundle.ACTIVE) {
						System.out.println("\tStarting bundle "
								+ b.getSymbolicName()
								+ " and wait 5 seconds ...");
						b.start();
						Thread.sleep(2500);
						System.out.println("\t\tDone");
					} else {
						System.out.println("\tBundle " + b.getSymbolicName()
								+ " already active.");
					}

					ServiceReference<?>[] scs = b.getRegisteredServices();
					if (scs == null || scs.length < 1) {
						throw new ServiceNotFoundException(
								"\tThe service for class "
										+ this.storageConnectorClassnames[i]
										+ " could not be found!");
					}
					IStorageConnector sc = (IStorageConnector) context
							.getService(scs[0]);
					if (this.storageConnectorClassnames[i].equals(sc.getClass()
							.getName()) && this.storageConnectors[i] == null) {
						this.storageConnectors[i] = sc;
						connectedServices++;
					}

				} catch (BundleException e) {
					System.err.println("Cannot start bundle " + b);
					e.printStackTrace();
				} catch (Exception e) {
					// System.err.println("Class "
					// + this.storageConnectorClassnames[i]
					// + " not found in bundle " + b.getSymbolicName());
				}

			}
			if (connectedServices == 3) {
				break;
			}
		}
		if (connectedServices == 3) {
			System.out
					.println("Resolved and started all required cloud connector bundles.");
		} else {
			System.err
					.println("Couldn't resolve or start all required bundles!");
			throw new ClassNotFoundException(
					"Couldn't resolve or start all required bundles!");
		}
		recursiveFileSystemWatcher = new RecursiveFileSystemWatcher(
				this.splitInputDir, config.getInt("filemanagement.intervall",
						60000));
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
	 *            Reference to the still existing {@link ICloudRAIDConfig
	 *            config} instance
	 */
	protected synchronized void unsetConfig(ICloudRAIDConfig config) {
		System.out.println("CloudRAIDService: unsetConfig: begin");
		System.out.println("CloudRAIDService: unsetConfig: " + config);
		this.config = null;
		System.out.println("CloudRAIDService: unsetConfig: " + this.config);
		System.out.println("CloudRAIDService: unsetConfig: end");
	}

	private void initPaths() throws MissingConfigValueException {
		this.mergeInputDir = this.config.getString("merge.input.dir");
		this.mergeOutputDir = this.config.getString("merge.output.dir");
		this.splitInputDir = this.config.getString("split.input.dir");
		this.splitOutputDir = this.config.getString("split.output.dir");

		if (this.mergeInputDir == null || this.mergeOutputDir == null
				|| this.splitInputDir == null || this.splitOutputDir == null) {
			throw new MissingConfigValueException(
					"Missing split or merge directory definitions.");
		}

		new File(this.mergeInputDir).mkdirs();
		new File(this.mergeOutputDir).mkdirs();
		new File(this.splitInputDir).mkdirs();
		new File(this.splitOutputDir).mkdirs();
	}
}
