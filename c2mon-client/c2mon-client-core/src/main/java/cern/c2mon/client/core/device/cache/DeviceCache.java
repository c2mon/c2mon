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
package cern.c2mon.client.core.device.cache;

import java.util.List;

import cern.c2mon.client.core.device.Device;

/**
 * This interface describes that methods which are provided by the C2MON client
 * device cache.
 *
 * @author Justin Lewis Salmon
 */
public interface DeviceCache {

  /**
   * Add a device to the cache.
   *
   * @param device the device to add
   */
  public void add(Device device);

  /**
   * Retrieve a device from the cache.
   *
   * @param deviceId the ID of the device to retrieve
   * @return the requested device if it exists in the cache, null otherwise
   */
  public Device get(Long deviceId);

  /**
   * Retrieve a device from the cache by name
   *
   * @param deviceName the name of the device to retrieve
   * @return the requested device if it exists in the cache, null otherwise
   */
  public Device get(String deviceName);

  /**
   * @return a list of all devices currently in the cache
   */
  public List<Device> getAllDevices();

  /**
   * Retrieve all devices from the cache that are of a particular class.
   *
   * @param deviceClassName the device class name of the devices you wish to
   *          retrieve
   * @return a list containing all devices of the requested class
   */
  public List<Device> getAllDevices(String deviceClassName);

  /**
   * Remove a device from the cache.
   *
   * @param device the device to remove from the cache
   */
  public void remove(Device device);

}
