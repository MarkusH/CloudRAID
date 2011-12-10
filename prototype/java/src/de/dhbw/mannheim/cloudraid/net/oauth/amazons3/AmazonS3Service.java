/**
 *
 */
package de.dhbw.mannheim.cloudraid.net.oauth.amazons3;

import org.scribe.model.OAuthConfig;
import org.scribe.model.OAuthConstants;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Token;
import org.scribe.model.Verifier;
import org.scribe.oauth.OAuthService;

/**
 * @author Markus Holtermann
 * 
 */
public class AmazonS3Service implements OAuthService {

	private static final String VERSION = "1.0";

	private AmazonS3Api api;
	private OAuthConfig config;

	/**
	 * This constructs a new OAuthService for AmazonS3 that handles the
	 * non-standard login and access-token generation
	 * 
	 * @param api
	 *            OAuth 1.0a api information
	 * @param config
	 *            OAuth 1.0a configuration param object
	 */
	public AmazonS3Service(AmazonS3Api api, OAuthConfig config) {
		this.api = api;
		this.config = config;
	}

	/**
	 * Returns the customer token!
	 * 
	 * @see de.dhbw.mannheim.cloudraid.net.oauth.AmazonS3.AmazonS3Service#getAccessToken(Token,
	 *      Verifier)
	 * @param requestToken
	 * @return
	 */
	public Token getAccessToken(Token requestToken) {
		return null;
	}

	/**
	 * Returns the customer token!
	 */
	@Override
	public Token getAccessToken(Token requestToken, Verifier verifier) {
		return null;
	}

	@Override
	public String getAuthorizationUrl(Token requestToken) {
		return "";
	}

	public String getBucketEndpoint(String bucket) {
		return String.format(this.api.getBucketEndpoint(), bucket);
	}

	/**
	 * Here we get our consumer token and api token that will be used by this
	 * application
	 * 
	 * @see org.scribe.oauth.OAuthService#getRequestToken()
	 */
	@Override
	public Token getRequestToken() {
		return null;
	}

	public String getS3Endpoint() {
		return api.getS3Endpoint();
	}

	@Override
	public String getVersion() {
		return VERSION;
	}

	public void signRequest(OAuthRequest request) {
		System.err.println("[DEBUG] AmazonS3Service.signRequest(): request = "
				+ request);

		String baseString = api.getHeaderExtractor().extract(request);

		String signature = api.getSignatureService().getSignature(baseString,
				this.config.getApiSecret());

		String signHeader = "AWS" + " " + this.config.getApiKey() + ":"
				+ signature;
		request.addHeader(OAuthConstants.HEADER, signHeader);
		System.err
				.println("[DEBUG] AmazonS3Service.addSignature(): Authorization = "
						+ request.getHeaders().get("Authorization"));

	}

	@Override
	public void signRequest(Token accessToken, OAuthRequest request) {
		signRequest(request);
	}
}
