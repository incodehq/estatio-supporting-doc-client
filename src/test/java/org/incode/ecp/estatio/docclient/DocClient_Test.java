package org.incode.ecp.estatio.docclient;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import org.estatio.canonical.documents.v2.DocumentsDto;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

/**
 * See the <code>README.adoc</code> for a query to return further example docs.
 */
@Ignore
public class DocClient_Test {

    private DocClient docClient;

    @Before
    public void setUp() throws Exception {

        final String host = "https://estatio-test.int.ecpnv.com";
        //final String host = "https://estatio-dev-dha.int.ecpnv.com";
        //final String host = "http://localhost:7070";

        final String user = "docreader";
        final String pass = "pass";

        docClient = new DocClient(host, user, pass);
    }

    @Test
    public void fetch() throws Exception {
        final DocumentsDto documentsDto = docClient.fetch("CAR-0259", 2017, "IT01", "ITCL10611");

        assertThat(documentsDto.getDocuments(), hasSize(1));
    }

    @Test
    public void fetchAndWrite() throws Exception {
        docClient.fetchAndWrite("CAR-0259", 2017, "IT01", "ITCL10611", "target/files");
    }
}