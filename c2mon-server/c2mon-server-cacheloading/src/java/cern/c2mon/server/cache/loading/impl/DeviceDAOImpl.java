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

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.ibatis.session.SqlSession;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cern.c2mon.server.cache.dbaccess.DeviceMapper;
import cern.c2mon.server.cache.loading.DeviceDAO;
import cern.c2mon.server.cache.loading.common.AbstractDefaultLoaderDAO;
import cern.c2mon.server.common.device.Device;
import cern.c2mon.server.common.device.DeviceCacheObject;
import cern.c2mon.shared.client.device.CommandValue;
import cern.c2mon.shared.client.device.PropertyValue;

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
  private SqlSession sqlSession;

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
  public Device getItem(Object id) {
    // TODO make MyBatis mapper to to this directly...
    DeviceCacheObject device = (DeviceCacheObject) sqlSession.selectOne("cern.c2mon.server.cache.dbaccess.DeviceMapper.getItem", id);
    List<PropertyValue> propertyValues = sqlSession.selectList("cern.c2mon.server.cache.dbaccess.DeviceMapper.getPropertyValuesForDevice", id);
    List<CommandValue> commandValueList = sqlSession.selectList("cern.c2mon.server.cache.dbaccess.DeviceMapper.getCommandValuesForDevice", id);
    device.setPropertyValues(propertyValues);
    device.setCommandValues(commandValueList);
    return device;
  }

  @Override
  public Map<Long, Device> getAllAsMap() {
    List<Device> cacheableList = deviceMapper.getAll();
    for (Device device : cacheableList) {
      List<PropertyValue> propertyValues = sqlSession.selectList("cern.c2mon.server.cache.dbaccess.DeviceMapper.getPropertyValuesForDevice", device.getId());
      List<CommandValue> commandValueList = sqlSession.selectList("cern.c2mon.server.cache.dbaccess.DeviceMapper.getCommandValuesForDevice", device.getId());

      ((DeviceCacheObject) device).setPropertyValues(propertyValues);
      ((DeviceCacheObject) device).setCommandValues(commandValueList);
    }

    ConcurrentHashMap<Long, Device> returnMap = new ConcurrentHashMap<>(2000);
    Iterator<Device> it = cacheableList.iterator();
    Device current;
    while (it.hasNext()) {
      current = it.next();
      if (current != null) {
        returnMap.put(current.getId(), doPostDbLoading(current));
      } else {
        LOGGER.warn("Null value retrieved from DB by Mapper " + deviceMapper.getClass().getSimpleName());
      }
    }
    return returnMap;
  }

  @Override
  public void deleteItem(Long id) {
    deviceMapper.deletePropertyValues(id);
    deviceMapper.deleteCommandValues(id);
    deviceMapper.deleteDevice(id);
  }

  @Override
  public void updateConfig(Device device) {
    deviceMapper.updateDeviceConfig(device);
  }

  @Override
  public void insert(Device device) {
    deviceMapper.insertDevice(device);
  }
}
