package cern.c2mon.web.configviewer.service;

import java.util.HashMap;

import javax.naming.CannotProceedException;
import javax.xml.transform.TransformerException;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.acls.model.NotFoundException;
import org.springframework.stereotype.Service;

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
  
  /**
   * Transforms the given Configuration Report to an html page.
   * 
   * @param report the report to be transformed to an html page
   * @return An html representation of the specified Configuration Report
   * 
   * @throws TransformerException if an error occurs while generating the html page
   * @throws TagIdException if an invalid Configuration Id was given
   * @throws CannotProceedException if a serious error occurs (like a Configuration Report
   * that is missing)
   */
  private String generateHtmlForConfigurationReport(final ConfigurationReport report)
    throws TagIdException, TransformerException, CannotProceedException {
      
    String xml = null;

    try {
      if (report != null)
        xml = report.toXML();
      else
        throw new CannotProceedException("Report cannot be found.");
    } catch (NumberFormatException e) {
      throw new TagIdException("Invalid configuration Id");
    }

    String html = null;

    try {
      html = xsltTransformer.performXsltTransformation(xml);
    } catch (TransformerException e) {
      logger.error("Error while performing xslt transformation." + e.getMessage());
      throw new TransformerException("Error while performing xslt transformation."
          + e.getMessage());
    }

    return html;
  }

  /**
   * Retrieves a ConfigurationReport object from the service gateway tagManager
   * and Transforms it to an html page.
   * 
   * @param configurationId id of the configuration
   * @return An html representation of the Configuration Report
   * 
   * @throws TransformerException if an error occurs while generating the html page
   * @throws TagIdException if an invalid Configuration Id was given
   * @throws CannotProceedException if a serious error occurs (like a Configuration Report
   * that is missing)
   */
  public String generateHtmlResponse(final String configurationId) 
    throws TagIdException, TransformerException, CannotProceedException {

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

    logger.debug("getConfigurationReport: Received configuration report? -> " 
        + configurationId + ": " + (report == null ? "NULL" : "SUCCESS"));
    
    if (report == null)
      logger.warn("Received NULL Configuration report for configuration id:" + configurationId);
      
    return report;
  }
  
  /**
   * Applies the specified configuration and stores the 
   * Configuration Report for later viewing.
   * 
   * @param configurationId id of the configuration
   * of the request
   * @throws CannotProceedException In case a serious error occurs (for example
   * in case a null Configuration Report is received).
   * */
  public void getConfigurationReportWithReportUpdates(final long configurationId) throws CannotProceedException {
    
    ReportHandler reportHandler = new ReportHandler(configurationId);
    progressReports.put("" + configurationId, reportHandler);
    
    ConfigurationReport report = gateway.getTagManager().applyConfiguration(
        configurationId, reportHandler); 
    
    if (report == null) {
      logger.error("Received NULL Configuration report for configuration id:" + configurationId);
      throw new CannotProceedException("Did not receive Configuration Report.");
    }
    finalReports.put("" + configurationId, report); // store the report for viewing later
  }
  
  /**
   * Retrieves a ConfigurationReport stored in the web server.
   * @param configurationId id of the configuration
   * of the request
   * @return Configuration Report
   * */
  public ConfigurationReport getStoredConfigurationReport(final String configurationId) {
    
    ConfigurationReport report = finalReports.get(configurationId);
    
    if (report == null) {
      logger.error("Could not retrieve Stored Configuration Report for configuration id:" + configurationId);    
      throw new NotFoundException("Cannot find Configuration Report for configuration id:" + configurationId);
    }
    logger.debug("Succesfully retrieved Stored Configuration Report for configuration id:" + configurationId);
    
    return report;
  }
  
  /**
   * Retrieves a ConfigurationReport stored in the web server
   * and returns the result as an Html Page.
   * 
   * @param configurationId id of the configuration
   * @return Html page for the Configuration Report
   * 
   * @throws TransformerException if an error occurs while generating the html page
   * @throws TagIdException if an invalid Configuration Id was given
   * @throws CannotProceedException if a serious error occurs (like a Configuration Report
   * that is missing)
   * */
  public String getStoredConfigurationReportHtml(final String configurationId) 
    throws TagIdException, TransformerException, CannotProceedException {
    
    return generateHtmlForConfigurationReport(finalReports.get(configurationId));
  }
  
  /**
   * @return all the previously applied configuration reports
   */
  public HashMap<String, ConfigurationReport> getFinalReports() {
    return finalReports;
  }
  
  /**
   * @param configurationId id of the configuration request
   * @return a Progress Report for the specified configuration (must be currently running!)
   * */
  public ClientRequestProgressReport getProgressReportForConfiguration(final String configurationId) {
    
    ClientRequestProgressReport report = null;
    ReportHandler reportHandler = progressReports.get(configurationId);
    
    if (reportHandler != null)
      report = reportHandler.getProgressReport();
    
    logger.info("ClientRequestProgressReport: fetch for report: " 
        + configurationId + ": " + (report == null ? "NULL" : "SUCCESS"));
    return report;
  }  
}
