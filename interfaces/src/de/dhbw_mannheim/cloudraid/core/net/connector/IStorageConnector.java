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

package de.dhbw_mannheim.cloudraid.core.net.connector;

import java.io.InputStream;
import java.util.HashMap;

import de.dhbw_mannheim.cloudraid.core.net.model.IVolumeModel;

/**
 * Defines the methods to be implemented by classes that are used to connect to
 * cloud services.
 * 
 * @author Florian Bausch, Markus Holtermann
 * 
 */
public interface IStorageConnector {

	/**
	 * Connects to a cloud service
	 * 
	 * @return true, if the connection could be established; false, if not.
	 */
	public boolean connect();

	/**
	 * Create a new instance of the <code>connector</code>.
	 * 
	 * @param parameter
	 *            The given HashMap contains the parameters as key-value pairs
	 *            that a <code>connector</code> should use during
	 *            initialization.
	 * 
	 * @return Returns a new initialized instance of the <code>connector</code>.
	 * @throws InstantiationException
	 *             Thrown if not all required parameters are passed.
	 */
	public IStorageConnector create(HashMap<String, String> parameter)
			throws InstantiationException;

	/**
	 * Create the volume name
	 * 
	 * @param name
	 *            The name of the new volume
	 * @return The instance of the newly created volume
	 */
	public IVolumeModel createVolume(String name);

	/**
	 * Deletes a file on a cloud service.
	 * 
	 * In case that the requested file <b>does not exist</b> (HTTP 404) or that
	 * the removal <b>was successful</b> (HTTP 200), the implementation has to
	 * return <code>true</code>!
	 * 
	 * @param resource
	 *            Delete the given resource
	 * @return true, if the file could be deleted; false, if not.
	 */
	public boolean delete(String resource);

	/**
	 * Delete a volume with all its content
	 * 
	 * @param name
	 *            The volume to delete
	 */
	public void deleteVolume(String name);

	/**
	 * Gets a file from a cloud service.
	 * 
	 * @param resource
	 *            Retrieve the given resource
	 * @return An InputStream to the regarding file.
	 */
	public InputStream get(String resource);

	/**
	 * Get a specific volume
	 * 
	 * @param name
	 *            The volume to get
	 * @return The volume instance
	 */
	public IVolumeModel getVolume(String name);

	/**
	 * Returns meta data for a resource.
	 * 
	 * @param resource
	 *            The resource to use
	 * @return The meta data.
	 */
	public String head(String resource);

	/**
	 * (Re)load all volumes
	 */
	public void loadVolumes();

	/**
	 * Returns the options available for a resource.
	 * 
	 * @param resource
	 *            The resource to use
	 * @return The options.
	 */
	public String[] options(String resource);

	/**
	 * Sends a file to a cloud service.
	 * 
	 * @param resource
	 *            The resource to use
	 * @param parent
	 *            The parent resource
	 * @return The link to the new file on the cloud service.
	 */
	public String post(String resource, String parent);

	/**
	 * Changes a file on a cloud service.
	 * 
	 * @param resource
	 *            The resource to use
	 * @return true, if the file could be changed; false, if not.
	 */
	public boolean put(String resource);

}
