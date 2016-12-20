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

import cern.c2mon.server.common.device.DeviceClass;

/**
 * The module public interface that should be used to access the
 * <code>DeviceClass</code>es in the server cache.
 *
 * @author Justin Lewis Salmon
 */
public interface DeviceClassCache extends C2monCacheWithListeners<Long, DeviceClass> {

  String cacheInitializedKey = "c2mon.cache.deviceclass.initialized";

  /**
   * Retrieve a particular <code>DeviceClass</code> instance from the cache by
   * specifying its name.
   *
   * @param deviceClassName the name of the device class
   * @return the <code>DeviceClass</code> instance with the specified name, or
   *         null if no instance exists in the cache
   */
  public Long getDeviceClassIdByName(String deviceClassName);

  /**
   * Update the device IDs of a device class by reloading it from the database.
   *
   * @param deviceClassId the ID of the device class to update
   */
  public void updateDeviceIds(Long deviceClassId);
}
