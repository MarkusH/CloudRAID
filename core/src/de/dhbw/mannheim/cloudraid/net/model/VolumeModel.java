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

package de.dhbw.mannheim.cloudraid.net.model;

import java.util.HashMap;

import de.dhbw.mannheim.cloudraid.net.connector.IStorageConnector;

/**
 * The {@link VolumeModel} represents a master storage location on a cloud
 * service. Taken Amazon S3, a {@link VolumeModel} is a bucket, taken UbuntuOne,
 * it is a volume. For Dropbox it is the application folder.
 * 
 * These master storage locations represent the base storage object where the
 * raid separated and encrypted files are stored. In case of Amazon S3, the
 * bucket is a kind of partition that can also be mounted by a EC2 instance. The
 * volume in UbuntuOne can be excluded from sync, so that it does not affect the
 * local storage. And in Dropbox the application folder under
 * <code>Dropbox/Apps/</code>. This folder can also be excluded from sync.
 * 
 * @author Markus Holtermann
 */
public abstract class VolumeModel {

	/**
	 * The {@link MetaData} stores the available meta data to this volume. Such
	 * as creation date, root path, name, etc. as long as they are available.
	 * The meta data should only accessed by the regarding
	 * {@link IStorageConnector}.
	 */
	public MetaData metadata = new MetaData();

	/**
	 * Every {@link VolumeModel} can contain a multiple {@link EntryModel}s. An
	 * {@link EntryModel} is something like a {@link DirectoryModel} or
	 * {@link FileModel}.
	 */
	private HashMap<String, EntryModel> entries = new HashMap<String, EntryModel>();

	/**
	 * The name is the visual representation of the volume. In general it is the
	 * name that specifies how the volume can be accessed.
	 */
	private String name;

	/**
	 * This function adds a {@link DirectoryModel} or {@link FileModel}, in
	 * general an {@link EntryModel}, to the list of entries.
	 * 
	 * @param entry
	 *            The {@link EntryModel} that should be added
	 */
	public final void addEntry(EntryModel entry) {
		this.entries.put(entry.getName(), entry);
	}

	/**
	 * @return Returns all existing {@link EntryModel}s in this
	 *         {@link VolumeModel} or <code>null</code> if none exists.
	 */
	public final HashMap<String, EntryModel> getEntries() {
		return this.entries;
	}

	/**
	 * This function returns the {@link EntryModel} for the given key.
	 * 
	 * @param key
	 *            The name of the {@link EntryModel}
	 * @return Returns the requested {@link EntryModel} in this
	 *         {@link VolumeModel} or <code>null</code> if it does not exist.
	 */
	public final EntryModel getEntry(String key) {
		return this.entries.get(key);
	}

	/**
	 * @return Return the representative name of this volume.
	 */
	public final String getName() {
		return name;
	}

	/**
	 * Set the name of this {@link VolumeModel}
	 * 
	 * @param name
	 *            The new name of this {@link VolumeModel}.
	 */
	public final void setName(String name) {
		this.name = name;
	}

	/**
	 * A subclass should implement an own <code>toString()</code> method to be
	 * used for printing. Something similar to:
	 * 
	 * <p>
	 * <code>return String.format("@ClassName(name=%s)", this.getName());</code>
	 * <p>
	 */
	@Override
	public abstract String toString();
}