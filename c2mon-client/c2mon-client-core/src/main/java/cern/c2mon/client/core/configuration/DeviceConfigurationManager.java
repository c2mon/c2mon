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
import cern.c2mon.shared.client.configuration.api.device.Device;

/**
 * The DeviceConfigurationManager allows to apply create, update and delete configurations for Devices.
 */
public interface DeviceConfigurationManager {

  /**
   * Creates a new 'Device' on the server for the given name and 'DeviceClass' name.
   * <p>
   * The Device is created with default parameters.
   *
   * @param deviceName The name of the device to be created.
   * @param deviceClassName The name of the device class that the device shall belong to.
   * @return A {@link ConfigurationReport} containing all details of the Device configuration, including if it was
   * successful or not.
   * @see DeviceConfigurationManager#createDevice(Device)
   */
  ConfigurationReport createDevice(String deviceName, String deviceClassName);
  /**
   * Creates a new 'Device' on the server for the given name and 'DeviceClass' ID.
   * <p>
   * The Device is created with default parameters.
   *
   * @param deviceName The name of the device to be created.
   * @param deviceClassId The id of the device class that the device shall belong to.
   * @return A {@link ConfigurationReport} containing all details of the Device configuration, including if it was successful or not.
   * @see DeviceConfigurationManager#createDevice(Device)
   */
  ConfigurationReport createDevice(String deviceName, long deviceClassId);

  /**
   * Creates a new 'Device' with the given parameters in the {@link Device} object.
   * <p>
   * Next to the specified parameters the Device is created with default parameters.
   * <p>
   * Note: You have to use {@link Device#create(String)} to instantiate the parameter of this method.
   *
   * @param device The {@link Device} configuration for the 'create'.
   * @return A {@link ConfigurationReport} containing all details of the Device configuration, including if it was
   * successful or not.
   * @see DeviceConfigurationManager#createDevice(String, String)
   */
  ConfigurationReport createDevice(Device device);

  /**
   * Removes a existing 'Device' with the given id.
   *
   * @param id The id of the Device which needs to be removed.
   * @return A {@link ConfigurationReport} containing all details of the Device configuration, including if it was
   * successful or not.
   */
  ConfigurationReport removeDeviceById(Long id);

  /**
   * Removes a existing 'Device' with the given name.
   *
   * @param name The name of the Device which needs to be removed.
   * @return A {@link ConfigurationReport} containing all details of the Device configuration, including if it was
   * successful or not.
   */
  ConfigurationReport removeDevice(String name);
}
