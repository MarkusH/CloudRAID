/*
 * Copyright 2011 by the CloudRAID Team, see AUTHORS for more details.
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

package de.dhbw.mannheim.cloudraid.fs;

import de.dhbw.mannheim.cloudraid.fs.FileQueue.FileAction;

/**
 * This class maps a filename to an according action.
 * 
 * @author Florian Bausch
 * 
 */
public class FileQueueEntry {

	/**
	 * The filename
	 */
	private String fileName;

	/**
	 * The action to be done
	 */
	private FileAction fileAction;

	/**
	 * Creates a new instance of FileQueueEntry that can be written into the
	 * {@link FileQueue}.
	 * 
	 * @param file
	 *            The filename
	 * @param fileAction
	 *            The action
	 */
	public FileQueueEntry(String file, FileAction fileAction) {
		this.fileName = file;
		this.fileAction = fileAction;
	}

	/**
	 * Returns the filename of the entry.
	 * 
	 * @return The filename as String.
	 */
	public String getFileName() {
		return this.fileName;
	}

	/**
	 * Returns the file action of the entry.
	 * 
	 * @return The action.
	 */
	public FileAction getFileAction() {
		return this.fileAction;
	}
}
