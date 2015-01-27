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
