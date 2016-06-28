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
package cern.c2mon.shared.client.device;

import java.util.List;

import cern.c2mon.shared.client.request.ClientRequestResult;

/**
 * This interface defines the transport object that is transferred to the client
 * layer for initialising a given <code>Device</code> object and its
 * corresponding mapping between properties and data tag IDs.
 *
 * @author Justin Lewis Salmon
 */
public interface TransferDevice extends ClientRequestResult {

  /**
   * Retrieve the unique device ID.
   *
   * @return the device ID
   */
  public Long getId();

  /**
   * Retrieve the device name.
   *
   * @return the device name
   */
  public String getName();

  /**
   * Retrieve the ID of the device class.
   *
   * @return the device class ID
   */
  public Long getDeviceClassId();

  /**
   * Retrieve the name of the class to which this device belongs.
   *
   * @return the class name
   */
  public String getDeviceClassName();

  /**
   * Retrieve the properties of this device.
   *
   * @return the list of device properties
   */
  public List<DeviceProperty> getDeviceProperties();

  /**
   * Retrieve the commands of this device.
   *
   * @return the list of device commands
   */
  public List<DeviceCommand> getDeviceCommands();
}
