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
package cern.c2mon.cache.loading.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cern.c2mon.cache.loading.DeviceDAO;
import cern.c2mon.cache.loading.common.AbstractDefaultLoaderDAO;
import cern.c2mon.server.cache.dbaccess.DeviceMapper;
import cern.c2mon.server.common.device.Device;
import cern.c2mon.shared.client.device.DeviceCommand;
import cern.c2mon.shared.client.device.DeviceProperty;

/**
 * Device loader DAO implementation.
 *
 * @author Justin Lewis Salmon
 */
@Slf4j
@Service("deviceDAO")
public class DeviceDAOImpl extends AbstractDefaultLoaderDAO<Device> implements DeviceDAO {

  /**
   * Reference to the MyBatis Device mapper.
   */
  private DeviceMapper deviceMapper;

  @Autowired
  public DeviceDAOImpl(final DeviceMapper deviceMapper) {
    super(2000, deviceMapper);
    this.deviceMapper = deviceMapper;
  }

  @Override
  protected Device doPostDbLoading(Device item) {
    return item;
  }

  @Override
  public void deleteItem(Long id) {
    deviceMapper.deletePropertyFields(id);
    deviceMapper.deleteDeviceProperties(id);
    deviceMapper.deleteDeviceCommands(id);
    deviceMapper.deleteDevice(id);
  }

  @Override
  public void updateConfig(Device device) {
    deviceMapper.updateDeviceConfig(device);
  }

  @Override
  public void insert(Device device) {
    deviceMapper.insertDevice(device);

    for (DeviceProperty property : device.getDeviceProperties()) {
      deviceMapper.insertDeviceProperty(device.getId(), property);
    }

    for (DeviceCommand command : device.getDeviceCommands()) {
      deviceMapper.insertDeviceCommand(device.getId(), command);
    }
  }
}
