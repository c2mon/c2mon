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
import java.util.Arrays;
import java.util.List;

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

import cern.c2mon.server.common.device.Command;
import cern.c2mon.server.common.device.DeviceClass;
import cern.c2mon.server.common.device.DeviceClassCacheObject;
import cern.c2mon.server.common.device.Property;
import cern.c2mon.server.test.TestDataInserter;

/**
 * @author Justin Lewis Salmon
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "classpath:cern/c2mon/server/cache/dbaccess/config/server-cachedbaccess-test.xml" })
@TransactionConfiguration(transactionManager = "cacheTransactionManager", defaultRollback = true)
@Transactional
public class DeviceClassMapperTest {

  /** Component to test */
  @Autowired
  private DeviceClassMapper deviceClassMapper;

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
    DeviceClassCacheObject deviceClass1 = (DeviceClassCacheObject) deviceClassMapper.getItem(400L);
    Assert.assertNotNull(deviceClass1);
    List<String> properties = deviceClass1.getProperties();
    Assert.assertNotNull(properties);
    Assert.assertTrue(properties.size() == 4);
    Assert.assertTrue(properties.contains("cpuLoadInPercent"));
    Assert.assertTrue(properties.contains("responsiblePerson"));
    Assert.assertTrue(properties.contains("someCalculations"));
    Assert.assertTrue(properties.contains("numCores"));

    List<String> commands = deviceClass1.getCommands();
    Assert.assertNotNull(commands);
    Assert.assertTrue(commands.size() == 2);
    Assert.assertTrue(commands.contains("TEST_COMMAND_1"));
    Assert.assertTrue(commands.contains("TEST_COMMAND_2"));

    List<Long> deviceIds = deviceClass1.getDeviceIds();
    Assert.assertNotNull(deviceIds);
    Assert.assertFalse(deviceIds.isEmpty());
    Assert.assertTrue(deviceIds.contains(300L));
    Assert.assertTrue(deviceIds.contains(301L));

    DeviceClassCacheObject deviceClass2 = (DeviceClassCacheObject) deviceClassMapper.getItem(401L);
    Assert.assertNotNull(deviceClass2);
    properties = deviceClass2.getProperties();
    Assert.assertNotNull(properties);
    Assert.assertTrue(properties.size() == 4);
    Assert.assertTrue(properties.contains("TEST_PROPERTY_1"));
    Assert.assertTrue(properties.contains("TEST_PROPERTY_2"));
    Assert.assertTrue(properties.contains("TEST_PROPERTY_3"));
    Assert.assertTrue(properties.contains("TEST_PROPERTY_4"));

    commands = deviceClass2.getCommands();
    Assert.assertNotNull(commands);
    Assert.assertTrue(commands.size() == 2);
    Assert.assertTrue(commands.contains("TEST_COMMAND_3"));
    Assert.assertTrue(commands.contains("TEST_COMMAND_4"));

    deviceIds = deviceClass2.getDeviceIds();
    Assert.assertNotNull(deviceIds);
    Assert.assertFalse(deviceIds.isEmpty());
    Assert.assertTrue(deviceIds.contains(302L));
    Assert.assertTrue(deviceIds.contains(303L));
  }

  @Test
  public void testGetAll() {
    List<DeviceClass> deviceClasses = deviceClassMapper.getAll();
    Assert.assertNotNull(deviceClasses);
    Assert.assertTrue(deviceClasses.size() == 2);
  }

  @Test
  public void testInsertDeviceClass() {
    DeviceClassCacheObject deviceClass = new DeviceClassCacheObject(402L, "TEST_DEVICE_CLASS_3", "Description of TEST_DEVICE_CLASS_3");
    deviceClass.setProperties(Arrays.asList(new Property("TEST_PROPERTY_1", "Test property 1"), new Property("TEST_PROPERTY_2", "Test property 2")));
    deviceClass.setCommands(Arrays.asList(new Command("TEST_COMMAND_1", "Test command 1"), new Command("TEST_COMMAND_2", "Test command 1")));

    deviceClassMapper.insertDeviceClass(deviceClass);
    for (String property : ((DeviceClassCacheObject) deviceClass).getProperties()) {
      deviceClassMapper.insertDeviceClassProperty(deviceClass.getId(), property);
    }
    for (String command : ((DeviceClassCacheObject) deviceClass).getCommands()) {
      deviceClassMapper.insertDeviceClassCommand(deviceClass.getId(), command);
    }

    Assert.assertTrue(deviceClassMapper.isInDb(402L));
    DeviceClassCacheObject fromDb = (DeviceClassCacheObject) deviceClassMapper.getItem(402L);
    Assert.assertNotNull(fromDb);
    List<String> properties = fromDb.getProperties();
    Assert.assertNotNull(properties);
    Assert.assertTrue(properties.size() == 2);
    List<String> commands = fromDb.getCommands();
    Assert.assertNotNull(commands);
    Assert.assertTrue(commands.size() == 2);
  }

  @Test
  public void testIsInDb() {
    Assert.assertTrue(deviceClassMapper.isInDb(400L));
    Assert.assertTrue(deviceClassMapper.isInDb(401L));
  }

  @Test
  public void testIsNotInDb() {
    Assert.assertFalse(deviceClassMapper.isInDb(1L));
    Assert.assertFalse(deviceClassMapper.isInDb(402L));
  }
}
