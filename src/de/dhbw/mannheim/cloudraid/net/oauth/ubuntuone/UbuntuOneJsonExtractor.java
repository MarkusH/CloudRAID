/**
 *
 */
package de.dhbw.mannheim.cloudraid.net.oauth.ubuntuone;

import org.json.JSONException;
import org.json.JSONObject;
import org.scribe.extractors.RequestTokenExtractor;
import org.scribe.model.Token;

/**
 * @author Markus Holtermann
 * 
 */
public class UbuntuOneJsonExtractor implements RequestTokenExtractor {

	@Override
	public Token extract(String response) {
		JSONObject body;
		try {
			body = new JSONObject(response);
			String token = body.getString("token");
			String secret = body.getString("token_secret");

			return new Token(token, secret);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return new Token("", "");
	}

	public Token extractConsumerToken(String response) {
		JSONObject body;
		try {
			body = new JSONObject(response);
			String token = body.getString("consumer_key");
			String secret = body.getString("consumer_secret");

			return new Token(token, secret);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return new Token("", "");
	}

}
