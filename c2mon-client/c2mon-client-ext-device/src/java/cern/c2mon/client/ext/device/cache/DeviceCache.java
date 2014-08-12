/*******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 *
 * Copyright (C) 2004 - 2014 CERN. This program is free software; you can
 * redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version. This program is distributed
 * in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received
 * a copy of the GNU General Public License along with this program; if not,
 * write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA 02111-1307, USA.
 *
 * Author: TIM team, tim.support@cern.ch
 ******************************************************************************/
package cern.c2mon.client.ext.device.cache;

import java.util.List;

import cern.c2mon.client.ext.device.Device;

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
