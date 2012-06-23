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

import de.dhbw_mannheim.cloudraid.core.net.connector.IStorageConnector;
import de.dhbw_mannheim.cloudraid.core.net.model.IEntryModel;
import de.dhbw_mannheim.cloudraid.core.net.model.IMetaData;
import de.dhbw_mannheim.cloudraid.core.net.model.IVolumeModel;

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
public abstract class VolumeModel implements IVolumeModel {

	/**
	 * The {@link MetaData} stores the available meta data to this volume. Such
	 * as creation date, root path, name, etc. as long as they are available.
	 * The meta data should only accessed by the regarding
	 * {@link IStorageConnector}.
	 */
	private IMetaData metadata = new MetaData();

	/**
	 * Every {@link VolumeModel} can contain a multiple {@link EntryModel}s. An
	 * {@link EntryModel} is something like a {@link DirectoryModel} or
	 * {@link FileModel}.
	 */
	private HashMap<String, IEntryModel> entries = new HashMap<String, IEntryModel>();

	/**
	 * The name is the visual representation of the volume. In general it is the
	 * name that specifies how the volume can be accessed.
	 */
	private String name;

	@Override
	public final void addEntry(IEntryModel entry) {
		this.entries.put(entry.getName(), entry);
	}

	@Override
	public final HashMap<String, IEntryModel> getEntries() {
		return this.entries;
	}

	@Override
	public final IEntryModel getEntry(String key) {
		return this.entries.get(key);
	}

	@Override
	public final String getName() {
		return name;
	}

	@Override
	public final void setName(String name) {
		this.name = name;
	}

	@Override
	public abstract String toString();

	@Override
	public final void setMetadata(IMetaData metadata) {
		this.metadata = metadata;
	}

	@Override
	public final IMetaData getMetadata() {
		return this.metadata;
	}
}
