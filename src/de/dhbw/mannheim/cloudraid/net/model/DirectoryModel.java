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

package de.dhbw.mannheim.cloudraid.net.model;

import java.util.HashMap;

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
public class DirectoryModel extends EntryModel {

	private HashMap<String, DirectoryModel> directories = new HashMap<String, DirectoryModel>();
	private HashMap<String, FileModel> files = new HashMap<String, FileModel>();

	public final void addEntry(EntryModel entry) {
		if (entry.getClass() == DirectoryModel.class) {
			this.directories.put(entry.getName(), (DirectoryModel) entry);
		} else {
			this.files.put(entry.getName(), (FileModel) entry);
		}
	}

	public final HashMap<String, DirectoryModel> getDirectories() {
		return this.directories;
	}

	public final EntryModel getEntry(String key) {
		return this.directories.get(key);
	}

}
