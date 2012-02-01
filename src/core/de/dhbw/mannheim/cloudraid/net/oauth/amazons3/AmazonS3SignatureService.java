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

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.scribe.exceptions.OAuthSignatureException;
import org.scribe.services.SignatureService;

import sun.misc.BASE64Encoder;

/**
 * HMAC-SHA1 implementation of {@SignatureService}
 * 
 * @author Pablo Fernandez
 * 
 */
public class AmazonS3SignatureService implements SignatureService {

	/**
	 * Just for easier writing
	 */
	private static final String UTF8 = "UTF-8";

	/**
	 * The Javax.crypto name of HMAC_SHA1
	 */
	private static final String HMAC_SHA1 = "HmacSHA1";

	/**
	 * The printable version of HMAC_SHA1
	 */
	private static final String METHOD = "HMAC-SHA1";

	/**
	 * @param toSign
	 * @param keyString
	 * @return
	 * @throws Exception
	 */
	private String doSign(String toSign, String keyString) throws Exception {
		SecretKeySpec key = new SecretKeySpec((keyString).getBytes(UTF8),
				HMAC_SHA1);
		Mac mac = Mac.getInstance(HMAC_SHA1);
		mac.init(key);
		byte[] bytes = mac.doFinal(toSign.getBytes(UTF8));
		return new BASE64Encoder().encode(bytes).replace("\r\n", "");
	}

	/**
	 * @param baseString
	 *            The string to sign
	 * @param secretKey
	 *            The secret key to use for signing
	 * @return Returns the signature
	 */
	public String getSignature(String baseString, String secretKey) {
		try {
			return doSign(baseString, secretKey);
		} catch (Exception e) {
			throw new OAuthSignatureException(baseString, e);
		}
	}

	/**
	 * Just to fulfill the requirements of the interface! <b>Not used!</b>
	 */
	@Override
	public String getSignature(String baseString, String apiSecret,
			String tokenSecret) {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getSignatureMethod() {
		return METHOD;
	}
}
