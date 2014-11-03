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
