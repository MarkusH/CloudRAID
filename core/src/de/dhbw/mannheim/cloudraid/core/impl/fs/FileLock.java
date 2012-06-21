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

package de.dhbw.mannheim.cloudraid.core.impl.fs;

import java.util.HashMap;

/**
 * Handles the locking of files.
 * 
 * @author Florian Bausch
 * 
 */
public class FileLock {

	private static HashMap<String, FileManager> lockedFiles = new HashMap<String, FileManager>();

	/**
	 * Lock a file. You <b>MUST</b> unlock it later.
	 * 
	 * @param filename
	 *            The file to be locked.
	 * @param fileManager
	 *            defines the {@link FileManager} that will handle this file
	 * @return true, if you got the lock, false, if not
	 */
	public static synchronized boolean lock(String filename,
			FileManager fileManager) {
		if (lockedFiles.containsKey(filename)) {
			return false;
		} else {
			lockedFiles.put(filename, fileManager);
			return true;
		}

	}

	/**
	 * Unlock a file.
	 * 
	 * @param filename
	 *            The file to be unlocked.
	 * @param fileManager
	 *            defines the {@link FileManager} that will handle this file
	 * @return true, if the file could be unlocked.
	 */
	public static synchronized boolean unlock(String filename,
			FileManager fileManager) {
		if (lockedFiles.containsKey(filename)) {
			if (lockedFiles.get(filename) == fileManager) {
				lockedFiles.remove(filename);
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

}
