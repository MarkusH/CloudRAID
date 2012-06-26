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
public class UbuntuOneServiceBuilder {
	/**
	 * The application key
	 */
	private String apiKey;
	/**
	 * The application secret key
	 */
	private String apiSecret;
	/**
	 * The {@link Api} for this {@link OAuthService}
	 */
	private UbuntuOneApi api;

	/**
	 * Default constructor
	 */
	public UbuntuOneServiceBuilder() {
	}

	/**
	 * Configures the api key
	 * 
	 * @param apiKey
	 *            The api key for your application
	 * @return the {@link ServiceBuilder} instance for method chaining
	 */
	public UbuntuOneServiceBuilder apiKey(String apiKey) {
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
	public UbuntuOneServiceBuilder apiSecret(String apiSecret) {
		Preconditions.checkEmptyString(apiSecret, "Invalid Api secret");
		this.apiSecret = apiSecret;
		return this;
	}

	/**
	 * Returns the fully configured {@link OAuthService}
	 * 
	 * @return fully configured {@link OAuthService}
	 */
	public UbuntuOneService build() {
		Preconditions.checkNotNull(api,
				"You must specify a valid api through the provider() method");
		Preconditions.checkEmptyString(apiKey, "You must provide an api key");
		Preconditions.checkEmptyString(apiSecret,
				"You must provide an api secret");
		return api.createService(new OAuthConfig(apiKey, apiSecret, null, null,
				null, null));
	}

	/**
	 * @param apiClass
	 *            Reference to the API class
	 * @return New instance of the given API class
	 */
	private UbuntuOneApi createApi(Class<? extends UbuntuOneApi> apiClass) {
		Preconditions.checkNotNull(apiClass, "Api class cannot be null");
		UbuntuOneApi api;
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
	 * @param apiClass
	 *            the class of one of the existent {@link Api}s on
	 *            org.scribe.api package
	 * @return the {@link ServiceBuilder} instance for method chaining
	 * 
	 */
	public UbuntuOneServiceBuilder provider(
			Class<? extends UbuntuOneApi> apiClass) {
		this.api = createApi(apiClass);
		return this;
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
	public UbuntuOneServiceBuilder provider(UbuntuOneApi api) {
		Preconditions.checkNotNull(api, "Api cannot be null");
		this.api = api;
		return this;
	}
}
