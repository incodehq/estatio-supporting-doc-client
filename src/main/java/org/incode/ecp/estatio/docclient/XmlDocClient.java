package org.incode.ecp.estatio.docclient;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.nio.file.Files;
import java.util.Base64;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

import static org.incode.ecp.estatio.docclient.Util.openConnection;

public class XmlDocClient {

    private final String host;
    private final String user;
    private final String pass;

    public XmlDocClient(final String host, final String user, final String pass) {
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
    public Document fetch(
            final String invoiceNumber,
            final int year,
            final String sellerReference,
            final String buyerReference)
            throws IOException, ParserConfigurationException, SAXException {
        final String url = String.format(
                "%s/restful/services/%s/actions/%s/invoke?invoiceNumber=%s&year=%d&sellerReference=%s&buyerReference=%s",
                host, "lease.SupportingDocumentService", "findSupportingDocuments", invoiceNumber, year, sellerReference, buyerReference);

        final HttpURLConnection connection = openConnection(url, user, pass, "org.estatio.canonical.documents.v2.DocumentsDto");
        return XmlUtil.toXmlDocument(connection);
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
            throws IOException, ParserConfigurationException, SAXException, XPathExpressionException {

        final File parent = new File(directory);
        parent.mkdirs();

        final Document document = fetch(invoiceNumber, year, sellerReference, buyerReference);
        final XPath xPath = XPathFactory.newInstance().newXPath();

        xPath.setNamespaceContext(new XmlUtil.NamespaceResolver(document));
        final NodeList nodes = (NodeList) xPath.evaluate(
                "/documents:documentsDto/documents:documents/documents:document/documents:blobBytesBase64Encoded/text()",
                document, XPathConstants.NODESET);
        for (int i = 0; i < nodes.getLength(); ++i) {
            final Text item = (Text) nodes.item(i);
            final String blobBytesBase64Encoded = item.getWholeText();

            final byte[] bytes = Base64.getDecoder().decode(blobBytesBase64Encoded);
            final String documentName = "file-" + (i+1) + ".pdf";
            Files.write(new File(parent, documentName).toPath(), bytes);
        }
    }

}

