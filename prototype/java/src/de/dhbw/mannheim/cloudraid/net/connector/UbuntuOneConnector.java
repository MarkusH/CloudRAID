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
			HashMap<String, String> params = new HashMap<String, String>();
			if (args.length == 2) {
				params.put("username", args[0]);
				params.put("password", args[1]);

			} else if (args.length == 4) {
				params.put("customer_key", args[0]);
				params.put("customer_secret", args[1]);
				params.put("token_key", args[2]);
				params.put("token_secret", args[3]);
			} else {
				System.err
						.println("usage: <customer_key> <customer_secret> <token_key> <token_secret>");
				System.exit(1);
			}
			IStorageConnector uoc = StorageConnectorFactory
					.create("de.dhbw.mannheim.cloudraid.net.connector.UbuntuOneConnector",
							params);
			if (uoc.connect()) {
				System.out.println("Connected");
			} else {
				System.err.println("Connection Error!");
				System.exit(2);
			}
			((UbuntuOneConnector) uoc).test();
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
				this.service.getApiBaseEndpoint() + "account/");
		if (response.getCode() == 200) {
			return true;
		}
		return false;
	}

	/**
	 * This function initializes the UbuntuOneConnector with the customer and
	 * application tokens. During the
	 * {@link de.dhbw.mannheim.cloudraid.net.connector.UbuntuOneConnector#connect(String)}
	 * process the given tokens are used. If <code>connect()</code> returns
	 * false, this class has to be reinstanciated and initialized with username
	 * and password.
	 * 
	 * @param param
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
		return this;
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
		Response response = sendRequest(Verb.DELETE,
				this.service.getFileStorageEndpoint() + "~/Ubuntu%20One/"
						+ URLUtils.percentEncode(resource));
		return (response.getCode() == 200 || response.getCode() == 404);
	}

	public void deleteVolume(String name) {
		if (this.volumes.containsKey(name)) {
			Response response = sendRequest(Verb.DELETE,
					this.service.getFileStorageEndpoint() + "volumes/~/" + name
							+ "/");
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

		this.delete("test.txt");
	}
}
