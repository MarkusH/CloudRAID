package de.dhbw.mannheim.cloudraid.net.connector;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.scribe.builder.ServiceBuilder;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.utils.URLUtils;

import de.dhbw.mannheim.cloudraid.net.model.VolumeModel;
import de.dhbw.mannheim.cloudraid.net.model.ubuntuone.UbuntuOneVolumeModel;
import de.dhbw.mannheim.cloudraid.net.oauth.ubuntuone.UbuntuOneApi;
import de.dhbw.mannheim.cloudraid.net.oauth.ubuntuone.UbuntuOneService;
import de.dhbw.mannheim.cloudraid.util.Config;

public class UbuntuOneConnector implements IStorageConnector {

	public static void main(String[] args) {
		try {
			if (args.length != 5) {
				System.err
						.println("usage: <customer_key> <customer_secret> <token_key> <token_secret> <resource>");
				System.exit(1);
			}
			UbuntuOneConnector uoc = new UbuntuOneConnector(args[0], args[1],
					args[2], args[3]);
			if (uoc.connect("")) {
				System.out.println("Connected");
			} else {
				System.err.println("Connection Error!");
				System.exit(2);
			}
			uoc.test();
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
	}

	private String username = null;
	private String password = null;
	private Token ctoken = null;
	private Token stoken = null;

	private UbuntuOneService service;

	private HashMap<String, UbuntuOneVolumeModel> volumes = new HashMap<String, UbuntuOneVolumeModel>();

	/**
	 * This constructor initializes the UbuntuOneConnector with a username and
	 * password combination. During the
	 * {@link de.dhbw.mannheim.cloudraid.net.connector.UbuntuOneConnector#connect(String)}
	 * process a customer and an application token is retrieved in a 2-way
	 * handshake. The tokens should be stored in the config with a salt to
	 * prevent the application from creating new ones on each connect process.
	 * 
	 * @param username
	 *            The email address or username to access the UbuntuOne account
	 * @param password
	 *            The password for the given account
	 */
	public UbuntuOneConnector(String username, String password) {
		this.username = username;
		this.password = password;
	}

	/**
	 * This constructor initializes the UbuntuOneConnector directly with the
	 * customer and application tokens. During the
	 * {@link de.dhbw.mannheim.cloudraid.net.connector.UbuntuOneConnector#connect(String)}
	 * process the given tokens are used. If <code>connect()</code> returns
	 * false, one has to authenticate with a username and password
	 * {@link de.dhbw.mannheim.cloudraid.net.connector.UbuntuOneConnector#UbuntuOneConnector(String, String)}
	 * .
	 * 
	 * @param customer_key
	 *            The customer public key
	 * @param customer_secret
	 *            The customer secret key
	 * @param token_key
	 *            The application public key
	 * @param token_secret
	 *            The application secret key
	 */
	public UbuntuOneConnector(String customer_key, String customer_secret,
			String token_key, String token_secret) {
		this.ctoken = new Token(customer_key, customer_secret);
		this.stoken = new Token(token_key, token_secret);
	}

	/**
	 * Similar to the String-based contructor
	 * {@link de.dhbw.mannheim.cloudraid.net.connector.UbuntuOneConnector#UbuntuOneConnector(String, String, String, String)}
	 * , but takes the customer and application tokens as
	 * {@link org.scribe.model.Token}
	 * 
	 * @param ctoken
	 *            The customer token
	 * @param stoken
	 *            The application token
	 */
	public UbuntuOneConnector(Token ctoken, Token stoken) {
		this.ctoken = ctoken;
		this.stoken = stoken;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean connect(String service) {
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
				this.service.getApiBaseEndpoint() + "account/");
		if (response.getCode() == 200) {
			return true;
		}
		return false;
	}

	public VolumeModel createVolume(String name) {
		if (this.volumes.containsKey(name)) {
			return this.volumes.get(name);
		}
		Response response = sendRequest(Verb.PUT,
				this.service.getFileStorageEndpoint() + "volumes/~/" + name
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
		return false;
	}

	public void deleteVolume(String name) {
		if (this.volumes.containsKey(name)) {
			Response response = sendRequest(Verb.DELETE,
					this.service.getFileStorageEndpoint() + "volumes/~/" + name
							+ "/");
			if (response.getCode() == 200) {
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
				this.service.getContentRootEndpoint() + "~/Ubuntu%20One/"
						+ resource);
		if (response.getCode() == 200) {
			return response.getStream();
		} else {
			return null;
		}
	}

	public VolumeModel getVolume(String name) {
		if (this.volumes.containsKey(name)) {
			return this.volumes.get(name);
		}
		Response response = sendRequest(Verb.GET,
				this.service.getFileStorageEndpoint() + "volumes/~/" + name
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
	public String head(String resource) {
		return null;
	}

	public void loadVolumes() {
		Response response = sendRequest(Verb.GET,
				this.service.getFileStorageEndpoint() + "volumes");
		if (response.getCode() == 200) {
			try {
				JSONArray vs = new JSONArray(response.getBody());
				for (int i = 0; i < vs.length(); i++) {
					UbuntuOneVolumeModel volume = new UbuntuOneVolumeModel(
							vs.getJSONObject(i));
					if (this.volumes.containsKey(volume.getName())) {
						this.volumes.get(volume.getName()).addMetadata(
								volume.getMetadata());
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
		if (f.length() > Config.MAX_FILE_SIZE) {
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
					this.service.getContentRootEndpoint() + "~/Ubuntu%20One/"
							+ URLUtils.percentEncode(resource), fileBytes);
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
	 * {@link http://tools.ietf.org/html/rfc2616#section-5.1.1}.
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
	 * @see de.dhbw.mannheim.cloudraid.net.connector.UbuntuOneConnector#sendRequest(Verb,
	 *      String)
	 * @param verb
	 * @param endpoint
	 * @param body
	 * @return
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

	public void test() {
		this.loadVolumes();
		System.out.println(this.volumes);
		this.createVolume("CloudRAID");
		System.out.println(this.volumes);
		this.deleteVolume("CloudRAID");
		System.out.println(this.volumes);
	}
}
