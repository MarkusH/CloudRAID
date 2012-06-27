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

package de.dhbw_mannheim.cloudraid.ubuntuone.impl.net.model;

import org.json.JSONException;
import org.json.JSONObject;

import de.dhbw_mannheim.cloudraid.core.impl.net.model.EntryModel;
import de.dhbw_mannheim.cloudraid.core.net.model.IEntryModel;

/**
 * @author Markus Holtermann
 */
public class UbuntuOneEntryModel extends EntryModel implements IEntryModel {

	/**
	 * @param object
	 *            A {@link JSONObject} with meta data.
	 */
	public UbuntuOneEntryModel(JSONObject object) {
		this.setMetadata(new UbuntuOneMetaData(object));
		this.setName(((String) this.getMetadata().get("path")).substring(2));
		((UbuntuOneMetaData) this.getMetadata()).addUrlEncoded("resource_path",
				"path", "content_path", "volume_path");
	}

	/**
	 * @param content
	 *            JSON Object representation string with meta data
	 * @throws JSONException
	 *             Thrown if content is not a valid JSON Object String
	 */
	public UbuntuOneEntryModel(String content) throws JSONException {
		this(new JSONObject(content));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.dhbw_mannheim.cloudraid.net.model.VolumeModel#toString()
	 */
	@Override
	public String toString() {
		return String.format("@UbuntuOneEntry(name=%s)", this.getName());
	}
}