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

public abstract class VolumeModel {

	private HashMap<Object, Object> metadata = new HashMap<Object, Object>();
	private HashMap<String, EntryModel> entries = new HashMap<String, EntryModel>();

	private String name;

	public final void addEntry(EntryModel entry) {
		this.entries.put(entry.getName(), entry);
	}

	public final void addMetadata(HashMap<Object, Object> map) {
		this.metadata.putAll(map);
	}

	public final void addMetadata(Object key, Object value) {
		this.metadata.put(key, value);
	}

	public final HashMap<String, EntryModel> getEntries() {
		return this.entries;
	}

	public final EntryModel getEntry(String key) {
		return this.entries.get(key);
	}

	public final HashMap<Object, Object> getMetadata() {
		return this.metadata;
	}

	public final Object getMetadata(Object key) {
		return this.metadata.get(key);
	}

	public final String getName() {
		return name;
	}

	public final void setName(String name) {
		this.name = name;
	}

	@Override
	public abstract String toString();
}
