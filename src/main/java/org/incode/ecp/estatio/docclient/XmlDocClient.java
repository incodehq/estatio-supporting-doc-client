package org.incode.ecp.estatio.docclient;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.util.Base64;
import java.util.Iterator;

import javax.net.ssl.HttpsURLConnection;
import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

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
     * @param invoiceNumber - stored as <code>extRef1</code> in <code>oas_docline</code>
     * @param year - invoice numbers are reset each year.
     */
    public Document fetch(final String invoiceNumber, final String year)
            throws IOException, ParserConfigurationException, SAXException {
        final String url = String.format(
                "https://%s/restful/services/%s/actions/%s/invoke?invoiceNumber=%s&year=%s",
                host, "lease.SupportingDocumentService", "findSupportingDocuments", invoiceNumber, year);

        final HttpsURLConnection connection = openConnection(url, user, pass);
        final InputStream inputStream = connection.getInputStream();
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        DocumentBuilder documentBuilder = dbf.newDocumentBuilder();
        final Document document = documentBuilder.parse(inputStream);
        connection.disconnect();
        return document;
    }

    /**
     * Convenience that calls {@link #fetch(String, String)} and then writes out all returned documents to specified directory.
     *
     * @param invoiceNumber
     * @param year
     * @param directory - automatically created if required.
     */
    public void fetchAndWrite(final String invoiceNumber, final String year, final String directory)
            throws IOException, ParserConfigurationException, SAXException, XPathExpressionException {

        final File parent = new File(directory);
        parent.mkdirs();

        final Document document = fetch(invoiceNumber, year);
        final XPath xPath = XPathFactory.newInstance().newXPath();

        xPath.setNamespaceContext(new NamespaceResolver(document));
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

    private static HttpsURLConnection openConnection(
            String urlStr, String user, String pass)
            throws IOException{

        String authStr = user+":"+pass;
        String authEncoded = Base64.getEncoder().encodeToString(authStr.getBytes());

        final URL url = new URL(urlStr);
        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();

        connection.setRequestMethod("GET");
        connection.setDoOutput(true);
        connection.setRequestProperty("Authorization", "Basic " + authEncoded);
        connection.setRequestProperty("Accept",
                "application/xml"
                        + ";profile=\"urn:org.restfulobjects:repr-types/action-result\""
                        + ";x-ro-domain-type=\"org.estatio.canonical.documents.v2.DocumentsDto\"");

        return connection;
    }

}

class NamespaceResolver implements NamespaceContext
{
    //Store the source document to search the namespaces
    private Document sourceDocument;

    public NamespaceResolver(Document document) {
        sourceDocument = document;
    }

    //The lookup for the namespace uris is delegated to the stored document.
    public String getNamespaceURI(String prefix) {
        if (prefix.equals(XMLConstants.DEFAULT_NS_PREFIX)) {
            return sourceDocument.lookupNamespaceURI(null);
        } else {
            return sourceDocument.lookupNamespaceURI(prefix);
        }
    }

    public String getPrefix(String namespaceURI) {
        return sourceDocument.lookupPrefix(namespaceURI);
    }

    @SuppressWarnings("rawtypes")
    public Iterator getPrefixes(String namespaceURI) {
        return null;
    }
}
