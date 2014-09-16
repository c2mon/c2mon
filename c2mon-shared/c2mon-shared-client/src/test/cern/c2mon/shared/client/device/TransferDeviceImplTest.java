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
    dti.addDeviceProperty(new DeviceProperty("cpuLoadInPercent", 100000L, null, null, null));
    dti.addDeviceProperty(new DeviceProperty("responsiblePerson", null, null, "Mr. Administrator", null));
    dti.addDeviceProperties(new ArrayList<DeviceProperty>() {
      {
        add(new DeviceProperty("someCalculations", null, "(#123 + #234) / 2", null, "Float"));
        add(new DeviceProperty("numCores", null, null, "4", "Integer"));
      }
    });

    String jsonString = dti.toJson();
    TransferDevice received = TransferDeviceImpl.fromJson(jsonString);
    List<DeviceProperty> deviceProperties = received.getDeviceProperties();

    Assert.assertTrue(deviceProperties.get(0).getTagId().equals(100000L));
    Assert.assertTrue(deviceProperties.get(1).getName().equals("responsiblePerson"));
    Assert.assertTrue(deviceProperties.get(2).getClientRule().equals("(#123 + #234) / 2"));
    Assert.assertTrue(deviceProperties.get(3).getConstantValue().equals("4"));
  }

  @Test
  public void testAddDeviceCommands() {
    TransferDeviceImpl dti = new TransferDeviceImpl(1L, "test_device_name", 1L);
    dti.addDeviceCommand(new DeviceCommand("test_command_name_1", 1000L));
    dti.addDeviceCommand(new DeviceCommand("test_command_name_2", 2000L));
    dti.addDeviceCommands(new ArrayList<DeviceCommand>() {
      {
        add(new DeviceCommand("test_command_name_3", 3000L));
        add(new DeviceCommand("test_command_name_4", 4000L));
      }
    });

    String jsonString = dti.toJson();
    TransferDevice received = TransferDeviceImpl.fromJson(jsonString);
    List<DeviceCommand> deviceCommands = received.getDeviceCommands();

    Assert.assertTrue(deviceCommands.get(0).getTagId().equals(1000L));
    Assert.assertTrue(deviceCommands.get(1).getTagId().equals(2000L));
    Assert.assertTrue(deviceCommands.get(2).getTagId().equals(3000L));
    Assert.assertTrue(deviceCommands.get(3).getTagId().equals(4000L));
  }
}
