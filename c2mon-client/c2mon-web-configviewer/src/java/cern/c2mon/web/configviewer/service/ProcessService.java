package cern.c2mon.web.configviewer.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.annotation.PostConstruct;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;

import cern.c2mon.client.auth.impl.RbacAuthenticationManager;
import cern.c2mon.client.common.tag.ClientCommandTag;
import cern.c2mon.client.core.tag.ClientCommandTagImpl;
import cern.c2mon.shared.client.process.ProcessNameResponse;
import cern.c2mon.web.configviewer.controller.AlarmController;
import cern.c2mon.web.configviewer.service.ServiceGateway;
import cern.c2mon.web.configviewer.service.TagIdException;
import cern.tim.shared.client.configuration.ConfigurationReport;

/**
 * Command service providing the XML representation of a given alarm
 * */
@Service
public class ProcessService {

  /**
   * ConfigLoaderService logger
   * */
  private static Logger logger = Logger.getLogger(ProcessService.class);
  
  /** the path to the xslt document */
  private static final String XSLT_PATH = "/optimised_tag.xsl";

  /**
   * Gateway to ConfigLoaderService 
   * */
  @Autowired
  private ServiceGateway gateway;

  @PostConstruct
  private void init() {

  }

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

  /**
   * Transforms the xml to Html using xslt.
   * @param xml the xml
   * @return the html
   */
  private String transformToHtml(final String xml) {

    OutputStream ostream = null;

    try {

      InputStream xsltResource = getClass().getResourceAsStream(XSLT_PATH);
      Source xsltSource = new StreamSource(xsltResource);
      TransformerFactory transFact;
      Transformer trans = null;

      transFact = TransformerFactory.newInstance();

      Source xmlSource = new StreamSource(new StringReader(xml));

      trans = transFact.newTransformer(xsltSource);

      ostream = new ByteArrayOutputStream();
      trans.transform(xmlSource, new StreamResult((ostream)));

    } catch (TransformerException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    
    String result = ostream.toString();
    
    // a little hack to make firefox happy!
    result = result.replace("<?xml version=\"1.0\" encoding=\"UTF-8\"?>",""); 

    return result;
  }

  
  public String generateHtmlResponse(final String processName) {

    String xml = getXml(processName);

    return transformToHtml(xml);
  }


  /**
   * Private helper method. Gets the XML representation of the process
   * @param processName processName
   * @return XML
   * */
  private String getXml(final String processName) {

    String xml = gateway.getTagManager().getProcessXml(processName);

    logger.debug("getXml fetch for process " + processName + ": " + (xml == null ? "NULL" : "SUCCESS"));
    return xml;
  }
}
