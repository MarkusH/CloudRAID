/**
 *
 */
package de.dhbw.mannheim.cloudraid;

import org.scribe.model.Token;

import de.dhbw.mannheim.cloudraid.net.connector.ubuntuone.UbuntuOneConnector;

/**
 * @author Markus Holtermann
 * 
 */
public class Uploader {

    /**
     * @param args
     */
    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println("Usage: <username> <password>");
            System.exit(1);
        }
        UbuntuOneConnector u1 = new UbuntuOneConnector(args[0], args[1]);
        Token requestToken = u1.getRequestToken();
        Token accessToken = u1.getAccessToken(requestToken, null);
        System.out.println(accessToken);
    }

}
