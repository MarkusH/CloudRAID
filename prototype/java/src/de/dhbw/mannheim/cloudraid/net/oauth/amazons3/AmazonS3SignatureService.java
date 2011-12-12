package de.dhbw.mannheim.cloudraid.net.oauth.amazons3;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.scribe.exceptions.OAuthSignatureException;
import org.scribe.services.SignatureService;

import sun.misc.BASE64Encoder;

/**
 * HMAC-SHA1 implementation of {@SignatureService}
 * 
 * @author Pablo Fernandez
 * 
 */
public class AmazonS3SignatureService implements SignatureService {
	private static final String EMPTY_STRING = "";
	private static final String CARRIAGE_RETURN = "\r\n";
	private static final String UTF8 = "UTF-8";
	private static final String HMAC_SHA1 = "HmacSHA1";
	private static final String METHOD = "HMAC-SHA1";

	private String doSign(String toSign, String keyString) throws Exception {
		SecretKeySpec key = new SecretKeySpec((keyString).getBytes(UTF8),
				HMAC_SHA1);
		Mac mac = Mac.getInstance(HMAC_SHA1);
		mac.init(key);
		byte[] bytes = mac.doFinal(toSign.getBytes(UTF8));
		return new BASE64Encoder().encode(bytes).replace(CARRIAGE_RETURN,
				EMPTY_STRING);
	}

	public String getSignature(String baseString, String secretKey) {
		try {
			return doSign(baseString, secretKey);
		} catch (Exception e) {
			throw new OAuthSignatureException(baseString, e);
		}
	}

	/**
	 * Just to fulfill the requirements of the interface
	 */
	@Override
	public String getSignature(String baseString, String apiSecret,
			String tokenSecret) {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getSignatureMethod() {
		return METHOD;
	}
}
