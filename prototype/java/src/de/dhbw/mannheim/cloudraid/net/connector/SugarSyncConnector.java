package de.dhbw.mannheim.cloudraid.net.connector;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class SugarSyncConnector implements IStorageConnector {

	private String token = "";
	private String username, password, accessKeyId, privateAccessKey;

	/**
	 * Does some stuff with the SugarSync API
	 * 
	 * @param username
	 * @param password
	 * @param accessKeyId
	 * @param privateAccessKey
	 */
	public SugarSyncConnector(String username, String password,
			String accessKeyId, String privateAccessKey) {
		this.password = password;
		this.username = username;
		this.accessKeyId = accessKeyId;
		this.privateAccessKey = privateAccessKey;
		/*
		 * try {
		 * 
		 * // Get the Access Token HttpsURLConnection con = null; String
		 * sugarsyncAccess = ""; InputStream is = null;
		 * 
		 * // Get user information: // url = new //
		 * URL("https://api.sugarsync.com/workspace/:sc:2099477:0"); // url =
		 * new URL("https://api.sugarsync.com/user/"); // url = new //
		 * URL("https://api.sugarsync.com/user/2099477/folders/contents"); //
		 * url = new //
		 * URL("https://api.sugarsync.com/folder/:sc:2099477:2/contents"); con =
		 * SugarSyncConnector .getConnection(
		 * "https://api.sugarsync.com/folder/:sc:2099477:202_91974608/contents",
		 * sugarsyncAccess, "GET"); con.setDoInput(true); is =
		 * con.getInputStream(); int i;
		 * System.out.println("SugarSync response for your request:"); while ((i
		 * = is.read()) >= 0) System.out.print((char) i);
		 * System.out.println("\n");
		 * 
		 * // Get a file System.out.println("Get a file..."); con =
		 * SugarSyncConnector .getConnection(
		 * "https://api.sugarsync.com/file/:sc:2099477:202_91974881/data",
		 * sugarsyncAccess, "GET"); con.setDoInput(true);
		 * con.setAllowUserInteraction(false);
		 * 
		 * is = null; is = con.getInputStream();
		 * 
		 * File f = new File("/tmp/sugarFile.pdf"); FileOutputStream fos = null;
		 * fos = new FileOutputStream(f);
		 * 
		 * byte[] inputBytes = new byte[02000]; int readLength; while
		 * ((readLength = is.read(inputBytes)) >= 0) { fos.write(inputBytes, 0,
		 * readLength); } System.out.println("Done."); } catch (Exception e) {
		 * e.printStackTrace(); return; }
		 */
	}

	/**
	 * Creates an HTTPS connection with some predefined values
	 * 
	 * @return A preconfigured connection.
	 */
	private static HttpsURLConnection getConnection(String address,
			String authToken, String method) throws IOException {
		HttpsURLConnection con = (HttpsURLConnection) new URL(address)
				.openConnection();
		con.setRequestMethod(method);
		con.setRequestProperty("User-Agent", "CloudRAID");
		con.setRequestProperty("Accept", "*/*");
		con.addRequestProperty("Authorization", authToken);

		return con;
	}

	public static void main(String[] args) {
		try {
			if (args.length != 4) {
				System.err
						.println("usage: username password accessKey privateAccessKey");
				return;
			}
			SugarSyncConnector ssc = new SugarSyncConnector(args[0], args[1],
					args[2], args[3]);
			ssc.connect("");
			InputStream is = ssc
					.get("Sample Documents/SugarSync QuickStart Guide.pdf");
			File f = new File("/tmp/sugarFile.pdf");
			FileOutputStream fos = null;
			fos = new FileOutputStream(f);

			byte[] inputBytes = new byte[02000];
			int readLength;
			while ((readLength = is.read(inputBytes)) >= 0) {
				fos.write(inputBytes, 0, readLength);
			}
			System.out.println("Done.");
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
	}

	@Override
	public boolean connect(String service) {
		try {
			// Get the Access Token
			HttpsURLConnection con = SugarSyncConnector.getConnection(
					"https://api.sugarsync.com/authorization", "", "POST");
			con.setDoOutput(true);
			con.setRequestProperty("Content-Type",
					"application/xml; charset=UTF-8");

			// Create authentication request
			String authReq = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n<authRequest>";
			authReq += "\n\t<username>" + username
					+ "</username>\n\t<password>" + password + "</password>";
			authReq += "\n\t<accessKeyId>" + accessKeyId + "</accessKeyId>";
			authReq += "\n\t<privateAccessKey>" + privateAccessKey
					+ "</privateAccessKey>";
			authReq += "\n</authRequest>";
			con.getOutputStream().write(authReq.getBytes());

			this.token = con.getHeaderField("Location");
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	@Override
	public boolean put(String resource) {
		// TODO Auto-generated method stub
		// String parentResource = this.getResourceURL(resource.substring(0,
		// resource.lastIndexOf("/")));
		return false;
	}

	@Override
	public InputStream get(String resource) {
		try {
			String parentResource;
			if (resource.contains("/"))
				parentResource = this.getResourceURL(resource.substring(0,
						resource.lastIndexOf("/") + 1));
			else
				parentResource = this.getResourceURL("");
			HttpsURLConnection con;
			String resourceURL = this.findFileInFolder(
					resource.substring(resource.indexOf("/") + 1),
					parentResource + "/contents?type=file");
			con = SugarSyncConnector.getConnection(resourceURL + "/data",
					this.token, "GET");
			con.setDoInput(true);
			return con.getInputStream();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public boolean delete(String resource) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String post(String resource, String parent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String[] options(String resource) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String head(String resource) {
		// TODO Auto-generated method stub
		return null;
	}

	private String getResourceURL(String parent) {
		try {
			HttpsURLConnection con = SugarSyncConnector.getConnection(
					"https://api.sugarsync.com/user", this.token, "GET");
			con.setDoInput(true);
			DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance()
					.newDocumentBuilder();
			docBuilder.setErrorHandler(null);

			// Build the XML tree.
			Document doc;
			doc = docBuilder.parse(con.getInputStream());
			Element node = (Element) doc.getDocumentElement()
					.getElementsByTagName("syncfolders").item(0);
			String folder = node.getTextContent().trim();

			folder = this.findFolderInFolder("Magic Briefcase", folder);

			System.out.println(folder);
			while (parent.contains("/")) {
				String nextName = parent.substring(0, parent.indexOf("/"));
				folder = folder + "/contents?type=folder";
				System.out.println(parent);

				folder = this.findFolderInFolder(nextName, folder);

				parent = parent.substring(parent.indexOf("/") + 1);
			}
			return folder;// + "/contents?type=files";
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	private String findFolderInFolder(String name, String parent)
			throws ParserConfigurationException, SAXException, IOException {
		HttpsURLConnection con = SugarSyncConnector.getConnection(parent,
				this.token, "GET");
		con.setDoInput(true);
		DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance()
				.newDocumentBuilder();
		docBuilder.setErrorHandler(null);

		// Build the XML tree.
		Document doc;
		doc = docBuilder.parse(con.getInputStream());
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

	private String findFileInFolder(String name, String parent)
			throws SAXException, IOException, ParserConfigurationException {
		HttpsURLConnection con = SugarSyncConnector.getConnection(parent,
				this.token, "GET");
		con.setDoInput(true);
		DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance()
				.newDocumentBuilder();
		docBuilder.setErrorHandler(null);

		// Build the XML tree.
		Document doc;
		doc = docBuilder.parse(con.getInputStream());
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

}
