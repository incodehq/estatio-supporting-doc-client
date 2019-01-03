package org.incode.ecp.estatio.docclient;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * See the <code>README.adoc</code> for a query to return further example docs.
 */
public class XmlDocListClient_Test {

    private XmlDocListClient docListClient;

    @Before
    public void setUp() throws Exception {

        final String host = "https://estatio.int.ecpnv.com";
        //final String host = "https://estatio-dev-dha.int.ecpnv.com";
        //final String host = "http://localhost:7070";

        final String user = "docreader";
        final String pass = "pass";

        final XmlDocClient docClient = new XmlDocClient(host, user, pass);

        docListClient = new XmlDocListClient(host, user, pass, docClient);
    }

    @Test
    public void fetch() throws Exception {
        final Document xmlDocument = docListClient.fetch(2017);

        assertThat(xmlDocument, is(notNullValue()));
    }

    @Test
    public void fetchAndWrite() throws Exception {
        docListClient.fetchAndWrite(2017, "target/files-xml");
    }
}