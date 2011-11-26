/**
 *
 */
package de.dhbw.mannheim.cloudraid.net.connector.ubuntuone;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.scribe.exceptions.OAuthParametersMissingException;
import org.scribe.extractors.HeaderExtractorImpl;
import org.scribe.model.OAuthRequest;
import org.scribe.utils.Preconditions;
import org.scribe.utils.URLUtils;

/**
 * @author Markus Holtermann
 * 
 */
public class UbuntuOneHeaderExtractor extends HeaderExtractorImpl {

    private static final String PARAM_SEPARATOR = ", ";
    private static final String PREAMBLE = "OAuth ";
    private static final String REALM = "realm=\"\"";

    /**
     * {@inheritDoc}
     */
    public String extract(OAuthRequest request) {
        checkPreconditions(request);
        Map<String, String> parameters = request.getOauthParameters();
        StringBuffer header = new StringBuffer(parameters.size() * 20);
        header.append(PREAMBLE);
        header.append(REALM);
        Set<String> set = parameters.keySet();
        String[] keys = new String[set.size()];
        set.toArray(keys);
        List<String> tmpkeyList = Arrays.asList(keys);
        Collections.sort(tmpkeyList);
        System.err.println("[DEBUG] UbuntuOneHeaderExtractor.extract(): tmpkeyList = " + tmpkeyList.toString());
        for (String key : tmpkeyList) {
            header.append(PARAM_SEPARATOR);
            header.append(String.format("%s=\"%s\"", key, URLUtils.percentEncode(parameters.get(key))));
        }
        return header.toString();
    }

    private void checkPreconditions(OAuthRequest request) {
        Preconditions.checkNotNull(request, "Cannot extract a header from a null object");

        if (request.getOauthParameters() == null || request.getOauthParameters().size() <= 0) {
            throw new OAuthParametersMissingException(request);
        }
    }
}
