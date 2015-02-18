package cern.c2mon.web.configviewer.service;

import java.util.Collection;
import java.util.HashMap;

import javax.naming.CannotProceedException;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.acls.model.NotFoundException;
import org.springframework.stereotype.Service;

import cern.c2mon.shared.client.configuration.ConfigurationReport;
import cern.c2mon.shared.client.request.ClientRequestProgressReport;
import cern.c2mon.web.configviewer.util.ReportHandler;

/**
 * ConfigLoaderService service providing the XML representation for a given
 * config
 */
@Service
public class ConfigLoaderService {

  /**
   * ConfigLoaderService logger
   */
  private static Logger logger = Logger.getLogger(ConfigLoaderService.class);

  /**
   * Gateway to ConfigLoaderService
   */
  @Autowired
  private ServiceGateway gateway;

  /**
   * Stores the ProgressReports.
   **/
  private HashMap<String, ReportHandler> progressReports = new HashMap<String, ReportHandler>();

  /**
   * Stores the ConfigurationReports.
   **/
  private HashMap<String, ConfigurationReport> finalReports = new HashMap<String, ConfigurationReport>();

  /**
   * Gets the XML representation of the ConfigurationReport
   *
   * @param configurationId id
   * @return XML representation of the ConfigurationReport
   * @throws TagIdException if id not found or a non-numeric id was requested (
   *           {@link TagIdException}), or any other exception thrown by the
   *           underlying service gateway.
   */
  public String getConfigurationReportXml(final String configurationId) throws TagIdException {

    try {
      ConfigurationReport report = getConfigurationReport(Long.parseLong(configurationId));
      if (report != null)
        return report.toXML();
      else
        throw new TagIdException("Id not found.");
    } catch (NumberFormatException e) {
      throw new TagIdException("Invalid configuration Id");
    }
  }


  /**
   * Retrieves a ConfigurationReport object from the service gateway tagManager
   *
   * @param configurationId id of the configuration
   * @return Configuration Report
   */
  private ConfigurationReport getConfigurationReport(final long configurationId) {
    ConfigurationReport report = gateway.getTagManager().applyConfiguration(configurationId);

    logger.debug("getConfigurationReport: Received configuration report? -> " + configurationId + ": " + (report == null ? "NULL" : "SUCCESS"));

    if (report == null)
      logger.warn("Received NULL Configuration report for configuration id:" + configurationId);

    return report;
  }

  /**
   * Applies the specified configuration and stores the Configuration Report for
   * later viewing.
   *
   * @param configurationId id of the configuration of the request
   * @throws CannotProceedException In case a serious error occurs (for example
   *           in case a null Configuration Report is received).
   */
  public void getConfigurationReportWithReportUpdates(final long configurationId) throws CannotProceedException {

    ReportHandler reportHandler = new ReportHandler(configurationId);
    progressReports.put("" + configurationId, reportHandler);

    ConfigurationReport report = gateway.getTagManager().applyConfiguration(configurationId, reportHandler);

    logger.debug("getConfigurationReport: Received configuration report? -> " + configurationId + ": " + (report == null ? "NULL" : "SUCCESS"));

    if (report == null) {
      logger.error("Received NULL Configuration report for configuration id:" + configurationId);
      throw new CannotProceedException("Did not receive Configuration Report.");
    }
    logger.debug("getConfigurationReport: Report=" + report.toXML());

    finalReports.put("" + configurationId, report); // store the report for
                                                    // viewing later
  }

  /**
   * Retrieves a ConfigurationReport stored in the web server.
   *
   * @param configurationId id of the configuration of the request
   * @return Configuration Report
   */
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
   * @return all the previously applied configuration reports
   */
  public HashMap<String, ConfigurationReport> getFinalReports() {
    if (finalReports.isEmpty()) {
      Collection<ConfigurationReport> reports = gateway.getTagManager().getConfigurationReports();
      for (ConfigurationReport report : reports) {
        finalReports.put(String.valueOf(report.getId()), report);
      }
    }
    return finalReports;
  }

  /**
   * @param configurationId id of the configuration request
   * @return a Progress Report for the specified configuration (must be
   *         currently running!)
   */
  public ClientRequestProgressReport getProgressReportForConfiguration(final String configurationId) {

    ClientRequestProgressReport report = null;
    ReportHandler reportHandler = progressReports.get(configurationId);

    if (reportHandler != null)
      report = reportHandler.getProgressReport();

    logger.debug("ClientRequestProgressReport: fetch for report: " + configurationId + ": " + (report == null ? "NULL" : "SUCCESS"));
    return report;
  }
}
