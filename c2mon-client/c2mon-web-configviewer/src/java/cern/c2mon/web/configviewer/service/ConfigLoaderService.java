package cern.c2mon.web.configviewer.service;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cern.c2mon.client.core.tag.ClientCommandTagImpl;
import cern.c2mon.client.common.tag.ClientCommandTag;
import cern.c2mon.web.configviewer.service.ServiceGateway;
import cern.c2mon.web.configviewer.service.TagIdException;
import cern.tim.shared.client.configuration.ConfigurationReport;

/**
 * Command service providing the XML representation of a given alarm
 * */
@Service
public class ConfigLoaderService {

  /**
   * ConfigLoaderService logger
   * */
  private static Logger logger = Logger.getLogger(ConfigLoaderService.class);

  /**
   * Gateway to ConfigLoaderService 
   * */
  @Autowired
  private ServiceGateway gateway;

  /** the path to the xslt document */
  private static final String XSLT_PATH = "/optimised_tag.xsl";

  /**
   * Gets the XML representation of the ConfigurationReport
   * @param configurationId id
   * @return XML representation of  the ConfigurationReport 
   * @throws Exception if id not found or a non-numeric id was requested ({@link TagIdException}), or any other exception
   * thrown by the underlying service gateway.
   * */
  public String getConfigurationReportXml(final String configurationId) throws Exception {

    try {
      ConfigurationReport  report = getConfigurationReport(Long.parseLong(configurationId));
      if (report != null)
        return report.toXML();
      else
        throw new TagIdException("Id not found.");
    } catch (NumberFormatException e) {
      throw new TagIdException("Invalid configuration Id");
    }
  }

  public String generateHtmlResponse(final String configurationId) throws TagIdException {

    String xml = null;
    
    try {
      ConfigurationReport  report = getConfigurationReport(Long.parseLong(configurationId));
      if (report != null)
        xml = report.toXML();
      else
        throw new TagIdException("Id not found.");
    } catch (NumberFormatException e) {
      throw new TagIdException("Invalid configuration Id");
    }

    return transformToHtml(xml);
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

  /**
   * Retrieves a ConfigurationReport object from the service gateway tagManager
   * @param configurationId id of the alarm
   * @return Configuration Report
   * */
  private ConfigurationReport getConfigurationReport(final long configurationId) {
    ConfigurationReport report = gateway.getTagManager().applyConfiguration(configurationId); 

    logger.debug("getConfigurationReport fetch for ConfigurationReport " + configurationId + ": " + (report == null ? "NULL" : "SUCCESS"));
    return report;
  }
}
