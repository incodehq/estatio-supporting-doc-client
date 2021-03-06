package org.incode.ecp.estatio.docclient;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.nio.file.Files;
import java.util.Base64;
import java.util.List;

import javax.xml.bind.JAXB;

import org.estatio.canonical.documents.v2.DocumentType;
import org.estatio.canonical.documents.v2.DocumentsDto;

import static org.incode.ecp.estatio.docclient.Util.openConnection;

public class DocClient {

    private final String host;
    private final String user;
    private final String pass;

    public DocClient(final String host, final String user, final String pass) {
        this.host = host;
        this.user = user;
        this.pass = pass;
    }

    /**
     * @param invoiceNumber - stored as <code>ref1</code> in the summary <code>oas_docline</code>
     * @param year - invoice numbers are reset each year.  Corresponds to the <code>yr</code> of the oas_dochead
     * @param sellerReference - the reference to the party that the caller believes is the seller of the invoice, ie the <code>cmpcode</code> of the <code>oas_dochead</code>.  If it is not, then no documents will be returned
     * @param buyerReference - the reference to the party that the caller believes is the buyer of the invoice, ie the <code>el6</code> of the summary <code>oas_docline</code>.  If it is not, then no documents will be returned
     */
    public DocumentsDto fetch(
            final String invoiceNumber,
            final int year,
            final String sellerReference,
            final String buyerReference)
            throws IOException {
        final String url = String.format(
                "%s/restful/services/%s/actions/%s/invoke?invoiceNumber=%s&year=%d&sellerReference=%s&buyerReference=%s",
                host, "lease.SupportingDocumentService", "findSupportingDocuments", invoiceNumber, year, sellerReference, buyerReference);

        final HttpURLConnection connection = openConnection(url, user, pass, DocumentsDto.class);
        final InputStream inputStream = connection.getInputStream();

        final DocumentsDto documentsDto = JAXB.unmarshal(inputStream, DocumentsDto.class);
        connection.disconnect();
        return documentsDto;
    }

    /**
     * Convenience that calls {@link #fetch(String, int, String, String)} and then writes out all returned documents to specified directory.
     *
     * @param invoiceNumber - stored as <code>ref1</code> in the summary <code>oas_docline</code>
     * @param year - invoice numbers are reset each year.  Corresponds to the <code>yr</code> of the oas_dochead
     * @param sellerReference - the reference to the party that the caller believes is the seller of the invoice, ie the <code>cmpcode</code> of the <code>oas_dochead</code>.  If it is not, then no documents will be returned
     * @param buyerReference - the reference to the party that the caller believes is the buyer of the invoice, ie the <code>el6</code> of the summary <code>oas_docline</code>.  If it is not, then no documents will be returned
     * @param directory - automatically created if required.
     */
    public void fetchAndWrite(
            final String invoiceNumber,
            final int year,
            final String sellerReference,
            final String buyerReference,
            final String directory)
            throws IOException {

        final File parent = new File(directory);
        parent.mkdirs();

        final DocumentsDto documentsDto = fetch(invoiceNumber, year, sellerReference, buyerReference);
        final List<DocumentType> documents = documentsDto.getDocuments();

        for (final DocumentType document : documents) {
            final String blobBytesBase64Encoded = document.getBlobBytesBase64Encoded();

            final byte[] bytes = Base64.getDecoder().decode(blobBytesBase64Encoded);
            final String documentName = documentNameFor(document);
            Files.write(new File(parent, documentName).toPath(), bytes);
        }

    }

    private static String documentNameFor(final DocumentType document) {
        String documentName = document.getName();
        documentName = documentName.toLowerCase().endsWith(".pdf") ? documentName : documentName + ".pdf";
        return documentName;
    }

}