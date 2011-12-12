/**
 *
 */
package de.dhbw.mannheim.cloudraid.net.oauth.ubuntuone;

import java.net.InetAddress;
import java.net.UnknownHostException;

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
public class UbuntuOneApi extends DefaultApi10a {

	private static final String REQUEST_URL = "https://login.ubuntu.com/api/1.0/authentications?ws.op=authenticate&token_name=Ubuntu%20One%20@%20";
	private static final String ACCESS_URL = "https://one.ubuntu.com/oauth/sso-finished-so-get-tokens/";
	private static final String API_BASE_URL = "https://one.ubuntu.com/api/";
	private static final String CONTENT_ROOT_URL = "https://files.one.ubuntu.com/content/";
	private static final String FILE_STORAGE_URL = "https://one.ubuntu.com/api/file_storage/v1/";

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
	public UbuntuOneService createService(OAuthConfig config) {
		return new UbuntuOneService(this, config);
	}

	@Override
	public String getAccessTokenEndpoint() {
		return ACCESS_URL;
	}

	@Override
	public Verb getAccessTokenVerb() {
		return Verb.GET;
	}

	public String getApiBaseEndpoint() {
		return API_BASE_URL;
	}

	@Override
	public String getAuthorizationUrl(Token requestToken) {
		return "";
	}

	public String getContentRootEndpoint() {
		return CONTENT_ROOT_URL;
	}

	public String getFileStorageEndpoint() {
		return FILE_STORAGE_URL;
	}

	@Override
	public HeaderExtractor getHeaderExtractor() {
		return new UbuntuOneHeaderExtractor();
	}

	@Override
	public String getRequestTokenEndpoint() {
		try {
			return REQUEST_URL + InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			e.printStackTrace();
			return REQUEST_URL + "localhost";
		}
	}

	@Override
	public RequestTokenExtractor getRequestTokenExtractor() {
		return new UbuntuOneJsonExtractor();
	}

	@Override
	public Verb getRequestTokenVerb() {
		return Verb.GET;
	}
}
