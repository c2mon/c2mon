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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cern.c2mon.shared.client.device.ResultType;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import cern.c2mon.server.common.device.Device;
import cern.c2mon.server.common.device.DeviceCacheObject;
import cern.c2mon.shared.client.device.DeviceCommand;
import cern.c2mon.shared.client.device.DeviceProperty;

import static cern.c2mon.server.test.device.ObjectComparison.assertDeviceCommandEquals;
import static cern.c2mon.server.test.device.ObjectComparison.assertDevicePropertyListContains;

/**
 * @author Justin Lewis Salmon
 */
public class DeviceMapperTest extends AbstractMapperTest {

  /** Component to test */
  @Autowired
  private DeviceMapper deviceMapper;

  @Autowired
  private DeviceClassMapper deviceClassMapper;

  @Test
  public void testGetItem() throws ClassNotFoundException {
    Device device1 = deviceMapper.getItem(300L); // getDevice(300L);
    Assert.assertNotNull(device1);
    List<DeviceProperty> deviceProperties = device1.getDeviceProperties();
    Assert.assertNotNull(deviceProperties);
    Assert.assertTrue(deviceProperties.size() == 4);
    assertDevicePropertyListContains(deviceProperties, DeviceProperty.forTagId(1L, "cpuLoadInPercent", 210000L));
    assertDevicePropertyListContains(deviceProperties, DeviceProperty.forConstantValue(2L, "responsiblePerson", "Mr. Administrator"));
    assertDevicePropertyListContains(deviceProperties, DeviceProperty.forClientRule(3L, "someCalculations", "(#123 + #234) / 2", ResultType.Float));
    assertDevicePropertyListContains(deviceProperties, DeviceProperty.forConstantValue(4L, "numCores", 4, ResultType.Integer));

    List<DeviceCommand> deviceCommands = device1.getDeviceCommands();
    Assert.assertNotNull(deviceCommands);
    Assert.assertTrue(deviceCommands.size() == 1);
    assertDeviceCommandEquals(DeviceCommand.forCommandTagId(1L, "TEST_COMMAND_1", 210004L), deviceCommands.get(0));

    Device device2 = deviceMapper.getItem(301L); // getDevice(301L);
    Assert.assertNotNull(device2);
    deviceProperties = device2.getDeviceProperties();
    Assert.assertNotNull(deviceProperties);
    Assert.assertTrue(deviceProperties.size() == 2);
    assertDevicePropertyListContains(deviceProperties, DeviceProperty.forTagId(5L, "TEST_PROPERTY_1", 210001L));
    assertDevicePropertyListContains(deviceProperties, DeviceProperty.forMappedProperty(9L, "TEST_PROPERTY_WITH_FIELDS", null));

    List<DeviceProperty> expectedFields = new ArrayList<>();
    expectedFields.add(DeviceProperty.forTagId(1L, "FIELD_CPULOAD",  210008L));
    expectedFields.add(DeviceProperty.forConstantValue(2L, "FIELD_RESPONSIBLE_PERSON", "Mr. Administrator"));
    expectedFields.add(DeviceProperty.forClientRule(3L, "FIELD_SOME_CALCULATIONS", "(#123 + #234) / 2", ResultType.Float));
    expectedFields.add(DeviceProperty.forConstantValue(4L, "FIELD_NUM_CORES", 2, ResultType.Integer));

    DeviceProperty expectedMappedProperty = DeviceProperty.forMappedProperty(9L, "TEST_PROPERTY_WITH_FIELDS", expectedFields);
    assertDevicePropertyListContains(deviceProperties, expectedMappedProperty);

    deviceCommands = device2.getDeviceCommands();
    Assert.assertNotNull(deviceCommands);
    Assert.assertTrue(deviceCommands.size() == 1);
    assertDeviceCommandEquals(DeviceCommand.forCommandTagId(2L, "TEST_COMMAND_2", 210005L), deviceCommands.get(0));
  }

  @Test
  public void testGetAll() {
    List<Device> devices = deviceMapper.getAll();
    Assert.assertNotNull(devices);
    Assert.assertTrue(devices.size() == 4);
  }

  @Test
  public void testInsertDevice() throws ClassNotFoundException {
    DeviceCacheObject device = new DeviceCacheObject(304L, "TEST_DEVICE_5", 400L);

    DeviceProperty dvp1 = DeviceProperty.forTagId(1L, "cpuLoadInPercent", 210005L);
    DeviceProperty dvp2 = DeviceProperty.forConstantValue(2L, "responsiblePerson", "Mr. Administrator");
    DeviceProperty dvp3 = DeviceProperty.forClientRule(3L, "someCalculations", "(#123 + #234) / 2", ResultType.Float);
    DeviceProperty dvp4 = DeviceProperty.forConstantValue(4L, "numCores", 4, ResultType.Integer);

    List<DeviceProperty> fields = new ArrayList<>();
    fields.add(DeviceProperty.forTagId(1L, "FIELD_CPULOAD", 210008L));
    fields.add(DeviceProperty.forConstantValue(2L, "FIELD_RESPONSIBLE_PERSON", "Mr. Administrator"));
    fields.add(DeviceProperty.forClientRule(3L, "FIELD_SOME_CALCULATIONS", "(#123 + #234) / 2", ResultType.Float));
    fields.add(DeviceProperty.forConstantValue(4L, "FIELD_NUM_CORES", 2, ResultType.Integer));
    DeviceProperty dvp5 = DeviceProperty.forMappedProperty(9L, "TEST_PROPERTY_WITH_FIELDS", fields);
    List<DeviceProperty> properties = new ArrayList<>(Arrays.asList(dvp1, dvp2, dvp3, dvp4, dvp5));

    device.setDeviceProperties(properties);

    DeviceCommand dvc1 = DeviceCommand.forCommandTagId(1L, "TEST_COMMAND_1", 20L);
    device.setDeviceCommands(new ArrayList<>(Arrays.asList(dvc1)));

    deviceMapper.insertDevice(device);
    for (DeviceProperty property : device.getDeviceProperties()) {
      deviceMapper.insertDeviceProperty(device.getId(), property);
    }
    for (DeviceCommand command : device.getDeviceCommands()) {
      deviceMapper.insertDeviceCommand(device.getId(), command);
    }

    Assert.assertTrue(deviceMapper.isInDb(device.getId()));
    DeviceCacheObject fromDb = (DeviceCacheObject) deviceMapper.getItem(304L);
    Assert.assertNotNull(fromDb);
    List<DeviceProperty> propertiesFromDb = fromDb.getDeviceProperties();
    Assert.assertNotNull(propertiesFromDb);
    Assert.assertTrue(propertiesFromDb.size() == 5);

    for (DeviceProperty property : properties) {
      assertDevicePropertyListContains(propertiesFromDb, property);

      if (property.getFields() != null && !property.getFields().isEmpty()) {
        for (DeviceProperty field : property.getFields().values()) {
          assertDevicePropertyListContains(new ArrayList<>(property.getFields().values()), field);
        }
      }
    }

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
