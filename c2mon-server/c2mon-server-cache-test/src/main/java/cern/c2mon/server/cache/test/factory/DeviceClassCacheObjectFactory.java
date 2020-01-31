package cern.c2mon.server.cache.test.factory;

import cern.c2mon.server.common.device.Command;
import cern.c2mon.server.common.device.DeviceClassCacheObject;
import cern.c2mon.server.common.device.Property;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DeviceClassCacheObjectFactory extends AbstractCacheObjectFactory<DeviceClassCacheObject> {

  @Override
  public DeviceClassCacheObject sampleBase() {
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
}
