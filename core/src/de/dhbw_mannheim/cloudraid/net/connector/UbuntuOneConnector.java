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

package de.dhbw_mannheim.cloudraid.net.connector;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.scribe.builder.ServiceBuilder;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.oauth.OAuthService;
import org.scribe.utils.OAuthEncoder;

import de.dhbw_mannheim.cloudraid.config.ICloudRAIDConfig;
import de.dhbw_mannheim.cloudraid.net.model.VolumeModel;
import de.dhbw_mannheim.cloudraid.net.model.ubuntuone.UbuntuOneVolumeModel;
import de.dhbw_mannheim.cloudraid.net.oauth.ubuntuone.UbuntuOneApi;
import de.dhbw_mannheim.cloudraid.net.oauth.ubuntuone.UbuntuOneService;

/**
 * @author Markus Holtermann
 */
public class UbuntuOneConnector implements IStorageConnector {

	public static void main(String[] args) {
		try {
			HashMap<String, String> params = new HashMap<String, String>();
			if (args.length == 2) {
				params.put("username", args[0]);
				params.put("password", args[1]);

			} else if (args.length == 4) {
				params.put("customer_key", args[0]);
				params.put("customer_secret", args[1]);
				params.put("token_key", args[2]);
				params.put("token_secret", args[3]);
			}
			IStorageConnector uoc = StorageConnectorFactory
					.create("de.dhbw_mannheim.cloudraid.net.connector.UbuntuOneConnector",
							params);
			if (uoc != null && uoc.connect()) {
				System.out.println("Connected");
			} else {
				System.err.println("Connection Error!");
				System.exit(2);
			}
			// uoc.loadVolumes();
			VolumeModel v = uoc.getVolume("CloudRAID");
			System.out.println(v.metadata);
			((UbuntuOneConnector) uoc).loadDirectories(v);
			System.out.println(v);
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
	}

	/**
	 * The user name at UbuntuOne. This has to be the users email address
	 */
	private String username = null;
	/**
	 * The users password
	 */
	private String password = null;
	/**
	 * Customer token
	 */
	private Token ctoken = null;
	/**
	 * Standard application token
	 */
	private Token stoken = null;

	/**
	 * The {@link OAuthService}
	 */
	private UbuntuOneService service;

	/**
	 * A internal storage for all volumes of the user
	 */
	private HashMap<String, UbuntuOneVolumeModel> volumes = new HashMap<String, UbuntuOneVolumeModel>();

	/**
	 * A reference to the current config;
	 */
	private ICloudRAIDConfig config = null;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean connect() {
		if (this.ctoken != null && this.stoken != null) {
			// We already have the two token sets and will try to use them
			this.service = (UbuntuOneService) new ServiceBuilder()
					.provider(UbuntuOneApi.class)
					.apiKey(this.ctoken.getToken())
					.apiSecret(this.ctoken.getSecret()).build();
		} else {
			this.service = (UbuntuOneService) new ServiceBuilder()
					.provider(UbuntuOneApi.class).apiKey(this.username)
					.apiSecret(this.password).build();
			this.stoken = this.service.getRequestToken();
			this.ctoken = this.service.getAccessToken(this.stoken);
		}

		Response response = sendRequest(Verb.GET,
				this.service.getApiBaseEndpoint() + "/account/");
		if (response.getCode() == 200) {
			return true;
		}
		return false;
	}

	/**
	 * This function initializes the UbuntuOneConnector with the customer and
	 * application tokens. During the
	 * {@link de.dhbw_mannheim.cloudraid.net.connector.UbuntuOneConnector#connect()}
	 * process the given tokens are used. If <code>connect()</code> returns
	 * false, this class has to be reinstantiated and initialized with username
	 * and password.
	 * 
	 * @param parameter
	 *            There are two creation modes. In case the tokens already
	 *            exist, the HashMap has to contain the following keys:
	 *            <ul>
	 *            <li><code>customer_key</li>
	 *            <li><code>customer_secret</code></li>
	 *            <li><code>token_key</code></li>
	 *            <li><code>token_secret</code></li>
	 *            </ul>
	 *            or
	 *            <ul>
	 *            <li><code>username</code></li>
	 *            <li><code>password</code></li>
	 *            </ul>
	 * @throws InstantiationException
	 *             Thrown if not all required parameters are passed.
	 * 
	 */
	@Override
	public IStorageConnector create(HashMap<String, String> parameter)
			throws InstantiationException {
		if (parameter.containsKey("customer_key")
				&& parameter.containsKey("customer_secret")
				&& parameter.containsKey("token_key")
				&& parameter.containsKey("token_secret")) {
			this.ctoken = new Token(parameter.get("customer_key"),
					parameter.get("customer_secret"));
			this.stoken = new Token(parameter.get("token_key"),
					parameter.get("token_secret"));
		} else if (parameter.containsKey("username")
				&& parameter.containsKey("password")) {
			this.username = parameter.get("username");
			this.password = parameter.get("password");
		} else {
			throw new InstantiationException(
					"Either customer_key, customer_secret, token_key and token_secret or username and password have to be set during creation!");
		}
		BundleContext ctx = FrameworkUtil.getBundle(this.getClass())
				.getBundleContext();
		ServiceReference<ICloudRAIDConfig> configServiceReference = ctx
				.getServiceReference(ICloudRAIDConfig.class);
		this.config = ctx.getService(configServiceReference);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public VolumeModel createVolume(String name) {
		if (this.volumes.containsKey(name)) {
			return this.volumes.get(name);
		}
		Response response = sendRequest(Verb.PUT,
				this.service.getFileStorageEndpoint() + "/volumes/~/" + name
						+ "/");
		if (response.getCode() == 200) {
			try {
				UbuntuOneVolumeModel volume = new UbuntuOneVolumeModel(
						response.getBody());
				this.volumes.put(volume.getName(), volume);
				return volume;
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean delete(String resource) {
		Response response = sendRequest(Verb.DELETE,
				this.service.getFileStorageEndpoint() + "/~/Ubuntu%20One/"
						+ OAuthEncoder.encode(resource));
		return (response.getCode() == 200 || response.getCode() == 404);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void deleteVolume(String name) {
		if (this.volumes.containsKey(name)) {
			Response response = sendRequest(Verb.DELETE,
					this.service.getFileStorageEndpoint() + "/volumes/~/"
							+ name + "/");
			if (response.getCode() == 200 || response.getCode() == 404) {
				this.volumes.remove(name);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public InputStream get(String resource) {
		Response response = sendRequest(Verb.GET,
				this.service.getContentRootEndpoint() + "/~/Ubuntu%20One/"
						+ resource);
		if (response.getCode() == 200) {
			return response.getStream();
		} else {
			return null;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public VolumeModel getVolume(String name) {
		if (this.volumes.containsKey(name)) {
			return this.volumes.get(name);
		}
		Response response = sendRequest(Verb.GET,
				this.service.getFileStorageEndpoint() + "/volumes/~/"
						+ OAuthEncoder.encode(name) + "/");
		System.out.println(response.getBody());
		if (response.getCode() == 200) {
			try {
				UbuntuOneVolumeModel volume = new UbuntuOneVolumeModel(
						response.getBody());
				this.volumes.put(volume.getName(), volume);
				return volume;
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String head(String resource) {
		return null;
	}

	/**
	 * @param volume
	 *            The volume to get the directories from
	 */
	public void loadDirectories(VolumeModel volume) {
		Response response = sendRequest(Verb.GET,
				this.service.getFileStorageEndpoint()
						+ (String) volume.metadata.get("node_path_safe")
						+ "/?include_children=true");
		System.out.println(response.getCode());
		System.out.println(response.getBody());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void loadVolumes() {
		Response response = sendRequest(Verb.GET,
				this.service.getFileStorageEndpoint() + "/volumes");
		if (response.getCode() == 200) {
			try {
				JSONArray vs = new JSONArray(response.getBody());
				System.out.println(response.getBody());
				for (int i = 0; i < vs.length(); i++) {
					UbuntuOneVolumeModel volume = new UbuntuOneVolumeModel(
							vs.getJSONObject(i));
					if (this.volumes.containsKey(volume.getName())) {
						this.volumes.get(volume.getName()).metadata
								.putAll(volume.metadata);
					} else {
						this.volumes.put(volume.getName(), volume);
					}
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String[] options(String resource) {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String post(String resource, String parent) {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean put(String resource) {
		File f = new File("/tmp/" + resource);
		int max_filesize;
		try {
			max_filesize = this.config.getInt("filesize.max", null);
			if (f.length() > max_filesize) {
				System.err.println("File too big");
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		if (f.length() > max_filesize) {
			System.err.println("File too big.");
		} else {
			byte[] fileBytes = new byte[(int) f.length()];
			InputStream fis;
			try {
				fis = new FileInputStream("/tmp/" + resource);
				fis.read(fileBytes);
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
			Response response = sendRequest(Verb.PUT,
					this.service.getContentRootEndpoint() + "/~/Ubuntu%20One/"
							+ OAuthEncoder.encode(resource), fileBytes);
			if (response.getCode() == 201) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Creates a {@link org.scribe.model.OAuthRequest} to <code>endpoint</code>
	 * as a HTTP <code>verb</code> Request Method. The request is signed with
	 * the secret customer and application keys.
	 * 
	 * HTTP Request Methods: http://tools.ietf.org/html/rfc2616#section-5.1.1
	 * 
	 * @param verb
	 *            The HTTP Request Method
	 * @param endpoint
	 *            The endpoint URL
	 * @return Returns the corresponding response object to the request
	 */
	private Response sendRequest(Verb verb, String endpoint) {
		System.err.flush();
		OAuthRequest request = new OAuthRequest(verb, endpoint);
		System.err.println(request);
		service.signRequest(this.stoken, request);
		Response response = request.send();
		System.err.println(String.format("@Response(%d, %s, %s)",
				response.getCode(), verb, endpoint));
		System.err.flush();
		return response;
	}

	/**
	 * Add a payload / body content to the request
	 * 
	 * @see de.dhbw_mannheim.cloudraid.net.connector.UbuntuOneConnector#sendRequest(Verb,
	 *      String)
	 * @param verb
	 *            The HTTP method
	 * @param endpoint
	 *            The URL that is called
	 * @param body
	 *            The payload
	 * @return Returns the response of getting the specified endpoint with the
	 *         given method.
	 */
	private Response sendRequest(Verb verb, String endpoint, byte[] body) {
		System.err.flush();
		OAuthRequest request = new OAuthRequest(verb, endpoint);
		System.err.println(request);
		service.signRequest(this.stoken, request);
		request.addPayload(body);
		Response response = request.send();
		System.err.println(String.format("@Response(%d, %s, %s)",
				response.getCode(), verb, endpoint));
		System.err.flush();
		return response;
	}

}
