package cern.c2mon.web.configviewer.service;

import java.util.HashMap;

import javax.xml.transform.TransformerException;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cern.c2mon.client.common.listener.ClientRequestReportListener;
import cern.c2mon.shared.client.request.ClientRequestErrorReport;
import cern.c2mon.shared.client.request.ClientRequestProgressReport;
import cern.c2mon.web.configviewer.util.ReportHandler;
import cern.c2mon.web.configviewer.util.XsltTransformUtility;
import cern.tim.shared.client.configuration.ConfigurationReport;

/**
 * ConfigLoaderService service providing the XML representation for a given config
 * */
@Service
public class ConfigLoaderService  {

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
  
  /**
   * Stores the ProgressReports. 
   **/
  private HashMap<String, ReportHandler> progressReports = new HashMap<String, ReportHandler>();
  
  /**
   * Stores the ConfigurationReports. 
   **/
  private HashMap<String, ConfigurationReport> finalReports = new HashMap<String, ConfigurationReport>();

  /** the path to the xslt document */
  private static final String XSLT_PATH = "/generic_tag.xsl";

  /**
   * Gets the XML representation of the ConfigurationReport
   * @param configurationId id
   * @return XML representation of  the ConfigurationReport 
   * @throws TagIdException if id not found or a non-numeric id was requested ({@link TagIdException}), or any other exception
   * thrown by the underlying service gateway.
   * */
  public String getConfigurationReportXml(final String configurationId) throws TagIdException {

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
  
  private String generateHtmlForConfigurationReport(final ConfigurationReport report)
    throws TagIdException, TransformerException {
      
    String xml = null;

    try {
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

  public String generateHtmlResponse(final String configurationId) 
    throws TagIdException, TransformerException {

    ConfigurationReport  report = getConfigurationReport(Long.parseLong(configurationId));
    return generateHtmlForConfigurationReport(report);
  }

  /**
   * Retrieves a ConfigurationReport object from the service gateway tagManager
   * @param configurationId id of the configuration
   * @return Configuration Report
   * */
  private ConfigurationReport getConfigurationReport(final long configurationId) {
    ConfigurationReport report = gateway.getTagManager().applyConfiguration(configurationId); 

    logger.debug("getConfigurationReport fetch for ConfigurationReport " 
        + configurationId + ": " + (report == null ? "NULL" : "SUCCESS"));
    return report;
  }
  
  /**
   * Retrieves a ConfigurationReport object from the service gateway tagManager
   * @param configurationId id of the configuration
   * of the request
   * @return Configuration Report
   * */
  public ConfigurationReport getConfigurationReportWithReportUpdates(final long configurationId) {
    
    ReportHandler reportHandler = new ReportHandler(configurationId);
    progressReports.put("" + configurationId, reportHandler);
    
    ConfigurationReport report = gateway.getTagManager().applyConfiguration(
        configurationId, reportHandler); 
    
    finalReports.put("" + configurationId, report); // store the report for viewing later
    
    logger.debug("getConfigurationReportWithReportUpdates fetch for ConfigurationReport " 
        + configurationId + ": " + (report == null ? "NULL" : "SUCCESS"));
    return report;
  }
  
  /**
   * Retrieves a ConfigurationReport stored in the web server.
   * @param configurationId id of the configuration
   * of the request
   * @return Configuration Report
   * */
  public ConfigurationReport getStoredConfigurationReport(final String configurationId) {
    
    return finalReports.get(configurationId);
  }
  
  /**
   * Retrieves a ConfigurationReport stored in the web server
   * and returns the result as an Html Page.
   * @param configurationId id of the configuration
   * @return Html page for the Configuration Report
   * @throws TransformerException 
   * @throws TagIdException 
   * */
  public String getStoredConfigurationReportHtml(final String configurationId) 
    throws TagIdException, TransformerException {
    
    return generateHtmlForConfigurationReport(finalReports.get(configurationId));
  }
  
  /**
   * @param configurationId id of the configuration request
   * @return a Report for the specified configuration (must be currently running!)
   * */
  public ClientRequestProgressReport getReportForConfiguration(final String configurationId) {
    
    ClientRequestProgressReport report = null;
    ReportHandler reportHandler = progressReports.get(configurationId);
    
    if (reportHandler != null)
      report = reportHandler.getProgressReport();
    
    logger.info("ClientRequestProgressReport: fetch for report: " 
        + configurationId + ": " + (report == null ? "NULL" : "SUCCESS"));
    return report;
  }  
}
