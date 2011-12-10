package de.dhbw.mannheim.cloudraid.net.connector;

import java.io.InputStream;
import java.util.HashMap;

import org.scribe.builder.ServiceBuilder;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Verb;

import de.dhbw.mannheim.cloudraid.net.model.VolumeModel;
import de.dhbw.mannheim.cloudraid.net.oauth.amazons3.AmazonS3Api;
import de.dhbw.mannheim.cloudraid.net.oauth.amazons3.AmazonS3Service;

public class AmazonS3Connector implements IStorageConnector {

	public static void main(String[] args) {
		try {
			HashMap<String, String> params = new HashMap<String, String>();
			if (args.length == 2) {
				params.put("accessKeyId", args[0]);
				params.put("secretAccessKey", args[1]);
			}
			IStorageConnector as3c = StorageConnectorFactory
					.create("de.dhbw.mannheim.cloudraid.net.connector.AmazonS3Connector",
							params);
			if (as3c.connect()) {
				System.out.println("Connected");
			} else {
				System.err.println("Connection Error!");
				System.exit(2);
			}
			// ((AmazonS3Connector) as3c).test();
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
	}

	private String accessKeyId = null;
	private String secretAccessKey = null;

	private AmazonS3Service service;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean connect() {
		this.service = (AmazonS3Service) new ServiceBuilder()
				.provider(AmazonS3Api.class).apiKey(this.accessKeyId)
				.apiSecret(this.secretAccessKey).build();
		loadVolumes();
		return true;
	}

	/**
	 * This function initializes the UbuntuOneConnector with the customer and
	 * application tokens. During the
	 * {@link de.dhbw.mannheim.cloudraid.net.connector.AmazonS3Connector#connect(String)}
	 * process the given tokens are used. If <code>connect()</code> returns
	 * false, this class has to be reinstanciated and initialized with username
	 * and password.
	 * 
	 * @param param
	 *            <ul>
	 *            <li><code>accessKeyId</code></li>
	 *            <li><code>secretAccessKey</code></li>
	 *            </ul>
	 * @throws InstantiationException
	 * 
	 */
	@Override
	public IStorageConnector create(HashMap<String, String> parameter)
			throws InstantiationException {
		if (parameter.containsKey("accessKeyId")
				&& parameter.containsKey("secretAccessKey")) {
			this.accessKeyId = parameter.get("accessKeyId");
			this.secretAccessKey = parameter.get("secretAccessKey");
		} else {
			throw new InstantiationException(
					"accessKeyId and secretAccessKey have to be set during creation!");
		}
		return this;
	}

	public VolumeModel createVolume(String name) {
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
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public InputStream get(String resource) {
		return null;
	}

	public VolumeModel getVolume(String name) {
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
		Response response = sendRequest(Verb.GET, this.service.getS3Endpoint());
		System.out.println(response.getCode());
		System.out.println(response.getBody());
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
		service.signRequest(request);
		Response response = request.send();
		System.err.println(String.format("@Response(%d, %s, %s)",
				response.getCode(), verb, endpoint));
		System.err.flush();
		return response;
	}

	/**
	 * Add a payload / body content to the request
	 * 
	 * @see de.dhbw.mannheim.cloudraid.net.connector.AmazonS3Connector#sendRequest(Verb,
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
		service.signRequest(request);
		request.addPayload(body);
		Response response = request.send();
		System.err.println(String.format("@Response(%d, %s, %s)",
				response.getCode(), verb, endpoint));
		System.err.flush();
		return response;
	}

	public void test() {
	}
}
