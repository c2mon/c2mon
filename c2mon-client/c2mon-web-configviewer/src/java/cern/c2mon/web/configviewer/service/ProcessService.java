package cern.c2mon.web.configviewer.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.xml.transform.TransformerException;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cern.c2mon.shared.client.process.ProcessNameResponse;
import cern.c2mon.web.configviewer.util.XsltTransformUtility;

/**
 * ProcessService providing the XML representation for a given process.
 * */
@Service
public class ProcessService {

  /**
   * ProcessService logger
   * */
  private static Logger logger = Logger.getLogger(ProcessService.class);
  
  /** the path to the xslt document */
  private static final String XSLT_PATH = "/generic_tag.xsl";
  
  /**
   * Performs xslt transformations. 
   * */
  @Autowired
  private XsltTransformUtility xsltTransformer;

  /**
   * Gateway to ConfigLoaderService 
   * */
  @Autowired
  private ServiceGateway gateway;


  /**
   * Gets the XML representation of the process
   * @param processName processName
   * @return XML
   * @throws Exception if id not found or a non-numeric id was requested ({@link TagIdException}), or any other exception
   * thrown by the underlying service gateway.
   * */
  public String getProcessXml(final String processName) throws Exception {

    try {
      String  xml = getXml(processName);
      if (xml != null)
        return xml;
      else
        throw new TagIdException("No luck. Try another processName.");
    } catch (NumberFormatException e) {
      throw new TagIdException("Invalid processName");
    }
  }

  /**
   * Gets all available process names
   * @return a collection of all available process names
   * */
  public Collection<String> getProcessNames() {

    Collection <ProcessNameResponse> processNames = gateway.getTagManager().getProcessNames();
    Collection <String> names = new ArrayList<String>();

    Iterator<ProcessNameResponse> i = processNames.iterator();

    while (i.hasNext()) {

      ProcessNameResponse p = (ProcessNameResponse) i.next();
      names.add(p.getProcessName());
    }
    return names;
  }    

  
  public String generateHtmlResponse(final String processName) throws TransformerException {

    String xml = getXml(processName);
    
    String html = null;

    try {
      html = xsltTransformer.performXsltTransformation(xml, true);
    } catch (TransformerException e) {
      logger.error("Error while performing xslt transformation.");
      throw new TransformerException("Error while performing xslt transformation.");
    }

    return html;
  }


  /**
   * Private helper method. Gets the XML representation of the process
   * @param processName processName
   * @return XML
   * */
  private String getXml(final String processName) {

    String xml = gateway.getTagManager().getProcessXml(processName);
    
    if (xml != null) 
      // @see http://issues/browse/TIMS-782
      xml = XsltTransformUtility.removeXmlHeader(xml);

    logger.debug("getXml fetch for process " + processName + ": " 
        + (xml == null ? "NULL" : "SUCCESS"));
    
    return xml;
  }
}
