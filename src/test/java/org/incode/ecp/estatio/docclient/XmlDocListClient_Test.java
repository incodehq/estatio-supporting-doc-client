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
    public void fetch_for_specific_company() throws Exception {
        final Document xmlDocument = docListClient.fetch(2017, "IT01");

        assertThat(xmlDocument, is(notNullValue()));

        System.out.println(XmlUtil.asString(xmlDocument));
    }


    @Test
    public void fetch_for_all() throws Exception {
        final Document xmlDocument = docListClient.fetch(2017);

        assertThat(xmlDocument, is(notNullValue()));

        System.out.println(XmlUtil.asString(xmlDocument));
    }

    @Test
    public void fetchAndWrite_for_all() throws Exception {
        docListClient.fetchAndWrite(2017, null, "target/files-xml/2017/_all_");
    }

    @Test
    public void fetchAndWrite_for_specific_companies() throws Exception {
        docListClient.fetchAndWrite(2017, "IT01", "target/files-xml/2017/IT01");
        docListClient.fetchAndWrite(2017, "IT04", "target/files-xml/2017/IT04");
        docListClient.fetchAndWrite(2017, "IT05", "target/files-xml/2017/IT05");
        docListClient.fetchAndWrite(2017, "IT07", "target/files-xml/2017/IT07");
        docListClient.fetchAndWrite(2017, "IT08", "target/files-xml/2017/IT08");
    }
}