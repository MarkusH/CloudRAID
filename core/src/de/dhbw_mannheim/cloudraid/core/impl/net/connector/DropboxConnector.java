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

package de.dhbw_mannheim.cloudraid.core.impl.net.connector;

import static org.scribe.model.Verb.GET;
import static org.scribe.model.Verb.POST;
import static org.scribe.model.Verb.PUT;

import java.awt.Desktop;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.Scanner;

import javax.activation.MimetypesFileTypeMap;

import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.DropBoxApi;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verifier;
import org.scribe.oauth.OAuthService;

import de.dhbw_mannheim.cloudraid.config.ICloudRAIDConfig;
import de.dhbw_mannheim.cloudraid.core.net.connector.IStorageConnector;
import de.dhbw_mannheim.cloudraid.core.net.model.IVolumeModel;

/**
 * The API wrapper for Dropbox (API version 1).
 * 
 * @author Florian Bausch
 * 
 */
public class DropboxConnector implements IStorageConnector {
	private final static String ROOT_NAME = "sandbox";
	private final static String DELETE_URL = "https://api.dropbox.com/1/fileops/delete?root="
			+ ROOT_NAME + "&path=";
	private final static String GET_URL = "https://api-content.dropbox.com/1/files/"
			+ ROOT_NAME + "/";
	private final static String PUT_URL = "https://api-content.dropbox.com/1/files_put/"
			+ ROOT_NAME + "/";

	private final static MimetypesFileTypeMap MIME_MAP = new MimetypesFileTypeMap();

	public static void main(String[] args) {
		try {
			HashMap<String, String> params = new HashMap<String, String>();
			if (args.length == 3) {
				params.put("appKey", args[0]);
				params.put("appSecret", args[1]);

			} else if (args.length == 5) {
				params.put("appKey", args[0]);
				params.put("appSecret", args[1]);
				params.put("accessTokenValue", args[2]);
				params.put("accessTokenSecret", args[3]);

			} else {
				System.err.println("Wrong parameters");
				System.err.println("usage: appKey appSecret path-to-resource");
				System.err
						.println("usage: appKey appSecret accessToken accessTokenSecret path-to-resource");
				return;
			}
			IStorageConnector dbc = StorageConnectorFactory
					.create("de.dhbw_mannheim.cloudraid.net.connector.DropboxConnector",
							params);
			if (dbc.connect()) {
				System.out.println("Connected");
				dbc.put(args[args.length - 1]);
				dbc.get(args[args.length - 1]);
				dbc.delete(args[args.length - 1]);
			} else {
				System.err.println("Connection Error!");
				System.exit(2);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
	}

	/**
	 * A reference to the current config;
	 */
	private ICloudRAIDConfig config = null;

	private String accessTokenValue = null;
	private String appKey = null;
	private String appSecret = null;
	private String accessTokenSecret = null;

	private OAuthService service = null;

	private Token accessToken = null;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean connect() {
		service = new ServiceBuilder().provider(DropBoxApi.class)
				.apiKey(this.appKey).apiSecret(this.appSecret).build();
		if (this.accessTokenValue == null || this.accessTokenSecret == null) {
			Scanner in = new Scanner(System.in);
			Token requestToken = service.getRequestToken();
			try {
				Desktop.getDesktop().browse(
						new URI(service.getAuthorizationUrl(requestToken)));
				System.out
						.println("Please authorize the app and then press enter in this window.");
			} catch (Exception e) {
				System.out
						.println("Please go to "
								+ service.getAuthorizationUrl(requestToken)
								+ " , authorize the app and then press enter in this window.");
			}
			in.nextLine();
			Verifier verifier = new Verifier("");
			System.out.println();
			this.accessToken = service.getAccessToken(requestToken, verifier);
			this.accessTokenSecret = this.accessToken.getSecret();
			this.accessTokenValue = this.accessToken.getToken();
			System.out.println("Your secret access token: "
					+ this.accessTokenSecret);
			System.out.println("Your public access token: "
					+ this.accessTokenValue);
		} else {
			this.accessToken = new Token(this.accessTokenValue,
					this.accessTokenSecret);
		}
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IStorageConnector create(HashMap<String, String> parameter)
			throws InstantiationException {
		if (parameter.containsKey("accessTokenSecret")
				&& parameter.containsKey("accessTokenValue")
				&& parameter.containsKey("appKey")
				&& parameter.containsKey("appSecret")) {
			this.accessTokenSecret = parameter.get("accessTokenSecret");
			this.accessTokenValue = parameter.get("accessTokenValue");
			this.appKey = parameter.get("appKey");
			this.appSecret = parameter.get("appSecret");
		} else if (parameter.containsKey("appKey")
				&& parameter.containsKey("appSecret")) {
			this.appKey = parameter.get("appKey");
			this.appSecret = parameter.get("appSecret");
		} else {
			throw new InstantiationException(
					"Could not find required parameters.");
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
	public IVolumeModel createVolume(String name) {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean delete(String resource) {
		System.out.println("DELETE " + resource);
		// This request has to be sent as "POST" not as "DELETE"
		OAuthRequest request = new OAuthRequest(POST, DELETE_URL + resource);
		this.service.signRequest(this.accessToken, request);
		Response response = request.send();
		System.out.println(response.getCode() + " " + response.getBody());
		if (response.getCode() == 406) {
			System.err.println("Would delete too much files");
			return false;
		} else if (response.getCode() == 404) {
			System.err.println("File does not exist.");
		}
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void deleteVolume(String name) {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public InputStream get(String resource) {
		System.out.println("GET " + resource);
		OAuthRequest request = new OAuthRequest(GET, GET_URL + resource);
		this.service.signRequest(this.accessToken, request);
		Response response = request.send();
		System.out.println(response.getCode());
		if (response.getCode() == 404) {
			return null;
		}
		return response.getStream();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IVolumeModel getVolume(String name) {
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
	 * {@inheritDoc}
	 */
	@Override
	public void loadVolumes() {

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
		System.out.println("PUT " + resource);
		File f = new File("/tmp/" + resource);
		if (!f.exists()) {
			System.err.println("File does not exist.");
			return false;
		} else {
			int max_filesize;
			try {
				max_filesize = this.config.getInt("filesize.max", null);
				if (f.length() > max_filesize) {
					System.err.println("File too big");
					return false;
				}
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
		}

		byte[] fileBytes = new byte[(int) f.length()];
		InputStream fis;
		try {
			fis = new FileInputStream("/tmp/" + resource);
			fis.read(fileBytes);
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		OAuthRequest request = new OAuthRequest(PUT, PUT_URL + resource);
		request.addHeader("Content-Type", MIME_MAP.getContentType(f));
		this.service.signRequest(this.accessToken, request);
		request.addPayload(fileBytes);
		Response response = request.send();
		System.out.println(response.getCode() + " " + response.getBody());
		if (response.getCode() == 411 || response.getCode() == 400) {
			System.err.println("Could not PUT file to Dropbox.");
			return false;
		}
		return true;
	}

}
