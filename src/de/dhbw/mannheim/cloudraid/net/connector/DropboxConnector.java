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
	private final static String ROOT_NAME = "sandbox";
	private final static String DELETE_URL = "https://api.dropbox.com/1/fileops/delete?root="
			+ ROOT_NAME + "&path=";
	private final static String GET_URL = "https://api-content.dropbox.com/1/files/"
			+ ROOT_NAME + "/";
	private final static String PUT_URL = "https://api-content.dropbox.com/1/files_put/"
			+ ROOT_NAME + "/";

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
					.create("de.dhbw.mannheim.cloudraid.net.connector.DropboxConnector",
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

	private final static MimetypesFileTypeMap MIME_MAP = new MimetypesFileTypeMap();

	private String accessTokenValue = null;
	private String appKey = null;
	private String appSecret = null;
	private String accessTokenSecret = null;

	private OAuthService service = null;

	private Token accessToken = null;

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

	@Override
	public void deleteVolume(String name) {
		// TODO Auto-generated method stub

	}

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
