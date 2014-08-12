/**
 *
 */
package cern.c2mon.client.ext.device;

import java.util.HashMap;
import java.util.Map;

import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import cern.c2mon.client.common.tag.ClientDataTagValue;
import cern.c2mon.client.core.C2monTagManager;
import cern.c2mon.client.core.tag.ClientDataTagImpl;
import cern.c2mon.client.ext.device.util.DeviceTestUtils;
import cern.c2mon.shared.rule.RuleFormatException;

/**
 * @author Justin Lewis Salmon
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "classpath:cern/c2mon/client/ext/device/config/c2mon-deviceimpl-test.xml" })
public class DeviceImplTest {

  /** Mocked components */
  @Autowired
  private C2monTagManager tagManagerMock;

  @Test
  public void testLazyLoadPropertyValue() {

    DeviceImpl device = new DeviceImpl(1000L, "test_device", 1L, "test_device_class", tagManagerMock);

    // Make a map with only IDs, no values
    Map<String, Long> propertyValueIds = new HashMap<String, Long>();
    propertyValueIds.put("test_property_1", 100000L);

    device.setPropertyValues(propertyValueIds);

    // Expect the device to get a single data tag
    ClientDataTagImpl cdt1 = new ClientDataTagImpl(100000L);
    EasyMock.expect(tagManagerMock.getDataTag(EasyMock.<Long> anyObject())).andReturn(cdt1).once();

    // Setup is finished, need to activate the mock
    EasyMock.replay(tagManagerMock);

    ClientDataTagValue property = device.getPropertyValue("test_property_1");
    Assert.assertTrue(property.getId() == cdt1.getId());

    // Verify that everything happened as expected
    EasyMock.verify(tagManagerMock);
    // Reset the mock
    EasyMock.reset(tagManagerMock);
  }

  @Test
  public void testGetPropertyValue() {

    DeviceImpl device = new DeviceImpl(1000L, "test_device", 1L, "test_device_class", tagManagerMock);
    ClientDataTagImpl cdt1 = new ClientDataTagImpl(200000L);

    // Make a map with real values
    Map<String, ClientDataTagValue> propertyValues = new HashMap<String, ClientDataTagValue>();
    propertyValues.put("test_property_1", cdt1);

    device.setPropertyValues(propertyValues);

    // Expect the device to never, ever call the server

    // Setup is finished, need to activate the mock
    EasyMock.replay(tagManagerMock);

    ClientDataTagValue property = device.getPropertyValue("test_property_1");
    Assert.assertTrue(property.getId() == cdt1.getId());

    // Verify that everything happened as expected
    EasyMock.verify(tagManagerMock);
    // Reset the mock
    EasyMock.reset(tagManagerMock);
  }

  @Test
  public void testLazyLoadPropertyValues() {

    DeviceImpl device = new DeviceImpl(1000L, "test_device", 1L, "test_device_class", tagManagerMock);

    // Make a map with only IDs, no values
    Map<String, Long> propertyValueIds = new HashMap<String, Long>();
    propertyValueIds.put("test_property_1", 100000L);
    propertyValueIds.put("test_property_2", 200000L);
    propertyValueIds.put("test_property_3", 300000L);

    device.setPropertyValues(propertyValueIds);

    ClientDataTagImpl cdt1 = new ClientDataTagImpl(100000L);
    ClientDataTagImpl cdt2 = new ClientDataTagImpl(200000L);
    ClientDataTagImpl cdt3 = new ClientDataTagImpl(300000L);

    // Expect the device to get three data tags
    EasyMock.expect(tagManagerMock.getDataTag(EasyMock.<Long> anyObject())).andReturn(cdt1).once();
    EasyMock.expect(tagManagerMock.getDataTag(EasyMock.<Long> anyObject())).andReturn(cdt2).once();
    EasyMock.expect(tagManagerMock.getDataTag(EasyMock.<Long> anyObject())).andReturn(cdt3).once();

    // Setup is finished, need to activate the mock
    EasyMock.replay(tagManagerMock);

    Map<String, ClientDataTagValue> properties = device.getPropertyValues();
    Assert.assertTrue(properties.get("test_property_1").getId() == cdt1.getId());
    Assert.assertTrue(properties.get("test_property_2").getId() == cdt2.getId());
    Assert.assertTrue(properties.get("test_property_3").getId() == cdt3.getId());

    // Verify that everything happened as expected
    EasyMock.verify(tagManagerMock);
    // Reset the mock
    EasyMock.reset(tagManagerMock);
  }

  @Test
  public void testGetPropertyValues() {

    DeviceImpl device = new DeviceImpl(1000L, "test_device", 1L, "test_device_class", tagManagerMock);
    ClientDataTagImpl cdt1 = new ClientDataTagImpl(100000L);
    ClientDataTagImpl cdt2 = new ClientDataTagImpl(200000L);
    ClientDataTagImpl cdt3 = new ClientDataTagImpl(300000L);

    // Make a map with real values
    Map<String, ClientDataTagValue> propertyValues = new HashMap<String, ClientDataTagValue>();
    propertyValues.put("test_property_1", cdt1);
    propertyValues.put("test_property_2", cdt2);
    propertyValues.put("test_property_3", cdt3);

    device.setPropertyValues(propertyValues);

    // Expect the device to never, ever call the server

    // Setup is finished, need to activate the mock
    EasyMock.replay(tagManagerMock);

    Map<String, ClientDataTagValue> properties = device.getPropertyValues();
    Assert.assertTrue(properties.get("test_property_1").getId() == cdt1.getId());
    Assert.assertTrue(properties.get("test_property_2").getId() == cdt2.getId());
    Assert.assertTrue(properties.get("test_property_3").getId() == cdt3.getId());

    // Verify that everything happened as expected
    EasyMock.verify(tagManagerMock);
    // Reset the mock
    EasyMock.reset(tagManagerMock);
  }

  @Test
  public void testPropertyUpdate() throws RuleFormatException {

    DeviceImpl device = new DeviceImpl(1000L, "test_device", 1L, "test_device_class", tagManagerMock);
    ClientDataTagImpl cdt1 = new ClientDataTagImpl(100000L);
    cdt1.update(DeviceTestUtils.createValidTransferTag(cdt1.getId(), "test_tag_name", 1L));

    Map<String, ClientDataTagValue> propertyValues = new HashMap<String, ClientDataTagValue>();
    propertyValues.put("test_property_name", cdt1);
    device.setPropertyValues(propertyValues);

    device.addDeviceUpdateListener(new DeviceUpdateListener() {
      @Override
      public void onUpdate(Device device, String propertyValueName) {
        // do nothing
      }
    });

    // Update the tag value
    ClientDataTagImpl cdt2 = new ClientDataTagImpl(100000L);
    cdt2.update(DeviceTestUtils.createValidTransferTag(cdt1.getId(), cdt1.getName(), 2L));
    device.onUpdate(cdt2);

    // Check that the device stored the new update properly
    Assert.assertTrue(((Long) device.getPropertyValue("test_property_name").getValue()) == 2L);
  }
}
