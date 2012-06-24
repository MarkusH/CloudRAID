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

import javax.naming.directory.InvalidAttributeValueException;

import org.osgi.framework.BundleContext;

import de.dhbw_mannheim.cloudraid.config.ICloudRAIDConfig;
import de.dhbw_mannheim.cloudraid.config.exceptions.ConfigException;
import de.dhbw_mannheim.cloudraid.config.exceptions.MissingConfigValueException;
import de.dhbw_mannheim.cloudraid.core.ICloudRAIDService;
import de.dhbw_mannheim.cloudraid.core.impl.fs.FileManager;
import de.dhbw_mannheim.cloudraid.core.impl.fs.RecursiveFileSystemWatcher;

/**
 * @author Markus Holtermann
 * 
 */
public class CloudRAIDService implements ICloudRAIDService {

	private ICloudRAIDConfig config = null;

	private FileManager[] fileManagers = null;

	private RecursiveFileSystemWatcher recursiveFileSystemWatcher = null;

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
	 */
	protected void startup(BundleContext context)
			throws NoSuchElementException, ConfigException,
			InvalidAttributeValueException {
		System.out.println("CloudRAIDService: startup: begin");

		String mergeInputDir = config.getString("merge.input.dir", null);
		String mergeOutputDir = config.getString("merge.output.dir", null);
		String splitInputDir = config.getString("split.input.dir", null);
		String splitOutputDir = config.getString("split.output.dir", null);

		if (mergeInputDir == null || mergeOutputDir == null
				|| splitInputDir == null || splitOutputDir == null) {
			throw new MissingConfigValueException(
					"Missing split or merge directory definitions.");
		}

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

}
