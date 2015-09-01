package cern.c2mon.client.core;

import java.util.Collection;

import cern.c2mon.client.common.listener.ClientRequestReportListener;
import cern.c2mon.shared.client.configuration.ConfigurationReport;
import cern.c2mon.shared.client.configuration.ConfigurationReportHeader;
import cern.c2mon.shared.client.process.ProcessNameResponse;
import cern.c2mon.shared.client.request.ClientRequestErrorReport;
import cern.c2mon.shared.client.request.ClientRequestProgressReport;
import cern.c2mon.shared.client.tag.TagConfig;

/**
 * The Configuration Service allows applying new configurations to the server and to
 * fetch the configuration for the configured DAQ Processes.
 * 
 * @author Matthias Braeger
 */
public interface ConfigurationService {

  /**
   * Returns a TagConfiguration object for every valid id on the list.
   * The values are fetched from the server.
   * However, in case of a connection error or an unknown tag id the corresponding
   * tag might be missing.
   *
   * @param tagIds A collection of data tag id's
   * @return A collection of all <code>TagConfiguration</code> objects
   */
  Collection<TagConfig> getTagConfigurations(final Collection<Long> tagIds);
  
  /**
   * Applies the configuration and returns a Configuration Report.
   * The values are fetched from the server.
   * However, in case of a connection error or an unknown configuration Id the corresponding
   * tag might be missing.
   *
   * @see TagService#applyConfiguration(Long, ClientRequestReportListener) that also sends
   * reports for the progress of the operation
   *
   * @param configurationId The configuration id used to fetch the Configuration Report object
   * @return A Configuration Report object
   */
  ConfigurationReport applyConfiguration(final Long configurationId);

  /**
   * Applies the configuration and returns a Configuration Report.
   * The values are fetched from the server.
   *
   * Has an extra parameter that allows the caller
   * to be informed for the progress of the operation.
   *
   * However, in case of a connection error or an unknown configuration Id the corresponding
   * tag might be missing.
   *
   * @param configurationId The configuration id used to fetch the Configuration Report object
   * @param reportListener Is informed about the progress of the operation on the server side.
   * @see ClientRequestProgressReport
   * @see ClientRequestErrorReport
   * @return A Configuration Report object
   */
  ConfigurationReport applyConfiguration(final Long configurationId, final ClientRequestReportListener reportListener);

  /**
   * Retrieve a list of all previously applied configuration reports from the
   * server. Note that this method will only return partial information about
   * each report. This is done to reduce the size of the message returned by the
   * server.
   *
   * To get the full report(s) for a particular configuration, use
   * {@link TagService#getConfigurationReports(Long)}.
   *
   * @return the list of previously applied configuration reports
   */
  Collection<ConfigurationReportHeader> getConfigurationReports();

  /**
   * Retrieve the full configuration report(s) for a given configuration. Since
   * a configuration may be run more than once, this method returns a collection
   * of all historical reports for the given configuration.
   *
   * @param id the id of the configuration report
   * @return the full configuration report(s) if the configuration was run more
   *         than once
   */
  Collection<ConfigurationReport> getConfigurationReports(Long id);

  /**
   * Requests the DAQ config XML for a given process.
   *
   * @param processName the name of the Process
   * @return the DAQ XML as String
   */
  String getProcessXml(final String processName);

  /**
   * Requests a list of Names for all the existing processes.
   *
   * @return a list of all process names
   */
  Collection<ProcessNameResponse> getProcessNames();
}
