/**
 * 
 */
package de.dhbw.mannheim.cloudraid.net.oauth.amazons3;

import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.Api;
import org.scribe.exceptions.OAuthException;
import org.scribe.model.OAuthConfig;
import org.scribe.model.OAuthConstants;
import org.scribe.model.SignatureType;
import org.scribe.oauth.OAuthService;
import org.scribe.utils.Preconditions;

/**
 * @author Markus Holtermann
 * 
 */
public class AmazonS3ServiceBuilder {
	private String apiKey;
	private String apiSecret;
	private String callback;
	private AmazonS3Api api;
	private String scope;
	private SignatureType signatureType;

	/**
	 * Default constructor
	 */
	public AmazonS3ServiceBuilder() {
		this.callback = OAuthConstants.OUT_OF_BAND;
	}

	/**
	 * Configures the api key
	 * 
	 * @param apiKey
	 *            The api key for your application
	 * @return the {@link ServiceBuilder} instance for method chaining
	 */
	public AmazonS3ServiceBuilder apiKey(String apiKey) {
		Preconditions.checkEmptyString(apiKey, "Invalid Api key");
		this.apiKey = apiKey;
		return this;
	}

	/**
	 * Configures the api secret
	 * 
	 * @param apiSecret
	 *            The api secret for your application
	 * @return the {@link ServiceBuilder} instance for method chaining
	 */
	public AmazonS3ServiceBuilder apiSecret(String apiSecret) {
		Preconditions.checkEmptyString(apiSecret, "Invalid Api secret");
		this.apiSecret = apiSecret;
		return this;
	}

	/**
	 * Returns the fully configured {@link OAuthService}
	 * 
	 * @return fully configured {@link OAuthService}
	 */
	public AmazonS3Service build() {
		Preconditions.checkNotNull(api,
				"You must specify a valid api through the provider() method");
		Preconditions.checkEmptyString(apiKey, "You must provide an api key");
		Preconditions.checkEmptyString(apiSecret,
				"You must provide an api secret");
		return api.createService(new OAuthConfig(apiKey, apiSecret, callback,
				signatureType, scope));
	}

	/**
	 * Adds an OAuth callback url
	 * 
	 * @param callback
	 *            callback url. Must be a valid url or 'oob' for out of band
	 *            OAuth
	 * @return the {@link ServiceBuilder} instance for method chaining
	 */
	public AmazonS3ServiceBuilder callback(String callback) {
		Preconditions.checkValidOAuthCallback(callback,
				"Callback must be a valid URL or 'oob'");
		this.callback = callback;
		return this;
	}

	private AmazonS3Api createApi(Class<? extends AmazonS3Api> apiClass) {
		Preconditions.checkNotNull(apiClass, "Api class cannot be null");
		AmazonS3Api api;
		try {
			api = apiClass.newInstance();
		} catch (Exception e) {
			throw new OAuthException("Error while creating the Api object", e);
		}
		return api;
	}

	/**
	 * Configures the {@link Api}
	 * 
	 * Overloaded version. Let's you use an instance instead of a class.
	 * 
	 * @param api
	 *            instance of {@link Api}s
	 * @return the {@link ServiceBuilder} instance for method chaining
	 */
	public AmazonS3ServiceBuilder provider(AmazonS3Api api) {
		Preconditions.checkNotNull(api, "Api cannot be null");
		this.api = api;
		return this;
	}

	/**
	 * Configures the {@link Api}
	 * 
	 * @param apiClass
	 *            the class of one of the existent {@link Api}s on
	 *            org.scribe.api package
	 * @return the {@link ServiceBuilder} instance for method chaining
	 * 
	 */
	public AmazonS3ServiceBuilder provider(Class<? extends AmazonS3Api> apiClass) {
		this.api = createApi(apiClass);
		return this;
	}

	/**
	 * Configures the OAuth scope. This is only necessary in some APIs (like
	 * Google's).
	 * 
	 * @param scope
	 *            The OAuth scope
	 * @return the {@link ServiceBuilder} instance for method chaining
	 */
	public AmazonS3ServiceBuilder scope(String scope) {
		Preconditions.checkEmptyString(scope, "Invalid OAuth scope");
		this.scope = scope;
		return this;
	}

	/**
	 * Configures the signature type, choose between header, querystring, etc.
	 * Defaults to Header
	 * 
	 * @param scope
	 *            The OAuth scope
	 * @return the {@link ServiceBuilder} instance for method chaining
	 */
	public AmazonS3ServiceBuilder signatureType(SignatureType type) {
		Preconditions.checkNotNull(type, "Signature type can't be null");
		this.signatureType = type;
		return this;
	}
}
