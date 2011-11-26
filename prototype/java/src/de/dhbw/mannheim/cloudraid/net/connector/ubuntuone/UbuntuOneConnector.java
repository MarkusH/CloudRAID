/**
 *
 */
package de.dhbw.mannheim.cloudraid.net.connector.ubuntuone;

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
public class UbuntuOneConnector implements OAuthService {

    private static final String VERSION = "1.0";

    private UbuntuOneApi api;
    private OAuthConfig config;
    private Token stoken; // security_token
    private Token ctoken; // consumer_token

    public UbuntuOneConnector(String username, String password) {
        this.api = new UbuntuOneApi();
        this.config = new OAuthConfig(username, password);
        System.err.println("[DEBUG] UbuntuOneConnector: username = " + username);
        System.err.println("[DEBUG] UbuntuOneConnector: password = <hidden>");
    }

    @Override
    public Token getRequestToken() {
        OAuthRequest tokenRequest = new OAuthRequest(Verb.GET, api.getRequestTokenEndpoint());
        System.err.println("[DEBUG] UbuntuOneConnector.getRequestToken(): tokenRequest = " + tokenRequest.getUrl());

        String encoding = new sun.misc.BASE64Encoder().encode((this.config.getApiKey() + ":" + this.config.getApiSecret()).getBytes());
        System.err.println("[DEBUG] UbuntuOneConnector.getRequestToken(): encoding = " + tokenRequest.getQueryStringParams().toString());

        tokenRequest.addHeader("Authorization", "Basic " + encoding);
        System.err.println("[DEBUG] UbuntuOneConnector.getRequestToken(): tokenRequest.getHeaders() = " + tokenRequest.getHeaders().toString());

        Response response = tokenRequest.send();
        System.err.println("[DEBUG] UbuntuOneConnector.getRequestToken(): response.getCode() = " + response.getCode());
        System.err.println("[DEBUG] UbuntuOneConnector.getRequestToken(): response.getHeaders() = " + response.getHeaders());
        System.err.println("[DEBUG] UbuntuOneConnector.getRequestToken(): response.getBody() = " + response.getBody());

        this.stoken = api.getRequestTokenExtractor().extract(response.getBody());
        System.err.println("[DEBUG] UbuntuOneConnector.getRequestToken(): stoken = " + this.stoken);
        this.ctoken = ((UbuntuOneJsonExtractor) api.getRequestTokenExtractor()).extractConsumerToken(response.getBody());
        System.err.println("[DEBUG] UbuntuOneConnector.getRequestToken(): ctoken = " + this.ctoken);
        return this.stoken;
    }

    @Override
    public Token getAccessToken(Token requestToken, Verifier verifier) {
        OAuthRequest request = new OAuthRequest(Verb.GET, api.getAccessTokenEndpoint() + this.config.getApiKey());
        System.err.println("[DEBUG] UbuntuOneConnector.getAccessToken(): request = " + request);

        addOAuthParams(request, requestToken);
        System.err.println("[DEBUG] UbuntuOneConnector.getAccessToken(): request.getOauthParameters() = " + request.getOauthParameters().toString());
        signRequest(requestToken, request);

        Response response = request.send();
        System.err.println("[DEBUG] UbuntuOneConnector.getRequestToken(): response.getCode() = " + response.getCode());
        System.err.println("[DEBUG] UbuntuOneConnector.getRequestToken(): response.getHeaders() = " + response.getHeaders());
        System.err.println("[DEBUG] UbuntuOneConnector.getRequestToken(): response.getBody() = " + response.getBody());

        return requestToken;
    }

    private void addOAuthParams(OAuthRequest request, Token token) {
        request.addOAuthParameter(OAuthConstants.TIMESTAMP, api.getTimestampService().getTimestampInSeconds());
        request.addOAuthParameter(OAuthConstants.TOKEN, token.getToken());
        request.addOAuthParameter(OAuthConstants.NONCE, api.getTimestampService().getNonce());
        request.addOAuthParameter(OAuthConstants.CONSUMER_KEY, this.ctoken.getToken());
        request.addOAuthParameter(OAuthConstants.SIGN_METHOD, api.getSignatureService().getSignatureMethod());
        request.addOAuthParameter(OAuthConstants.VERSION, getVersion());
    }

    private void addSignature(OAuthRequest request) {
        String oauthHeader = api.getHeaderExtractor().extract(request);
        String signature = getSignature(request, this.stoken);
        oauthHeader = oauthHeader + ", " + OAuthConstants.SIGNATURE + "=\"" + URLUtils.percentEncode(signature) + "\"";
        request.addHeader(OAuthConstants.HEADER, oauthHeader);
        System.err.println("[DEBUG] UbuntuOneConnector.addSignature(): Authorization = " + request.getHeaders().get("Authorization"));
    }

    @Override
    public void signRequest(Token accessToken, OAuthRequest request) {
        addSignature(request);

    }

    private String getSignature(OAuthRequest request, Token token) {
        String baseString = api.getBaseStringExtractor().extract(request);
        return api.getSignatureService().getSignature(baseString, this.ctoken.getSecret(), token.getSecret());
    }

    @Override
    public String getVersion() {
        return VERSION;
    }

    @Override
    public String getAuthorizationUrl(Token requestToken) {
        return "";
    }
}
