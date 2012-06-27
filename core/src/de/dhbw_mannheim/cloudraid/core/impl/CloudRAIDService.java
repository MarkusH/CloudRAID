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
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

import de.dhbw_mannheim.cloudraid.config.ICloudRAIDConfig;
import de.dhbw_mannheim.cloudraid.config.exceptions.ConfigException;
import de.dhbw_mannheim.cloudraid.config.exceptions.MissingConfigValueException;
import de.dhbw_mannheim.cloudraid.core.ICloudRAIDService;
import de.dhbw_mannheim.cloudraid.core.ICoreAccess;
import de.dhbw_mannheim.cloudraid.core.net.connector.IStorageConnector;

/**
 * @author Markus Holtermann
 * 
 */
public class CloudRAIDService implements ICloudRAIDService {

	private ICloudRAIDConfig config = null;

	private IStorageConnector[] storageConnectors = {null, null, null};
	private String[] storageConnectorClassnames = {null, null, null};

	private Queue<ICoreAccess> availableSlots = new LinkedList<ICoreAccess>();
	private Queue<ICoreAccess> freeSlots = new LinkedList<ICoreAccess>();
	private Queue<ICoreAccess> usedSlots = new LinkedList<ICoreAccess>();

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
	 * This function is called upon shutdown of this service. The
	 * {@link ICloudRAIDConfig} will be saved.
	 */
	protected synchronized void shutdown() {
		System.out.println("CloudRAIDService: shutdown: begin");
		config.save();
		System.out.println("CloudRAIDService: shutdown: end");
	}

	/**
	 * During the {@link #startup(BundleContext) startup} this service reads the
	 * three {@link IStorageConnector cloud storage connectors} to be used from
	 * the configuration. The regarding bundles containing these classes are
	 * loaded and hence their services registered.
	 * 
	 * @param context
	 *            The {@link BundleContext} for this bundle
	 * @throws ConfigException
	 *             Thrown, if a configuration value is invalid
	 * @throws InstantiationException
	 *             Thrown, if a {@link IStorageConnector} cannot be created /
	 *             initialized.
	 */
	protected synchronized void startup(BundleContext context)
			throws ConfigException, InstantiationException {
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
		/*
		 * First we iterate over all installed bundles. Second we iterate over
		 * the IStorageConnectors defined in the configuration. We try to load
		 * the given full qualified class name from the current bundle. On
		 * success, the bundle gets started, which will register a service
		 * providing an IStorageConnectors implementation. If the service can
		 * then accessed, we continue with the next required connector. Finally,
		 * the array storageConnectors contains the references to the 3 required
		 * services.
		 */
		Bundle[] bundles = context.getBundles();
		for (Bundle b : bundles) {
			for (int i = 0; i < 3 && connectedServices < 3; i++) {

				try {
					Class<?> klass = b
							.loadClass(this.storageConnectorClassnames[i]);

					IStorageConnector connector = (IStorageConnector) klass
							.newInstance();
					this.storageConnectors[i] = connector;
					connectedServices++;

				} catch (ClassNotFoundException ignore) {
				} catch (Exception e) {
					System.err.println("Cannot start bundle " + b);
					e.printStackTrace();
				}

			}
			if (connectedServices == 3) {
				break;
			}
		}
		if (connectedServices == 3) {
			System.out
					.println("Resolved and started all required cloud connector bundles.");
			System.out.println(Arrays.toString(this.storageConnectors));
		} else {
			System.err
					.println("Couldn't resolve or start all required bundles!");
			return;
		}

		for (int i = 0; i < 3; i++) {
			this.storageConnectors[i].create(i, this.config);
		}

		System.out.println("CloudRAIDService: startup: end");
	}

	/**
	 * @param config
	 *            Reference to the still existing {@link ICloudRAIDConfig
	 *            configuration} instance
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

	@Override
	public synchronized ICoreAccess getSlot() throws InstantiationException {
		ICoreAccess slot;
		if (this.freeSlots.size() > 0) {
			slot = this.freeSlots.poll();
			this.usedSlots.add(slot);
		} else {
			// TODO replace with service!
			slot = new CoreAccess();
			this.availableSlots.add(slot);
			this.usedSlots.add(slot);
		}
		return slot;
	}

	@Override
	public IStorageConnector[] getStorageConnectors() {
		if (this.storageConnectors[0] == null
				|| this.storageConnectors[1] == null
				|| this.storageConnectors[2] == null) {
			throw new IllegalStateException(
					"At least one storage connector missing");
		}
		return this.storageConnectors;
	}
}
