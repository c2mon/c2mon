package cern.c2mon.web.configviewer.util;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
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
   * */
  private static Logger logger = Logger.getLogger(XsltTransformUtility.class);

  /** the path to the xslt document */
  private static final String XSLT_PATH = "/generic_tag.xsl";
  
  /** the path to the xslt document */
  private static final String REMOVE_NAMESPACES_XSLT_PATH = "/removeXmlNamespaces.xsl";

  /** Default xslt Source. Used to perform xslt transformations */
  private Source defaultXsltSource;
  
  /** Used to remove XML Namespaces */
  private Source removeXmlNamespacesXsltSource;

  /** Transformer performs xslt transformations */
  private Transformer trans;
  
  /** Transformer used to remove Xml Namespaces */
  private Transformer removeXmlNameSpacesTransformer;

  /**
   * XsltTransformUtility.
   **/
  public XsltTransformUtility() { 
    
    initializeXsltResources();

    TransformerFactory transFact = TransformerFactory.newInstance();
    
    try {
      trans = transFact.newTransformer(defaultXsltSource);
      removeXmlNameSpacesTransformer = transFact.newTransformer(removeXmlNamespacesXsltSource);
    } catch (TransformerConfigurationException e) {
      logger.error("XsltTransformUtility() Error while initialising xslt TransformerFactory:"
          + e.getMessage());
    }
  };
  
  /**
   * Reads the xslt files used in the xslt transformations.
   */
  private void initializeXsltResources() {
    
    // xslt used to remove Xml Namespaces
    InputStream removeNamespaceXsltResource = new Object() { }.getClass().getResourceAsStream(REMOVE_NAMESPACES_XSLT_PATH);
    if (removeNamespaceXsltResource != null) {
      logger.info("XsltTransformUtility(): Sucessfully initialised xsltResource -" + REMOVE_NAMESPACES_XSLT_PATH);
    }
    removeXmlNamespacesXsltSource = new StreamSource(removeNamespaceXsltResource);    
    if (removeXmlNamespacesXsltSource == null) {
      logger.error("XsltTransformUtility() removeXmlNamespacesXsltSource is null!");
    }
    
    // default xslt used in transformations
    InputStream defaultxsltResource = new Object() { }.getClass().getResourceAsStream(XSLT_PATH);
    if (defaultxsltResource != null) {
      logger.info("XsltTransformUtility(): Sucessfully initialised xsltResource -" + XSLT_PATH);
    }    
    defaultXsltSource = new StreamSource(defaultxsltResource);    
    if (defaultXsltSource == null) {
      logger.error("XsltTransformUtility() defaultxsltResource is null!");
    }
  }
  
  /**
   * Removes Namespaces from the specified XML file.
   * 
   * @param xml the XML file
   * @return the XML file without Namespaces
   * @throws TransformerException
   */
  private String removeXmlNameSpaces(final String xml) throws TransformerException {
    
    Source xmlSource = new StreamSource(new StringReader(xml)); // the input file
    
    StringWriter writer = new StringWriter();
    removeXmlNameSpacesTransformer.transform(xmlSource, new StreamResult(writer));
    return writer.toString();
  }

  /**
   * Transforms the xml to Html using xslt.
   * @param xml The xml
   * @return The html produced by transforming the xml using xslt.
   * @throws TransformerException if something goes wrong during the transformation
   */
  public String performXsltTransformation(final String xml) throws TransformerException {

    return performXsltTransformation(xml, false);
  }
  
  /**
   * Transforms the xml to Html using xslt.
   * @param xml The xml to be transformed
   * @param ignoreXmlNameSpaces if true Namespaces in the Xml file will be removed
   * before the transformation takes place.
   * 
   * @return The html produced by transforming the xml using xslt.
   * @throws TransformerException if something goes wrong during the transformation
   */
  public String performXsltTransformation(final String xml, final boolean ignoreXmlNameSpaces) throws TransformerException {

    OutputStream ostream = null;
    String xmlToBeTransformed = xml;
    
    //DebugUtil.writeToFile(xmlToBeTransformed, "xmlToBeTransformed" + ".xml");
    
    if (ignoreXmlNameSpaces)
      xmlToBeTransformed = removeXmlNameSpaces(xml);
    
    Source xmlSource = new StreamSource(new StringReader(xmlToBeTransformed));

    ostream = new ByteArrayOutputStream();
    trans.transform(xmlSource, new StreamResult((ostream)));

    final String result = ostream.toString();
    logger.info("XsltTransformUtility(): Sucessfully performed xslt transformation.");

    return result;
  }
  
  /**
   * Removes the xml header
   * <?xml version="1.0" encoding="UTF-8" ?>
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
   * Transforms the xml to Html using xslt.
   * @param xml the xml
   * @param xslt path to the xslt file
   * @return The html produced by transforming the xml using xslt.
   * @throws TransformerException if something goes wrong during the transformation
   */
  public static String performXsltTransformation(final String xml, final String xslt) throws TransformerException {

    OutputStream ostream = null;

    InputStream xsltResource = new Object() { }.getClass().getResourceAsStream(xslt);
    Source xsltSource = new StreamSource(xsltResource);
    TransformerFactory transFact;
    Transformer trans = null;

    transFact = TransformerFactory.newInstance();

    Source xmlSource = new StreamSource(new StringReader(xml));

    trans = transFact.newTransformer(xsltSource);

    ostream = new ByteArrayOutputStream();
    trans.transform(xmlSource, new StreamResult((ostream)));

    String result = ostream.toString();
    result = removeXmlHeader(result);

    logger.info("XsltTransformUtility(): Sucessfully performed xslt transformation.");

    return result;
  }
}
