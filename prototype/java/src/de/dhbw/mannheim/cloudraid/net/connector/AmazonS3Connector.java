package de.dhbw.mannheim.cloudraid.net.connector;

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
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import de.dhbw.mannheim.cloudraid.net.model.VolumeModel;
import de.dhbw.mannheim.cloudraid.net.model.amazons3.AmazonS3VolumeModel;
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
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
	}

	private String accessKeyId = null;
	private String secretAccessKey = null;
	private DocumentBuilder docBuilder;
	private InputSource is;

	private AmazonS3Service service;

	private HashMap<String, AmazonS3VolumeModel> volumes = null;

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
		try {
			docBuilder = DocumentBuilderFactory.newInstance()
					.newDocumentBuilder();
			docBuilder.setErrorHandler(null);
			is = new InputSource();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
		return this;
	}

	@Override
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

	@Override
	public void deleteVolume(String name) {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public InputStream get(String resource) {
		return null;
	}

	@Override
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
						this.volumes.get(volume.getName()).addMetadata(
								volume.getMetadata());
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

}
