package cern.c2mon.shared.client.device;

import java.util.ArrayList;
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
  public void testAddDeviceProperties() {
    TransferDeviceImpl dti = new TransferDeviceImpl(1L, "test_device_name", 1L);
    List<DeviceProperty> properties = new ArrayList<>();
    properties.add(new DeviceProperty(1L, "cpuLoadInPercent", "987654", "tagId", null));
    properties.add(new DeviceProperty(2L, "responsiblePerson", "Mr. Administrator", "constantValue", null));
    properties.add(new DeviceProperty(3L, "someCalculations", "(#123 + #234) / 2", "clientRule", "Float"));
    properties.add(new DeviceProperty(4L, "numCores", "4", "constantValue", "Integer"));

    DeviceProperty propertyWithFields = new DeviceProperty(5L, "TEST_PROPERTY_WITH_FIELDS", "mappedProperty", new ArrayList<>(properties));
    properties.add(propertyWithFields);

    dti.addDeviceProperties(properties);

    String jsonString = dti.toJson();
    TransferDevice received = TransferDeviceImpl.fromJson(jsonString);
    List<DeviceProperty> deviceProperties = received.getDeviceProperties();

    Assert.assertTrue(deviceProperties.get(0).getName().equals("cpuLoadInPercent"));
    Assert.assertTrue(deviceProperties.get(0).getValue().equals("987654"));
    Assert.assertTrue(deviceProperties.get(0).getCategory().equals("tagId"));
    Assert.assertTrue(deviceProperties.get(1).getName().equals("responsiblePerson"));
    Assert.assertTrue(deviceProperties.get(1).getValue().equals("Mr. Administrator"));
    Assert.assertTrue(deviceProperties.get(1).getCategory().equals("constantValue"));
    Assert.assertTrue(deviceProperties.get(1).getResultType().equals("String"));
    Assert.assertTrue(deviceProperties.get(2).getName().equals("someCalculations"));
    Assert.assertTrue(deviceProperties.get(2).getValue().equals("(#123 + #234) / 2"));
    Assert.assertTrue(deviceProperties.get(2).getCategory().equals("clientRule"));
    Assert.assertTrue(deviceProperties.get(3).getName().equals("numCores"));
    Assert.assertTrue(deviceProperties.get(3).getValue().equals("4"));
    Assert.assertTrue(deviceProperties.get(3).getCategory().equals("constantValue"));
    Assert.assertTrue(deviceProperties.get(3).getResultType().equals("Integer"));

    Assert.assertTrue(deviceProperties.get(4).getName().equals("TEST_PROPERTY_WITH_FIELDS"));
    List<DeviceProperty> fields = new ArrayList<>(deviceProperties.get(4).getFields().values());

    Assert.assertTrue(fields.get(3).getName().equals("cpuLoadInPercent"));
    Assert.assertTrue(fields.get(3).getValue().equals("987654"));
    Assert.assertTrue(fields.get(3).getCategory().equals("tagId"));
    Assert.assertTrue(fields.get(2).getName().equals("responsiblePerson"));
    Assert.assertTrue(fields.get(2).getValue().equals("Mr. Administrator"));
    Assert.assertTrue(fields.get(2).getCategory().equals("constantValue"));
    Assert.assertTrue(fields.get(2).getResultType().equals("String"));
    Assert.assertTrue(fields.get(1).getName().equals("someCalculations"));
    Assert.assertTrue(fields.get(1).getValue().equals("(#123 + #234) / 2"));
    Assert.assertTrue(fields.get(1).getCategory().equals("clientRule"));
    Assert.assertTrue(fields.get(0).getName().equals("numCores"));
    Assert.assertTrue(fields.get(0).getValue().equals("4"));
    Assert.assertTrue(fields.get(0).getCategory().equals("constantValue"));
    Assert.assertTrue(fields.get(0).getResultType().equals("Integer"));
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
}
