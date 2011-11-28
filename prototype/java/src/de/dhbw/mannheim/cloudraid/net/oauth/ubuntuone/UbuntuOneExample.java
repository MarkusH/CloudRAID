package de.dhbw.mannheim.cloudraid.net.oauth.ubuntuone;

import org.scribe.builder.ServiceBuilder;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;

public class UbuntuOneExample {
    private static final String PROTECTED_RESOURCE_URL = "https://one.ubuntu.com/api/file_storage/v1";
    private static final String CONTENT_ROOT = "https://files.one.ubuntu.com";

    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println("Usage: <username> <password>");
            System.exit(1);
        }
        UbuntuOneService service = (UbuntuOneService) new ServiceBuilder().provider(UbuntuOneApi.class)
                        .apiKey(args[0]).apiSecret(args[1]).build();
                        // Comment the line above and uncomment the next one and fill them with the customer_token and customer_secret
                        //.apiKey("").apiSecret("").build();


        System.out.println("=== UbuntuOne Connection ===");
        System.out.println();
        

        // Comment the next lines to use an existing token
        // Obtain the Request Token
        System.out.println("Fetching the Request Token...");
        Token requestToken = service.getRequestToken();
        System.out.println("Got the Request Token!");
        System.out.println("(if your curious it looks like this: " + requestToken + " )");
        System.out.println();
        // Trade the Request Token and Verfier for the Access Token
        System.out.println("Trading the Request Token for an Access Token...");
        Token accessToken = service.getAccessToken(requestToken);
        System.out.println("Got the Access Token!");
        System.out.println("(if your curious it looks like this: " + accessToken + " )");
        System.out.println();


        // Uncomment and insert the access token data ("public", "secret")
        //Token accessToken = new Token("", "");
        
        // Returns a User Representation for this user
        System.out.println("Now we're going to access a protected resource...");
        OAuthRequest request = new OAuthRequest(Verb.GET, PROTECTED_RESOURCE_URL);
        service.signRequest(accessToken, request);
        request.addHeader("GData-Version", "3.0");
        Response response = request.send();
        System.out.println("Got it! Lets see what we found...");
        System.out.println();
        System.out.println(response.getCode());
        System.out.println(response.getBody());

        // show infos to file /~/Ubuntu%20One/file1.txt
        System.out.println();
        System.out.println("Now we're going to access a protected resource...");
        request = new OAuthRequest(Verb.GET, PROTECTED_RESOURCE_URL + "/~/Ubuntu%20One/file1.txt");
        service.signRequest(accessToken, request);
        request.addHeader("GData-Version", "3.0");
        response = request.send();
        System.out.println("Got it! Lets see what we found...");
        System.out.println();
        System.out.println(response.getCode());
        System.out.println(response.getBody());

        // show content of file /~/Ubuntu%20One/file1.txt
        System.out.println();
        System.out.println("Now we're going to access a protected resource...");
        request = new OAuthRequest(Verb.GET, CONTENT_ROOT + "/content/~/Ubuntu%20One/file1.txt");
        service.signRequest(accessToken, request);
        request.addHeader("GData-Version", "3.0");
        response = request.send();
        System.out.println("Got it! Lets see what we found...");
        System.out.println();
        System.out.println(response.getCode());
        System.out.println(response.getBody());
    }
}