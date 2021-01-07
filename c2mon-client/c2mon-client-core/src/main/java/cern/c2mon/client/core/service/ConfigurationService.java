/*******************************************************************************
 * Copyright (C) 2010-2016 CERN. All rights not expressly granted are reserved.
 *
 * This file is part of the CERN Control and Monitoring Platform 'C2MON'.
 * C2MON is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the license.
 *
 * C2MON is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
 * more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with C2MON. If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package cern.c2mon.client.core.service;

import cern.c2mon.client.common.listener.ClientRequestReportListener;
import cern.c2mon.client.core.configuration.*;
import cern.c2mon.shared.client.configuration.ConfigurationReport;
import cern.c2mon.shared.client.configuration.ConfigurationReportHeader;
import cern.c2mon.shared.client.configuration.api.Configuration;
import cern.c2mon.shared.client.process.ProcessNameResponse;
import cern.c2mon.shared.client.request.ClientRequestErrorReport;
import cern.c2mon.shared.client.request.ClientRequestProgressReport;
import cern.c2mon.shared.client.tag.TagConfig;

import java.util.Collection;

/**
 * The Configuration Service allows applying new configurations to the server
 * and to fetch the configuration for the configured DAQ Processes.
 *
 * @author Matthias Braeger
 */
public interface ConfigurationService extends ProcessConfigurationManager,
    EquipmentConfigurationManager, SubEquipmentConfigurationManager, DataTagConfigurationManager,
    RuleTagConfigurationManager, AlarmConfigurationManager, ControlTagConfigurationManager,
    CommandTagConfigurationManager, DeviceClassConfigurationManager, DeviceConfigurationManager {

  /**
   * Returns a TagConfiguration object for every valid id on the list. The
   * values are fetched from the server. However, in case of a connection
   * error or an unknown tag id the corresponding tag might be missing.
   *
   * @param tagIds A collection of data tag id's
   * @return A collection of all <code>TagConfiguration</code> objects
   */
  Collection<TagConfig> getTagConfigurations(final Collection<Long> tagIds);

  /**
   * Applies the configuration and returns a Configuration Report. The values
   * are fetched from the server. However, in case of a connection error or an
   * unknown configuration Id the corresponding tag might be missing.
   *
   * @param configurationId The configuration id used to fetch the
   *                        Configuration Report object
   * @return A Configuration Report object that also sends reports for the
   * progress of the operation
   *
   * @deprecated use {@link #applyConfiguration(Configuration, ClientRequestReportListener)}
   * instead
   */
  ConfigurationReport applyConfiguration(final Long configurationId);

  /**
   * Applies the configuration and returns a Configuration Report. The values
   * are fetched from the server.
   * <p>
   * Has an extra parameter that allows the caller to be informed for the
   * progress of the operation.
   * <p>
   * However, in case of a connection error or an unknown configuration Id the
   * corresponding tag might be missing.
   *
   * @param configurationId The configuration id used to fetch the
   *                        Configuration Report object
   * @param reportListener  Is informed about the progress of the operation on
   *                        the server side.
   * @return A {@link ConfigurationReport} object
   * @see ClientRequestProgressReport
   * @see ClientRequestErrorReport
   *
   * @deprecated use {@link #applyConfiguration(Configuration, ClientRequestReportListener)}
   * instead
   */
  ConfigurationReport applyConfiguration(final Long configurationId, final ClientRequestReportListener reportListener);

  /**
   * Applies a configuration based on a {@link Configuration} object. The
   * object holds all information what should be configured and how it should
   * be configured. This includes the common operations CREATE, DELETE and
   * UPDATE.
   * <p>
   * For more information of the configuration object read the documentation of
   * {@link Configuration} or follow the instruction in the
   * <a href="http://c2mon.web.cern.ch/c2mon/docs/#_offline_configuration_via_c2mon_database_test_purpose_only">
   * c2mon documentation</a>.
   *
   * @param configuration
   * @param listener
   * @return A {@link ConfigurationReport} object
   */
  ConfigurationReport applyConfiguration(final Configuration configuration, final ClientRequestReportListener listener);

  /**
   * Retrieve a list of all previously applied configuration reports from the
   * server. Note that this method will only return partial information about
   * each report. This is done to reduce the size of the message returned by
   * the server.
   * <p>
   * To get the full report(s) for a particular configuration, use
   * {@link #getConfigurationReports(Long)}.
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
   * than once
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
