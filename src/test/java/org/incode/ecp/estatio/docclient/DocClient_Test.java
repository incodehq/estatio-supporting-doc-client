package org.incode.ecp.estatio.docclient;

import org.junit.Before;
import org.junit.Test;

import org.estatio.canonical.documents.v2.DocumentsDto;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

/**
 * See the <code>README.adoc</code> for a query to return further example docs.
 */
public class DocClient_Test {

    private DocClient docClient;

    @Before
    public void setUp() throws Exception {

        final String host = "estatio-test.int.ecpnv.com";
        final String user = "docreader";
        final String pass = "pass";

        docClient = new DocClient(host, user, pass);
    }

    @Test
    public void fetch() throws Exception {
        final DocumentsDto documentsDto = docClient.fetch("CAR-0259", "2017");

        assertThat(documentsDto.getDocuments(), hasSize(1));
    }

    @Test
    public void fetchAndWrite() throws Exception {
        docClient.fetchAndWrite("CAR-0259", "2017", "target/files");
    }
}