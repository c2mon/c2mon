/******************************************************************************
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
 *****************************************************************************/
package cern.c2mon.server.configuration;

import cern.c2mon.shared.client.configuration.ConfigurationException;
import cern.c2mon.shared.client.configuration.ConfigurationReport;
import cern.c2mon.shared.client.configuration.ConfigurationReportHeader;
import cern.c2mon.shared.client.configuration.api.Configuration;

import java.util.List;

/**
 * Public bean that can be accessed to load configurations into
 * the server at runtime.
 *
 * <p>Another option provided by this module for activating a reconfiguration
 * is through the ${tim.config} JMS queue. Clients can send reconfiguration
 * requests directly to this queue (the ConfigurationLoader is then called
 * internally).
 *
 * @author Mark Brightwell
 *
 */
public interface ConfigurationLoader {

  /**
   * Applies the configuration with the given id to the server. This involves
   * retrieving the configuration details from the Config database and making
   * the appropriate changes in the server cache and cache database. If necessary,
   * configuration elements are also passed on to the DAQ layer for further action.
   *
   * <p>The returned report is made up of reports for each individual configuration
   * element, including reports returned by the DAQ.
   *
   * <p>Configuration are made up of a number of configuration elements. Configuration
   * elements are either applied successfully on the server or not at all. However,
   * if a configuration element fails on the DAQ layer for some reason, but applied
   * successfully on the server, the change will be committed on the server (DAQ
   * may need a restart).
   *
   * <p>TODO NOT DECIDED YET: If a reconfiguration of a point, rule or alarm fails for
   * some reason, the element mode is switched to UNCONFIGURED and a new configuration
   * should be applied. Notice that if an equipment or process reconfiguration fails,
   * the state tag is set to RECONFIGURED until a new successful reconfiguration is applied.
   *
   * @param configId the id of the configuration to apply to the server
   * @param configProgressMonitor will be provided with callbacks on the progress of the call;
   *        can be set null if no callbacks wished for
   * @return a report with details of the success/failure of the reconfiguration
   * @throws ConfigurationException if the configuration fails (report is attached)
   */
  ConfigurationReport applyConfiguration(int configId, ConfigProgressMonitor configProgressMonitor);

  /**
   * For other method but for use with no progress feedback.
   * @param configId id of configuration
   * @return report
   */
  ConfigurationReport applyConfiguration(int configId);

  /**
   * Applies a configuration based on a configuration object to the server.
   * The object itself holds all information for the configuration and don't depend on the Config database.
   * The object is used to create a list configuration elements which are used fot
   * the configuration by the server itself.
   *
   * <p>The returned report is made up of reports for each individual configuration
   * element.
   *
   * <p>Configuration are made up of a number of configuration elements. Configuration
   * elements are either applied successfully on the server or not at all. However,
   * if a configuration element fails on the DAQ layer for some reason, but applied
   * successfully on the server, the change will be committed on the server (DAQ
   * may need a restart).
   *
   * @param configuration
   * @return
   */
  ConfigurationReport applyConfiguration(Configuration configuration);

  /**
   * Retrieve all previously applied configuration reports. Note: this only
   * returns partial information about the configuration report. To retrieve the
   * full report, use {@link ConfigurationLoader#getConfigurationReports(String)}.
   *
   * @return the list of reports
   */
  List<ConfigurationReportHeader> getConfigurationReports();

  /**
   * Retrieve the full configuration report(s) for a given configuration. Since
   * a configuration may be run more than once, this method returns a collection
   * of all historical reports for the given configuration.
   *
   * @param id the id of the configuration report
   * @return the full configuration report(s) if the configuration was run more
   *         than once
   */
  List<ConfigurationReport> getConfigurationReports(String id);
}
