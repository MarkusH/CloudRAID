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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.scribe.exceptions.OAuthParametersMissingException;
import org.scribe.extractors.HeaderExtractorImpl;
import org.scribe.model.OAuthRequest;
import org.scribe.utils.OAuthEncoder;
import org.scribe.utils.Preconditions;

/**
 * @author Markus Holtermann
 * 
 */
public class UbuntuOneHeaderExtractor extends HeaderExtractorImpl {

	/**
	 * Headers are separated by this value:
	 * <code>{@value #PARAM_SEPARATOR}</code>
	 */
	private static final String PARAM_SEPARATOR = ", ";
	/**
	 * Begin of the Authorization header: <code>{@value #AUTH_HEADER}</code>
	 */
	private static final String AUTH_HEADER = "OAuth realm=\"\"";

	/**
	 * @param request
	 *            The current request
	 */
	private void checkPreconditions(OAuthRequest request) {
		Preconditions.checkNotNull(request,
				"Cannot extract a header from a null object");

		if (request.getOauthParameters() == null
				|| request.getOauthParameters().size() <= 0) {
			throw new OAuthParametersMissingException(request);
		}
	}

	@Override
	public String extract(OAuthRequest request) {
		checkPreconditions(request);
		Map<String, String> parameters = request.getOauthParameters();
		StringBuffer header = new StringBuffer(parameters.size() * 20);
		header.append(AUTH_HEADER);
		Set<String> set = parameters.keySet();
		String[] keys = new String[set.size()];
		set.toArray(keys);
		List<String> tmpkeyList = Arrays.asList(keys);
		Collections.sort(tmpkeyList);
		System.err
				.println("[DEBUG] UbuntuOneHeaderExtractor.extract(): tmpkeyList = "
						+ tmpkeyList.toString());
		for (String key : tmpkeyList) {
			header.append(PARAM_SEPARATOR);
			header.append(String.format("%s=\"%s\"", key,
					OAuthEncoder.encode(parameters.get(key))));
		}
		return header.toString();
	}
}
