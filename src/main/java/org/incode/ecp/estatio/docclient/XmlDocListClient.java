package org.incode.ecp.estatio.docclient;

import java.io.IOException;
import java.net.HttpURLConnection;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import static org.incode.ecp.estatio.docclient.Util.openConnection;
import static org.incode.ecp.estatio.docclient.XmlUtil.toXmlDocument;

public class XmlDocListClient {

    private final String host;
    private final String user;
    private final String pass;
    private final XmlDocClient docClient;

    public XmlDocListClient(
            final String host,
            final String user,
            final String pass) {
        this(host, user, pass, new XmlDocClient(host, user, pass));
    }

    public XmlDocListClient(
            final String host,
            final String user,
            final String pass,
            final XmlDocClient docClient) {
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
    public Document fetch(final int year) throws ParserConfigurationException, SAXException, IOException {
        return fetch(year, null);
    }

    /**
     * Returns all the invoices that have at least one supporting document, for the given year and seller reference (ie <code>cmpcode</code>).
     *
     * @param year - invoice numbers are reset each year.
     * @param sellerReferenceIfAny - the reference to the party acting as the seller of the invoice, ie the <code>cmpcode</code> of the <code>oas_dochead</code>.  Invoices that are not sold by this seller are ignored (filtered out).
     */
    public Document fetch(final int year, final String sellerReferenceIfAny)
            throws IOException, ParserConfigurationException, SAXException {
        String url = String.format(
                "%s/restful/services/%s/actions/%s/invoke?year=%d",
                host, "lease.SupportingDocumentService", "findInvoicesWithSupportingDocuments", year);
        if (sellerReferenceIfAny != null) {
            url += String.format(
                    "&sellerReference=%s",sellerReferenceIfAny);
        }

        final HttpURLConnection connection = openConnection(url, user, pass, "org.estatio.canonical.invoicenumbers.v2.InvoiceNumbersDto");
        return toXmlDocument(connection);
    }

    /**
     * Convenience that calls {@link #fetch(int)} and then writes out all returned documents to specified directory.
     */
    public void fetchAndWrite(
            final int year,
            final String directory)
            throws IOException, XPathExpressionException, SAXException, ParserConfigurationException {
        fetchAndWrite(year, null, directory);
    }

    /**
     * Convenience that calls {@link #fetch(int, String)} and then writes out all returned documents to specified directory.
     */
    public void fetchAndWrite(
            final int year,
            final String sellerReferenceIfAny,
            final String directory)
            throws IOException, XPathExpressionException, SAXException, ParserConfigurationException {

        final Document document = fetch(year, sellerReferenceIfAny);

        final XPath xPath = XPathFactory.newInstance().newXPath();

        xPath.setNamespaceContext(new XmlUtil.NamespaceResolver(document));
        final NodeList nodes = (NodeList) xPath.evaluate(
                "/invoicenumbers:invoiceNumbersDto/invoicenumbers:invoiceNumbers/invoicenumbers:invoiceNumber",
                document, XPathConstants.NODESET);
        for (int i = 0; i < nodes.getLength(); ++i) {
            final Node node = nodes.item(i);
            final String invoiceNumber = string(node, "invoicenumbers:invoiceNumber/text()", xPath);
            final String sellerReference = string(node, "invoicenumbers:sellerReference/text()", xPath);
            final String buyerReference = string(node, "invoicenumbers:buyerReference/text()", xPath);
            docClient.fetchAndWrite(invoiceNumber, year, sellerReference, buyerReference, directory + "/" + invoiceNumber);
        }
    }

    private static String string(
            final Node node,
            final String xpathExpression,
            final XPath xPath) throws XPathExpressionException {
        return (String) xPath.evaluate(xpathExpression, node, XPathConstants.STRING);
    }

}