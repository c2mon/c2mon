package cern.c2mon.web.configviewer.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.naming.CannotProceedException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.acls.model.NotFoundException;
import org.springframework.stereotype.Service;

import cern.c2mon.shared.client.configuration.ConfigurationReport;
import cern.c2mon.shared.client.configuration.ConfigurationReportHeader;
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
  private static Logger logger = LoggerFactory.getLogger(ConfigLoaderService.class);

  /**
   * Gateway to ConfigLoaderService
   */
  @Autowired
  private ServiceGateway gateway;

  /**
   * Stores the ProgressReports.
   */
  private Map<String, ReportHandler> progressReports = new HashMap<>();

  /**
   * Stores the partial {@link ConfigurationReportHeader} objects. Each
   * configuration id may have multiple reports (as it may be run multiple
   * times).
   */
  private Map<String, List<ConfigurationReportHeader>> configurationReportHeaders = new TreeMap<>();

  /**
   * Stores the full {@link ConfigurationReport} objects. Each configuration id
   * may have multiple reports (as it may be run multiple times).
   */
  private Map<String, List<ConfigurationReport>> configurationReports = new HashMap<>();

  /**
   * Applies the specified configuration and stores the Configuration Report for
   * later viewing.
   *
   * @param configurationId id of the configuration of the request
   * @throws CannotProceedException In case a serious error occurs (for example
   *           in case a null Configuration Report is received).
   */
  public void applyConfiguration(final long configurationId) throws CannotProceedException {

    ReportHandler reportHandler = new ReportHandler(configurationId);
    progressReports.put(String.valueOf(configurationId), reportHandler);

    ConfigurationReport report = gateway.getTagManager().applyConfiguration(configurationId, reportHandler);

    logger.debug("getConfigurationReport: Received configuration report? -> " + configurationId + ": " + (report == null ? "NULL" : "SUCCESS"));

    if (report == null) {
      logger.error("Received NULL Configuration report for configuration id:" + configurationId);
      throw new CannotProceedException("Did not receive Configuration Report.");
    }
    logger.debug("getConfigurationReport: Report=" + report.toXML());

    if (report.getName().equals("UNKNOWN")) {
      if (configurationReports.containsKey(String.valueOf(configurationId))) {
        configurationReports.get(String.valueOf(configurationId)).add(report);
      } else {
        List<ConfigurationReport> reports = getConfigurationReports(String.valueOf(configurationId));
        if (reports.isEmpty()) {
          reports.add(report);
        }
        configurationReports.put(String.valueOf(configurationId), reports);
      }
    }

    // store the report for viewing later
    ConfigurationReportHeader header = new ConfigurationReportHeader(report.getId(), report.getName(), report.getUser(), report.getStatus(),
        report.getStatusDescription(), report.getTimestamp());

    Map<String, List<ConfigurationReportHeader>> headers = getConfigurationReports(false);
    if (headers.containsKey(String.valueOf(configurationId))) {
      headers.get(String.valueOf(configurationId)).add(header);
    } else {

      headers.put(String.valueOf(configurationId), new ArrayList<>(Arrays.asList(header)));
    }
  }

  /**
   * Retrieves all full reports for a particular configuration.
   *
   * @param configurationId id of the configuration of the request
   * @return a map of full reports
   */
  public List<ConfigurationReport> getConfigurationReports(final String configurationId) {
    List<ConfigurationReport> reports;

    if (configurationReports.containsKey(configurationId)) {
      reports = configurationReports.get(configurationId);
    }

    else {
      reports = new ArrayList<>(gateway.getTagManager().getConfigurationReports(Long.valueOf(configurationId)));
      Collections.sort(reports);
    }

    if (reports == null) {
      logger.error("Could not retrieve Stored Configuration Report for configuration id:" + configurationId);
      throw new NotFoundException("Cannot find Configuration Report for configuration id:" + configurationId);
    }
    logger.debug("Successfully retrieved Stored Configuration Report for configuration id:" + configurationId);

    return reports;
  }

  /**
   * Retrieve partial information about all previous configurations.
   *
   * @return all the previously applied configuration reports
   */
  public Map<String, List<ConfigurationReportHeader>> getConfigurationReports(boolean refresh) {

    if (refresh || configurationReportHeaders.isEmpty()) {
      List<ConfigurationReportHeader> reports = new ArrayList<>(gateway.getTagManager().getConfigurationReports());
      Collections.sort(reports);

      for (ConfigurationReportHeader report : reports) {
        if (configurationReportHeaders.containsKey(String.valueOf(report.getId()))) {
          configurationReportHeaders.get(String.valueOf(report.getId())).add(report);
        } else {
          configurationReportHeaders.put(String.valueOf(report.getId()), new ArrayList<>(Arrays.asList(report)));
        }
      }
    }

    return configurationReportHeaders;
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
