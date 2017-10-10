package cern.c2mon.cache.device;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import cern.c2mon.cache.api.Cache;
import cern.c2mon.server.cache.device.DeviceCacheObjectFactory;
import cern.c2mon.server.common.device.*;
import cern.c2mon.shared.client.device.DeviceCommand;
import cern.c2mon.shared.client.device.DeviceProperty;
import cern.c2mon.shared.common.ConfigurationException;

import static cern.c2mon.server.test.device.ObjectComparison.assertDeviceCommandListContains;
import static cern.c2mon.server.test.device.ObjectComparison.assertDevicePropertyListContains;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

/**
 * @author Szymon Halastra
 */
public class DeviceCacheObjectFactoryTest {

  private Cache<Long, DeviceClass> deviceClassCache;

  @Before
  public void init() {
    deviceClassCache = EasyMock.createNiceMock(Cache.class);
  }

  @Test
  public void createDeviceCacheObject() throws IllegalAccessException, ClassNotFoundException {
    reset(deviceClassCache);

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
    EasyMock.expect(deviceClassCache.get(400L)).andReturn(deviceClass);

    // Expect the facade to attempt get a non-existent DeviceClass
    EasyMock.expect(deviceClassCache.get(-1L)).andReturn(null);

    // Expect the facade to get the DeviceClass for the device
    EasyMock.expect(deviceClassCache.get(400L)).andReturn(deviceClass).times(2);

    replay(deviceClassCache);

    DeviceCacheObjectFactory deviceCacheObjectFactory = new DeviceCacheObjectFactory(deviceClassCache);

    Properties deviceProperties = createDeviceProperties();

    Device device = deviceCacheObjectFactory.createCacheObject(10L, deviceProperties);

    assertNotNull(device);
    assertTrue(device.getId() == 10L);
    assertTrue(device.getName() == deviceProperties.getProperty("name"));
    assertTrue(device.getDeviceClassId() == 400L);

    assertTrue(device.getDeviceProperties().size() == 3);
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
  }

  @Test
  public void parseXMLWithInvalidXML() throws IllegalAccessException {
    Properties deviceProperties = createDeviceProperties();

    replay(deviceClassCache);

    DeviceCacheObjectFactory deviceCacheObjectFactory = new DeviceCacheObjectFactory(deviceClassCache);

    deviceProperties.put("deviceProperties", "invalid XML string");
    try {
      deviceCacheObjectFactory.createCacheObject(10L, deviceProperties);
      fail("createCacheObject() did not throw exception");
    }
    catch (ConfigurationException e) {
      assertEquals("INVLAID_PARAMETER should be thrown", ConfigurationException.INVALID_PARAMETER_VALUE, e.getErrorCode());
    }

    verify(deviceClassCache);
  }

  @Test
  public void parseXMLWithInvalidString() throws IllegalAccessException {
    Properties deviceProperties = createDeviceProperties();

    replay(deviceClassCache);

    DeviceCacheObjectFactory factory = new DeviceCacheObjectFactory(deviceClassCache);

    deviceProperties.put("deviceProperties",
            "<DeviceProperties><DeviceProperty name=\"TEST_PROPERTY_1\" id=\"1\"><value>100430</value><category>tagId</category></DeviceProperty></DeviceProperties>");

    deviceProperties.put("deviceCommands", "invalid XML string");
    try {
      factory.createCacheObject(10L, deviceProperties);
      fail("createCacheObject() did not throw exception");
    }
    catch (ConfigurationException e) {
      assertEquals("INVLAID_PARAMETER should be thrown", ConfigurationException.INVALID_PARAMETER_VALUE, e.getErrorCode());
    }

    verify(deviceClassCache);
  }

  @Test
  public void createDeviceWithInvalidDeviceClass() throws IllegalAccessException {
    Properties deviceProperties = createDeviceProperties();

    replay(deviceClassCache);

    DeviceCacheObjectFactory factory = new DeviceCacheObjectFactory(deviceClassCache);

    deviceProperties.put("classId", "-1");
    try {
      factory.createCacheObject(10L, deviceProperties);
      fail("createCacheObject() did not throw exception");
    }
    catch (ConfigurationException e) {
      assertEquals("INVLAID_PARAMETER should be thrown", ConfigurationException.INVALID_PARAMETER_VALUE, e.getErrorCode());
    }

    verify(deviceClassCache);
  }

  @Test
  public void createDeviceWithInvalidPropertyName() throws IllegalAccessException {
    Properties deviceProperties = createDeviceProperties();

    replay(deviceClassCache);

    DeviceCacheObjectFactory factory = new DeviceCacheObjectFactory(deviceClassCache);

    deviceProperties.put("deviceProperties", "<DeviceProperties><DeviceProperty name=\"NONEXISTENT\"> id=\"-1\""
            + "<value>1</value><category>tagId</category></DeviceProperty></DeviceProperties>");
    try {
      factory.createCacheObject(10L, deviceProperties);
      Assert.fail("createCacheObject() did not throw exception");
    }
    catch (ConfigurationException e) {
      assertEquals("INVLAID_PARAMETER should be thrown", ConfigurationException.INVALID_PARAMETER_VALUE, e.getErrorCode());
    }

    verify(deviceClassCache);
  }

  @Test
  public void createDeviceWithEmptyPropertyList() throws IllegalAccessException {
    Properties deviceProperties = createDeviceProperties();

    // Expect the facade to get the DeviceClass for the device
    EasyMock.expect(deviceClassCache.get(400L)).andReturn(createDeviceClass());

    replay(deviceClassCache);

    DeviceCacheObjectFactory factory = new DeviceCacheObjectFactory(deviceClassCache);

    deviceProperties.put("deviceProperties", "<DeviceProperties />");

    DeviceCacheObject object = (DeviceCacheObject) factory.createCacheObject(10L, deviceProperties);

    verify(deviceClassCache);

    assertEquals("DeviceProperties should be empty", 0, object.getDeviceProperties().size());
  }

  @Test
  public void createDeviceWithEmptyCommandList() throws IllegalAccessException {
    Properties deviceProperties = createDeviceProperties();

    // Expect the facade to get the DeviceClass for the device
    EasyMock.expect(deviceClassCache.get(400L)).andReturn(createDeviceClass());

    replay(deviceClassCache);

    DeviceCacheObjectFactory factory = new DeviceCacheObjectFactory(deviceClassCache);

    deviceProperties.put("deviceCommands", "<DeviceCommands />");

    DeviceCacheObject object = (DeviceCacheObject) factory.createCacheObject(10L, deviceProperties);

    verify(deviceClassCache);

    assertEquals("DeviceCommands should be empty", 0, object.getDeviceCommands().size());
  }

  private DeviceClass createDeviceClass() {
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

    return deviceClass;
  }

  private Properties createDeviceProperties() {
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
    return deviceProperties;
  }
}
