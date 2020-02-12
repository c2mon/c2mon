package cern.c2mon.server.configuration.loader;

import cern.c2mon.cache.actions.device.DeviceService;
import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.server.cache.dbaccess.DeviceMapper;
import cern.c2mon.server.common.device.Device;
import cern.c2mon.server.common.device.DeviceCacheObject;
import cern.c2mon.shared.client.configuration.ConfigConstants;
import cern.c2mon.shared.client.configuration.ConfigurationReport;
import cern.c2mon.shared.client.device.DeviceCommand;
import cern.c2mon.shared.client.device.DeviceProperty;
import org.junit.Test;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class DeviceConfigTest extends ConfigurationCacheLoaderTest<Device> {

  @Inject
  private C2monCache<Device> deviceCache;

  @Inject
  private DeviceService deviceService;

  @Inject
  private DeviceMapper deviceMapper;

  @Test
  public void testCreateUpdateDevice() throws ClassNotFoundException {
    ConfigurationReport report = configurationLoader.applyConfiguration(33);

    assertFalse(report.toXML().contains(ConfigConstants.Status.FAILURE.toString()));

    DeviceCacheObject cacheObject = (DeviceCacheObject) deviceCache.get(20L);
    DeviceCacheObject expectedObject = new DeviceCacheObject(20L, "TEST_DEVICE_20", 400L);

    List<DeviceProperty> expectedProperties = new ArrayList<>();
    expectedProperties.add(new DeviceProperty(1L, "cpuLoadInPercent", "987654", "tagId", null));
    expectedProperties.add(new DeviceProperty(2L, "responsiblePerson", "Mr. Administrator", "constantValue", null));
    expectedProperties.add(new DeviceProperty(3L, "someCalculations", "(#123 + #234) / 2", "clientRule", "Float"));

    List<DeviceProperty> expectedFields = new ArrayList<>();
    expectedFields.add(new DeviceProperty(1L, "field1", "987654", "tagId", null));
    expectedFields.add(new DeviceProperty(2L, "field2", "(#123 + #234) / 2", "clientRule", null));
    expectedProperties.add(new DeviceProperty(9L, "TEST_PROPERTY_WITH_FIELDS", "mappedProperty", expectedFields));

    List<DeviceCommand> expectedCommands = new ArrayList<>();
    expectedCommands.add(new DeviceCommand(1L, "TEST_COMMAND_1", "4287", "commandTagId", null));
    expectedCommands.add(new DeviceCommand(2L, "TEST_COMMAND_2", "4288", "commandTagId", null));

    expectedObject.setDeviceProperties(expectedProperties);
    expectedObject.setDeviceCommands(expectedCommands);

    assertEquals(expectedObject, cacheObject);

    // Update should succeed
    report = configurationLoader.applyConfiguration(34);

    assertFalse(report.toXML().contains(ConfigConstants.Status.FAILURE.toString()));
    cacheObject = (DeviceCacheObject) deviceCache.get(20L);

    expectedProperties.add(new DeviceProperty(4L, "numCores", "4", "constantValue", "Integer"));
    expectedObject.setDeviceProperties(expectedProperties);
    assertEquals(expectedObject, cacheObject);
  }

  @Test
  public void deviceCreationAddsSelfToClass() {
    assertEquals(2, deviceService.getByDeviceClassId(400L).size());
    ConfigurationReport report = configurationLoader.applyConfiguration(33);

    assertFalse(report.toXML().contains(ConfigConstants.Status.FAILURE.toString()));

    assertEquals(3, deviceService.getByDeviceClassId(400L).size());

    Device device = deviceCache.get(20L);
    assertNotNull(device);
  }

  @Test
  public void testRemoveDevice() {
    Device device = deviceCache.get(300L);
    assertNotNull(device);
    assertTrue(deviceCache.containsKey(300L));
    assertNotNull(deviceMapper.getItem(300L));

    ConfigurationReport report = configurationLoader.applyConfiguration(35);

    assertFalse(report.toXML().contains(ConfigConstants.Status.FAILURE.toString()));
    assertFalse(deviceCache.containsKey(300L));
    assertNull(deviceMapper.getItem(300L));
  }
}
