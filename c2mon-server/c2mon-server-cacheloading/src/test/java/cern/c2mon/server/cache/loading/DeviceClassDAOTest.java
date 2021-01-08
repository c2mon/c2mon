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

import java.util.List;

import cern.c2mon.server.cache.dbaccess.config.CacheDbAccessModule;
import cern.c2mon.server.cache.loading.config.CacheLoadingModule;
import cern.c2mon.server.common.config.CommonModule;
import cern.c2mon.server.test.DatabasePopulationRule;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import cern.c2mon.server.common.device.DeviceClass;
import cern.c2mon.server.common.device.DeviceClassCacheObject;

import static org.junit.Assert.*;

/**
 * @author Justin Lewis Salmon
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
    CommonModule.class,
    CacheDbAccessModule.class,
    CacheLoadingModule.class,
    DatabasePopulationRule.class
})
public class DeviceClassDAOTest {

  @Rule
  @Autowired
  public DatabasePopulationRule databasePopulationRule;

  /** Component to test */
  @Autowired
  DeviceClassDAO deviceClassDAO;

  @Test
  public void testGetItem() {
    DeviceClassCacheObject device1 = (DeviceClassCacheObject) deviceClassDAO.getItem(400L);
    assertNotNull(device1);
    List<Long> deviceIds = device1.getDeviceIds();
    Assert.assertFalse(deviceIds.isEmpty());

    DeviceClass device2 = deviceClassDAO.getItem(401L);
    assertNotNull(device2);
  }
//  @Test
//  public void testGetByName() {
//    String name = "TEST_DEVICE_CLASS_1";
//    DeviceClassCacheObject device1 = (DeviceClassCacheObject) deviceClassDAO.getByName(name);
//    assertNotNull(device1);
//    assertEquals(name, device1.getName());
//    List<Long> deviceIds = device1.getDeviceIds();
//    Assert.assertFalse(deviceIds.isEmpty());
//
//    name = "TEST_DEVICE_CLASS_2";
//    DeviceClass device2 = deviceClassDAO.getByName(name);
//    assertNotNull(device2);
//    assertEquals(name, device2.getName());
//  }
//
//
//  @Test
//  public void getByNameShouldReturnNullIfNoObjectExists() {
//    DeviceClassCacheObject device1 = (DeviceClassCacheObject) deviceClassDAO.getByName("DOES_NOT_EXIST");
//    assertNull(device1);
//  }
//
//  @Test
//  public void testGetIdByName() {
//    long deviceId1 = deviceClassDAO.getIdByName("TEST_DEVICE_CLASS_1");
//    assertEquals(400L, deviceId1);
//
//    long deviceId2 = deviceClassDAO.getIdByName("TEST_DEVICE_CLASS_2");
//    assertEquals(401L, deviceId2);
//  }
//
//
//  @Test
//  public void getIdByNameShouldReturnNullIfNoObjectExists() {
//    Long deviceId1 = deviceClassDAO.getIdByName("DOES_NOT_EXIST");
//    assertNull(deviceId1);
//  }
//
//  @Test
//  public void getPropertyIdByNameAndDeviceClassId() {
//    DeviceClassCacheObject device1 = (DeviceClassCacheObject) deviceClassDAO.getItem(400L);
//    assertNotNull(device1);
//    List<Long> deviceIds = device1.getDeviceIds();
//    Assert.assertFalse(deviceIds.isEmpty());
//
//    DeviceClass device2 = deviceClassDAO.getItem(401L);
//    assertNotNull(device2);
//  }

}
