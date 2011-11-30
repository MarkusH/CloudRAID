package de.dhbw.mannheim.cloudraid.net.connector;

import java.io.OutputStream;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class SugarSyncConnector {

	public SugarSyncConnector(String username, String password,
			String accessKeyId, String privateAccessKey) {
		try {
			URL url = null;
			HttpsURLConnection con = null;
			// String sugarsyncAccess = "";

			url = new URL("https://api.sugarsync.com/authorization");

			con = (HttpsURLConnection) url.openConnection();
			con.setRequestMethod("POST");
			con.setDoOutput(true);
			con.setRequestProperty("Content-Type",
					"application/xml; charset=UTF-8");
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
			if (con.getResponseCode() == HttpsURLConnection.HTTP_UNAUTHORIZED) {
				System.err.println("Authorization denied");
				return;
			}

			System.out.println(con.getResponseMessage());

			// Not working:
			// url = new URL("https://api.sugarsync.com/file/xyzabc123/data");
			//
			// con = (HttpsURLConnection) url.openConnection();
			// con.addRequestProperty("Authorization", sugarsyncAccess);
			//
			// con.setDoOutput(true);
			// con.setRequestMethod("GET");
			//
			// int responseCode = con.getResponseCode();
			// String responseProperties = con.getResponseMessage();
			//
			// System.out.println(responseCode + "; " + responseProperties);
			//
			// InputStream is = null;
			// is = con.getInputStream();
			//
			// File f = new File("/tmp/sugarFile.txt");
			// FileOutputStream fos = null;
			// fos = new FileOutputStream(f);
			//
			// byte[] inputBytes = new byte[1024];
			// int readLength;
			// while ((readLength = is.read(inputBytes)) >= 0) {
			// fos.write(inputBytes, 0, readLength);
			// }
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
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
