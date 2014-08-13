package cern.c2mon.shared.client.device;

import java.util.HashMap;
import java.util.Map;

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
    dti.addPropertyValue("test_property_name_1", 1000L);
    dti.addPropertyValue("test_property_name_2", 2000L);
    dti.addPropertyValues(new HashMap<String, Long>() {
      {
        put("test_property_name_3", 3000L);
        put("test_property_name_4", 4000L);
      }
    });

    String jsonString = dti.toJson();
    TransferDevice received = TransferDeviceImpl.fromJson(jsonString);
    Map<String, Long> propertyValues = received.getPropertyValues();

    Assert.assertTrue(propertyValues.get("test_property_name_1").equals(1000L));
    Assert.assertTrue(propertyValues.get("test_property_name_2").equals(2000L));
    Assert.assertTrue(propertyValues.get("test_property_name_3").equals(3000L));
    Assert.assertTrue(propertyValues.get("test_property_name_4").equals(4000L));
  }

  @Test
  public void testAddCommandValues() {
    TransferDeviceImpl dti = new TransferDeviceImpl(1L, "test_device_name", 1L);
    dti.addCommandValue("test_command_name_1", 1000L);
    dti.addCommandValue("test_command_name_2", 2000L);
    dti.addCommandValues(new HashMap<String, Long>() {
      {
        put("test_command_name_3", 3000L);
        put("test_command_name_4", 4000L);
      }
    });

    String jsonString = dti.toJson();
    TransferDevice received = TransferDeviceImpl.fromJson(jsonString);
    Map<String, Long> commandValues = received.getCommandValues();

    Assert.assertTrue(commandValues.get("test_command_name_1").equals(1000L));
    Assert.assertTrue(commandValues.get("test_command_name_2").equals(2000L));
    Assert.assertTrue(commandValues.get("test_command_name_3").equals(3000L));
    Assert.assertTrue(commandValues.get("test_command_name_4").equals(4000L));
  }
}
