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
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static cern.c2mon.server.test.device.ObjectComparison.assertCommandListContains;
import static cern.c2mon.server.test.device.ObjectComparison.assertPropertyListContains;

/**
 * @author Justin Lewis Salmon
 */

public class DeviceClassMapperTest extends AbstractMapperTest {

  /** Component to test */
  @Autowired
  private DeviceClassMapper deviceClassMapper;

  @Test
  public void testGetItem() throws ClassNotFoundException {
    DeviceClassCacheObject deviceClass1 = (DeviceClassCacheObject) deviceClassMapper.getItem(400L);
    testDeviceClass1Completeness(deviceClass1);

    DeviceClassCacheObject deviceClass2 = (DeviceClassCacheObject) deviceClassMapper.getItem(401L);
    testDeviceClass2Completeness(deviceClass2);
  }

  @Test
  public void getByName() throws ClassNotFoundException {
    List<DeviceClass> deviceClasses = deviceClassMapper.getByName("TEST_DEVICE_CLASS_1");
    Assert.assertEquals(1, deviceClasses.size());
    Assert.assertTrue(deviceClasses.get(0) instanceof DeviceClassCacheObject);
    testDeviceClass1Completeness((DeviceClassCacheObject) deviceClasses.get(0));

    deviceClasses = deviceClassMapper.getByName("TEST_DEVICE_CLASS_2");
    Assert.assertEquals(1, deviceClasses.size());
    Assert.assertTrue(deviceClasses.get(0) instanceof DeviceClassCacheObject);
    testDeviceClass2Completeness((DeviceClassCacheObject) deviceClasses.get(0));
  }


  @Test
  public void getByNameShouldReturnEmptyListIfClassDoesNotExist() {
    List<DeviceClass> deviceClasses = deviceClassMapper.getByName("DOES_NOT_EXIST");
    Assert.assertTrue(deviceClasses.isEmpty());
  }

  @Test
  public void testGetAll() {
    List<DeviceClass> deviceClasses = deviceClassMapper.getAll();
    Assert.assertNotNull(deviceClasses);
    Assert.assertTrue(deviceClasses.size() == 2);

    for (DeviceClass deviceClass : deviceClasses) {
      Assert.assertFalse(deviceClass.getProperties().size() == 0);
    }
  }

  @Test
  public void testInsertDeviceClass() throws ClassNotFoundException {
    DeviceClassCacheObject deviceClass = new DeviceClassCacheObject(402L, "TEST_DEVICE_CLASS_3", "Description of TEST_DEVICE_CLASS_3");

    List<Property> properties = new ArrayList<>();

    properties.add(new Property(10L, "TEST_PROPERTY_1", "Test property 1"));
    properties.add(new Property(11L, "TEST_PROPERTY_2", "Test property 2"));

    List<Property> fields = new ArrayList<>();
    fields.add(new Property(12L, "TEST_FIELD_1", null));
    fields.add(new Property(13L, "TEST_FIELD_2", null));

    properties.add(new Property(14L, "TEST_PROPERTY_WITH_FIELDS", "Test property with fields", fields));

    deviceClass.setProperties(properties);

    deviceClass.setCommands(Arrays.asList(new Command(10L, "TEST_COMMAND_1", "Test command 1"), new Command(11L, "TEST_COMMAND_2", "Test command 2")));

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

    Assert.assertTrue(deviceClassMapper.isInDb(402L));
    DeviceClassCacheObject fromDb = (DeviceClassCacheObject) deviceClassMapper.getItem(402L);
    Assert.assertNotNull(fromDb);
    List<Property> retrievedProperties = fromDb.getProperties();
    Assert.assertNotNull(retrievedProperties);
    Assert.assertTrue(retrievedProperties.size() == 3);

    assertPropertyListContains(retrievedProperties, new Property(10L, "TEST_PROPERTY_1", "Test property 1"));
    assertPropertyListContains(retrievedProperties, new Property(11L, "TEST_PROPERTY_2", "Test property 2"));
    assertPropertyListContains(retrievedProperties, new Property(14L, "TEST_PROPERTY_WITH_FIELDS", "Test property with fields", fields));

    List<Property> retrievedFields = retrievedProperties.get(0).getFields();
    assertPropertyListContains(retrievedFields, new Property(12L, "TEST_FIELD_1", null));
    assertPropertyListContains(retrievedFields, new Property(13L, "TEST_FIELD_2", null));

    List<Command> commands = fromDb.getCommands();
    Assert.assertNotNull(commands);
    Assert.assertTrue(commands.size() == 2);

    assertCommandListContains(commands, new Command(10L, "TEST_COMMAND_1", "Test command 1"));
    assertCommandListContains(commands, new Command(11L, "TEST_COMMAND_2", "Test command 2"));
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

  @Test
  public void getPropertyIdByPropertyNameAndDevClassIdShouldReturnIdIfExists() {
    long property = deviceClassMapper.getPropertyIdByPropertyNameAndDevClassId("cpuLoadInPercent", 400L);
    Assert.assertEquals(1L, property);
    property = deviceClassMapper.getPropertyIdByPropertyNameAndDevClassId("responsiblePerson", 400L);
    Assert.assertEquals(2L, property);
    property = deviceClassMapper.getPropertyIdByPropertyNameAndDevClassId("TEST_PROPERTY_1", 401L);
    Assert.assertEquals(5L, property);
  }

  @Test
  public void getPropertyIdByPropertyNameAndDevClassIdShouldReturnNullIfPropertyNameDoesNotExist() {
    Long property = deviceClassMapper.getPropertyIdByPropertyNameAndDevClassId("DOES_NOT_EXIST", 400L);
    Assert.assertNull(property);
  }

  @Test
  public void getPropertyIdByPropertyNameAndDevClassIdShouldReturnNullIfDeviceClassIdDoesNotExist() {
    long idDoesNotExist = 0L;
    Long property = deviceClassMapper.getPropertyIdByPropertyNameAndDevClassId("cpuLoadInPercent", idDoesNotExist);
    Assert.assertNull(property);
  }

  private void testDeviceClass1Completeness(DeviceClassCacheObject deviceClass1) throws ClassNotFoundException {
    Assert.assertNotNull(deviceClass1);
    List<Property> properties = deviceClass1.getProperties();
    Assert.assertNotNull(properties);
    Assert.assertEquals(5, properties.size());
    assertPropertyListContains(properties, new Property(1L, "cpuLoadInPercent", "The current CPU load in percent"));
    assertPropertyListContains(properties, new Property(2L, "responsiblePerson", "The person responsible for this device"));
    assertPropertyListContains(properties, new Property(3L, "someCalculations", "Some super awesome calculations"));
    assertPropertyListContains(properties, new Property(4L, "numCores", "The number of CPU cores on this device"));

    List<Property> fields = new ArrayList<>();
    fields.add(new Property(1L, "FIELD_CPULOAD", null));
    fields.add(new Property(2L, "FIELD_RESPONSIBLE_PERSON", null));
    fields.add(new Property(3L, "FIELD_SOME_CALCULATIONS", null));
    fields.add(new Property(4L, "FIELD_NUM_CORES", null));

    assertPropertyListContains(properties, new Property(9L, "TEST_PROPERTY_WITH_FIELDS", "Description of TEST_PROPERTY_WITH_FIELDS", fields));

    List<Command> commands = deviceClass1.getCommands();
    Assert.assertNotNull(commands);
    Assert.assertEquals(2, commands.size());
    assertCommandListContains(commands, new Command(1L, "TEST_COMMAND_1", "Description of TEST_COMMAND_1"));
    assertCommandListContains(commands, new Command(2L, "TEST_COMMAND_2", "Description of TEST_COMMAND_2"));

    List<Long> deviceIds = deviceClass1.getDeviceIds();
    Assert.assertNotNull(deviceIds);
    Assert.assertFalse(deviceIds.isEmpty());
    Assert.assertTrue(deviceIds.contains(300L));
    Assert.assertTrue(deviceIds.contains(301L));
  }

  private void testDeviceClass2Completeness(DeviceClassCacheObject deviceClass2) throws ClassNotFoundException {
    Assert.assertNotNull(deviceClass2);
    List<Property> properties = deviceClass2.getProperties();
    Assert.assertNotNull(properties);
    Assert.assertEquals(4, properties.size());
    assertPropertyListContains(properties, new Property(5L, "TEST_PROPERTY_1", "Description of TEST_PROPERTY_1"));
    assertPropertyListContains(properties, new Property(6L, "TEST_PROPERTY_2", "Description of TEST_PROPERTY_2"));
    assertPropertyListContains(properties, new Property(7L, "TEST_PROPERTY_3", "Description of TEST_PROPERTY_3"));
    assertPropertyListContains(properties, new Property(8L, "TEST_PROPERTY_4", "Description of TEST_PROPERTY_4"));

    List<Command> commands = deviceClass2.getCommands();
    Assert.assertNotNull(commands);
    Assert.assertEquals(2, commands.size());
    assertCommandListContains(commands, new Command(3L, "TEST_COMMAND_3", "Description of TEST_COMMAND_3"));
    assertCommandListContains(commands, new Command(4L, "TEST_COMMAND_4", "Description of TEST_COMMAND_4"));

    List<Long> deviceIds = deviceClass2.getDeviceIds();
    Assert.assertNotNull(deviceIds);
    Assert.assertFalse(deviceIds.isEmpty());
    Assert.assertTrue(deviceIds.contains(302L));
    Assert.assertTrue(deviceIds.contains(303L));
  }
}