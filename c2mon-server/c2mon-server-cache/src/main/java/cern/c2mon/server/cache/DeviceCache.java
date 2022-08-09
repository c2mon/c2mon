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

import cern.c2mon.server.common.device.Device;

/**
 * The module public interface that should be used to access the
 * <code>Device</code>s in the server cache.
 *
 * @author Justin Lewis Salmon
 */
public interface DeviceCache extends C2monCacheWithListeners<Long, Device> {

  String cacheInitializedKey = "c2mon.cache.device.initialized";

  List<Device> getByDeviceClassId(Long deviceClassId);

  /**
   * Retrieve a particular <code>Device</code> instance from the cache by
   * specifying its name.
   *
   * @param deviceName the name of the device
   * @return the corresponding device ID, or null if no instance exists in the cache
   */
  Long getDeviceIdByName(String deviceName);
}
