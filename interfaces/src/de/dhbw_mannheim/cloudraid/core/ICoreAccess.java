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

package de.dhbw_mannheim.cloudraid.core;

import java.io.InputStream;

import de.dhbw_mannheim.cloudraid.metadatamgr.IMetadataManager;

/**
 * @author Markus Holtermann
 * 
 */
public interface ICoreAccess {

	/**
	 * This function <b>must</b> release itself from the {@link ICoreAccess}
	 * slots in the {@link ICloudRAIDService}.
	 * 
	 * @param fileid
	 *            The id for this file from the {@link IMetadataManager}
	 * @return True on success, else false.
	 */
	public boolean deleteData(int fileid);

	/**
	 * This function <b>must not</b> release itself from the {@link ICoreAccess}
	 * slots in the {@link ICloudRAIDService}. A calling function <b>must</b>
	 * release the slot!
	 * 
	 * @param fileid
	 *            The id for this file from the {@link IMetadataManager}
	 * @return Returns an InputStream providing the merged file or null in case
	 *         of an error.
	 */
	public InputStream getData(int fileid);

	/**
	 * This function <b>must</b> release itself from the {@link ICoreAccess}
	 * slots in the {@link ICloudRAIDService}. A calling function <b>must
	 * not</b> release the slot!
	 * 
	 * @param is
	 *            InputStream providing the data
	 * @param fileid
	 *            The id for this file from the {@link IMetadataManager}
	 * @return True on success, else false.
	 * 
	 * @see #putData(InputStream, int, boolean)
	 */
	public boolean putData(InputStream is, int fileid);

	/**
	 * This function <b>must</b> release itself from the {@link ICoreAccess}
	 * slots in the {@link ICloudRAIDService}. A calling function <b>must
	 * not</b> release the slot!
	 * 
	 * @param is
	 *            InputStream providing the data
	 * @param fileid
	 *            The id for this file from the {@link IMetadataManager}
	 * @param update
	 *            <code>true</code> to overwrite an existing file.
	 * @return True on success, else false.
	 */
	public boolean putData(InputStream is, int fileid, boolean update);

	/**
	 * Reset the internal states
	 */
	public void reset();

}
