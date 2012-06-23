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

public interface IDirectoryModel extends IEntryModel {

	/**
	 * @param entry
	 *            The new {@link IEntryModel entry}
	 */
	public abstract void addEntry(IEntryModel entry);

	/**
	 * @return Returns a HashMap listing all {@link IDirectoryModel
	 *         directories}
	 */
	public abstract HashMap<String, IDirectoryModel> getDirectories();

	/**
	 * @param key
	 *            The file to look for
	 * @return Returns the requested {@link IEntryModel entry}, either from the
	 *         {@link IDirectoryModel directories} or from the
	 *         {@link IFileModel files}. The lookup should be in the order
	 *         {@link IDirectoryModel}, {@link IFileModel}.
	 */
	public abstract IEntryModel getEntry(String key);

}