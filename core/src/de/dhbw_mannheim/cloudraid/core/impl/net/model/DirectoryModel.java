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

package de.dhbw_mannheim.cloudraid.core.impl.net.model;

import java.util.HashMap;

import de.dhbw_mannheim.cloudraid.core.net.model.IDirectoryModel;
import de.dhbw_mannheim.cloudraid.core.net.model.IEntryModel;
import de.dhbw_mannheim.cloudraid.core.net.model.IFileModel;

/**
 * An {@link DirectoryModel} is a special {@link EntryModel} that can hold
 * multiple {@link DirectoryModel}s and {@link EntryModel}s. So the directory
 * structure <code>Volume:/foo/bar/buz/test.txt</code> will result in a
 * {@link VolumeModel} <code>Volume</code>, three nested {@link DirectoryModel}s
 * <code>foo</code>, <code>bar</code> and <code>buz</code> and a
 * {@link FileModel} <code>test.txt</code>.
 * 
 * @author Markus Holtermann
 */
public class DirectoryModel extends EntryModel implements IDirectoryModel {

	/**
	 * Contains all sub-directories of this directory
	 */
	private HashMap<String, IDirectoryModel> directories = new HashMap<String, IDirectoryModel>();
	/**
	 * Contains all files within this directory
	 */
	private HashMap<String, IFileModel> files = new HashMap<String, IFileModel>();

	@Override
	public final void addEntry(IEntryModel entry) {
		if (entry instanceof IDirectoryModel) {
			this.directories.put(entry.getName(), (IDirectoryModel) entry);
		} else {
			this.files.put(entry.getName(), (FileModel) entry);
		}
	}

	@Override
	public final HashMap<String, IDirectoryModel> getDirectories() {
		return this.directories;
	}

	@Override
	public final IEntryModel getEntry(String key) {
		if (this.directories.containsKey(key)) {
			return this.directories.get(key);
		} else if (this.files.containsKey(key)) {
			return this.files.get(key);
		} else {
			return null;
		}
	}

}
