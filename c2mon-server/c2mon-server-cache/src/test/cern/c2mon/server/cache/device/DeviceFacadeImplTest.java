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
package cern.c2mon.server.cache.device;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import cern.c2mon.server.cache.DeviceCache;
import cern.c2mon.server.cache.DeviceClassCache;
import cern.c2mon.server.cache.DeviceClassFacade;
import cern.c2mon.server.cache.DeviceFacade;
import cern.c2mon.server.common.device.Device;
import cern.c2mon.server.common.device.DeviceCacheObject;
import cern.c2mon.server.common.device.DeviceClass;
import cern.c2mon.server.common.device.DeviceClassCacheObject;
import cern.c2mon.shared.client.device.DeviceCommand;
import cern.c2mon.shared.client.device.DeviceProperty;
import cern.c2mon.shared.common.ConfigurationException;

/**
 * @author Justin Lewis Salmon
 */
@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext
@ContextConfiguration({ "classpath:cern/c2mon/server/cache/config/server-cache-device-facade-test.xml" })
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

    Device device1 = new DeviceCacheObject(1000L, "test_device_1", 1L);
    Device device2 = new DeviceCacheObject(2000L, "test_device_2", 1L);

    // Expect the facade to get the device class object
    EasyMock.expect(deviceClassCacheMock.getDeviceClassByName(deviceClassName)).andReturn(deviceClassReturn);
    // Expect the facade to get the devices
    EasyMock.expect(deviceCacheMock.getCopy(1000L)).andReturn(device1);
    EasyMock.expect(deviceCacheMock.getCopy(2000L)).andReturn(device2);

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
  public void testCreateDeviceClassCacheObject() throws IllegalAccessException {
    Properties properties = new Properties();
    properties.put("name", "device_class_name");
    properties.put("properties", "<Properties><Property name=\"TEST_PROPERTY_1\"><description>Description of TEST_PROPERTY_1</description></Property>"
        + "<Property name=\"TEST_PROPERTY_2\"><description>Description of TEST_PROPERTY_2</description></Property></Properties>");
    properties.put("commands", "<Commands><Command name=\"TEST_COMMAND_1\"><description>Description of TEST_COMMAND_1</description></Command>"
        + "<Command name=\"TEST_COMMAND_2\"><description>Description of TEST_COMMAND_2</description></Command></Commands>");

    DeviceClass deviceClass = deviceClassFacade.createCacheObject(10L, properties);
    Assert.assertNotNull(deviceClass);
    Assert.assertTrue(deviceClass.getId() == 10L);
    Assert.assertTrue(deviceClass.getName() == properties.getProperty("name"));

    Assert.assertTrue(deviceClass.getProperties().size() == 2);
    Assert.assertTrue(deviceClass.getProperties().contains("TEST_PROPERTY_1"));
    Assert.assertTrue(deviceClass.getProperties().contains("TEST_PROPERTY_2"));

    Assert.assertTrue(deviceClass.getCommands().size() == 2);
    Assert.assertTrue(deviceClass.getCommands().contains("TEST_COMMAND_1"));
    Assert.assertTrue(deviceClass.getCommands().contains("TEST_COMMAND_2"));

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
    deviceClass.setProperties(Arrays.asList("TEST_PROPERTY_1", "TEST_PROPERTY_2", "TEST_PROPERTY_WITH_FIELDS"));
    deviceClass.setCommands(Arrays.asList("TEST_COMMAND_1", "TEST_COMMAND_2"));

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
        + "  <DeviceProperty name=\"TEST_PROPERTY_1\"><tag-id>100430</tag-id></DeviceProperty>"
        + "  <DeviceProperty name=\"TEST_PROPERTY_2\"><tag-id>100431</tag-id></DeviceProperty>"
        + "  <DeviceProperty name=\"TEST_PROPERTY_WITH_FIELDS\">"
        + "    <Fields>"
        + "      <Field name=\"cpuLoadInPercent\"><tag-id>987654</tag-id></Field>"
        + "      <Field name=\"responsiblePerson\"><constant-value>Mr. Administrator</constant-value></Field>"
        + "      <Field name=\"someCalculations\"><client-rule><![CDATA[(#123 + #234) / 2]]></client-rule></Field>"
        + "      <Field name=\"numCores\"><constant-value>4</constant-value><result-type>Integer</result-type></Field>"
        + "    </Fields>"
        + "  </DeviceProperty>"
        + "</DeviceProperties>");
    deviceProperties.put("deviceCommands", ""
        + "<DeviceCommands>"
        + "  <DeviceCommand name=\"TEST_COMMAND_1\"><command-tag-id>4287</command-tag-id></DeviceCommand>"
        + "  <DeviceCommand name=\"TEST_COMMAND_2\"><command-tag-id>4288</command-tag-id></DeviceCommand>"
        + "</DeviceCommands>");

    Device device = deviceFacade.createCacheObject(10L, deviceProperties);
    Assert.assertNotNull(device);
    Assert.assertTrue(device.getId() == 10L);
    Assert.assertTrue(device.getName() == deviceProperties.getProperty("name"));
    Assert.assertTrue(device.getDeviceClassId() == 400L);

    Assert.assertTrue(device.getDeviceProperties().size() == 3);
    assertDevicePropertyListContains(device.getDeviceProperties(), new DeviceProperty("TEST_PROPERTY_1", 100430L, null, null, null));
    assertDevicePropertyListContains(device.getDeviceProperties(), new DeviceProperty("TEST_PROPERTY_2", 100431L, null, null, null));

    Assert.assertTrue(device.getDeviceCommands().size() == 2);
    assertDeviceCommandListContains(device.getDeviceCommands(), new DeviceCommand("TEST_COMMAND_1", 4287L));
    assertDeviceCommandListContains(device.getDeviceCommands(), new DeviceCommand("TEST_COMMAND_2", 4288L));

    // Test XML parser throws exception with invalid XML
    deviceProperties.put("deviceProperties", "invalid XML string");
    try {
      deviceFacade.createCacheObject(10L, deviceProperties);
      Assert.fail("createCacheObject() did not throw exception");
    } catch (ConfigurationException e) {
    }
    deviceProperties.put("deviceProperties", "<DeviceProperties><DeviceProperty name=\"TEST_PROPERTY_1\"><tag-id>100430</tag-id></DeviceProperty></DeviceProperties>");

    deviceProperties.put("deviceCommands", "invalid XML string");
    try {
      deviceFacade.createCacheObject(10L, deviceProperties);
      Assert.fail("createCacheObject() did not throw exception");
    } catch (ConfigurationException e) {
    }
    deviceProperties.put("deviceCommands", "<DeviceCommands><DeviceCommand name=\"TEST_COMMAND_1\"><command-tag-id>4287</command-tag-id></DeviceCommand></DeviceCommands>");

    // Test invalid device class ID
    deviceProperties.put("classId", "-1");
    try {
      deviceFacade.createCacheObject(10L, deviceProperties);
      Assert.fail("createCacheObject() did not throw exception");
    } catch (ConfigurationException e) {
    }
    deviceProperties.put("classId", "400");

    // Test invalid property name
    deviceProperties.put("deviceProperties", "<DeviceProperties><DeviceProperty name=\"NONEXISTENT\"><tag-id>1</tag-id></DeviceProperty></DeviceProperties>");
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

  public void assertDeviceCommandListContains(List<DeviceCommand> deviceCommands, DeviceCommand expectedObject) {
    for (DeviceCommand deviceCommand : deviceCommands) {
      if (deviceCommand.getName().equals(expectedObject.getName())) {
        assertDeviceCommandEquals(expectedObject, deviceCommand);
      }
    }
  }
}
