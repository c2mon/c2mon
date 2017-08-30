package cern.c2mon.cache;

import java.util.Properties;

import org.junit.Assert;
import org.junit.Test;

import cern.c2mon.server.cache.deviceclass.components.DeviceClassCacheObjectFactory;
import cern.c2mon.server.common.device.Command;
import cern.c2mon.server.common.device.DeviceClass;
import cern.c2mon.server.common.device.Property;
import cern.c2mon.shared.common.ConfigurationException;

import static cern.c2mon.server.test.device.ObjectComparison.assertCommandListContains;
import static cern.c2mon.server.test.device.ObjectComparison.assertPropertyListContains;
import static org.junit.Assert.*;

/**
 * @author Szymon Halastra
 */
public class DeviceClassCacheObjectFactoryTest {

  @Test
  public void testCreateDeviceClassCacheObject() throws IllegalAccessException, ClassNotFoundException {
    Properties properties = createProperties();
    DeviceClassCacheObjectFactory factory = new DeviceClassCacheObjectFactory();

    DeviceClass deviceClass = factory.createCacheObject(10L, properties);
    Assert.assertNotNull(deviceClass);
    assertTrue(deviceClass.getId() == 10L);
    assertTrue(deviceClass.getName() == properties.getProperty("name"));

    assertTrue(deviceClass.getProperties().size() == 2);

    assertPropertyListContains(deviceClass.getProperties(), new Property(1L, "TEST_PROPERTY_1", "Description of TEST_PROPERTY_1"));
    assertPropertyListContains(deviceClass.getProperties(), new Property(2L, "TEST_PROPERTY_2", "Description of TEST_PROPERTY_2"));

    assertTrue(deviceClass.getCommands().size() == 2);
    assertCommandListContains(deviceClass.getCommands(), new Command(1L, "TEST_COMMAND_1", "Description of TEST_COMMAND_1"));
    assertCommandListContains(deviceClass.getCommands(), new Command(2L, "TEST_COMMAND_2", "Description of TEST_COMMAND_2"));
  }

  @Test
  public void parseXMLWithInvalidXml() throws IllegalAccessException {
    Properties properties = createProperties();
    DeviceClassCacheObjectFactory factory = new DeviceClassCacheObjectFactory();

    properties.put("properties", "invalid XML string");
    try {
      factory.createCacheObject(10L, properties);
      fail("createCacheObject() did not throw exception");
    }
    catch (ConfigurationException e) {
      assertEquals("INVALID_PARAMETER should be thrown", ConfigurationException.INVALID_PARAMETER_VALUE, e.getErrorCode());
    }
  }

  @Test
  public void parseXMLWithInvalidString() throws IllegalAccessException {
    Properties properties = createProperties();
    DeviceClassCacheObjectFactory factory = new DeviceClassCacheObjectFactory();

    properties.put("commands", "invalid XML string");
    try {
      factory.createCacheObject(10L, properties);
      fail("createCacheObject() did not throw exception");
    }
    catch (ConfigurationException e) {
      assertEquals("INVALID_PARAMETER should be thrown", ConfigurationException.INVALID_PARAMETER_VALUE, e.getErrorCode());
    }
  }

  @Test
  public void createDeviceClassWithEmptyPropertyList() throws IllegalAccessException {
    Properties properties = createProperties();
    DeviceClassCacheObjectFactory factory = new DeviceClassCacheObjectFactory();

    properties.put("properties", "<Properties />");
    factory.createCacheObject(10L, properties);
  }

  @Test
  public void createDeviceClassWithEmptyCommandList() throws IllegalAccessException {
    Properties properties = createProperties();
    DeviceClassCacheObjectFactory factory = new DeviceClassCacheObjectFactory();


    properties.put("commands", "<Commands />");
    factory.createCacheObject(10L, properties);
  }

  private Properties createProperties() {
    Properties properties = new Properties();
    properties.put("name", "device_class_name");
    properties.put("properties", "<Properties><Property name=\"TEST_PROPERTY_1\" id=\"1\"><description>Description of TEST_PROPERTY_1</description></Property>"
            + "<Property name=\"TEST_PROPERTY_2\" id=\"2\"><description>Description of TEST_PROPERTY_2</description></Property></Properties>");
    properties.put("commands", "<Commands><Command name=\"TEST_COMMAND_1\" id=\"1\"><description>Description of TEST_COMMAND_1</description></Command>"
            + "<Command name=\"TEST_COMMAND_2\" id=\"2\"><description>Description of TEST_COMMAND_2</description></Command></Commands>");

    return properties;
  }
}
