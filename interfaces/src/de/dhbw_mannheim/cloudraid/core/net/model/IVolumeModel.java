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

package de.dhbw_mannheim.cloudraid.core.net.model;

import java.util.HashMap;

public interface IVolumeModel {

	/**
	 * This function adds a {@link IDirectoryModel} or {@link IFileModel}, in
	 * general an {@link IEntryModel}, to the list of entries.
	 * 
	 * @param entry
	 *            The {@link IEntryModel} that should be added
	 */
	public void addEntry(IEntryModel entry);

	/**
	 * @return Returns all existing {@link IEntryModel}s in this
	 *         {@link IVolumeModel} or <code>null</code> if none exists.
	 */
	public HashMap<String, IEntryModel> getEntries();

	/**
	 * This function returns the {@link IEntryModel} for the given key.
	 * 
	 * @param key
	 *            The name of the {@link IEntryModel}
	 * @return Returns the requested {@link IEntryModel} in this
	 *         {@link IVolumeModel} or <code>null</code> if it does not exist.
	 */
	public IEntryModel getEntry(String key);

	/**
	 * @return Return the representative name of this volume.
	 */
	public String getName();

	/**
	 * Set the name of this {@link IVolumeModel}
	 * 
	 * @param name
	 *            The new name of this {@link IVolumeModel}.
	 */
	public void setName(String name);

	/**
	 * A subclass should implement an own <code>toString()</code> method to be
	 * used for printing. Something similar to:
	 * 
	 * <p>
	 * <code>return String.format("@ClassName(name=%s)", this.getName());</code>
	 * <p>
	 */
	public String toString();

	/**
	 * @param metadata
	 *            The {@link IMetaData} to this volume
	 */
	public void setMetadata(IMetaData metadata);

	/**
	 * @return The {@link IMetaData} to this volume
	 */
	public IMetaData getMetadata();

}