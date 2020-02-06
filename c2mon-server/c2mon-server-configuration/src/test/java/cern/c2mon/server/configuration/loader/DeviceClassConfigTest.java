package cern.c2mon.server.configuration.loader;

import cern.c2mon.cache.actions.device.DeviceService;
import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.server.cache.dbaccess.DeviceClassMapper;
import cern.c2mon.server.common.device.*;
import cern.c2mon.shared.client.configuration.ConfigConstants;
import cern.c2mon.shared.client.configuration.ConfigurationReport;
import org.junit.Test;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static cern.c2mon.server.common.util.KotlinAPIs.apply;
import static org.junit.Assert.*;

public class DeviceClassConfigTest extends ConfigurationCacheLoaderTest<Device> {

  @Inject
  private DeviceClassMapper deviceClassMapper;

  @Inject
  private C2monCache<DeviceClass> deviceClassCache;

  @Inject
  private DeviceService deviceService;

  @Test
  public void create() {
    ConfigurationReport report = configurationLoader.applyConfiguration(30);

    assertFalse(report.toXML().contains(ConfigConstants.Status.FAILURE.toString()));

    assertNotNull(deviceClassMapper.getItem(10L));

    sortAndCompareWithExpected(expectedObject(), deviceClassCache.get(10L));

    // Assert that the object from the DB is also the same
    sortAndCompareWithExpected(expectedObject(), deviceClassMapper.getItem(10L));
  }

  @Test
  public void update() {
    configurationLoader.applyConfiguration(30);
    ConfigurationReport report = configurationLoader.applyConfiguration(31);

    assertFalse(report.toXML().contains(ConfigConstants.Status.FAILURE.toString()));

    DeviceClassCacheObject expected = expectedObject();

    // The properties update is not getting to the database
    sortAndCompareWithExpected(expected, deviceClassMapper.getItem(10L));

    apply(expected.getProperties(),
      list -> list.add(new Property(14L, "numCores", "The number of CPU cores on this device")));
    sortAndCompareWithExpected(expected, deviceClassCache.get(10L));
  }

  @Test
  public void remove() {
    assertFalse(deviceService.getByDeviceClassId(400L).isEmpty());
    ConfigurationReport report = configurationLoader.applyConfiguration(32);

    assertFalse(report.toXML().contains(ConfigConstants.Status.FAILURE.toString()));
    assertFalse(deviceClassCache.containsKey(400L));
    DeviceClass dbItem = deviceClassMapper.getItem(400L);
    assertNull(dbItem);
    assertTrue(deviceService.getByDeviceClassId(400L).isEmpty());
  }

  private static DeviceClassCacheObject expectedObject(){
    DeviceClassCacheObject expectedObject = new DeviceClassCacheObject(10L, "TEST_DEVICE_CLASS_10", "Description of TEST_DEVICE_CLASS_10");

    List<Property> expectedProperties = new ArrayList<>();
    expectedProperties.add(new Property(10L, "cpuLoadInPercent", "The current CPU load in percent"));
    expectedProperties.add(new Property(11L, "responsiblePerson", "The person responsible for this device"));
    expectedProperties.add(new Property(12L, "someCalculations", "Some super awesome calculations"));

    List<Property> expectedFields = new ArrayList<>();
    expectedFields.add(new Property(10L, "field1", null));
    expectedFields.add(new Property(11L, "field2", null));
    expectedFields.add(new Property(12L, "field3", null));

    expectedProperties.add(new Property(13L, "TEST_PROPERTY_WITH_FIELDS", "A property containing fields", expectedFields));

    List<Command> expectedCommands = new ArrayList<>();
    expectedCommands.add(new Command(10L, "TEST_COMMAND_1", "Description of TEST_COMMAND_1"));
    expectedCommands.add(new Command(11L, "TEST_COMMAND_2", "Description of TEST_COMMAND_2"));

    expectedObject.setProperties(expectedProperties);
    expectedObject.setCommands(expectedCommands);

    return expectedObject;
  }

  private static void sortAndCompareWithExpected(DeviceClass expected, DeviceClass deviceClass) {
    assertNotNull(deviceClass);

    DeviceClassCacheObject deviceClassCacheObject = (DeviceClassCacheObject) deviceClass;
    deviceClassCacheObject.setProperties(
      deviceClass.getProperties().stream().sorted(Comparator.comparing(Property::getId)).collect(Collectors.toList()));

    assertEquals(expected, deviceClass);
  }
}
