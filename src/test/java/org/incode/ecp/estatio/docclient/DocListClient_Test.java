package org.incode.ecp.estatio.docclient;

import org.junit.Before;
import org.junit.Test;

import org.estatio.canonical.invoicenumbers.v2.InvoiceNumbersDto;

import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertThat;

/**
 * See the <code>README.adoc</code> for a query to return further example docs.
 */
public class DocListClient_Test {

    private DocListClient docListClient;

    @Before
    public void setUp() throws Exception {

        final String host = "https://estatio.int.ecpnv.com";
        //final String host = "https://estatio-dev-dha.int.ecpnv.com";
        //final String host = "http://localhost:7070";

        final String user = "docreader";
        final String pass = "pass";

        final DocClient docClient = new DocClient(host, user, pass);

        docListClient = new DocListClient(host, user, pass, docClient);
    }

    @Test
    public void fetch() throws Exception {
        final InvoiceNumbersDto invoiceNumbersDto = docListClient.fetch(2017);

        assertThat(invoiceNumbersDto.getInvoiceNumbers().size(), greaterThan(50));
    }

    @Test
    public void fetchAndWrite() throws Exception {
        docListClient.fetchAndWrite(2017, "target/files");
    }
}