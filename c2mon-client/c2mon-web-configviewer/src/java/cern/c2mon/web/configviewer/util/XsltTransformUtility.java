package cern.c2mon.web.configviewer.util;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

/**
 * Utility class to perform xslt transformations
 * */
@Service
public final class XsltTransformUtility {

  /**
   * XsltTransformUtility logger
   */
  private static Logger logger = Logger.getLogger(XsltTransformUtility.class);

  /** the path to the xslt document */
  private static final String REMOVE_NAMESPACES_XSLT_PATH = "../xslt/remove_xml_namespaces.xsl";

  /**
   *
   * @param xml
   * @param xslt
   * @return
   * @throws TransformerException
   */
  public static String performXsltTransformation(final String xml, final String xslt) throws TransformerException {
    return performXsltTransformation(xml, xslt, false);
  }

  /**
   * Transforms the xml to Html using xslt.
   *
   * @param xml the xml
   * @param xslt path to the xslt file
   * @return The html produced by transforming the xml using xslt.
   * @throws TransformerException if something goes wrong during the
   *           transformation
   */
  public static String performXsltTransformation(String xml, final String xslt, final boolean removeXmlNamespaces) throws TransformerException {

    OutputStream ostream = null;

    if (removeXmlNamespaces) {
      xml = removeXmlNameSpaces(xml);
    }

    InputStream xsltResource = new Object() {
    }.getClass().getResourceAsStream(xslt);
    Source xsltSource = new StreamSource(xsltResource);
    TransformerFactory transFact;
    Transformer trans = null;

    transFact = TransformerFactory.newInstance();

    transFact.setURIResolver(new URIResolver() {
      @Override
      public Source resolve(String href, String base) throws TransformerException {
        try {
          InputStream inputStream = this.getClass().getResourceAsStream("../xslt/" + href);
          return new StreamSource(inputStream);
        } catch (Exception e) {
          logger.error("Exception caught resolving URI", e);
          return null;
        }
      }
    });

    Source xmlSource = new StreamSource(new StringReader(xml));

    trans = transFact.newTransformer(xsltSource);

    ostream = new ByteArrayOutputStream();
    trans.transform(xmlSource, new StreamResult((ostream)));

    String result = ostream.toString();
    result = removeXmlHeader(result);

    logger.info("XsltTransformUtility(): Sucessfully performed xslt transformation.");

    return result;
  }

  /**
   * Removes the xml header <?xml version="1.0" encoding="UTF-8" ?>
   *
   * from the given xml string.
   *
   * @param xml The xml whose header will be removed
   * @return The given xml with the header removed
   */
  public static String removeXmlHeader(final String xml) {

    String result = new String(xml);

    // this little hack makes firefox happy!
    //
    // @see http://issues/browse/TIMS-782
    result = result.replace("<?xml version=\"1.0\" encoding=\"UTF-8\"?>", "");
    result = result.replace("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>", "");
    result = result.replace("<?xml", "");

    return result;
  }

  /**
   * Removes Namespaces from the specified XML file.
   *
   * @param xml the XML file
   * @return the XML file without Namespaces
   * @throws TransformerException
   */
  private static String removeXmlNameSpaces(final String xml) throws TransformerException {

    Source xmlSource = new StreamSource(new StringReader(xml));

    InputStream removeNamespaceXsltResource = new Object() {
    }.getClass().getResourceAsStream(REMOVE_NAMESPACES_XSLT_PATH);

    if (removeNamespaceXsltResource != null) {
      logger.info("XsltTransformUtility(): Sucessfully initialised xsltResource -" + REMOVE_NAMESPACES_XSLT_PATH);
    }

    Source source = new StreamSource(removeNamespaceXsltResource);
    TransformerFactory transFact = TransformerFactory.newInstance();
    Transformer transformer = transFact.newTransformer(source);

    StringWriter writer = new StringWriter();
    transformer.transform(xmlSource, new StreamResult(writer));
    return writer.toString();
  }
}
