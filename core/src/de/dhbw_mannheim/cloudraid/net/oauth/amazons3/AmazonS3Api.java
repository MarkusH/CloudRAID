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

package de.dhbw_mannheim.cloudraid.net.oauth.amazons3;

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
public class AmazonS3Api extends DefaultApi10a {

	/**
	 * The base domain for the Amazon Simple Storage Service
	 */
	public static final String S3_BASE_URL = "s3.amazonaws.com";
	/**
	 * The S3 URL
	 */
	private static final String S3_URL = "https://" + S3_BASE_URL + "/";
	/**
	 * A templated S3 Bucket URL
	 */
	private static final String BUCKET_URL = "https://%s." + S3_BASE_URL + "/";

	/**
	 * Returns the {@link org.scribe.oauth.OAuthService} for this
	 * {@link org.scribe.builder.api.DefaultApi10a API}
	 * 
	 * @param config
	 *            The config for the new {@link AmazonS3Api}
	 */
	@Override
	public AmazonS3Service createService(OAuthConfig config) {
		return new AmazonS3Service(this, config);
	}

	@Override
	public String getAccessTokenEndpoint() {
		return "";
	}

	@Override
	public Verb getAccessTokenVerb() {
		return Verb.GET;
	}

	@Override
	public String getAuthorizationUrl(Token requestToken) {
		return "";
	}

	/**
	 * @return The {@link #BUCKET_URL}: {@value #BUCKET_URL}
	 */
	public String getBucketEndpoint() {
		return BUCKET_URL;
	}

	@Override
	public HeaderExtractor getHeaderExtractor() {
		return new AmazonS3HeaderExtractor();
	}

	@Override
	public String getRequestTokenEndpoint() {
		return "";
	}

	@Override
	public RequestTokenExtractor getRequestTokenExtractor() {
		return null;
	}

	@Override
	public Verb getRequestTokenVerb() {
		return Verb.GET;
	}

	/**
	 * @return The {@link #S3_URL}: {@value #S3_URL}
	 */
	public String getS3Endpoint() {
		return S3_URL;
	}

	@Override
	public AmazonS3SignatureService getSignatureService() {
		return new AmazonS3SignatureService();
	}
}
