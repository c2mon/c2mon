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

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.SqlSession;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import cern.c2mon.server.common.device.CommandValue;
import cern.c2mon.server.common.device.Device;
import cern.c2mon.server.common.device.DeviceCacheObject;
import cern.c2mon.server.common.device.PropertyValue;
import cern.c2mon.server.test.TestDataInserter;

/**
 * @author Justin Lewis Salmon
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "classpath:cern/c2mon/server/cache/dbaccess/config/server-cachedbaccess-test.xml" })
@TransactionConfiguration(transactionManager = "cacheTransactionManager", defaultRollback = true)
@Transactional
public class DeviceMapperTest {

  /** Component to test */
  @Autowired
  private DeviceMapper deviceMapper;

  @Autowired
  private SqlSession sqlSession;

  @Autowired
  TestDataInserter testDataInserter;

  @Before
  public void insertTestData() throws IOException {
    testDataInserter.removeTestData();
    testDataInserter.insertTestData();
  }

  @After
  public void removeTestData() throws IOException {
    testDataInserter.removeTestData();
  }

  @Test
  public void testGetItem() {
    Device device1 = getDevice(300L);
    Assert.assertNotNull(device1);
    Map<String, Long> propertyValues = device1.getPropertyValues();
    Assert.assertNotNull(propertyValues);
    Assert.assertTrue(propertyValues.size() == 2);
    Assert.assertTrue(propertyValues.get("TEST_PROPERTY_1") == 210000);
    Assert.assertTrue(propertyValues.get("TEST_PROPERTY_5") == 210007);

    Map<String, Long> commandValues = device1.getCommandValues();
    Assert.assertNotNull(commandValues);
    Assert.assertTrue(commandValues.size() == 1);
    Assert.assertTrue(commandValues.get("TEST_COMMAND_1") == 210004);

    Device device2 = getDevice(301L);
    Assert.assertNotNull(device2);
    propertyValues = device2.getPropertyValues();
    Assert.assertNotNull(propertyValues);
    Assert.assertTrue(propertyValues.size() == 1);
    Assert.assertTrue(propertyValues.get("TEST_PROPERTY_2") == 210001);

    commandValues = device2.getCommandValues();
    Assert.assertNotNull(commandValues);
    Assert.assertTrue(commandValues.size() == 1);
    Assert.assertTrue(commandValues.get("TEST_COMMAND_2") == 210005);
  }

  public Device getDevice(Long id) {
    // Normally the DAO handles this hacky property value access
    DeviceCacheObject device = (DeviceCacheObject) sqlSession.selectOne("cern.c2mon.server.cache.dbaccess.DeviceMapper.getItem", id);
    List<PropertyValue> propertyValueList = sqlSession.selectList("cern.c2mon.server.cache.dbaccess.DeviceMapper.getPropertyValuesForDevice", id);
    List<CommandValue> commandValueList = sqlSession.selectList("cern.c2mon.server.cache.dbaccess.DeviceMapper.getCommandValuesForDevice", id);
    device.setPropertyValues(propertyValueList);
    device.setCommandValues(commandValueList);
    return device;
  }

  @Test
  public void testGetAll() {
    List<Device> devices = deviceMapper.getAll();
    Assert.assertNotNull(devices);
    Assert.assertTrue(devices.size() == 4);
  }

  @Test
  public void testInsertDevice() {
    DeviceCacheObject device = new DeviceCacheObject(304L, "TEST_DEVICE_5", 400L);
    deviceMapper.insertDevice(device);
    Assert.assertTrue(deviceMapper.isInDb(device.getId()));
  }

  @Test
  public void testIsInDb() {
    Assert.assertTrue(deviceMapper.isInDb(300L));
    Assert.assertTrue(deviceMapper.isInDb(301L));
  }

  @Test
  public void testIsNotInDb() {
    Assert.assertFalse(deviceMapper.isInDb(1L));
  }
}
