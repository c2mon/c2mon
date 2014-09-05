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
  public void testAddPropertyValues() {
    TransferDeviceImpl dti = new TransferDeviceImpl(1L, "test_device_name", 1L);
    dti.addPropertyValue(new PropertyValue("cpuLoadInPercent", 100000L, null, null, null));
    dti.addPropertyValue(new PropertyValue("responsiblePerson", null, null, "Mr. Administrator", null));
    dti.addPropertyValues(new ArrayList<PropertyValue>() {
      {
        add(new PropertyValue("someCalculations", null, "(#123 + #234) / 2", null, "Float"));
        add(new PropertyValue("numCores", null, null, "4", "Integer"));
      }
    });

    String jsonString = dti.toJson();
    TransferDevice received = TransferDeviceImpl.fromJson(jsonString);
    List<PropertyValue> propertyValues = received.getPropertyValues();

    Assert.assertTrue(propertyValues.get(0).getTagId().equals(100000L));
    Assert.assertTrue(propertyValues.get(1).getName().equals("responsiblePerson"));
    Assert.assertTrue(propertyValues.get(2).getClientRule().equals("(#123 + #234) / 2"));
    Assert.assertTrue(propertyValues.get(3).getConstantValue().equals("4"));
  }

  @Test
  public void testAddCommandValues() {
    TransferDeviceImpl dti = new TransferDeviceImpl(1L, "test_device_name", 1L);
    dti.addCommandValue(new CommandValue("test_command_name_1", 1000L));
    dti.addCommandValue(new CommandValue("test_command_name_2", 2000L));
    dti.addCommandValues(new ArrayList<CommandValue>() {
      {
        add(new CommandValue("test_command_name_3", 3000L));
        add(new CommandValue("test_command_name_4", 4000L));
      }
    });

    String jsonString = dti.toJson();
    TransferDevice received = TransferDeviceImpl.fromJson(jsonString);
    List<CommandValue> commandValues = received.getCommandValues();

    Assert.assertTrue(commandValues.get(0).getTagId().equals(1000L));
    Assert.assertTrue(commandValues.get(1).getTagId().equals(2000L));
    Assert.assertTrue(commandValues.get(2).getTagId().equals(3000L));
    Assert.assertTrue(commandValues.get(3).getTagId().equals(4000L));
  }
}
