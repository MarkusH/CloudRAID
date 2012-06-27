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

package de.dhbw_mannheim.cloudraid.sugarsync.impl.net.connector;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import javax.activation.MimetypesFileTypeMap;
import javax.net.ssl.HttpsURLConnection;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.osgi.framework.BundleContext;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import de.dhbw_mannheim.cloudraid.config.ICloudRAIDConfig;
import de.dhbw_mannheim.cloudraid.config.exceptions.MissingConfigValueException;
import de.dhbw_mannheim.cloudraid.core.net.connector.IStorageConnector;
import de.dhbw_mannheim.cloudraid.core.net.model.IVolumeModel;

/**
 * The API wrapper for SugarSync.
 * 
 * @author Florian Bausch
 * 
 */
public class SugarSyncConnector implements IStorageConnector {

	private final static String AUTH_URL = "https://api.sugarsync.com/authorization";
	private final static MimetypesFileTypeMap MIME_MAP = new MimetypesFileTypeMap();
	private final static String USER_INFO_URL = "https://api.sugarsync.com/user";

	private String splitOutputDir = null;

	/**
	 * Creates an HTTPS connection with some predefined values
	 * 
	 * @param address
	 *            The address to connect to
	 * @param authToken
	 *            The authentication token
	 * @param method
	 *            The HTTP method
	 * 
	 * @return A preconfigured connection.
	 * @throws IOException
	 *             Thrown, if the connection cannot be established
	 */
	private static HttpsURLConnection getConnection(String address,
			String authToken, String method) throws IOException {
		HttpsURLConnection con = (HttpsURLConnection) new URL(address)
				.openConnection();
		con.setRequestMethod(method);
		con.setRequestProperty("User-Agent", "CloudRAID");
		con.setRequestProperty("Accept", "*/*");
		con.setRequestProperty("Authorization", authToken);

		return con;
	}

	/**
	 * A reference to the current config;
	 */
	private ICloudRAIDConfig config = null;

	private String baseURL = null;

	private DocumentBuilder docBuilder;

	private String token = "";

	private String username, password, accessKeyId, privateAccessKey;
	private int id = -1;

	@Override
	public boolean connect() {
		try {
			// Get the Access Token
			HttpsURLConnection con = SugarSyncConnector.getConnection(AUTH_URL,
					"", "POST");
			con.setDoOutput(true);
			con.setRequestProperty("Content-Type",
					"application/xml; charset=UTF-8");

			// Create authentication request
			StringBuilder authReqBuilder = new StringBuilder(
					"<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n<authRequest><username>");
			authReqBuilder.append(username).append("</username><password>");
			authReqBuilder.append(password).append(
					"</password>\n\t<accessKeyId>");
			authReqBuilder.append(accessKeyId).append(
					"</accessKeyId><privateAccessKey>");
			authReqBuilder.append(privateAccessKey).append(
					"</privateAccessKey></authRequest>");
			String authReq = authReqBuilder.toString();

			con.connect();
			con.getOutputStream().write(authReq.getBytes());
			this.token = con.getHeaderField("Location");
			con.disconnect();

			return true;
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * This function initializes the {@link SugarSyncConnector} with the
	 * customer and application tokens. During the {@link #connect()} process
	 * various tokens are used. If {@link #connect()} returns <code>false</code>
	 * , this class has to be re-instantiated and initialized with proper
	 * credentials. </br>
	 * 
	 * The {@link ICloudRAIDConfig} must contain following keys:
	 * <ul>
	 * <li><code>connector.ID.username</li>
	 * <li><code>connector.ID.customer_secret</code></li>
	 * <li><code>connector.ID.accessKeyId</code></li>
	 * <li><code>connector.ID.privateAccessKey</code></li>
	 * </ul>
	 * 
	 * @param connectorid
	 *            The internal id of this connector.
	 * 
	 * @throws InstantiationException
	 *             Thrown, if some parameters are missing
	 */
	@Override
	public IStorageConnector create(int connectorid)
			throws InstantiationException {
		this.id = connectorid;
		String kUsername = String.format("connector.%d.username", this.id);
		String kPassword = String.format("connector.%d.password", this.id);
		String kAccessKey = String.format("connector.%d.accessKey", this.id);
		String kPrivateAccessKey = String.format(
				"connector.%d.privateAccessKey", this.id);
		try {
			splitOutputDir = this.config.getString("split.output.dir");
			if (this.config.keyExists(kUsername)
					&& this.config.keyExists(kPassword)
					&& this.config.keyExists(kAccessKey)
					&& this.config.keyExists(kPrivateAccessKey)) {
				this.username = this.config.getString(kUsername);
				this.password = this.config.getString(kPassword);
				this.accessKeyId = this.config.getString(kAccessKey);
				this.privateAccessKey = this.config
						.getString(kPrivateAccessKey);
			} else {
				throw new InstantiationException(kUsername + ", " + kPassword
						+ ", " + kAccessKey + " and " + kPrivateAccessKey
						+ " have to be set in the config!");
			}
			docBuilder = null;
			try {
				docBuilder = DocumentBuilderFactory.newInstance()
						.newDocumentBuilder();
				docBuilder.setErrorHandler(null);
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

	/**
	 * Creates a file on SugarSync.
	 * 
	 * @param name
	 *            The file name.
	 * @param f
	 *            The file to be uploaded.
	 * @param parent
	 *            The URL to the parent.
	 * @throws IOException
	 *             Thrown, if no data can be read
	 * @throws SAXException
	 *             Thrown, if the content cannot be parsed
	 * @throws ParserConfigurationException
	 *             Thrown, if the content cannot be parsed
	 */
	private void createFile(String name, File f, String parent)
			throws IOException, SAXException, ParserConfigurationException {
		String mime = MIME_MAP.getContentType(f);
		String request = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><file><displayName>"
				+ name
				+ "</displayName><mediaType>"
				+ mime
				+ "</mediaType></file>";
		HttpsURLConnection con = SugarSyncConnector.getConnection(parent,
				this.token, "POST");
		con.setRequestProperty("Content-Type", "text/xml");
		con.setDoOutput(true);

		con.connect();
		con.getOutputStream().write(request.getBytes());
		InputStream is = con.getInputStream();
		System.out.println(con.getResponseCode() + ": "
				+ con.getResponseMessage());
		con.disconnect();

		String file = this.findFileInFolder(name, parent
				+ "/contents?type=file")
				+ "/data";

		con = SugarSyncConnector.getConnection(file, this.token, "PUT");
		con.setDoOutput(true);
		con.setRequestProperty("Content-Type", mime);

		con.connect();
		OutputStream os = con.getOutputStream();
		is = new FileInputStream(f);
		int i;
		while ((i = is.read()) >= 0) {
			os.write(i);
		}
		System.out.println(con.getResponseCode() + ": "
				+ con.getResponseMessage());
		con.disconnect();
	}

	/**
	 * Creates a folder on SugarSync.
	 * 
	 * @param name
	 *            The name of the folder.
	 * @param parent
	 *            The URL to the parent folder.
	 * @throws IOException
	 *             Thrown, if the content cannot be processed
	 */
	private void createFolder(String name, String parent) throws IOException {
		String request = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
				+ "<folder>" + "\t<displayName>" + name + "</displayName>"
				+ "</folder>";
		HttpsURLConnection con = SugarSyncConnector.getConnection(parent,
				this.token, "POST");
		con.setRequestProperty("Content-Type", "text/xml");
		con.setDoOutput(true);
		con.setDoInput(true);

		con.connect();
		con.getOutputStream().write(request.getBytes());
		InputStream is = con.getInputStream();
		int i;
		while ((i = is.read()) >= 0) {
			System.out.print((char) i);
		}
		con.disconnect();
	}

	@Override
	public IVolumeModel createVolume(String name) {
		return null;
	}

	@Override
	public boolean delete(String resource) {
		String parent;
		if (resource.contains("/"))
			parent = this
					.getResourceURL(resource.substring(0,
							resource.lastIndexOf("/") + 1), false);
		else
			parent = this.getResourceURL("", false);

		if (parent == null) {
			return true;
		}

		String resourceURL;
		try {
			resourceURL = this.findFileInFolder(
					resource.substring(resource.lastIndexOf("/") + 1), parent
							+ "/contents?type=file");
		} catch (Exception e) {
			e.printStackTrace();
			return true;
		}

		HttpsURLConnection con = null;
		try {
			con = SugarSyncConnector.getConnection(resourceURL, this.token,
					"DELETE");
			con.connect();
			con.disconnect();
		} catch (Exception e) {
			e.printStackTrace();
			int returnCode = -1;
			try {
				returnCode = con.getResponseCode();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			if (!(returnCode == 404 || returnCode == 204)) {
				return false;
			}
		}

		try {
			while (this.isFolderEmpty(parent)) {
				String oldParent = parent;
				parent = this.getParentFolder(parent);

				con = SugarSyncConnector.getConnection(oldParent, this.token,
						"DELETE");

				con.connect();
				System.out.println(con.getResponseCode() + ": "
						+ con.getResponseMessage());
				con.disconnect();
			}
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return true;
		}
	}

	@Override
	public void deleteVolume(String name) {
	}

	/**
	 * Checks, if a file is in the specific folder on the SugarSync servers.
	 * 
	 * @param name
	 *            The file name.
	 * @param parent
	 *            The URL to the parent folder.
	 * @return The URL to the file, or null, if it could not be found.
	 * @throws SAXException
	 *             Thrown, if the content cannot be parsed
	 * @throws IOException
	 *             Thrown, if no data can be written
	 * @throws ParserConfigurationException
	 *             Thrown, if the content cannot be parsed
	 */
	private String findFileInFolder(String name, String parent)
			throws SAXException, IOException, ParserConfigurationException {
		Document doc;
		HttpsURLConnection con = SugarSyncConnector.getConnection(parent,
				this.token, "GET");
		con.setDoInput(true);

		// Build the XML tree.
		con.connect();
		doc = docBuilder.parse(con.getInputStream());
		con.disconnect();
		NodeList nl = doc.getDocumentElement().getElementsByTagName("file");
		for (int i = 0; i < nl.getLength(); i++) {
			String displayName = ((Element) nl.item(i))
					.getElementsByTagName("displayName").item(0)
					.getTextContent();
			if (displayName.equalsIgnoreCase(name)) {
				return ((Element) nl.item(i)).getElementsByTagName("ref")
						.item(0).getTextContent();
			}
		}
		return null;
	}

	/**
	 * Checks, if a folder is in the specific folder on the SugarSync servers.
	 * 
	 * @param name
	 *            The folder name.
	 * @param parent
	 *            The URL to the parent folder.
	 * @return The URL to the file, or null, if it could not be found.
	 * @throws SAXException
	 *             Thrown, if the content cannot be parsed
	 * @throws IOException
	 *             Thrown, if no data can be written
	 * @throws ParserConfigurationException
	 *             Thrown, if the content cannot be parsed
	 */
	private String findFolderInFolder(String name, String parent)
			throws ParserConfigurationException, SAXException, IOException {
		Document doc;
		HttpsURLConnection con = SugarSyncConnector.getConnection(parent,
				this.token, "GET");
		con.setDoInput(true);

		// Build the XML tree.
		con.connect();
		doc = docBuilder.parse(con.getInputStream());
		con.disconnect();
		NodeList nl = doc.getDocumentElement().getElementsByTagName(
				"collection");
		for (int i = 0; i < nl.getLength(); i++) {
			String displayName = ((Element) nl.item(i))
					.getElementsByTagName("displayName").item(0)
					.getTextContent();
			if (displayName.equalsIgnoreCase(name)) {
				return ((Element) nl.item(i)).getElementsByTagName("ref")
						.item(0).getTextContent();
			}
		}
		return null;
	}

	@Override
	public InputStream get(String resource) {
		try {
			String parent;
			if (resource.contains("/"))
				parent = this.getResourceURL(
						resource.substring(0, resource.lastIndexOf("/") + 1),
						false);
			else
				parent = this.getResourceURL("", false);
			String resourceURL = this.findFileInFolder(
					resource.substring(resource.lastIndexOf("/") + 1), parent
							+ "/contents?type=file");

			HttpsURLConnection con;
			con = SugarSyncConnector.getConnection(resourceURL + "/data",
					this.token, "GET");
			con.setDoInput(true);

			return con.getInputStream();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Loads and caches the URL to the 'Magic Briefcase' folder.
	 * 
	 * @return The URL to the 'Magic Briefcase' folder on SugarSync.
	 * @throws IOException
	 *             Thrown, if no connection can be established
	 * @throws SAXException
	 *             Thrown, if the content cannot be parsed
	 * @throws ParserConfigurationException
	 *             Thrown, if the content cannot be parsed
	 */
	private String getBaseUrl() throws IOException, SAXException,
			ParserConfigurationException {
		if (baseURL == null) {
			HttpsURLConnection con = SugarSyncConnector.getConnection(
					USER_INFO_URL, this.token, "GET");
			con.setDoInput(true);

			// Build the XML tree.
			con.connect();
			Document doc = docBuilder.parse(con.getInputStream());
			con.disconnect();

			Element node = (Element) doc.getDocumentElement()
					.getElementsByTagName("syncfolders").item(0);
			String folder = node.getTextContent().trim();

			this.baseURL = this.findFolderInFolder("Magic Briefcase", folder);
		}
		return this.baseURL;
	}

	/**
	 * Returns the parent folder of a folder
	 * 
	 * @param folder
	 *            The URL to the folder.
	 * @return The URL of the parent folder.
	 * @throws IOException
	 *             Thrown, if no connection can be established
	 * @throws SAXException
	 *             Thrown, if the content cannot be parsed
	 */
	private String getParentFolder(String folder) throws IOException,
			SAXException {
		HttpsURLConnection con = SugarSyncConnector.getConnection(folder,
				this.token, "GET");
		con.setDoInput(true);

		con.connect();
		Document doc = this.docBuilder.parse(con.getInputStream());
		con.disconnect();

		return doc.getDocumentElement().getElementsByTagName("parent").item(0)
				.getTextContent();
	}

	/**
	 * Runs recursively through the folders in 'Magic Briefcase' to find the
	 * specified folder.
	 * 
	 * @param resource
	 *            The folder to be found.
	 * @param createResource
	 *            Create missing folders.
	 * @return The URL to the folder.
	 */
	private String getResourceURL(String resource, boolean createResource) {
		try {
			String folder = this.getBaseUrl();
			System.out.println(folder);
			while (resource.contains("/")) {
				String parent = folder;
				this.isFolderEmpty(folder);
				folder += "/contents?type=folder";
				String nextName = resource.substring(0, resource.indexOf("/"));
				System.out.println(resource);

				folder = this.findFolderInFolder(nextName, folder);

				resource = resource.substring(resource.indexOf("/") + 1);
				if (createResource && folder == null) {
					this.createFolder(nextName, parent);
					folder = this.findFolderInFolder(nextName, parent
							+ "/contents?type=folder");
				}
			}
			return folder;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public IVolumeModel getVolume(String name) {
		return null;
	}

	@Override
	public String head(String resource) {
		return null;
	}

	/**
	 * Returns if a folder is empty and can be deleted.
	 * 
	 * @param folder
	 *            The URL of the folder.
	 * @return true, if the folder is empty.
	 * @throws IOException
	 *             Thrown, if no connection can be established
	 * @throws SAXException
	 *             Thrown, if the content cannot be parsed
	 */
	private boolean isFolderEmpty(String folder) throws IOException,
			SAXException {
		HttpsURLConnection con = SugarSyncConnector.getConnection(folder
				+ "/contents", this.token, "GET");
		con.setDoInput(true);

		con.connect();
		Document doc = this.docBuilder.parse(con.getInputStream());
		con.disconnect();

		if (!doc.getDocumentElement().hasAttribute("end")
				|| !doc.getDocumentElement().getAttribute("end").equals("0"))
			return false;

		con = SugarSyncConnector.getConnection(folder, this.token, "GET");
		con.setDoInput(true);

		con.connect();
		doc = this.docBuilder.parse(con.getInputStream());
		con.disconnect();

		return !doc.getDocumentElement().getElementsByTagName("displayName")
				.item(0).getTextContent().equals("Magic Briefcase");
	}

	@Override
	public void loadVolumes() {
	}

	@Override
	public String[] options(String resource) {
		return null;
	}

	@Override
	public String post(String resource) {
		return null;
	}

	@Override
	public boolean put(String resource) {
		File f = new File(splitOutputDir + "/" + resource + "." + this.id);
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
			System.err.println("File too big");
		} else if (!f.exists()) {
			System.err.println("File does not exist");
		} else {
			try {
				String parent;
				if (resource.contains("/")) {
					parent = this.getResourceURL(resource.substring(0,
							resource.lastIndexOf("/") + 1), true);
				} else {
					parent = this.getResourceURL("", true);
				}

				String fileName = resource
						.substring(resource.lastIndexOf("/") + 1);
				String resourceURL = this.findFileInFolder(fileName, parent
						+ "/contents?type=file");
				try {
					if (resourceURL != null) {
						System.err
								.println("The file already exists. DELETE it. "
										+ resourceURL);
						HttpsURLConnection con = SugarSyncConnector
								.getConnection(resourceURL, this.token,
										"DELETE");
						con.setDoInput(true);
						con.connect();
						// Do the following steps to _really_ delete the file.
						// If the following steps are missing, the files do not
						// get deleted.
						InputStream is = con.getInputStream();
						while (is.read() >= 0) {
						}
						con.disconnect();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}

				this.createFile(fileName, f, parent);
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
			return true;
		}
		return false;
	}

	protected synchronized void setConfig(ICloudRAIDConfig config) {
		this.config = config;
	}

	protected synchronized void startup(BundleContext context) {

	}

	protected synchronized void shutdown() {

	}

	protected synchronized void unsetConfig(ICloudRAIDConfig config) {
		this.config = null;
	}
}