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

package de.dhbw.mannheim.cloudraid.net.oauth.ubuntuone;

import com.miginfocom.base64.Base64;

import org.scribe.builder.api.Api;
import org.scribe.model.OAuthConfig;
import org.scribe.model.OAuthConstants;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.model.Verifier;
import org.scribe.oauth.OAuthService;
import org.scribe.utils.OAuthEncoder;

import de.dhbw.mannheim.cloudraid.net.model.VolumeModel;

/**
 * @author Markus Holtermann
 * 
 */
public class UbuntuOneService implements OAuthService {

	/**
	 * The OAuth version
	 */
	private static final String VERSION = "1.0";

	/**
	 * The {@link Api} for this {@link OAuthService}
	 */
	private UbuntuOneApi api;

	/**
	 * The config that is used for this {@link OAuthService}
	 */
	private OAuthConfig config;

	/**
	 * The email address of the user. Used for the first request to get the
	 * customer and api tokens.
	 */
	private String email;

	/**
	 * The password of the user. Used for the first request to get the customer
	 * and api tokens.
	 */
	private String password;

	/**
	 * This constructs a new OAuthService for UbuntuOne that handles the
	 * non-standard login and access-token generation
	 * 
	 * @param api
	 *            OAuth 1.0a api information
	 * @param config
	 *            OAuth 1.0a configuration param object
	 */
	public UbuntuOneService(UbuntuOneApi api, OAuthConfig config) {
		this.api = api;
		this.config = config;
		this.email = config.getApiKey();
		this.password = config.getApiSecret();
	}

	/**
	 * @param request
	 * @param token
	 */
	private void addOAuthParams(OAuthRequest request, Token token) {
		request.addOAuthParameter(OAuthConstants.TIMESTAMP, api
				.getTimestampService().getTimestampInSeconds());
		request.addOAuthParameter(OAuthConstants.TOKEN, token.getToken());
		request.addOAuthParameter(OAuthConstants.NONCE, api
				.getTimestampService().getNonce());
		request.addOAuthParameter(OAuthConstants.CONSUMER_KEY,
				this.config.getApiKey());
		request.addOAuthParameter(OAuthConstants.SIGN_METHOD, api
				.getSignatureService().getSignatureMethod());
		request.addOAuthParameter(OAuthConstants.VERSION, getVersion());
	}

	/**
	 * @param request
	 * @param token
	 */
	private void addSignature(OAuthRequest request, Token token) {
		String oauthHeader = api.getHeaderExtractor().extract(request);
		String signature = getSignature(request, token);
		oauthHeader = oauthHeader + ", " + OAuthConstants.SIGNATURE + "=\""
				+ OAuthEncoder.encode(signature) + "\"";
		request.addHeader(OAuthConstants.HEADER, oauthHeader);
		System.err
				.println("[DEBUG] UbuntuOneService.addSignature(): Authorization = "
						+ request.getHeaders().get("Authorization"));
	}

	/**
	 * Returns the customer token!
	 * 
	 * @see de.dhbw.mannheim.cloudraid.net.oauth.ubuntuone.UbuntuOneService#getAccessToken(Token,
	 *      Verifier)
	 * 
	 * @param requestToken
	 *            The initial request token
	 * @return the customer token
	 */
	public Token getAccessToken(Token requestToken) {
		return this.getAccessToken(requestToken, new Verifier(this.email));
	}

	/**
	 * Returns the customer token!
	 */
	@Override
	public Token getAccessToken(Token requestToken, Verifier verifier) {
		OAuthRequest request = new OAuthRequest(Verb.GET,
				api.getAccessTokenEndpoint() + this.email);

		signRequest(requestToken, request);

		Response response = request.send();
		System.err
				.println("[DEBUG] UbuntuOneService.getAccessToken(): response.getCode() = "
						+ response.getCode());
		System.err
				.println("[DEBUG] UbuntuOneService.getAccessToken(): response.getHeaders() = "
						+ response.getHeaders());
		System.err
				.println("[DEBUG] UbuntuOneService.getAccessToken(): response.getBody() = "
						+ response.getBody());

		return new Token(this.config.getApiKey(), this.config.getApiSecret());
	}

	/**
	 * @return {@link UbuntuOneApi#getApiBaseEndpoint()}
	 */
	public String getApiBaseEndpoint() {
		return this.api.getApiBaseEndpoint();
	}

	@Override
	public String getAuthorizationUrl(Token requestToken) {
		return "";
	}

	/**
	 * @return {@link UbuntuOneApi#getContentRootEndpoint()}
	 */
	public String getContentRootEndpoint() {
		return this.api.getContentRootEndpoint();
	}

	/**
	 * @return {@link UbuntuOneApi#getFileStorageEndpoint()}
	 */
	public String getFileStorageEndpoint() {
		return this.api.getFileStorageEndpoint();
	}

	/**
	 * Here we get our consumer token and api token that will be used by this
	 * application
	 * 
	 * @see org.scribe.oauth.OAuthService#getRequestToken()
	 */
	@Override
	public Token getRequestToken() {
		OAuthRequest tokenRequest = new OAuthRequest(Verb.GET,
				api.getRequestTokenEndpoint());
		System.err
				.println("[DEBUG] UbuntuOneService.getRequestToken(): tokenRequest = "
						+ tokenRequest.getUrl());

		String encoding = Base64.encodeToString(
				(this.email + ":" + this.password).getBytes(), false);
		System.err
				.println("[DEBUG] UbuntuOneService.getRequestToken(): encoding = "
						+ tokenRequest.getQueryStringParams().toString());

		tokenRequest.addHeader("Authorization", "Basic " + encoding);
		System.err
				.println("[DEBUG] UbuntuOneService.getRequestToken(): tokenRequest.getHeaders() = "
						+ tokenRequest.getHeaders().toString());

		Response response = tokenRequest.send();
		System.err
				.println("[DEBUG] UbuntuOneService.getRequestToken(): response.getCode() = "
						+ response.getCode());
		System.err
				.println("[DEBUG] UbuntuOneService.getRequestToken(): response.getHeaders() = "
						+ response.getHeaders());
		System.err
				.println("[DEBUG] UbuntuOneService.getRequestToken(): response.getBody() = "
						+ response.getBody());

		Token stoken = api.getRequestTokenExtractor().extract(
				response.getBody());
		System.err
				.println("[DEBUG] UbuntuOneService.getRequestToken(): stoken = "
						+ stoken);
		Token ctoken = ((UbuntuOneJsonExtractor) api.getRequestTokenExtractor())
				.extractConsumerToken(response.getBody());
		this.config = new OAuthConfig(ctoken.getToken(), ctoken.getSecret(),
				this.config.getCallback(), this.config.getSignatureType(),
				this.config.getScope(), null);
		System.err
				.println("[DEBUG] UbuntuOneService.getRequestToken(): config = "
						+ this.config);
		return stoken;
	}

	/**
	 * @param request
	 * @param token
	 * @return
	 */
	private String getSignature(OAuthRequest request, Token token) {
		String baseString = api.getBaseStringExtractor().extract(request);
		return api.getSignatureService().getSignature(baseString,
				this.config.getApiSecret(), token.getSecret());
	}

	@Override
	public String getVersion() {
		return VERSION;
	}

	/**
	 * @param name
	 *            The requested {@link VolumeModel} name
	 * @return Returns the full URL to the volume
	 */
	public String getVolumeURLByName(String name) {
		return this.api.getFileStorageEndpoint() + "volumes/~/" + name + "/";
	}

	@Override
	public void signRequest(Token accessToken, OAuthRequest request) {
		addOAuthParams(request, accessToken);
		System.err
				.println("[DEBUG] UbuntuOneService.signRequest(): request.getOauthParameters() = "
						+ request.getOauthParameters().toString());
		System.err.println("[DEBUG] UbuntuOneService.signRequest(): request = "
				+ request);
		addSignature(request, accessToken);

	}
}
