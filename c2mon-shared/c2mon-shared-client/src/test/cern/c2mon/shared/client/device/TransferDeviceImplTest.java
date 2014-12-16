package cern.c2mon.shared.client.device;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Justin Lewis Salmon
 */
public class TransferDeviceImplTest {

  @Test
  public void testBasicUsage() {
    TransferDeviceImpl dti = new TransferDeviceImpl(1L, "test_device_name", 1L);
    String jsonString = dti.toJson();

    TransferDevice received = TransferDeviceImpl.fromJson(jsonString);
    Assert.assertTrue(received.getId().equals(dti.getId()));
    Assert.assertTrue(received.getName().equals(dti.getName()));
    Assert.assertTrue(received.getDeviceClassId().equals(dti.getDeviceClassId()));
  }

  @Test
  public void testAddDeviceProperties() throws ClassNotFoundException {
    TransferDeviceImpl dti = new TransferDeviceImpl(1L, "test_device_name", 1L);
    List<DeviceProperty> properties = new ArrayList<>();
    DeviceProperty p1 = new DeviceProperty(1L, "cpuLoadInPercent", "987654", "tagId", null);
    DeviceProperty p2 = new DeviceProperty(2L, "responsiblePerson", "Mr. Administrator", "constantValue", null);
    DeviceProperty p3 = new DeviceProperty(3L, "someCalculations", "(#123 + #234) / 2", "clientRule", "Float");
    DeviceProperty p4 =new DeviceProperty(4L, "numCores", "4", "constantValue", "Integer");
    properties.addAll(Arrays.asList(p1, p2, p3, p4));

    DeviceProperty propertyWithFields = new DeviceProperty(5L, "TEST_PROPERTY_WITH_FIELDS", "mappedProperty", new ArrayList<>(properties));
    properties.add(propertyWithFields);

    dti.addDeviceProperties(properties);

    String jsonString = dti.toJson();
    TransferDevice received = TransferDeviceImpl.fromJson(jsonString);
    List<DeviceProperty> deviceProperties = received.getDeviceProperties();

    assertDevicePropertyListContains(deviceProperties, p1);
    assertDevicePropertyListContains(deviceProperties, p2);
    assertDevicePropertyListContains(deviceProperties, p3);
    assertDevicePropertyListContains(deviceProperties, p4);
    assertDevicePropertyListContains(deviceProperties, propertyWithFields);
  }

  @Test
  public void testAddDeviceCommands() {
    TransferDeviceImpl dti = new TransferDeviceImpl(1L, "test_device_name", 1L);
    dti.addDeviceCommand(new DeviceCommand(1L, "TEST_COMMAND_1", "1000", "commandTagId", null));
    dti.addDeviceCommand(new DeviceCommand(1L, "TEST_COMMAND_1", "2000", "commandTagId", null));
    dti.addDeviceCommands(new ArrayList<DeviceCommand>() {
      {
        add(new DeviceCommand(1L, "TEST_COMMAND_1", "3000", "commandTagId", null));
        add(new DeviceCommand(1L, "TEST_COMMAND_1", "4000", "commandTagId", null));
      }
    });

    String jsonString = dti.toJson();
    TransferDevice received = TransferDeviceImpl.fromJson(jsonString);
    List<DeviceCommand> deviceCommands = received.getDeviceCommands();

    Assert.assertTrue(deviceCommands.get(0).getValue().equals("1000"));
    Assert.assertTrue(deviceCommands.get(1).getValue().equals("2000"));
    Assert.assertTrue(deviceCommands.get(2).getValue().equals("3000"));
    Assert.assertTrue(deviceCommands.get(3).getValue().equals("4000"));
  }

  public static void assertDevicePropertyEquals(DeviceProperty expectedObject, DeviceProperty cacheObject) throws ClassNotFoundException {
    assertEquals(expectedObject.getId(), cacheObject.getId());
    assertEquals(expectedObject.getName(), cacheObject.getName());
    assertEquals(expectedObject.getCategory(), cacheObject.getCategory());

    if ((expectedObject.getCategory() != null && expectedObject.getCategory().equals("mappedProperty")) || expectedObject.getFields() != null) {
      for (DeviceProperty field : expectedObject.getFields().values()) {
        assertDevicePropertyListContains(new ArrayList<>(cacheObject.getFields().values()), field);
      }
    } else {
      assertEquals(expectedObject.getValue(), cacheObject.getValue());
      assertEquals(expectedObject.getResultType(), cacheObject.getResultType());
    }
  }

  public static void assertDevicePropertyListContains(List<DeviceProperty> deviceProperties, DeviceProperty expectedObject) throws ClassNotFoundException {
    for (DeviceProperty deviceProperty : deviceProperties) {
      if (deviceProperty.getName().equals(expectedObject.getName())) {
        assertDevicePropertyEquals(expectedObject, deviceProperty);
      }
    }
  }
}
