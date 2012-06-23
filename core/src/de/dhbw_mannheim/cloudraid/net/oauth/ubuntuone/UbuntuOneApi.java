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

package de.dhbw_mannheim.cloudraid.net.oauth.ubuntuone;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.scribe.builder.api.DefaultApi10a;
import org.scribe.extractors.HeaderExtractor;
import org.scribe.extractors.RequestTokenExtractor;
import org.scribe.model.OAuthConfig;
import org.scribe.model.Token;
import org.scribe.model.Verb;

/**
 * @author Markus Holtermann
 * 
 */
public class UbuntuOneApi extends DefaultApi10a {

	/**
	 * First step of the 2-way authentication if the user doesn't have a token
	 * jet
	 */
	private static final String REQUEST_URL = "https://login.ubuntu.com/api/1.0/authentications?ws.op=authenticate&token_name=Ubuntu%20One%20@%20";
	/**
	 * Second step of the 2-way authentication to get the customer tokens
	 */
	private static final String ACCESS_URL = "https://one.ubuntu.com/oauth/sso-finished-so-get-tokens/";
	/**
	 * Used for requesting the token validity
	 */
	private static final String API_BASE_URL = "https://one.ubuntu.com/api";
	/**
	 * Endpoint for file contents
	 */
	private static final String CONTENT_ROOT_URL = "https://files.one.ubuntu.com/content";
	/**
	 * Endpoint for general requests
	 */
	private static final String FILE_STORAGE_URL = "https://one.ubuntu.com/api/file_storage/v1";

	/**
	 * Returns the {@link org.scribe.oauth.OAuthService} for this
	 * {@link org.scribe.builder.api.DefaultApi10a API}
	 * 
	 * @param config
	 *            The config for the new {@link UbuntuOneService}
	 */
	@Override
	public UbuntuOneService createService(OAuthConfig config) {
		return new UbuntuOneService(this, config);
	}

	/**
	 * @return The {@link #ACCESS_URL}: {@value #ACCESS_URL}
	 */
	@Override
	public String getAccessTokenEndpoint() {
		return ACCESS_URL;
	}

	@Override
	public Verb getAccessTokenVerb() {
		return Verb.GET;
	}

	/**
	 * @return The {@link #API_BASE_URL}: {@value #API_BASE_URL}
	 */
	public String getApiBaseEndpoint() {
		return API_BASE_URL;
	}

	@Override
	public String getAuthorizationUrl(Token requestToken) {
		return "";
	}

	/**
	 * @return The {@link #CONTENT_ROOT_URL}: {@value #CONTENT_ROOT_URL}
	 */
	public String getContentRootEndpoint() {
		return CONTENT_ROOT_URL;
	}

	/**
	 * @return The {@link #FILE_STORAGE_URL}: {@value #FILE_STORAGE_URL}
	 */
	public String getFileStorageEndpoint() {
		return FILE_STORAGE_URL;
	}

	@Override
	public HeaderExtractor getHeaderExtractor() {
		return new UbuntuOneHeaderExtractor();
	}

	@Override
	public String getRequestTokenEndpoint() {
		try {
			return REQUEST_URL + InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			e.printStackTrace();
			return REQUEST_URL + "localhost";
		}
	}

	@Override
	public RequestTokenExtractor getRequestTokenExtractor() {
		return new UbuntuOneJsonExtractor();
	}

	@Override
	public Verb getRequestTokenVerb() {
		return Verb.GET;
	}
}
