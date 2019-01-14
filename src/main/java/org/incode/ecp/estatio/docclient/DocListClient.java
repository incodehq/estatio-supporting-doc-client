package org.incode.ecp.estatio.docclient;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.List;

import javax.xml.bind.JAXB;

import org.estatio.canonical.invoicenumbers.v2.InvoiceNumberType;
import org.estatio.canonical.invoicenumbers.v2.InvoiceNumbersDto;

import static org.incode.ecp.estatio.docclient.Util.openConnection;

public class DocListClient {

    private final String host;
    private final String user;
    private final String pass;
    private final DocClient docClient;

    public DocListClient(
            final String host,
            final String user,
            final String pass) {
        this(host, user, pass, new DocClient(host,user,pass));
    }

    public DocListClient(
            final String host,
            final String user,
            final String pass,
            final DocClient docClient) {
        this.host = host;
        this.user = user;
        this.pass = pass;
        this.docClient = docClient;
    }

    /**
     * Returns all the invoices that have at least one supporting document, for the given year.
     *
     * @param year - invoice numbers are reset each year.
     */
    public InvoiceNumbersDto fetch(final int year)
            throws IOException {
        return fetch(year, null);
    }

    /**
     * Returns all the invoices that have at least one supporting document, for the given year and seller reference (ie <code>cmpcode</code>).
     *
     * @param year - invoice numbers are reset each year.
     * @param sellerReferenceIfAny - the reference to the party acting as the seller of the invoice, ie the <code>cmpcode</code> of the <code>oas_dochead</code>.  Invoices that are not sold by this seller are ignored (filtered out).
     */
    public InvoiceNumbersDto fetch(final int year, final String sellerReferenceIfAny)
            throws IOException {
        String url = String.format(
                "%s/restful/services/%s/actions/%s/invoke?year=%d",
                host, "lease.SupportingDocumentService", "findInvoicesWithSupportingDocuments", year);
        if (sellerReferenceIfAny != null) {
            url += String.format(
                    "&sellerReference=%s",sellerReferenceIfAny);
        }

        final HttpURLConnection connection = openConnection(url, user, pass, InvoiceNumbersDto.class);
        final InputStream inputStream = connection.getInputStream();

        final InvoiceNumbersDto invoiceNumbersDto = JAXB.unmarshal(inputStream, InvoiceNumbersDto.class);
        connection.disconnect();

        return invoiceNumbersDto;
    }

    /**
     * Convenience that calls {@link #fetch(int)} and then writes out all returned documents to specified directory.
     */
    public void fetchAndWrite(
            final int year,
            final String directory)
            throws IOException {

        fetchAndWrite(year, null, directory);
    }

    /**
     * Convenience that calls {@link #fetch(int, String)} and then writes out all returned documents to specified directory.
     */
    public void fetchAndWrite(
            final int year,
            final String sellerReferenceIfAny,
            final String directory)
            throws IOException {

        final InvoiceNumbersDto invoiceNumbersDto = fetch(year, sellerReferenceIfAny);
        final List<InvoiceNumberType> invoiceNumbers = invoiceNumbersDto.getInvoiceNumbers();

        for (final InvoiceNumberType invoiceNumberDto : invoiceNumbers) {
            final String invoiceNumber = invoiceNumberDto.getInvoiceNumber();
            final String sellerReference = invoiceNumberDto.getSellerReference();
            final String buyerReference = invoiceNumberDto.getBuyerReference();
            docClient.fetchAndWrite(invoiceNumber, year, sellerReference, buyerReference, directory + "/" + invoiceNumber);
        }
    }


}