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
package cern.c2mon.server.common.device;

import cern.c2mon.shared.client.device.DeviceCommand;
import cern.c2mon.shared.client.device.DeviceProperty;
import cern.c2mon.shared.common.Cacheable;

import java.util.List;

/**
 * This interface describes the methods provided by a Device object used in the
 * server Device cache.
 *
 * @author Justin Lewis Salmon
 */
public interface Device extends Cacheable {

  /**
   * Retrieve the unique ID of this device.
   *
   * @return the device ID
   */
  Long getId();

  /**
   * Retrieve the name of this device.
   *
   * @return the device name
   */
  String getName();

  /**
   * Retrieve the ID of the class to which this device belongs.
   *
   * @return the device class ID
   */
  Long getDeviceClassId();

  /**
   * Retrieve the device properties of this device.
   *
   * @return the list of device properties
   */
  List<DeviceProperty> getDeviceProperties();

  /**
   * Retrieve the device commands of this device.
   *
   * @return the list of device commands
   */
  List<DeviceCommand> getDeviceCommands();

}
