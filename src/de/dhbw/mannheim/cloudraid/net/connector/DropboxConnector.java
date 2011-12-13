package de.dhbw.mannheim.cloudraid.net.connector;

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

import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.DropBoxApi;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verifier;
import org.scribe.oauth.OAuthService;

import de.dhbw.mannheim.cloudraid.net.model.VolumeModel;
import de.dhbw.mannheim.cloudraid.util.Config;

public class DropboxConnector implements IStorageConnector {
	private String appKey = null;
	private String appSecret = null;
	private String sAccessToken = null;
	private String accessToken = null;

	private OAuthService service = null;

	private final static String ROOT_NAME = "sandbox";
	private final static String GET_URL = "https://api-content.dropbox.com/1/files/"
			+ ROOT_NAME + "/";
	private final static String PUT_URL = "https://api-content.dropbox.com/1/files_put/"
			+ ROOT_NAME + "/";
	private final static String DELETE_URL = "https://api.dropbox.com/1/fileops/delete?root="
			+ ROOT_NAME + "&path=";

	public static void main(String[] args) {
		try {
			HashMap<String, String> params = new HashMap<String, String>();
			if (args.length == 2) {
				params.put("appKey", args[0]);
				params.put("appSecret", args[1]);

			} else if (args.length == 4) {
				params.put("appKey", args[0]);
				params.put("appSecret", args[1]);
				params.put("accessToken", args[2]);
				params.put("sAccessToken", args[3]);
			}
			IStorageConnector dbc = StorageConnectorFactory
					.create("de.dhbw.mannheim.cloudraid.net.connector.DropboxConnector",
							params);
			if (dbc.connect()) {
				System.out.println("Connected");
				dbc.put(args[5]);
				dbc.get(args[5]);
				dbc.delete(args[5]);
			} else {
				System.err.println("Connection Error!");
				System.exit(2);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
	}

	@Override
	public boolean connect() {
		service = new ServiceBuilder().provider(DropBoxApi.class)
				.apiKey(this.appKey).apiSecret(this.appSecret).build();
		if (this.accessToken == null || this.sAccessToken == null) {
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
			Token accessToken = service.getAccessToken(requestToken, verifier);
			this.sAccessToken = accessToken.getSecret();
			this.accessToken = accessToken.getToken();
			System.out.println("Your secret access token: "
					+ accessToken.getSecret());
			System.out.println("Your public access token: "
					+ accessToken.getToken());
		}
		return true;
	}

	@Override
	public IStorageConnector create(HashMap<String, String> parameter)
			throws InstantiationException {
		if (parameter.containsKey("sAccessToken")
				&& parameter.containsKey("accessToken")
				&& parameter.containsKey("appKey")
				&& parameter.containsKey("appSecret")) {
			this.sAccessToken = parameter.get("sAccessToken");
			this.accessToken = parameter.get("accessToken");
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
		return this;
	}

	@Override
	public VolumeModel createVolume(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean delete(String resource) {
		System.out.println("DELETE " + resource);
		// This request has to be sent as "POST" not as "DELETE"
		OAuthRequest request = new OAuthRequest(POST, DELETE_URL + resource);
		Token accessToken = new Token(this.accessToken, this.sAccessToken);
		this.service.signRequest(accessToken, request);
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

	@Override
	public void deleteVolume(String name) {
		// TODO Auto-generated method stub

	}

	@Override
	public InputStream get(String resource) {
		System.out.println("GET " + resource);
		OAuthRequest request = new OAuthRequest(GET, GET_URL + resource);
		Token accessToken = new Token(this.accessToken, this.sAccessToken);
		this.service.signRequest(accessToken, request);
		Response response = request.send();
		System.out.println(response.getCode());
		if (response.getCode() == 404) {
			return null;
		}
		return response.getStream();
	}

	@Override
	public VolumeModel getVolume(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String head(String resource) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void loadVolumes() {
		// TODO Auto-generated method stub

	}

	@Override
	public String[] options(String resource) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String post(String resource, String parent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean put(String resource) {
		System.out.println("PUT " + resource);
		File f = new File("/tmp/" + resource);
		if (!f.exists()) {
			System.err.println("File does not exist.");
			return false;
		} else if (f.length() > Config.MAX_FILE_SIZE) {
			System.err.println("File too big");
			return false;
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
		String mime = new MimetypesFileTypeMap().getContentType(f);
		OAuthRequest request = new OAuthRequest(PUT, PUT_URL + resource);
		request.addHeader("Content-Length", String.valueOf(f.length()));
		request.addHeader("Content-Type", mime);
		Token accessToken = new Token(this.accessToken, this.sAccessToken);
		this.service.signRequest(accessToken, request);
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
