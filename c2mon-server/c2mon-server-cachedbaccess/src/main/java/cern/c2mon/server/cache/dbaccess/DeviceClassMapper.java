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

import cern.c2mon.server.common.device.Command;
import cern.c2mon.server.common.device.DeviceClass;
import cern.c2mon.server.common.device.DeviceClassCacheObject;
import cern.c2mon.shared.client.device.Property;
import org.apache.ibatis.annotations.Param;

/**
 * MyBatis mapper for for accessing and updating {@link DeviceClassCacheObject}s
 * in the cache database.
 *
 * @author Justin Lewis Salmon
 */
public interface DeviceClassMapper extends LoaderMapper<DeviceClass>, PersistenceMapper<DeviceClass> {

  /**
   * Insert a device class object from the cache into the db.
   *
   * @param deviceClass the device class cache object to insert
   */
  void insertDeviceClass(DeviceClass deviceClass);

  /**
   * Insert a property of a device class into the DB.
   *
   * @param id the ID of the device class to which this property belongs
   * @param property the property to insert
   */
  void insertDeviceClassProperty(@Param("id") Long id, @Param("property") Property property);

  /**
   * Insert a command of a device class into the DB.
   *
   * @param id the ID of the device class to which this command belongs
   * @param command the command to insert
   */
  void insertDeviceClassCommand(@Param("id") Long id, @Param("command") Command command);

  /**
   * Insert a field of a property into the DB.
   *
   * @param id the ID of the property to which this field belongs
   * @param field the field to insert
   */
  void insertDeviceClassField(@Param("id") Long id, @Param("field") Property field);

  /**
   * Delete a device class object from the db.
   *
   * @param id the ID of the device class object to be deleted
   */
  void deleteDeviceClass(Long id);

  /**
   * Update a device object in the db.
   *
   * @param deviceClass the device class object to be updated
   */
  void updateDeviceClassConfig(DeviceClass deviceClass);

  /**
   * Delete all properties belonging to a particular device class.
   *
   * @param id the ID of the device class from which to delete properties
   */
  void deleteProperties(Long id);

  /**
   * Delete all fields belonging to a particular device class.
   *
   * @param id the ID of the device class from which to delete fields
   */
  void deleteFields(Long id);

  /**
   * Delete all commands belonging to a particular device class.
   *
   * @param id the ID of the device class from which to delete commands
   */
  void deleteCommands(Long id);


  /**
   * Retrieve the id of the cache object with the given name.
   *
   * @param name the unique name of the cache object
   * @return the id of the cache object
   */
  Long getIdByName(String name);

  Long getPropertyIdByNameAndDeviceClassId(@Param("name") String name, @Param("device_class_id") Long deviceClassID);
}
