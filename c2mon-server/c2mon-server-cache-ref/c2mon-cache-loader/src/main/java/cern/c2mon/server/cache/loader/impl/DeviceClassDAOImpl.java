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
package cern.c2mon.server.cache.loader.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cern.c2mon.server.cache.dbaccess.DeviceClassMapper;
import cern.c2mon.server.cache.loader.DeviceClassDAO;
import cern.c2mon.server.cache.loader.common.AbstractDefaultLoaderDAO;
import cern.c2mon.server.common.device.Command;
import cern.c2mon.server.common.device.DeviceClass;
import cern.c2mon.server.common.device.DeviceClassCacheObject;
import cern.c2mon.server.common.device.Property;

/**
 * DeviceClass loader DAO implementation.
 *
 * @author Justin Lewis Salmon
 */
//TODO: refer a name
@Service("deviceClassDAORef")
public class DeviceClassDAOImpl extends AbstractDefaultLoaderDAO<DeviceClass> implements DeviceClassDAO {

  /**
   * Reference to the DeviceClass MyBatis loader.
   */
  private DeviceClassMapper deviceClassMapper;

  @Autowired
  public DeviceClassDAOImpl(final DeviceClassMapper deviceClassMapper) {
    super(2000, deviceClassMapper);
    this.deviceClassMapper = deviceClassMapper;
  }

  @Override
  public DeviceClass getItem(Object id) {
    return deviceClassMapper.getItem(id);
  }

  @Override
  protected DeviceClass doPostDbLoading(DeviceClass item) {
    return item;
  }

  @Override
  public void deleteItem(DeviceClass deviceClass) {
    for (Long propertyId : deviceClass.getPropertyIds()) {
      deviceClassMapper.deleteFields(propertyId);
    }

    deviceClassMapper.deleteProperties(deviceClass.getId());
    deviceClassMapper.deleteCommands(deviceClass.getId());
    deviceClassMapper.deleteDeviceClass(deviceClass.getId());
  }

  @Override
  public void updateConfig(DeviceClass deviceClass) {
    deviceClassMapper.updateDeviceClassConfig(deviceClass);
  }

  @Override
  public void insert(DeviceClass deviceClass) {
    deviceClassMapper.insertDeviceClass(deviceClass);

    for (Property property : ((DeviceClassCacheObject) deviceClass).getProperties()) {
      deviceClassMapper.insertDeviceClassProperty(deviceClass.getId(), property);

      if (property.getFields() != null) {
        for (Property field : property.getFields()) {
          deviceClassMapper.insertDeviceClassField(property.getId(), field);
        }
      }
    }

    for (Command command : ((DeviceClassCacheObject) deviceClass).getCommands()) {
      deviceClassMapper.insertDeviceClassCommand(deviceClass.getId(), command);
    }
  }

  @Override
  public void deleteItem(Long id) {
  }
}
