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

import de.dhbw_mannheim.cloudraid.core.impl.net.model.MetaData;
import de.dhbw_mannheim.cloudraid.core.impl.net.util.NetUtils;

/**
 * @author Markus Holtermann
 */
public class UbuntuOneMetaData extends MetaData {

	/**
	 * The serial uid
	 */
	private static final long serialVersionUID = -1967202873493249324L;

	/**
	 * @param object
	 *            The initial JSON meta data representation
	 */
	public UbuntuOneMetaData(JSONObject object) {
		String[] names = JSONObject.getNames(object);
		for (int i = 0; i < names.length; i++) {
			try {
				this.put(names[i], object.get(names[i]));
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * @param keys
	 *            safely URL encodes the values of the given keys by invoking
	 *            {@link de.dhbw_mannheim.cloudraid.core.impl.net.util.NetUtils#safeURLPercentEncode(String)}
	 */
	public void addUrlEncoded(String... keys) {
		for (String key : keys) {
			this.put(key + "_safe",
					NetUtils.safeURLPercentEncode((String) this.get(key)));
		}
	}

}
