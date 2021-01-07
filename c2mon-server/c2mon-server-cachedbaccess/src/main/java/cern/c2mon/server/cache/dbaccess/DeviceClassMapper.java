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

import cern.c2mon.shared.client.device.Command;
import cern.c2mon.server.common.device.DeviceClass;
import cern.c2mon.server.common.device.DeviceClassCacheObject;
import cern.c2mon.shared.client.device.Property;
import org.apache.ibatis.annotations.Param;

import java.util.List;

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
  /**
   * Retrieve the cache object with the given name.
   *
   * @param name the unique name of the cache object
   * @return a list containing the cache object if one is found, else null. The list has at most a size of 1, as device class names are unique.
   */
  List<DeviceClass> getByName(String name);

  /**
   * Retrieve the ID of a property using the property's name and the associated device class ID.
   *
   * @param name          the name of the property to retrieve. The name is unique only within the device class.
   * @param deviceClassID the id of the device class for whom the property is defined
   * @return the id of the property if one is found, else null.
   */
  Long getPropertyIdByPropertyNameAndDevClassId(@Param("property_name") String name, @Param("device_class_id") Long deviceClassID);

  /**
   * Retrieve the ID of a command using the command's name and the associated device class ID.
   *
   * @param name          the name of the command to retrieve. The name is unique only within the device class.
   * @param deviceClassID the id of the device class for whom the property is defined
   * @return the id of the command if one is found, else null.
   */
  Long getCommandIdByCommandNameAndDevClassId(@Param("command_name") String name, @Param("device_class_id") Long deviceClassID);
}
