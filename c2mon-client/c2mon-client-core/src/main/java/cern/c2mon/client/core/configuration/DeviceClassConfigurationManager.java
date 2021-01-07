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
import cern.c2mon.shared.client.configuration.api.device.DeviceClass;

/**
 * The DeviceClassConfigurationManager allows to apply create, update and delete configurations for device classes.
 */
public interface DeviceClassConfigurationManager {

  /**
   * Creates a new 'DeviceClass' on the server for the given name.
   * <p>
   * The DeviceClass is created with default parameters.
   *
   * @param deviceClassName The name of the device class to be created.
   * @return A {@link ConfigurationReport} containing all details of the
   * DeviceClass configuration, including if it was successful or not.
   * @see DeviceClassConfigurationManager#createDeviceClass(DeviceClass)
   */
  ConfigurationReport createDeviceClass(String deviceClassName);

  /**
   * Creates a new 'DeviceClass' with the given parameters in the {@link DeviceClass} object.
   * <p>
   * Next to the specified parameters the DeviceClass is created with default  parameters.
   * <p>
   * Note: You have to use {@link DeviceClass#create(String)} to instantiate the parameter of this method.
   *
   * @param deviceClass The {@link DeviceClass} configuration for the 'create'.
   * @return A {@link ConfigurationReport} containing all details of the Process configuration, including if it was
   * successful or not.
   * @see DeviceClassConfigurationManager#createDeviceClass(String)
   */
  ConfigurationReport createDeviceClass(DeviceClass deviceClass);

  /**
   * Removes a existing 'DeviceClass' with the given id.
   *
   * @param id The id of the DeviceClass which needs to be removed.
   * @return A {@link ConfigurationReport} containing all details of the DeviceClass configuration, including if it was
   * successful or not.
   */
  ConfigurationReport removeDeviceClassById(Long id);

  /**
   * Removes a existing 'DeviceClass' with the given name.
   *
   * @param name The name of the DeviceClass which needs to be removed.
   * @return A {@link ConfigurationReport} containing all details of the DeviceClass configuration, including if it was
   * successful or not.
   */
  ConfigurationReport removeDeviceClass(String name);
}
