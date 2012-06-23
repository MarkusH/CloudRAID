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

import de.dhbw_mannheim.cloudraid.core.net.model.IDirectoryModel;
import de.dhbw_mannheim.cloudraid.core.net.model.IEntryModel;
import de.dhbw_mannheim.cloudraid.core.net.model.IMetaData;
import de.dhbw_mannheim.cloudraid.core.net.model.IVolumeModel;

/**
 * An {@link EntryModel} is any kind of object that exists within an
 * {@link VolumeModel}. Normally this is a {@link DirectoryModel} or a
 * {@link FileModel}.
 * 
 * @author Markus Holtermann
 */
public abstract class EntryModel implements IEntryModel {

	/**
	 * The {@link MetaData} stores the available meta data to this entry. Such
	 * as creation date, root path, name, etc. as long as they are available.
	 */
	private IMetaData metadata = new MetaData();
	/**
	 * The name of this {@link EntryModel}.
	 */
	private String name;
	/**
	 * The parent directory.
	 */
	private IDirectoryModel parent = null;
	/**
	 * The {@link VolumeModel} this entry belongs to.
	 */
	private IVolumeModel volume = null;

	@Override
	public final String getName() {
		return name;
	}

	@Override
	public final IEntryModel getParent() {
		return parent;
	}

	@Override
	public final IVolumeModel getVolume() {
		return volume;
	}

	@Override
	public final void setName(String name) {
		this.name = name;
	}

	@Override
	public final void setParent(IDirectoryModel parent) {
		this.parent = parent;
	}

	@Override
	public final void setVolume(IVolumeModel volume) {
		this.volume = volume;
	}

	@Override
	public void setMetadata(IMetaData metadata) {
		this.metadata = metadata;
	}

	@Override
	public IMetaData getMetadata() {
		return this.metadata;
	}

}
