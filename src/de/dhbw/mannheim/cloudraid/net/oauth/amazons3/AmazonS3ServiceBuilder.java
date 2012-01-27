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

package de.dhbw.mannheim.cloudraid.net.oauth.amazons3;

import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.Api;
import org.scribe.exceptions.OAuthException;
import org.scribe.model.OAuthConfig;
import org.scribe.oauth.OAuthService;
import org.scribe.utils.Preconditions;

/**
 * @author Markus Holtermann
 * 
 */
public class AmazonS3ServiceBuilder {

	/**
	 * 
	 */
	private String apiKey;

	/**
	 * 
	 */
	private String apiSecret;

	/**
	 * 
	 */
	private AmazonS3Api api;

	/**
	 * Configures the api key
	 * 
	 * @param apiKey
	 *            The api key for your application
	 * @return the {@link ServiceBuilder} instance for method chaining
	 */
	public AmazonS3ServiceBuilder apiKey(String apiKey) {
		Preconditions.checkEmptyString(apiKey, "Invalid Api key");
		this.apiKey = apiKey;
		return this;
	}

	/**
	 * Configures the api secret
	 * 
	 * @param apiSecret
	 *            The api secret for your application
	 * @return the {@link ServiceBuilder} instance for method chaining
	 */
	public AmazonS3ServiceBuilder apiSecret(String apiSecret) {
		Preconditions.checkEmptyString(apiSecret, "Invalid Api secret");
		this.apiSecret = apiSecret;
		return this;
	}

	/**
	 * Returns the fully configured {@link OAuthService}
	 * 
	 * @return fully configured {@link OAuthService}
	 */
	public AmazonS3Service build() {
		Preconditions.checkNotNull(api,
				"You must specify a valid api through the provider() method");
		Preconditions.checkEmptyString(apiKey, "You must provide an api key");
		Preconditions.checkEmptyString(apiSecret,
				"You must provide an api secret");
		return api.createService(new OAuthConfig(apiKey, apiSecret, null, null,
				null));
	}

	/**
	 * @param apiClass
	 * @return
	 */
	private AmazonS3Api createApi(Class<? extends AmazonS3Api> apiClass) {
		Preconditions.checkNotNull(apiClass, "Api class cannot be null");
		AmazonS3Api api;
		try {
			api = apiClass.newInstance();
		} catch (Exception e) {
			throw new OAuthException("Error while creating the Api object", e);
		}
		return api;
	}

	/**
	 * Configures the {@link Api}
	 * 
	 * Overloaded version. Let's you use an instance instead of a class.
	 * 
	 * @param api
	 *            instance of {@link Api}s
	 * @return the {@link ServiceBuilder} instance for method chaining
	 */
	public AmazonS3ServiceBuilder provider(AmazonS3Api api) {
		Preconditions.checkNotNull(api, "Api cannot be null");
		this.api = api;
		return this;
	}

	/**
	 * Configures the {@link Api}
	 * 
	 * @param apiClass
	 *            the class of one of the existent {@link Api}s on
	 *            org.scribe.api package
	 * @return the {@link ServiceBuilder} instance for method chaining
	 * 
	 */
	public AmazonS3ServiceBuilder provider(Class<? extends AmazonS3Api> apiClass) {
		this.api = createApi(apiClass);
		return this;
	}
}
