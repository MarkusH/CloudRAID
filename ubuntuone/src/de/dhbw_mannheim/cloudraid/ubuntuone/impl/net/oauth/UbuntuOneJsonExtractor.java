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

package de.dhbw_mannheim.cloudraid.ubuntuone.impl.net.oauth;

import org.json.JSONException;
import org.json.JSONObject;
import org.scribe.extractors.RequestTokenExtractor;
import org.scribe.model.Token;

/**
 * @author Markus Holtermann
 * 
 */
public class UbuntuOneJsonExtractor implements RequestTokenExtractor {

	/**
	 * @param response
	 *            the JSON encoded response string
	 * @return Returns a {@link Token} that contains the token and the token
	 *         secret
	 */
	@Override
	public Token extract(String response) {
		JSONObject body;
		try {
			body = new JSONObject(response);
			String token = body.getString("token");
			String secret = body.getString("token_secret");

			return new Token(token, secret);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return new Token("", "");
	}

	/**
	 * @param response
	 *            the JSON encoded response string
	 * @return Returns a {@link Token} that contains the consumer key and the
	 *         consumer secret
	 */
	public Token extractConsumerToken(String response) {
		JSONObject body;
		try {
			body = new JSONObject(response);
			String token = body.getString("consumer_key");
			String secret = body.getString("consumer_secret");

			return new Token(token, secret);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return new Token("", "");
	}

}
