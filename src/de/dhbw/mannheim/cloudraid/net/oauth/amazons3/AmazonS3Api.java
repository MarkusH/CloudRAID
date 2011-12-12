/**
 *
 */
package de.dhbw.mannheim.cloudraid.net.oauth.amazons3;

import org.scribe.builder.api.DefaultApi10a;
import org.scribe.extractors.HeaderExtractor;
import org.scribe.extractors.RequestTokenExtractor;
import org.scribe.model.OAuthConfig;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.oauth.OAuthService;

/**
 * @author Markus Holtermann
 * 
 */
public class AmazonS3Api extends DefaultApi10a {

	public static final String S3_BASE_URL = "s3.amazonaws.com";
	private static final String S3_URL = "https://" + S3_BASE_URL + "/";
	private static final String BUCKET_URL = "https://%s." + S3_BASE_URL + "/";

	/**
	 * Returns the {@link OAuthService} for this Api
	 * 
	 * @param apiKey
	 *            Key
	 * @param apiSecret
	 *            Api Secret
	 * @param callback
	 *            OAuth callback (either URL or 'oob')
	 * @param scope
	 *            OAuth scope (optional)
	 */
	@Override
	public AmazonS3Service createService(OAuthConfig config) {
		return new AmazonS3Service(this, config);
	}

	@Override
	public String getAccessTokenEndpoint() {
		return "";
	}

	@Override
	public Verb getAccessTokenVerb() {
		return Verb.GET;
	}

	@Override
	public String getAuthorizationUrl(Token requestToken) {
		return "";
	}

	public String getBucketEndpoint() {
		return BUCKET_URL;
	}

	@Override
	public HeaderExtractor getHeaderExtractor() {
		return new AmazonS3HeaderExtractor();
	}

	@Override
	public String getRequestTokenEndpoint() {
		return "";
	}

	@Override
	public RequestTokenExtractor getRequestTokenExtractor() {
		return null;
	}

	@Override
	public Verb getRequestTokenVerb() {
		return Verb.GET;
	}

	public String getS3Endpoint() {
		return S3_URL;
	}

	@Override
	public AmazonS3SignatureService getSignatureService() {
		return new AmazonS3SignatureService();
	}
}
