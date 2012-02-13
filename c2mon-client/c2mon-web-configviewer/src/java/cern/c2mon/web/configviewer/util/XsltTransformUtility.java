package cern.c2mon.web.configviewer.util;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cern.c2mon.web.configviewer.service.AlarmService;

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

  /** xslt Source. Used to perform xslt transformations */
  private Source xsltSource;

  /** Transformer performs xslt transformations */
  private Transformer trans;

  /**
   * XsltTransformUtility.
   **/
  public XsltTransformUtility() { 

    InputStream xsltResource = new Object() { }.getClass().getResourceAsStream(XSLT_PATH);
    
    if (xsltResource != null) {
      logger.info("XsltTransformUtility(): Sucessfully initialised xsltResource -" + XSLT_PATH);
    }
    
    xsltSource = new StreamSource(xsltResource);    
    
    if (xsltSource == null) {
      logger.error("XsltTransformUtility() xsltSource is null!");
    }

    TransformerFactory transFact = TransformerFactory.newInstance();
    try {
      trans = transFact.newTransformer(xsltSource);
    } catch (TransformerConfigurationException e) {
      logger.error("XsltTransformUtility() Error while initialising xslt TransformerFactory:"
          + e.getMessage());
    }
  };

  /**
   * Transforms the xml to Html using xslt.
   * @param xml The xml
   * @return The html produced by transforming the xml using xslt.
   * @throws TransformerException if something goes wrong during the transformation
   */
  public String performXsltTransformation(final String xml) throws TransformerException {

    OutputStream ostream = null;

    Source xmlSource = new StreamSource(new StringReader(xml));

    ostream = new ByteArrayOutputStream();
    trans.transform(xmlSource, new StreamResult((ostream)));

    String result = ostream.toString();

    // a little hack to make firefox happy!
    result = result.replace("<?xml version=\"1.0\" encoding=\"UTF-8\"?>", ""); 
    
    logger.info("XsltTransformUtility(): Sucessfully performed xslt transformation.");

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

    // a little hack to make firefox happy!
    result = result.replace("<?xml version=\"1.0\" encoding=\"UTF-8\"?>", ""); 
    
    logger.info("XsltTransformUtility(): Sucessfully performed xslt transformation.");

    return result;
  }
}
