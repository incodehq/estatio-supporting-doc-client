package org.incode.ecp.estatio.docclient;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;

class Util {

    static HttpURLConnection openConnection(
            String urlStr, String user, String pass, final Class<?> dtoClass)
            throws IOException {
        return openConnection(urlStr, user, pass, dtoClass.getName());
    }

    static HttpURLConnection openConnection(
            String urlStr, String user, String pass, final String dtoClassName)
            throws IOException{

        String authStr = user+":"+pass;
        String authEncoded = Base64.getEncoder().encodeToString(authStr.getBytes());

        final URL url = new URL(urlStr);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestMethod("GET");
        connection.setDoOutput(true);
        connection.setRequestProperty("Authorization", "Basic " + authEncoded);
        connection.setRequestProperty("Accept",
                "application/xml"
                        + ";profile=\"urn:org.restfulobjects:repr-types/action-result\""
                        + ";x-ro-domain-type=\""
                        + dtoClassName
                        + "\"");

        return connection;
    }

}
