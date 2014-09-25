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

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.ArrayList;
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

import cern.c2mon.server.common.device.Device;
import cern.c2mon.server.common.device.DeviceCacheObject;
import cern.c2mon.server.test.TestDataInserter;
import cern.c2mon.shared.client.device.DeviceCommand;
import cern.c2mon.shared.client.device.DeviceProperty;

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
  public void testGetItem() throws ClassNotFoundException {
    Device device1 = deviceMapper.getItem(300L); //getDevice(300L);
    Assert.assertNotNull(device1);
    List<DeviceProperty> deviceProperties = device1.getDeviceProperties();
    Assert.assertNotNull(deviceProperties);
    Assert.assertTrue(deviceProperties.size() == 4);
    assertDevicePropertyListContains(deviceProperties, new DeviceProperty("cpuLoadInPercent", 210000L, null, null, null));
    assertDevicePropertyListContains(deviceProperties, new DeviceProperty("responsiblePerson", null, null, "Mr. Administrator", null));
    assertDevicePropertyListContains(deviceProperties, new DeviceProperty("someCalculations", null, "(#123 + #234) / 2", null, "Float"));
    assertDevicePropertyListContains(deviceProperties, new DeviceProperty("numCores", null, null, "4", "Integer"));

    List<DeviceCommand> deviceCommands = device1.getDeviceCommands();
    Assert.assertNotNull(deviceCommands);
    Assert.assertTrue(deviceCommands.size() == 1);
    assertDeviceCommandEquals(new DeviceCommand("TEST_COMMAND_1", 210004L), deviceCommands.get(0));

    Device device2 = deviceMapper.getItem(301L); //getDevice(301L);
    Assert.assertNotNull(device2);
    deviceProperties = device2.getDeviceProperties();
    Assert.assertNotNull(deviceProperties);
    Assert.assertTrue(deviceProperties.size() == 1);
    assertDevicePropertyListContains(deviceProperties, new DeviceProperty("TEST_PROPERTY_1", 210001L, null, null, null));

    deviceCommands = device2.getDeviceCommands();
    Assert.assertNotNull(deviceCommands);
    Assert.assertTrue(deviceCommands.size() == 1);
    assertDeviceCommandEquals(new DeviceCommand("TEST_COMMAND_2", 210005L), deviceCommands.get(0));
  }

  public void assertDevicePropertyEquals(DeviceProperty expectedObject, DeviceProperty cacheObject) throws ClassNotFoundException {
    assertEquals(expectedObject.getName(), cacheObject.getName());
    assertEquals(expectedObject.getTagId(), cacheObject.getTagId());
    assertEquals(expectedObject.getClientRule(), cacheObject.getClientRule());
    assertEquals(expectedObject.getConstantValue(), cacheObject.getConstantValue());
    assertEquals(expectedObject.getResultType(), cacheObject.getResultType());
  }

  public void assertDevicePropertyListContains(List<DeviceProperty> deviceProperties, DeviceProperty expectedObject) throws ClassNotFoundException {
    for (DeviceProperty deviceProperty : deviceProperties) {
      if (deviceProperty.getName().equals(expectedObject.getName())) {
        assertDevicePropertyEquals(expectedObject, deviceProperty);
      }
    }
  }

  public void assertDeviceCommandEquals(DeviceCommand expectedObject, DeviceCommand cacheObject) {
    assertEquals(expectedObject.getName(), cacheObject.getName());
    assertEquals(expectedObject.getTagId(), cacheObject.getTagId());
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

    DeviceProperty dvp1 = new DeviceProperty("test_property_5", 210005L, null, null, null);
    DeviceProperty dvp2 = new DeviceProperty("test_property_6", null, null, "Mr. Administrator", null);
    DeviceProperty dvp3 = new DeviceProperty("test_property_7", null, "(#123 + #234) / 2", null, "Float");
    DeviceProperty dvp4 = new DeviceProperty("test_property_8", null, null, "4", "Integer");
    device.setDeviceProperties(new ArrayList<>(Arrays.asList(dvp1, dvp2, dvp3, dvp4)));

    DeviceCommand dvc1 = new DeviceCommand("test_command", 20L);
    device.setDeviceCommands(new ArrayList<>(Arrays.asList(dvc1)));

    deviceMapper.insertDevice(device);
    for (DeviceProperty property : ((DeviceCacheObject) device).getDeviceProperties()) {
      deviceMapper.insertDeviceProperty(device.getId(), property);
    }
    for (DeviceCommand command : ((DeviceCacheObject) device).getDeviceCommands()) {
      deviceMapper.insertDeviceCommand(device.getId(), command);
    }

    Assert.assertTrue(deviceMapper.isInDb(device.getId()));
    DeviceCacheObject fromDb = (DeviceCacheObject) deviceMapper.getItem(304L);
    Assert.assertNotNull(fromDb);
    List<DeviceProperty> properties = fromDb.getDeviceProperties();
    Assert.assertNotNull(properties);
    Assert.assertTrue(properties.size() == 4);
    List<DeviceCommand> commands = fromDb.getDeviceCommands();
    Assert.assertNotNull(commands);
    Assert.assertTrue(commands.size() == 1);
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
