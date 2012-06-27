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

package de.dhbw_mannheim.cloudraid.amazons3.impl.net.connector;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.scribe.builder.ServiceBuilder;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Verb;
import org.scribe.oauth.OAuthService;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import de.dhbw_mannheim.cloudraid.amazons3.impl.net.model.AmazonS3VolumeModel;
import de.dhbw_mannheim.cloudraid.amazons3.impl.net.oauth.AmazonS3Api;
import de.dhbw_mannheim.cloudraid.amazons3.impl.net.oauth.AmazonS3Service;
import de.dhbw_mannheim.cloudraid.config.ICloudRAIDConfig;
import de.dhbw_mannheim.cloudraid.config.exceptions.MissingConfigValueException;
import de.dhbw_mannheim.cloudraid.core.net.connector.IStorageConnector;
import de.dhbw_mannheim.cloudraid.core.net.model.IVolumeModel;

/**
 * @author Markus Holtermann
 */
public class AmazonS3Connector implements IStorageConnector {

	/**
	 * The users public key
	 */
	private String accessKeyId = null;

	/**
	 * The users secret key
	 */
	private String secretAccessKey = null;

	/**
	 * A global XML parser
	 */
	private DocumentBuilder docBuilder;

	/**
	 * The input source to parse a String as XML
	 */
	private InputSource is;

	/**
	 * The regarding {@link OAuthService}
	 */
	private AmazonS3Service service;

	/**
	 * A internal storage for all volumes of the user
	 */
	private HashMap<String, AmazonS3VolumeModel> volumes = null;

	/**
	 * A reference to the current config;
	 */
	private ICloudRAIDConfig config = null;

	private int id = -1;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean connect() {
		this.service = (AmazonS3Service) new ServiceBuilder()
				.provider(AmazonS3Api.class).apiKey(this.accessKeyId)
				.apiSecret(this.secretAccessKey).build();
		loadVolumes();
		// This works, since `this.volumes` is null by default and only becomes
		// a HashMap iff `loadVolumes()` succeeded.
		if (this.volumes == null) {
			// but we create the volumes map here just to ensure that we do not
			// run in NullPointerExceptions
			this.volumes = new HashMap<String, AmazonS3VolumeModel>();
			return false;
		}
		return true;
	}

	/**
	 * This function initializes the {@link AmazonS3Connector} with the customer
	 * and application tokens. During the {@link #connect()} process various
	 * tokens are used. If {@link #connect()} returns <code>false</code>, this
	 * class has to be re-instantiated and initialized with proper credentials.
	 * </br>
	 * 
	 * The {@link ICloudRAIDConfig} must contain following keys:
	 * <ul>
	 * <li><code>connector.ID.accessKeyId</code></li>
	 * <li><code>connector.ID.secretAccessKey</code></li>
	 * </ul>
	 * 
	 * @param connectorid
	 *            The internal id of this connector.
	 * @param config
	 *            The reference to a running {@link ICloudRAIDConfig} service.
	 * 
	 * @throws InstantiationException
	 *             Thrown if not all required parameters are passed.
	 */
	@Override
	public IStorageConnector create(int connectorid, ICloudRAIDConfig config)
			throws InstantiationException {
		this.id = connectorid;
		this.config = config;
		String kAccessKeyId = String
				.format("connector.%d.accessKeyId", this.id);
		String ksecretAccessKey = String.format("connector.%d.secretAccessKey",
				this.id);
		try {
			if (this.config.keyExists(kAccessKeyId)
					&& this.config.keyExists(ksecretAccessKey)) {
				this.accessKeyId = this.config.getString(kAccessKeyId);
				this.secretAccessKey = this.config.getString(ksecretAccessKey);
			} else {
				throw new InstantiationException(kAccessKeyId + " and "
						+ ksecretAccessKey + " have to be set in the config!");
			}
			try {
				docBuilder = DocumentBuilderFactory.newInstance()
						.newDocumentBuilder();
				docBuilder.setErrorHandler(null);
				is = new InputSource();
			} catch (ParserConfigurationException e) {
				e.printStackTrace();
				throw new InstantiationException(e.getMessage());
			}
		} catch (MissingConfigValueException e) {
			e.printStackTrace();
			throw new InstantiationException(e.getMessage());
		}
		return this;
	}

	@Override
	public IVolumeModel createVolume(String name) {
		return null;
	}

	@Override
	public boolean delete(String resource) {
		return false;
	}

	@Override
	public void deleteVolume(String name) {
	}

	@Override
	public void disconnect() {
		// TODO Auto-generated method stub

	}

	@Override
	public InputStream get(String resource) {
		return null;
	}

	@Override
	public IVolumeModel getVolume(String name) {
		if (this.volumes.containsKey(name)) {
			return this.volumes.get(name);
		}
		// There is no way to detect the meta data for a single bucket. So
		// reload it an return is if possible
		loadVolumes();
		if (this.volumes.containsKey(name)) {
			return this.volumes.get(name);
		}
		return null;
	}

	@Override
	public void loadVolumes() {
		Response response = sendRequest(Verb.GET, this.service.getS3Endpoint());
		if (response.getCode() == 200) {
			if (this.volumes == null) {
				this.volumes = new HashMap<String, AmazonS3VolumeModel>();
			}
			try {
				is.setCharacterStream(new StringReader(response.getBody()));
				Document doc = docBuilder.parse(is);
				NodeList nl = doc.getDocumentElement().getElementsByTagName(
						"Bucket");
				for (int i = 0; i < nl.getLength(); i++) {
					AmazonS3VolumeModel volume = new AmazonS3VolumeModel(
							nl.item(i));
					if (this.volumes.containsKey(volume.getName())) {
						this.volumes.get(volume.getName()).getMetadata()
								.putAll(volume.getMetadata());
					} else {
						this.volumes.put(volume.getName(), volume);
					}
				}
			} catch (SAXException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
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
		service.signRequest(request);
		Response response = request.send();
		System.err.println(String.format("@Response(%d, %s, %s)",
				response.getCode(), verb, endpoint));
		System.err.print(response.getBody());
		System.err.flush();
		return response;
	}

	@Override
	public boolean update(String resource) {
		return false;
	}

	@Override
	public boolean upload(String resource) {
		return false;
	}
}
