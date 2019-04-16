package org.incode.ecp.estatio.docclient;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import org.estatio.canonical.invoicenumbers.v2.InvoiceNumbersDto;

import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertThat;

/**
 * See the <code>README.adoc</code> for a query to return further example docs.
 */
@Ignore
public class DocListClient_Test {

    private DocListClient docListClient;

    @Before
    public void setUp() throws Exception {

        final String host = "https://estatio-test.int.ecpnv.com";
        //final String host = "https://estatio-dev-dha.int.ecpnv.com";
        //final String host = "http://localhost:7070";

        final String user = "docreader";
        final String pass = "pass";

        final DocClient docClient = new DocClient(host, user, pass);

        docListClient = new DocListClient(host, user, pass, docClient);
    }

    @Test
    public void fetch_for_specific_company() throws Exception {
        final InvoiceNumbersDto invoiceNumbersDto = docListClient.fetch(2017, "IT01");
        final int size = invoiceNumbersDto.getInvoiceNumbers().size();
        assertThat(size, greaterThan(50));
    }

    @Test
    public void fetch_for_all() throws Exception {
        final InvoiceNumbersDto invoiceNumbersDto = docListClient.fetch(2017);
        final int size = invoiceNumbersDto.getInvoiceNumbers().size();
        assertThat(size, greaterThan(50));
    }

    @Test
    public void fetchAndWrite_for_all() throws Exception {
        docListClient.fetchAndWrite(2017, "target/files/2017/_all_");
    }

    @Test
    public void fetchAndWrite_for_specific_companies() throws Exception {
        docListClient.fetchAndWrite(2017, "IT01", "target/files/2017/IT01");
        docListClient.fetchAndWrite(2017, "IT04", "target/files/2017/IT04");
        docListClient.fetchAndWrite(2017, "IT05", "target/files/2017/IT05");
        docListClient.fetchAndWrite(2017, "IT07", "target/files/2017/IT07");
        docListClient.fetchAndWrite(2017, "IT08", "target/files/2017/IT08");
    }
}