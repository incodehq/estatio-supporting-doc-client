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
public class XmlDocClient_Test {

    private XmlDocClient xmlDocClient;

    @Before
    public void setUp() throws Exception {

        //final String host = "https://estatio-dev-dha.int.ecpnv.com";
        final String host = "http://localhost:7070";

        final String user = "docreader";
        final String pass = "pass";

        xmlDocClient = new XmlDocClient(host, user, pass);
    }

    @Test
    public void fetch() throws Exception {
        final Document xmlDocument = xmlDocClient.fetch("CAR-0259", 2017, "IT01", "ITCL10611");

        assertThat(xmlDocument, is(notNullValue()));
    }

    @Test
    public void fetchAndWrite() throws Exception {
        xmlDocClient.fetchAndWrite("CAR-0259", 2017, "IT01", "ITCL10611", "target/files-xml");
    }
}