/**
 *
 */
package de.dhbw.mannheim.cloudraid.net.oauth.ubuntuone;

import org.scribe.model.OAuthConfig;
import org.scribe.model.OAuthConstants;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.model.Verifier;
import org.scribe.oauth.OAuthService;
import org.scribe.utils.URLUtils;

/**
 * @author Markus Holtermann
 * 
 */
public class UbuntuOneService implements OAuthService {

	private static final String VERSION = "1.0";

	private UbuntuOneApi api;
	private OAuthConfig config;

	private String email;
	private String password;

	/**
	 * This constructs a new OAuthService for UbuntuOne that handles the
	 * non-standard login and access-token generation
	 * 
	 * @param api
	 *            OAuth 1.0a api information
	 * @param config
	 *            OAuth 1.0a configuration param object
	 */
	public UbuntuOneService(UbuntuOneApi api, OAuthConfig config) {
		this.api = api;
		this.config = config;
		this.email = config.getApiKey();
		this.password = config.getApiSecret();
	}

	/**
	 * Here we get our consumer token and api token that will be used by this
	 * application
	 * 
	 * @see org.scribe.oauth.OAuthService#getRequestToken()
	 */
	@Override
	public Token getRequestToken() {
		OAuthRequest tokenRequest = new OAuthRequest(Verb.GET,
				api.getRequestTokenEndpoint());
		System.err
				.println("[DEBUG] UbuntuOneService.getRequestToken(): tokenRequest = "
						+ tokenRequest.getUrl());

		String encoding = new sun.misc.BASE64Encoder()
				.encode((this.email + ":" + this.password).getBytes());
		System.err
				.println("[DEBUG] UbuntuOneService.getRequestToken(): encoding = "
						+ tokenRequest.getQueryStringParams().toString());

		tokenRequest.addHeader("Authorization", "Basic " + encoding);
		System.err
				.println("[DEBUG] UbuntuOneService.getRequestToken(): tokenRequest.getHeaders() = "
						+ tokenRequest.getHeaders().toString());

		Response response = tokenRequest.send();
		System.err
				.println("[DEBUG] UbuntuOneService.getRequestToken(): response.getCode() = "
						+ response.getCode());
		System.err
				.println("[DEBUG] UbuntuOneService.getRequestToken(): response.getHeaders() = "
						+ response.getHeaders());
		System.err
				.println("[DEBUG] UbuntuOneService.getRequestToken(): response.getBody() = "
						+ response.getBody());

		Token stoken = api.getRequestTokenExtractor().extract(
				response.getBody());
		System.err
				.println("[DEBUG] UbuntuOneService.getRequestToken(): stoken = "
						+ stoken);
		Token ctoken = ((UbuntuOneJsonExtractor) api.getRequestTokenExtractor())
				.extractConsumerToken(response.getBody());
		this.config = new OAuthConfig(ctoken.getToken(), ctoken.getSecret(),
				this.config.getCallback(), this.config.getSignatureType(),
				this.config.getScope());
		System.err
				.println("[DEBUG] UbuntuOneService.getRequestToken(): config = "
						+ this.config);
		return stoken;
	}

	/**
	 * Returns the customer token!
	 * 
	 * @see de.dhbw.mannheim.cloudraid.net.oauth.ubuntuone.UbuntuOneService#getAccessToken(Token,
	 *      Verifier)
	 * @param requestToken
	 * @return
	 */
	public Token getAccessToken(Token requestToken) {
		return this.getAccessToken(requestToken, new Verifier(this.email));
	}

	/**
	 * Returns the customer token!
	 */
	@Override
	public Token getAccessToken(Token requestToken, Verifier verifier) {
		OAuthRequest request = new OAuthRequest(Verb.GET,
				api.getAccessTokenEndpoint() + this.email);

		signRequest(requestToken, request);

		Response response = request.send();
		System.err
				.println("[DEBUG] UbuntuOneService.getAccessToken(): response.getCode() = "
						+ response.getCode());
		System.err
				.println("[DEBUG] UbuntuOneService.getAccessToken(): response.getHeaders() = "
						+ response.getHeaders());
		System.err
				.println("[DEBUG] UbuntuOneService.getAccessToken(): response.getBody() = "
						+ response.getBody());

		return new Token(this.config.getApiKey(), this.config.getApiSecret());
	}

	private void addOAuthParams(OAuthRequest request, Token token) {
		request.addOAuthParameter(OAuthConstants.TIMESTAMP, api
				.getTimestampService().getTimestampInSeconds());
		request.addOAuthParameter(OAuthConstants.TOKEN, token.getToken());
		request.addOAuthParameter(OAuthConstants.NONCE, api
				.getTimestampService().getNonce());
		request.addOAuthParameter(OAuthConstants.CONSUMER_KEY,
				this.config.getApiKey());
		request.addOAuthParameter(OAuthConstants.SIGN_METHOD, api
				.getSignatureService().getSignatureMethod());
		request.addOAuthParameter(OAuthConstants.VERSION, getVersion());
	}

	private void addSignature(OAuthRequest request, Token token) {
		String oauthHeader = api.getHeaderExtractor().extract(request);
		String signature = getSignature(request, token);
		oauthHeader = oauthHeader + ", " + OAuthConstants.SIGNATURE + "=\""
				+ URLUtils.percentEncode(signature) + "\"";
		request.addHeader(OAuthConstants.HEADER, oauthHeader);
		System.err
				.println("[DEBUG] UbuntuOneService.addSignature(): Authorization = "
						+ request.getHeaders().get("Authorization"));
	}

	@Override
	public void signRequest(Token accessToken, OAuthRequest request) {
		addOAuthParams(request, accessToken);
		System.err
				.println("[DEBUG] UbuntuOneService.signRequest(): request.getOauthParameters() = "
						+ request.getOauthParameters().toString());
		System.err.println("[DEBUG] UbuntuOneService.signRequest(): request = "
				+ request);
		addSignature(request, accessToken);

	}

	private String getSignature(OAuthRequest request, Token token) {
		String baseString = api.getBaseStringExtractor().extract(request);
		return api.getSignatureService().getSignature(baseString,
				this.config.getApiSecret(), token.getSecret());
	}

	@Override
	public String getVersion() {
		return VERSION;
	}

	@Override
	public String getAuthorizationUrl(Token requestToken) {
		return "";
	}

	public String getApiBaseEndpoint() {
		return this.api.getApiBaseEndpoint();
	}

	public String getContentRootEndpoint() {
		return this.api.getContentRootEndpoint();
	}
}
