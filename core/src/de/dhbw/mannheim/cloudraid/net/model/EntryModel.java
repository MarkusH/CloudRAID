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

/**
 * An {@link EntryModel} is any kind of object that exists within an
 * {@link VolumeModel}. Normally this is a {@link DirectoryModel} or a
 * {@link FileModel}.
 * 
 * @author Markus Holtermann
 */
public abstract class EntryModel {

	/**
	 * The {@link MetaData} stores the available meta data to this entry. Such
	 * as creation date, root path, name, etc. as long as they are available.
	 */
	public MetaData metadata = new MetaData();
	/**
	 * The name of this {@link EntryModel}.
	 */
	private String name;
	/**
	 * The parent directory.
	 */
	private DirectoryModel parent = null;
	/**
	 * The {@link VolumeModel} this entry belongs to.
	 */
	private VolumeModel volume = null;

	/**
	 * @return Returns the name of the {@link EntryModel}.
	 */
	public final String getName() {
		return name;
	}

	/**
	 * @return Returns the parent {@link EntryModel}
	 */
	public final EntryModel getParent() {
		return parent;
	}

	/**
	 * @return Returns the {@link VolumeModel}
	 */
	public final VolumeModel getVolume() {
		return volume;
	}

	/**
	 * Sets the name of this {@link EntryModel}
	 * 
	 * @param name
	 *            The new name of this {@link EntryModel}.
	 */
	public final void setName(String name) {
		this.name = name;
	}

	/**
	 * @param parent
	 *            The parent {@link EntryModel}
	 */
	public final void setParent(DirectoryModel parent) {
		this.parent = parent;
	}

	/**
	 * @param volume
	 *            The {@link VolumeModel}
	 */
	public final void setVolume(VolumeModel volume) {
		this.volume = volume;
	}

}
