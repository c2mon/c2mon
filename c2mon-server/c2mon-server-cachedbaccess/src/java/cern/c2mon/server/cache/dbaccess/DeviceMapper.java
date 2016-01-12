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
package cern.c2mon.server.cache.dbaccess;

import org.apache.ibatis.annotations.Param;

import cern.c2mon.server.common.device.Device;
import cern.c2mon.server.common.device.DeviceCacheObject;
import cern.c2mon.shared.client.device.DeviceCommand;
import cern.c2mon.shared.client.device.DeviceProperty;

/**
 * MyBatis mapper for for accessing and updating {@link DeviceCacheObject}s in
 * the cache database.
 *
 * @author Justin Lewis Salmon
 */
public interface DeviceMapper extends LoaderMapper<Device> {

  /**
   * Insert a device object from the cache into the db.
   *
   * @param device the device object to insert
   */
  void insertDevice(Device device);

  /**
   * Insert a property of a device into the DB.
   *
   * @param id the ID of the device to which this property belongs
   * @param property the property to insert
   */
  void insertDeviceProperty(@Param("id") Long id, @Param("property") DeviceProperty property);

  /**
   * Insert a field of a property into the DB.
   *
   * @param propertyId the ID of the property to which this field belongs
   * @param deviceId the ID of the device to which this field belongs
   * @param field the field to insert
   */
  void insertPropertyField(@Param("propertyId") Long propertyId, @Param("deviceId") Long deviceId, @Param("field") DeviceProperty field);

  /**
   * Insert a command of a device into the DB.
   *
   * @param id the ID of the device to which this command belongs
   * @param command the command to insert
   */
  void insertDeviceCommand(@Param("id") Long id, @Param("command") DeviceCommand command);

  /**
   * Delete a device object from the db.
   *
   * @param id the ID of the Device object to be deleted
   */
  void deleteDevice(Long id);

  /**
   * Update a device object in the db.
   *
   * @param device the Device object to be updated
   */
  void updateDeviceConfig(Device device);

  /**
   * Delete all properties belonging to a device.
   *
   * @param id the ID of the device from which to delete properties
   */
  void deleteDeviceProperties(Long id);

  /**
   * Delete all commands belonging to a device.
   *
   * @param id the ID of the device from which to delete commands
   */
  void deleteDeviceCommands(Long id);

  /**
   * Delete all fields belonging to a device.
   *
   * @param id the ID of the device from which to delete commands
   */
  void deletePropertyFields(Long id);
}
