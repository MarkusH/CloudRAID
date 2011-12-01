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
    public String getAuthorizationUrl(Token requestToken) {
        return "";
    }

    @Override
    public String getAccessTokenEndpoint() {
        return ACCESS_URL;
    }

    @Override
    public Verb getAccessTokenVerb() {
        return Verb.GET;
    }

    @Override
    public Verb getRequestTokenVerb() {
        return Verb.GET;
    }

    public RequestTokenExtractor getRequestTokenExtractor() {
        return new UbuntuOneJsonExtractor();
    }

    @Override
    public HeaderExtractor getHeaderExtractor() {
        return new UbuntuOneHeaderExtractor();
    }

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
    public UbuntuOneService createService(OAuthConfig config) {
        return new UbuntuOneService(this, config);
    }
    
    public String getApiBaseEndpoint() {
        return API_BASE_URL;
    }

    public String getContentRootEndpoint() {
        return CONTENT_ROOT_URL;
    }
}
