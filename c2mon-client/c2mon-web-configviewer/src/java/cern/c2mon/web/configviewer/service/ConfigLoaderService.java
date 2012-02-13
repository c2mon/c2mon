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
import cern.c2mon.web.configviewer.util.XsltTransformUtility;
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
  
  /**
   * Performs xslt transformations. 
   * */
  @Autowired
  private XsltTransformUtility xsltTransformer;

  /** the path to the xslt document */
  private static final String XSLT_PATH = "/generic_tag.xsl";

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

  public String generateHtmlResponse(final String configurationId) 
    throws TagIdException, TransformerException {

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

    String html = null;

    try {
      html = xsltTransformer.performXsltTransformation(xml);
    } catch (TransformerException e) {
      logger.error("Error while performing xslt transformation.");
      throw new TransformerException("Error while performing xslt transformation.");
    }

    return html;
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
