package de.dhbw.mannheim.cloudraid.net.connector;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class SugarSyncConnector {

	/**
	 * Does some stuff with the SugarSync API
	 * @param username 
	 * @param password
	 * @param accessKeyId
	 * @param privateAccessKey
	 */
	public SugarSyncConnector(String username, String password,
			String accessKeyId, String privateAccessKey) {
		try {

			// Get the Access Token
			HttpsURLConnection con = null;
			String sugarsyncAccess = "";
			InputStream is = null;

			con = SugarSyncConnector.getConnection(
					"https://api.sugarsync.com/authorization", "", "POST");
			con.setDoOutput(true);
			con.setRequestProperty("Content-Type",
					"application/xml; charset=UTF-8");

			// Create authentication request
			OutputStream os = con.getOutputStream();
			String authReq = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n<authRequest>";
			authReq += "\n\t<username>" + username
					+ "</username>\n\t<password>" + password + "</password>";
			authReq += "\n\t<accessKeyId>" + accessKeyId + "</accessKeyId>";
			authReq += "\n\t<privateAccessKey>" + privateAccessKey
					+ "</privateAccessKey>";
			authReq += "\n</authRequest>";
			os.write(authReq.getBytes());

			System.out.println(con.getResponseCode());
			sugarsyncAccess = con.getHeaderField("Location");
			System.out.println("Access Token: " + sugarsyncAccess);
			System.out.println();

			// Get user information:
			// url = new
			// URL("https://api.sugarsync.com/workspace/:sc:2099477:0");
			// url = new URL("https://api.sugarsync.com/user/");
			// url = new
			// URL("https://api.sugarsync.com/user/2099477/folders/contents");
			// url = new
			// URL("https://api.sugarsync.com/folder/:sc:2099477:2/contents");
			con = SugarSyncConnector
					.getConnection(
							"https://api.sugarsync.com/folder/:sc:2099477:202_91974608/contents",
							sugarsyncAccess, "GET");
			con.setDoInput(true);
			is = con.getInputStream();
			int i;
			System.out.println("SugarSync response for your request:");
			while ((i = is.read()) >= 0)
				System.out.print((char) i);
			System.out.println("\n");

			// Get a file
			System.out.println("Get a file...");
			con = SugarSyncConnector
					.getConnection(
							"https://api.sugarsync.com/file/:sc:2099477:202_91974881/data",
							sugarsyncAccess, "GET");
			con.setDoInput(true);
			con.setAllowUserInteraction(false);

			is = null;
			is = con.getInputStream();

			File f = new File("/tmp/sugarFile.pdf");
			FileOutputStream fos = null;
			fos = new FileOutputStream(f);

			byte[] inputBytes = new byte[1024];
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
		if (args.length != 4) {
			System.err
					.println("usage: username password accessKey privateAccessKey");
			return;
		}
		new SugarSyncConnector(args[0], args[1], args[2], args[3]);
	}

}
