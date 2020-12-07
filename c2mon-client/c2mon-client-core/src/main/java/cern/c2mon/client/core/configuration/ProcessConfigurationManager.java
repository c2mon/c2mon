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
package cern.c2mon.client.core.configuration;

import cern.c2mon.shared.client.configuration.ConfigurationReport;
import cern.c2mon.shared.client.configuration.api.process.Process;

/**
 * The ProcessConfigurationManager allows to apply create, update and delete
 * configurations for Processes.
 *
 * @author Franz Ritter
 */
public interface ProcessConfigurationManager {

  /**
   * Creates a new 'Process' on the server for the given name, which allows to
   * start a new DAQ process.
   * <p>
   * The Process is created with default parameters including the standard
   * ControlTags.
   *
   * @param processName The name of the process to be created.
   * @return A {@link ConfigurationReport} containing all details of the
   * Process configuration, including if it was successful or not.
   * @see ProcessConfigurationManager#createProcess(Process)
   */
  ConfigurationReport createProcess(String processName);

  /**
   * Creates a new 'Process' with the given parameters in the
   * {@link Process} object. The created Process allows than to start a new DAQ
   * process.
   * <p>
   * Next to the specified parameters the Process is created with default
   * parameters including the standard ControlTags.
   * <p>
   * Note: You have to use {@link Process#create(String)} to instantiate the
   * parameter of this method.
   *
   * @param process The {@link Process} configuration for the 'create'.
   * @return A {@link ConfigurationReport} containing all details of the
   * Process configuration, including if it was successful or not.
   * @see ProcessConfigurationManager#createProcess(String)
   */
  ConfigurationReport createProcess(Process process);

  /**
   * Updates a existing 'Process' with the given parameters in the
   * {@link Process} object.
   * <p>
   * Note: You have to use {@link Process#update(Long)} or
   * {@link Process#update(String)} to instantiate the parameter of this
   * method.
   *
   * @param process The {@link Process} configuration for the 'update'.
   * @return A {@link ConfigurationReport} containing all details of the
   * Process configuration, including if it was successful or not.
   */
  ConfigurationReport updateProcess(Process process);

  /**
   * Removes a existing 'Process' with the given id.
   *
   * @param id The id of the Process which needs to be removed.
   * @return A {@link ConfigurationReport} containing all details of the
   * Process configuration, including if it was successful or not.
   */
  ConfigurationReport removeProcessById(Long id);

  /**
   * Removes a existing 'Process' with the given name.
   *
   * @param name The name of the Process which needs to be removed.
   * @return A {@link ConfigurationReport} containing all details of the
   * Process configuration, including if it was successful or not.
   */
  ConfigurationReport removeProcess(String name);
}
