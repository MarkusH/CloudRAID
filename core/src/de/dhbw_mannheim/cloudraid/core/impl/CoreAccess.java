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

package de.dhbw_mannheim.cloudraid.core.impl;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;

import de.dhbw_mannheim.cloudraid.config.ICloudRAIDConfig;
import de.dhbw_mannheim.cloudraid.config.exceptions.MissingConfigValueException;
import de.dhbw_mannheim.cloudraid.core.ICloudRAIDService;
import de.dhbw_mannheim.cloudraid.core.ICoreAccess;
import de.dhbw_mannheim.cloudraid.core.impl.jni.RaidAccessInterface;
import de.dhbw_mannheim.cloudraid.core.net.connector.IStorageConnector;
import de.dhbw_mannheim.cloudraid.metadatamgr.IMetadataManager;
import de.dhbw_mannheim.cloudraid.metadatamgr.IMetadataManager.FILE_STATUS;

/**
 * @author Markus Holtermann
 * 
 */
public class CoreAccess extends Thread implements ICoreAccess {

	private IMetadataManager metadata = null;
	private ICloudRAIDConfig config = null;
	private ICloudRAIDService coreService = null;

	private String path;
	private int userid;
	private int fileid;
	private boolean update;
	private File file;

	public CoreAccess() throws InstantiationException {
		// unset/initialize the values for this instance
		reset();

		BundleContext ctx = FrameworkUtil.getBundle(this.getClass())
				.getBundleContext();

		ServiceReference<IMetadataManager> srm = ctx
				.getServiceReference(IMetadataManager.class);
		this.metadata = ctx.getService(srm);
		if (this.metadata == null) {
			throw new InstantiationException(
					"No running metadata manager found");
		}

		ServiceReference<ICloudRAIDConfig> src = ctx
				.getServiceReference(ICloudRAIDConfig.class);
		this.config = ctx.getService(src);
		if (this.config == null) {
			throw new InstantiationException("No running config found");
		}

		ServiceReference<ICloudRAIDService> srcore = ctx
				.getServiceReference(ICloudRAIDService.class);
		this.coreService = ctx.getService(srcore);
		if (this.coreService == null) {
			throw new InstantiationException("No core service found");
		}

	}

	@Override
	public boolean putData(InputStream is, int fileid) {
		return this.putData(is, fileid, false);
	}

	@Override
	public boolean putData(InputStream is, int fileid, boolean update) {
		this.fileid = fileid;
		this.update = update;
		try {
			// Retrieve the metadata from the database
			ResultSet rs = this.metadata.fileById(this.fileid);
			if (rs != null) {
				this.path = rs.getString("path_name");
				this.userid = rs.getInt("user_id");
				String status = rs.getString("status");

				if (FILE_STATUS.valueOf(status) != FILE_STATUS.UPLOADING) {
					throw new IllegalStateException(String.format(
							"File %s has state %s but UPLOADING expected!",
							path, status));
				}

				int bufsize = 4096;

				// Create the file
				this.file = new File(this.config.getString("split.input.dir")
						+ File.separator + this.userid + File.separator
						+ this.path);
				this.file.getParentFile().mkdirs();

				// Write data to file
				BufferedInputStream bis = new BufferedInputStream(is, bufsize);
				BufferedOutputStream bos = new BufferedOutputStream(
						new FileOutputStream(this.file), bufsize);
				byte[] inputBytes = new byte[bufsize];
				int readLength;
				while ((readLength = bis.read(inputBytes)) >= 0) {
					bos.write(inputBytes, 0, readLength);
				}

				// Update file state in database
				this.metadata
						.fileUpdateState(this.fileid, FILE_STATUS.UPLOADED);

				try {
					bis.close();
				} catch (IOException ignore) {
				}

				try {
					bos.close();
				} catch (IOException ignore) {
				}

				if (this.config.getBoolean("upload.asynchronous")) {
					this.start();
				} else {
					this.run();
					return true;
				}
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MissingConfigValueException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public OutputStream getData(int fileid) {
		return null;
	}

	@Override
	public boolean deleteData(int fileid) {
		return false;
	}

	public void run() {
		// Update state to splitting
		this.metadata.fileUpdateState(this.fileid, FILE_STATUS.SPLITTING);
		try {
			// perform the splitting process
			RaidAccessInterface.splitInterface(this.file.getParent(),
					this.file.getName(), config.getString("split.output.dir"),
					config.getString("file.password"));
			// Update state to splitted
			this.metadata.fileUpdateState(this.fileid, FILE_STATUS.SPLITTED);

			IStorageConnector[] storageConnectors = coreService
					.getStorageConnectors();

			for (int i = 0; i < 3; i++) {
				// TODO iterate over all connectors and upload the files with
				// extension .0, .1 and .2 AND the .m file to all connectors
			}

		} catch (MissingConfigValueException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void reset() {
		this.path = null;
		this.userid = -1;
		this.fileid = -1;
		this.update = false;
		this.file = null;

	}

}