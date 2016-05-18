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
package cern.c2mon.server.configuration.dao;

import java.util.List;

import cern.c2mon.shared.client.configuration.ConfigurationDescriptor;
import cern.c2mon.shared.client.configuration.ConfigurationElement;

/**
 * DAO interface that must be implemented for use by the C2MON server
 * configuration module.
 *
 * @author Mark Brightwell
 *
 */
public interface ConfigurationDAO {

  /**
   * Returns the name of a configuration given its id.
   *
   * @param configId the unique configuration id
   * @return the name of the configuration
   */
  String getConfigName(int configId);

  /**
   * Returns the list of ConfigurationElements for a given
   * configuration id.
   * @param configId the unique configuration id
   * @return a list of configuration elements
   */
  List<ConfigurationElement> getConfigElements(int configId);

  /**
   * Return a descriptor of the specified configuration. Not used yet.
   * @param configId the id of the configuration
   * @return a configuration descriptor
   */
  ConfigurationDescriptor getConfiguration(int configId);

  /**
   * Return a list of descriptors of all available configurations in
   * the DB. Not used yet.
   * @return a list of configurations
   */
  List<ConfigurationDescriptor> getConfigurations();

  /**
   * Update the status & DAQ status columns of the element in the element table.
   * The server status indicates the success/failure of deploying this
   * configuration to the server. The DAQ status indicates the status of applying
   * this change to the DAQ (both Failure and Restart mean the DAQ needs restarting).
   *
   * @param configurationElement the element with the status info to persist
   */
  void saveStatusInfo(ConfigurationElement configurationElement);

  /**
   * Marks the configuration with the passed id as having been applied
   * (status column) and sets the apply date
   * @param id of the configuration that has been applied
   */
  void markAsApplied(int id);
}
