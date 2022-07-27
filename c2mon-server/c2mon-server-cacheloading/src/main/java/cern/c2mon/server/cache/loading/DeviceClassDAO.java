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
package cern.c2mon.server.cache.loading;

import cern.c2mon.server.common.device.DeviceClass;

/**
 * DeviceClass DAO interface.
 *
 * @author Justin Lewis Salmon
 */
public interface DeviceClassDAO extends CacheLoaderDAO<DeviceClass>, ConfigurableDAO<DeviceClass> {

  /**
   * Remove a device class.
   *
   * @param deviceClass the device class to remove
   */
  void deleteItem(DeviceClass deviceClass);

  Long getIdByName(String name);

  Long getPropertyIdByPropertyNameAndDeviceClassId(String propertyName, Long deviceClassId);

  Long getCommandIdByCommandNameAndDeviceClassId(String propertyName, Long deviceClassId);

  DeviceClass getByName(String name);

}
