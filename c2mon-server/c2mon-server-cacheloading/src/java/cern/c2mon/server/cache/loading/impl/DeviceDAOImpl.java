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
package cern.c2mon.server.cache.loading.impl;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cern.c2mon.server.cache.dbaccess.DeviceMapper;
import cern.c2mon.server.cache.loading.DeviceDAO;
import cern.c2mon.server.cache.loading.common.AbstractDefaultLoaderDAO;
import cern.c2mon.server.common.device.Device;
import cern.c2mon.server.common.device.DeviceCacheObject;
import cern.c2mon.shared.client.device.DeviceCommand;
import cern.c2mon.shared.client.device.DeviceProperty;

/**
 * Device loader DAO implementation.
 *
 * @author Justin Lewis Salmon
 */
@Service("deviceDAO")
public class DeviceDAOImpl extends AbstractDefaultLoaderDAO<Device> implements DeviceDAO {

  private static Logger LOGGER = Logger.getLogger(DeviceDAOImpl.class);

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

    for (DeviceProperty property : ((DeviceCacheObject) device).getDeviceProperties()) {
      deviceMapper.insertDeviceProperty(device.getId(), property);
    }

    for (DeviceCommand command : ((DeviceCacheObject) device).getDeviceCommands()) {
      deviceMapper.insertDeviceCommand(device.getId(), command);
    }
  }
}
