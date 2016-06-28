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
package cern.c2mon.server.cache;

import java.util.List;
import java.util.Set;

import cern.c2mon.server.cache.common.ConfigurableCacheFacade;
import cern.c2mon.server.common.device.Device;
import cern.c2mon.shared.client.device.DeviceInfo;

/**
 * This interface defines the methods provided for interacting with the
 * {@link Device} cache.
 *
 * @author Justin Lewis Salmon
 */
public interface DeviceFacade extends ConfigurableCacheFacade<Device> {

  /**
   * Retrieve a list of all devices currently in the cache that belong to a
   * particular device class.
   *
   * @param deviceClassName the name of the desired device class
   * @return the list of devices
   */
  List<Device> getDevices(String deviceClassName);

  /**
   * Retrieve a list of devices currently in the cache.
   *
   * @param deviceInfoList the list of {@link DeviceInfo} objects that describe
   *          the devices to be retrieved
   * @return the list of devices that were retrieved. If any were not found,
   *         they will be omitted from the returned list.
   */
  List<Device> getDevices(Set<DeviceInfo> deviceInfoList);

  /**
   * Retrieve the name of the parent device class of a device.
   *
   * @param deviceId the id of the device
   * @return the name of the parent class of the device
   */
  String getClassNameForDevice(Long deviceId);
}
