/**
 *
 */
package de.dhbw.mannheim.cloudraid.net.connector.ubuntuone;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.scribe.builder.api.DefaultApi10a;
import org.scribe.extractors.HeaderExtractor;
import org.scribe.extractors.RequestTokenExtractor;
import org.scribe.model.Token;
import org.scribe.model.Verb;

/**
 * @author Markus Holtermann
 * 
 */
public class UbuntuOneApi extends DefaultApi10a {

    private static final String REQUEST_URL = "https://login.ubuntu.com/api/1.0/authentications?ws.op=authenticate&token_name=Ubuntu%20One%20@%20";
    private static final String ACCESS_URL = "https://one.ubuntu.com/oauth/sso-finished-so-get-tokens/";

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
}
