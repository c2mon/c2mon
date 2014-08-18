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
package cern.c2mon.server.cache.device;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import cern.c2mon.server.cache.DeviceCache;
import cern.c2mon.server.cache.DeviceClassCache;
import cern.c2mon.server.cache.DeviceFacade;
import cern.c2mon.server.common.device.Device;
import cern.c2mon.server.common.device.DeviceClass;
import cern.c2mon.server.common.device.DeviceClassCacheObject;

/**
 * Implementation of the Device facade which defines the methods provided for
 * interacting with {@link Device} and {@link DeviceClass} caches.
 *
 * @author Justin Lewis Salmon
 */
public class DeviceFacadeImpl implements DeviceFacade {

  /**
   * Reference to the <code>Device</code> cache
   */
  private DeviceCache deviceCache;

  /**
   * Reference to the <code>DeviceClass</code> cache.
   */
  private DeviceClassCache deviceClassCache;

  /**
   * Default constructor used by Spring to autowire the device and device class
   * cache beans.
   *
   * @param pDeviceCache reference to the Device cache bean
   * @param pDeviceClassCache reference to the DeviceClass cache bean
   */
  @Autowired
  public DeviceFacadeImpl(final DeviceCache pDeviceCache, final DeviceClassCache pDeviceClassCache) {
    deviceCache = pDeviceCache;
    deviceClassCache = pDeviceClassCache;
  }

  @Override
  public List<String> getDeviceClassNames() {
    List<String> classNames = new ArrayList<>();

    for (Long deviceClassId : deviceClassCache.getKeys()) {
      DeviceClass deviceClass = deviceClassCache.get(deviceClassId);
      classNames.add(deviceClass.getName());
    }

    return classNames;
  }

  @Override
  public List<Device> getDevices(String deviceClassName) {
    List<Device> devices = new ArrayList<>();

    // Search the name attribute of the class cache
    DeviceClassCacheObject deviceClass = (DeviceClassCacheObject) deviceClassCache.getDeviceClassByName(deviceClassName);
    List<Long> deviceIds = deviceClass.getDeviceIds();

    for (Long deviceId : deviceIds) {
      devices.add(deviceCache.getCopy(deviceId));
    }

    return devices;
  }
}
