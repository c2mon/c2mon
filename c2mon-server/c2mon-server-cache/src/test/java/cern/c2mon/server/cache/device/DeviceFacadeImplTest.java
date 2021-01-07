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
package cern.c2mon.server.cache.device;

import java.util.*;

import cern.c2mon.shared.client.device.*;
import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import cern.c2mon.server.cache.DeviceCache;
import cern.c2mon.server.cache.DeviceClassCache;
import cern.c2mon.server.cache.DeviceClassFacade;
import cern.c2mon.server.cache.DeviceFacade;
import cern.c2mon.server.cache.config.CacheModule;
import cern.c2mon.server.cache.dbaccess.config.CacheDbAccessModule;
import cern.c2mon.server.cache.loading.config.CacheLoadingModule;
import cern.c2mon.server.common.config.CommonModule;
import cern.c2mon.server.common.device.*;
import cern.c2mon.shared.common.ConfigurationException;

import static cern.c2mon.server.test.device.ObjectComparison.*;

/**
 * @author Justin Lewis Salmon
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
    CommonModule.class,
    CacheModule.class,
    CacheDbAccessModule.class,
    CacheLoadingModule.class,
    DeviceClassCacheMock.class,
    DeviceCacheMock.class
})
public class DeviceFacadeImplTest {

  /** Component to test */
  @Autowired
  DeviceClassFacade deviceClassFacade;

  @Autowired
  DeviceFacade deviceFacade;

  /** Mocked components */
  @Autowired
  DeviceCache deviceCacheMock;

  @Autowired
  DeviceClassCache deviceClassCacheMock;

  @Test
  public void testGetClassNames() {
    // Reset the mock
    EasyMock.reset(deviceCacheMock, deviceClassCacheMock);

    List<String> classNamesReturn = new ArrayList<>();
    classNamesReturn.add("test_device_class_name_1");
    classNamesReturn.add("test_device_class_name_2");

    List<Long> classIds = new ArrayList<>();
    classIds.add(1000L);
    classIds.add(2000L);

    DeviceClass class1 = new DeviceClassCacheObject(1000L, "test_device_class_name_1", "Test description");
    DeviceClass class2 = new DeviceClassCacheObject(2000L, "test_device_class_name_2", "Test description");

    // Expect the facade to get the class names from the class cache
    EasyMock.expect(deviceClassCacheMock.getKeys()).andReturn(classIds);
    EasyMock.expect(deviceClassCacheMock.get(EasyMock.anyLong())).andReturn(class1);
    EasyMock.expect(deviceClassCacheMock.get(EasyMock.anyLong())).andReturn(class2);

    // Setup is finished, need to activate the mock
    EasyMock.replay(deviceCacheMock, deviceClassCacheMock);

    List<String> classNames = deviceClassFacade.getDeviceClassNames();
    Assert.assertTrue(classNames.get(0).equals("test_device_class_name_1"));
    Assert.assertTrue(classNames.get(1).equals("test_device_class_name_2"));

    // Verify that everything happened as expected
    EasyMock.verify(deviceCacheMock, deviceClassCacheMock);
  }

  @Test
  public void testGetDevices() {
    // Reset the mock
    EasyMock.reset(deviceCacheMock, deviceClassCacheMock);

    String deviceClassName = "test_device_class_name";
    DeviceClassCacheObject deviceClassReturn = new DeviceClassCacheObject(1L, "test_device_class_name_1", "Test description");

    List<Long> deviceIds = new ArrayList<>();
    deviceIds.add(1000L);
    deviceIds.add(2000L);
    deviceClassReturn.setDeviceIds(deviceIds);

    DeviceCacheObject device1 = new DeviceCacheObject(1000L, "test_device_1", 1L);
    device1.setDeviceProperties(new ArrayList<>(Arrays.asList(new DeviceProperty(10L, "test_property", "10", "tagId", null))));
    device1.setDeviceCommands(new ArrayList<>(Arrays.asList(new DeviceCommand(10L, "test_command", "20", "commandTagId", null))));
    Device device2 = new DeviceCacheObject(2000L, "test_device_2", 1L);

    List<Device> deviceReturn = new ArrayList<>();
    deviceReturn.add(device1);
    deviceReturn.add(device2);

    // Expect the facade to get the device class object
    EasyMock.expect(deviceClassCacheMock.getDeviceClassIdByName(deviceClassName)).andReturn(deviceClassReturn.getId());
    EasyMock.expect(deviceCacheMock.getByDeviceClassId(deviceClassReturn.getId())).andReturn(deviceReturn);

    // Setup is finished, need to activate the mock
    EasyMock.replay(deviceCacheMock, deviceClassCacheMock);

    List<Device> devices = deviceFacade.getDevices(deviceClassName);
    Assert.assertTrue(devices.get(0).getId() == device1.getId());
    Assert.assertTrue(devices.get(1).getId() == device2.getId());
    Assert.assertTrue(devices.get(0).getDeviceClassId() == device1.getDeviceClassId());
    Assert.assertTrue(devices.get(1).getDeviceClassId() == device2.getDeviceClassId());

    // Verify that everything happened as expected
    EasyMock.verify(deviceCacheMock, deviceClassCacheMock);
  }

  @Test
  public void testCreateDeviceClassCacheObject() throws IllegalAccessException, ClassNotFoundException {
    // Reset the mock
    EasyMock.reset(deviceCacheMock, deviceClassCacheMock);
    // Expect the locks to be used
    deviceClassCacheMock.acquireReadLockOnKey(EasyMock.anyLong());
    EasyMock.expectLastCall().atLeastOnce();
    // Expect the locks to be used
    deviceClassCacheMock.releaseReadLockOnKey(EasyMock.anyLong());
    EasyMock.expectLastCall().atLeastOnce();

    Properties properties = new Properties();
    properties.put("name", "device_class_name");
    properties.put("properties", "<Properties><Property name=\"TEST_PROPERTY_1\" id=\"1\"><description>Description of TEST_PROPERTY_1</description></Property>"
        + "<Property name=\"TEST_PROPERTY_2\" id=\"2\"><description>Description of TEST_PROPERTY_2</description></Property></Properties>");
    properties.put("commands", "<Commands><Command name=\"TEST_COMMAND_1\" id=\"1\"><description>Description of TEST_COMMAND_1</description></Command>"
        + "<Command name=\"TEST_COMMAND_2\" id=\"2\"><description>Description of TEST_COMMAND_2</description></Command></Commands>");

    EasyMock.replay(deviceCacheMock, deviceClassCacheMock);

    DeviceClass deviceClass = deviceClassFacade.createCacheObject(10L, properties);
    Assert.assertNotNull(deviceClass);
    Assert.assertTrue(deviceClass.getId() == 10L);
    Assert.assertTrue(deviceClass.getName() == properties.getProperty("name"));

    Assert.assertTrue(deviceClass.getProperties().size() == 2);

    assertPropertyListContains(deviceClass.getProperties(), new Property(1L, "TEST_PROPERTY_1", "Description of TEST_PROPERTY_1"));
    assertPropertyListContains(deviceClass.getProperties(), new Property(2L, "TEST_PROPERTY_2", "Description of TEST_PROPERTY_2"));

    Assert.assertTrue(deviceClass.getCommands().size() == 2);
    assertCommandListContains(deviceClass.getCommands(), new Command(1L, "TEST_COMMAND_1", "Description of TEST_COMMAND_1"));
    assertCommandListContains(deviceClass.getCommands(), new Command(2L, "TEST_COMMAND_2", "Description of TEST_COMMAND_2"));

    // Test XML parser throws exception with invalid XML
    properties.put("properties", "invalid XML string");
    try {
      deviceClassFacade.createCacheObject(10L, properties);
      Assert.fail("createCacheObject() did not throw exception");
    } catch (ConfigurationException e) {
    }

    // Test empty property list
    properties.put("properties", "<Properties />");
    deviceClassFacade.createCacheObject(10L, properties);

    // Test empty command list
    properties.put("commands", "<Commands />");
    deviceClassFacade.createCacheObject(10L, properties);

    properties.put("commands", "invalid XML string");
    try {
      deviceClassFacade.createCacheObject(10L, properties);
      Assert.fail("createCacheObject() did not throw exception");
    } catch (ConfigurationException e) {
    }
  }

  @Test
  public void testCreateDeviceCacheObject() throws IllegalAccessException, ClassNotFoundException {
    // Reset the mock
    EasyMock.reset(deviceCacheMock, deviceClassCacheMock);

    DeviceClassCacheObject deviceClass = new DeviceClassCacheObject(400L, "TEST_DEVICE_CLASS_1", "Description of TEST_DEVICE_CLASS_1");
    List<Property> classProperties = new ArrayList<>();
    classProperties.addAll(Arrays.asList(new Property(1L, "TEST_PROPERTY_1", "Test property 1"), new Property(2L, "TEST_PROPERTY_2", "Test property 2")));

    List<Property> fields = new ArrayList<>();
    fields.addAll(Arrays.asList(new Property(1L, "field1", null), new Property(2L, "field2", null), new Property(3L, "field3", null), new Property(4L,
        "field4", null)));
    Property propertyWithFields = new Property(3L, "TEST_PROPERTY_WITH_FIELDS", "Test property with fields", fields);
    classProperties.add(propertyWithFields);

    deviceClass.setProperties(classProperties);
    deviceClass.setCommands(Arrays.asList(new Command(1L, "TEST_COMMAND_1", "Test command 1"), new Command(2L, "TEST_COMMAND_2", "Test command 1")));

    // Expect the facade to get the DeviceClass for the device
    EasyMock.expect(deviceClassCacheMock.get(400L)).andReturn(deviceClass);

    // Expect the facade to attempt get a non-existent DeviceClass
    EasyMock.expect(deviceClassCacheMock.get(-1L)).andReturn(null);

    // Expect the facade to get the DeviceClass for the device
    EasyMock.expect(deviceClassCacheMock.get(400L)).andReturn(deviceClass).times(2);

    // Setup is finished, need to activate the mock
    EasyMock.replay(deviceCacheMock, deviceClassCacheMock);

    Properties deviceProperties = new Properties();
    deviceProperties.put("name", "device_name");
    deviceProperties.put("classId", "400");
    deviceProperties.put("deviceProperties", ""
        + "<DeviceProperties>"
        + "  <DeviceProperty name=\"TEST_PROPERTY_1\" id=\"1\">"
        + "    <value>100430</value>"
        + "    <category>tagId</category>"
        + "  </DeviceProperty>"
        + "  <DeviceProperty name=\"TEST_PROPERTY_2\" id=\"2\">"
        + "    <value>100431</value>"
        + "    <category>tagId</category>"
        + "  </DeviceProperty>"
        + "  <DeviceProperty name=\"TEST_PROPERTY_WITH_FIELDS\" id=\"3\">"
        + "    <category>mappedProperty</category>"
        + "    <PropertyFields>"
        + "      <PropertyField name=\"field1\" id=\"1\"><value>987654</value><category>tagId</category></PropertyField>"
        + "      <PropertyField name=\"field2\" id=\"2\"><value>Mr. Administrator</value><category>constantValue</category></PropertyField>"
        + "      <PropertyField name=\"field3\" id=\"3\"><value><![CDATA[(#123 + #234) / 2]]></value><category>clientRule</category>"
        + "         <result-type>Float</result-type></PropertyField>"
        + "      <PropertyField name=\"field4\" id=\"4\"><value>4</value><category>constantValue</category><result-type>Integer</result-type></PropertyField>"
        + "    </PropertyFields>"
        + "  </DeviceProperty>"
        + "</DeviceProperties>");
    deviceProperties.put("deviceCommands", ""
        + "<DeviceCommands>"
        + "  <DeviceCommand name=\"TEST_COMMAND_1\" id=\"1\"><value>4287</value><category>commandTagId</category></DeviceCommand>"
        + "  <DeviceCommand name=\"TEST_COMMAND_2\" id=\"2\"><value>4288</value><category>commandTagId</category></DeviceCommand>"
        + "</DeviceCommands>");

    Device device = deviceFacade.createCacheObject(10L, deviceProperties);
    Assert.assertNotNull(device);
    Assert.assertTrue(device.getId() == 10L);
    Assert.assertTrue(device.getName() == deviceProperties.getProperty("name"));
    Assert.assertTrue(device.getDeviceClassId() == 400L);

    Assert.assertTrue(device.getDeviceProperties().size() == 3);
    assertDevicePropertyListContains(device.getDeviceProperties(), new DeviceProperty(1L, "TEST_PROPERTY_1", "100430", "tagId", null));
    assertDevicePropertyListContains(device.getDeviceProperties(), new DeviceProperty(2L, "TEST_PROPERTY_2", "100431", "tagId", null));

    List<DeviceProperty> expectedFields = new ArrayList<>();
    expectedFields.add(new DeviceProperty(1L, "field1", "987654", "tagId", "String"));
    expectedFields.add(new DeviceProperty(2L, "field2", "Mr. Administrator", "constantValue", "String"));
    expectedFields.add(new DeviceProperty(3L, "field3", "(#123 + #234) / 2", "clientRule", "Float"));
    expectedFields.add(new DeviceProperty(4L, "field4", "4", "constantValue", "Integer"));
    assertDevicePropertyListContains(device.getDeviceProperties(), new DeviceProperty(3L, "TEST_PROPERTY_WITH_FIELDS", "mappedProperty", expectedFields));

    Assert.assertTrue(device.getDeviceCommands().size() == 2);
    assertDeviceCommandListContains(device.getDeviceCommands(), new DeviceCommand(1L, "TEST_COMMAND_1", "4287", "commandTagId", null));
    assertDeviceCommandListContains(device.getDeviceCommands(), new DeviceCommand(2L, "TEST_COMMAND_2", "4288", "commandTagId", null));

    // Test XML parser throws exception with invalid XML
    deviceProperties.put("deviceProperties", "invalid XML string");
    try {
      deviceFacade.createCacheObject(10L, deviceProperties);
      Assert.fail("createCacheObject() did not throw exception");
    } catch (ConfigurationException e) {
    }
    deviceProperties.put("deviceProperties",
        "<DeviceProperties><DeviceProperty name=\"TEST_PROPERTY_1\" id=\"1\"><value>100430</value><category>tagId</category></DeviceProperty></DeviceProperties>");

    deviceProperties.put("deviceCommands", "invalid XML string");
    try {
      deviceFacade.createCacheObject(10L, deviceProperties);
      Assert.fail("createCacheObject() did not throw exception");
    } catch (ConfigurationException e) {
    }
    deviceProperties.put("deviceCommands",
        "<DeviceCommands><DeviceCommand name=\"TEST_COMMAND_1\" id=\"1\"><value>4287</value><category>commandTagId</category></DeviceCommand></DeviceCommands>");

    // Test invalid device class ID
    deviceProperties.put("classId", "-1");
    try {
      deviceFacade.createCacheObject(10L, deviceProperties);
      Assert.fail("createCacheObject() did not throw exception");
    } catch (ConfigurationException e) {
    }
    deviceProperties.put("classId", "400");

    // Test invalid property name
    deviceProperties.put("deviceProperties", "<DeviceProperties><DeviceProperty name=\"NONEXISTENT\"> id=\"-1\""
        + "<value>1</value><category>tagId</category></DeviceProperty></DeviceProperties>");
    try {
      deviceFacade.createCacheObject(10L, deviceProperties);
      Assert.fail("createCacheObject() did not throw exception");
    } catch (ConfigurationException e) {
    }

    // Test empty property list
    deviceProperties.put("deviceProperties", "<DeviceProperties />");
    deviceFacade.createCacheObject(10L, deviceProperties);

    // Test empty command list
    deviceProperties.put("deviceCommands", "<DeviceCommands />");
    deviceFacade.createCacheObject(10L, deviceProperties);

    // Verify that everything happened as expected
    EasyMock.verify(deviceCacheMock, deviceClassCacheMock);
  }

  @Test
  public void testGetDevicesByName() {
    // Reset the mock
    EasyMock.reset(deviceCacheMock, deviceClassCacheMock);

    DeviceClassCacheObject class1 = new DeviceClassCacheObject(10L, "class_1", "");
    DeviceClassCacheObject class2 = new DeviceClassCacheObject(20L, "class_2", "");

    DeviceCacheObject device1 = new DeviceCacheObject(1L, "device_a", 10L);
    DeviceCacheObject device2 = new DeviceCacheObject(2L, "device_b", 10L);
    DeviceCacheObject device3 = new DeviceCacheObject(3L, "device_a", 20L);

    class1.setDeviceIds(Arrays.asList(device1.getId(), device2.getId()));
    class2.setDeviceIds(Arrays.asList(device3.getId()));

    DeviceInfo di1 = new DeviceInfo("class_1", "device_a");
    DeviceInfo di2 = new DeviceInfo("class_1", "device_b");
    DeviceInfo di3 = new DeviceInfo("class_2", "device_a");
    DeviceInfo di4 = new DeviceInfo("class_2", "unknown_device");
    Set<DeviceInfo> deviceInfoList = new HashSet<>(Arrays.asList(di1, di2, di3, di4));

    // Expectations
    List<Device> deviceList1 = new ArrayList<>();
    deviceList1.add(device1);
    deviceList1.add(device2);

    List<Device> deviceList2 = new ArrayList<>();
    deviceList2.add(device3);

    EasyMock.expect(deviceCacheMock.getByDeviceClassId(class1.getId())).andReturn(deviceList1);
    EasyMock.expect(deviceCacheMock.getByDeviceClassId(class2.getId())).andReturn(deviceList2);
    EasyMock.expect(deviceClassCacheMock.getDeviceClassIdByName(di1.getClassName())).andReturn(class1.getId());
    EasyMock.expect(deviceClassCacheMock.getDeviceClassIdByName(di3.getClassName())).andReturn(class2.getId());

    // Setup is finished, need to activate the mock
    EasyMock.replay(deviceCacheMock, deviceClassCacheMock);

    List<Device> devices = deviceFacade.getDevices(deviceInfoList);
    // Should only have 3 devices
    Assert.assertTrue(devices.size() == 3);

    // Verify that everything happened as expected
    EasyMock.verify(deviceCacheMock, deviceClassCacheMock);
  }
}
