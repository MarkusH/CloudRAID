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

package de.dhbw.mannheim.cloudraid.persistence;

import java.sql.ResultSet;

/**
 * @author Florian Bausch, Markus Holtermann
 * 
 */
public interface IDatabaseConnector {

	/**
	 * Defines various states a file enters during
	 */
	public enum FILE_STATUS {
		/**
		 * File has been uploaded. Next state will be SPLITTING
		 */
		UPLOADED,
		/**
		 * File is going to be splitted now. Next state will be SPLITTED
		 */
		SPLITTING,
		/**
		 * File has been splitted. Next state will be DISTRIBUTING
		 */
		SPLITTED,
		/**
		 * File is going to be distributed to the cloud storages. Next state
		 * will be DISTRIBUTED
		 */
		DISTRIBUTING,
		/**
		 * File has been distributed. Next state will be READY.
		 */
		DISTRIBUTED,
		/**
		 * File has successfully been stored in the cloud.
		 */
		READY,
		/**
		 * File is going to be removed from the cloud. Next step will be
		 * DELETED. This state cannot be interrupted!
		 */
		DELETING,
		/**
		 * File has been removed from the cloud storages. Next action will
		 * remove the item from the database!
		 */
		DELETED
	}

	/**
	 * Creates a connection to a specific database.
	 * 
	 * @param database
	 *            The absolute path to the database.
	 * 
	 * @return true, if the connection could be opened; false, if not.
	 */
	public boolean connect(String database);

	/**
	 * Closes the connection to the database opened by {@link #connect(String)}.
	 * 
	 * @return true, if the connection could be closed.
	 */
	public boolean disconnect();

	/**
	 * Creates the database schemas.
	 * 
	 * @return true, if the initialization could be executed.
	 */
	public boolean initialize();

	/**
	 * Inserts a data set into the database.
	 * 
	 * @deprecated Will be replaced by fileNew() and fileUpdate()
	 * 
	 * @param path
	 *            The path of a file.
	 * @param hash
	 *            The hash of the file name.
	 * @param lastMod
	 *            The last modification date.
	 * @param userId
	 *            The user id this file belongs to
	 * @return true, if the data set could be inserted into the database.
	 */
	public boolean insert(String path, String hash, long lastMod, int userId);

	/**
	 * Looks up the hash value of an entry in the database.
	 * 
	 * @deprecated Not needed
	 * 
	 * @param path
	 *            The path of the file (identifies the data set).
	 * @param userId
	 *            The user id this file belongs to
	 * @return The hash value. Or <code>null</code>, if the path does not exist
	 *         in the database.
	 */
	public String getHash(String path, int userId);

	/**
	 * Looks up the last modification date of a file.
	 * 
	 * @deprecated Not needed
	 * 
	 * @param path
	 *            The path of the file.
	 * @param userId
	 *            The user id this file belongs to
	 * @return The last modification date. Or <code>-1L</code>, if the path does
	 *         not exist in the database.
	 */
	public long getLastMod(String path, int userId);

	/**
	 * Looks up a file name for a given hash value.
	 * 
	 * @deprecated Not needed
	 * 
	 * @param hash
	 *            The hash value.
	 * @param userId
	 *            The user id this file belongs to
	 * @return The path of the file. Or <code>null</code>, if the hash does not
	 *         exist in the database.
	 */
	public String getName(String hash, int userId);

	/**
	 * Deletes a data set in the database defined by the path.
	 * 
	 * @param path
	 *            The path of the file.
	 * @param userId
	 *            The user id this file belongs to
	 * @return The number of deleted records or -1
	 */
	public int fileDelete(String path, int userId);

	/**
	 * Checks, if the given file is available for the certain user
	 * 
	 * @param path
	 *            The path of the file.
	 * @param userId
	 *            The user id this file belongs to
	 * @return The SQL ResultSet of the file
	 */
	public ResultSet fileGet(String path, int userId);

	/**
	 * Inserts a data set into the database.
	 * 
	 * @param path
	 *            The path of a file.
	 * @param hash
	 *            The hash of the file name.
	 * @param lastMod
	 *            The last modification date.
	 * @param userId
	 *            The user id this file belongs to
	 * @return true, if the data set could be inserted into the database.
	 */
	public boolean fileNew(String path, String hash, long lastMod, int userId);

	/**
	 * Inserts a data set into the database.
	 * 
	 * @param path
	 *            The path of a file.
	 * @param hash
	 *            The hash of the file name.
	 * @param lastMod
	 *            The last modification date.
	 * @param userId
	 *            The user id this file belongs to
	 * @return true, if the data set could be inserted into the database.
	 */
	public boolean fileUpdate(String path, String hash, long lastMod, int userId);

	/**
	 * Update the state of a file.
	 * 
	 * @param id
	 *            The id of the regarding file
	 * @param state
	 *            The new state
	 * @return True if the status has been updated
	 */
	public boolean fileUpdateState(int id, FILE_STATUS state);

	/**
	 * @param username
	 *            The username of the new user. Max length is 32 characters
	 * @param password
	 *            The password
	 * @return true, if and only if the new user has been created, otherwise
	 *         false;
	 */
	public boolean addUser(String username, String password);

	/**
	 * This function must return the user id on success. Otherwise this function
	 * <b>must</b> return -1
	 * 
	 * @param username
	 *            The username
	 * @param password
	 *            The password (UTF-8 encoded)
	 * @return -1, if either the username does not exist or the password does
	 *         not match the record in the database
	 */
	public int authUser(String username, String password);

	/**
	 * Returns a ResultSet that contains all files of a user.
	 * 
	 * @param userId
	 *            The user id this file belongs to
	 * @return The SQL ResultSet of all file belonging to the given user.
	 */
	public ResultSet fileList(int userId);
}
