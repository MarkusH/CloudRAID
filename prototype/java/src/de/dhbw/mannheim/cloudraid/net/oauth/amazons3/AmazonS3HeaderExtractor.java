/**
 *
 */
package de.dhbw.mannheim.cloudraid.net.oauth.amazons3;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

import org.scribe.extractors.HeaderExtractorImpl;
import org.scribe.model.OAuthRequest;

/**
 * @author Markus Holtermann
 * 
 */
public class AmazonS3HeaderExtractor extends HeaderExtractorImpl {

	private static final String PARAM_SEPARATOR = "\n";

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String extract(OAuthRequest request) {
		Map<String, String> parameters = request.getHeaders();
		StringBuffer header = new StringBuffer();
		String contentMD5 = "";
		String contentType = "";
		String date;
		String canonicalizedResource = "";
		ArrayList<String> xAmz = new ArrayList<String>();

		Format format = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z");
		date = format.format(new Date());

		Iterator<String> iterator = parameters.keySet().iterator();
		String key;

		while (iterator.hasNext()) {
			key = iterator.next();
			if (key.equalsIgnoreCase("Content-MD5")) {
				contentMD5 = parameters.get(key);
			} else if (key.equalsIgnoreCase("Content-Type")) {
				contentType = parameters.get(key);
			} else if (key.equalsIgnoreCase("Date")) {
				date = parameters.get(key);
			} else if (key.substring(0, 6).equalsIgnoreCase("X-AMZ-")) {
				xAmz.add(key);
			}
		}
		request.addHeader("Date", date);

		header.append(request.getVerb());

		header.append(PARAM_SEPARATOR);
		header.append(contentMD5);

		header.append(PARAM_SEPARATOR);
		header.append(contentType);

		header.append(PARAM_SEPARATOR);
		header.append(date);

		// append the X-Amz-* header attributes to the signature string
		Collections.sort(xAmz);
		System.err
				.println("[DEBUG] AmazonS3HeaderExtractor.extract(): tmpkeyList = "
						+ xAmz.toString());
		for (String xAmzKey : xAmz) {
			header.append(PARAM_SEPARATOR);
			header.append(String.format("%s:%s", xAmzKey.toLowerCase(),
					parameters.get(xAmzKey)));
		}

		// generate the canonicalized resource
		String url = request.getSanitizedUrl().substring(8);
		if (!url.startsWith(AmazonS3Api.S3_BASE_URL)) {
			canonicalizedResource += "/";
			canonicalizedResource += url.substring(0, url.indexOf('.'));
		}
		canonicalizedResource += url.substring(url.indexOf('/'));

		// TODO: Implement step 4 of
		// "Constructing the CanonicalizedResource Element" at
		// http://docs.amazonwebservices.com/AmazonS3/latest/dev/RESTAuthentication.html

		header.append(PARAM_SEPARATOR);
		header.append(canonicalizedResource);

		System.out.flush();
		System.out.println(header.toString());
		System.out.flush();
		return header.toString();
	}
}
