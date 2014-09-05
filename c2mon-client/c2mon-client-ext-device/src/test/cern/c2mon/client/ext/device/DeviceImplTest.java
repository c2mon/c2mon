/**
 *
 */
package cern.c2mon.client.ext.device;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import cern.c2mon.client.common.listener.DataTagUpdateListener;
import cern.c2mon.client.common.tag.ClientCommandTag;
import cern.c2mon.client.common.tag.ClientDataTagValue;
import cern.c2mon.client.core.C2monCommandManager;
import cern.c2mon.client.core.C2monTagManager;
import cern.c2mon.client.core.tag.ClientCommandTagImpl;
import cern.c2mon.client.core.tag.ClientDataTagImpl;
import cern.c2mon.client.core.tag.ClientRuleTag;
import cern.c2mon.client.ext.device.tag.ClientConstantValueTag;
import cern.c2mon.client.ext.device.util.DeviceTestUtils;
import cern.c2mon.shared.client.device.CommandValue;
import cern.c2mon.shared.client.device.PropertyValue;
import cern.c2mon.shared.rule.RuleFormatException;
import cern.c2mon.shared.rule.SimpleRuleExpression;

/**
 * @author Justin Lewis Salmon
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "classpath:cern/c2mon/client/ext/device/config/c2mon-deviceimpl-test.xml" })
public class DeviceImplTest {

  /** Mocked components */
  @Autowired
  private C2monTagManager tagManagerMock;

  @Autowired
  private C2monCommandManager commandManagerMock;

  @Test
  public void testLazyLoadPropertyValue() throws RuleFormatException, ClassNotFoundException {
    // Reset the mock
    EasyMock.reset(tagManagerMock);

    DeviceImpl device = new DeviceImpl(1000L, "test_device", 1L, "test_device_class", tagManagerMock, commandManagerMock);

    List<PropertyValue> propertyValues = new ArrayList<>();
    propertyValues.add(new PropertyValue("test_property_1", 100000L, null, null, null));

    device.setPropertyValues(propertyValues);

    // Expect the device to get a single data tag
    ClientDataTagImpl cdt1 = new ClientDataTagImpl(100000L);
    EasyMock.expect(tagManagerMock.getDataTag(EasyMock.<Long> anyObject())).andReturn(cdt1).once();

    // Setup is finished, need to activate the mock
    EasyMock.replay(tagManagerMock);

    ClientDataTagValue property = device.getPropertyValue("test_property_1");
    Assert.assertTrue(property.getId() == cdt1.getId());

    // Verify that everything happened as expected
    EasyMock.verify(tagManagerMock);
  }

  @Test
  public void testGetPropertyValue() throws RuleFormatException {
    // Reset the mock
    EasyMock.reset(tagManagerMock);

    DeviceImpl device = new DeviceImpl(1000L, "test_device", 1L, "test_device_class", tagManagerMock, commandManagerMock);
    ClientDataTagImpl cdt1 = new ClientDataTagImpl(100000L);
    ClientConstantValueTag ccv1 = new ClientConstantValueTag("Mr. Administrator", null);
    ClientRuleTag crt = new ClientRuleTag(new SimpleRuleExpression("(#123 + #234) / 2"), Float.class);
    ClientConstantValueTag ccv2 = new ClientConstantValueTag(4, Integer.class);

    // Make a map with real values
    Map<String, ClientDataTagValue> propertyValues = new HashMap<String, ClientDataTagValue>();
    propertyValues.put("cpuLoadInPercent", cdt1);
    propertyValues.put("responsiblePerson", ccv1);
    propertyValues.put("someCalculations", crt);
    propertyValues.put("numCores", ccv2);

    device.setPropertyValues(propertyValues);

    // Expect the device to check if the rule tag is subscribed, but make no
    // other calls
    EasyMock.expect(tagManagerMock.isSubscribed(EasyMock.<DataTagUpdateListener> anyObject())).andReturn(true);

    // Setup is finished, need to activate the mock
    EasyMock.replay(tagManagerMock);

    ClientDataTagValue property = device.getPropertyValue("cpuLoadInPercent");
    Assert.assertTrue(property.getId() == cdt1.getId());
    property = device.getPropertyValue("responsiblePerson");
    Assert.assertTrue(property.getId() == ccv1.getId());
    property = device.getPropertyValue("someCalculations");
    Assert.assertTrue(property.getId() == crt.getId());
    property = device.getPropertyValue("numCores");
    Assert.assertTrue(property.getId() == ccv2.getId());

    // Verify that everything happened as expected
    EasyMock.verify(tagManagerMock);
  }

  @Test
  public void testLazyLoadPropertyValues() throws RuleFormatException, ClassNotFoundException {
    // Reset the mock
    EasyMock.reset(tagManagerMock);

    DeviceImpl device = new DeviceImpl(1000L, "test_device", 1L, "test_device_class", tagManagerMock, commandManagerMock);

    List<PropertyValue> propertyValues = new ArrayList<>();
    propertyValues.add(new PropertyValue("cpuLoadInPercent", 100000L, null, null, null));
    propertyValues.add(new PropertyValue("responsiblePerson", null, null, "Mr. Administrator", null));
    propertyValues.add(new PropertyValue("someCalculations", null, "(#123 + #234) / 2", null, "Float"));
    propertyValues.add(new PropertyValue("numCores", null, null, "4", "Integer"));

    ClientDataTagImpl cdt = new ClientDataTagImpl(100000L);
    ClientConstantValueTag ccv1 = new ClientConstantValueTag("Mr. Administrator", null);
    ClientRuleTag crt = new ClientRuleTag(new SimpleRuleExpression("(#123 + #234) / 2"), Float.class);
    ClientConstantValueTag ccv2 = new ClientConstantValueTag(4, Integer.class);

    List<ClientDataTagValue> ruleResultTags = new ArrayList<>();
    ClientDataTagImpl cdt1 = new ClientDataTagImpl(123L);
    ClientDataTagImpl cdt2 = new ClientDataTagImpl(234L);
    cdt1.update(DeviceTestUtils.createValidTransferTag(123L, "test_tag_1", 1F));
    cdt2.update(DeviceTestUtils.createValidTransferTag(234L, "test_tag_2", 2F));
    ruleResultTags.add(cdt1);
    ruleResultTags.add(cdt2);

    // Expect the device to get one data tag
    EasyMock.expect(tagManagerMock.getDataTag(EasyMock.<Long> anyObject())).andReturn(cdt).once();
    // Expect the device to check if the rule tag is subscribed
    EasyMock.expect(tagManagerMock.isSubscribed(EasyMock.<DataTagUpdateListener> anyObject())).andReturn(false);
    // Expect the device to get the tags inside the rule tag
    EasyMock.expect(tagManagerMock.getDataTags(EasyMock.<List<Long>> anyObject())).andReturn(ruleResultTags).once();

    // Setup is finished, need to activate the mock
    EasyMock.replay(tagManagerMock);

    device.setPropertyValues(propertyValues);

    Map<String, ClientDataTagValue> properties = device.getPropertyValues();

    Assert.assertTrue(properties.get("cpuLoadInPercent").getId().equals(cdt.getId()));
    Assert.assertTrue(properties.get("responsiblePerson").getId().equals(ccv1.getId()));
    Assert.assertTrue(properties.get("responsiblePerson").getType() == String.class);
    Assert.assertTrue(properties.get("responsiblePerson").getValue().equals(ccv1.getValue()));
    Assert.assertTrue(properties.get("someCalculations").getId().equals(crt.getId()));
    Assert.assertTrue(properties.get("someCalculations").getRuleExpression().getExpression().equals(crt.getRuleExpression().getExpression()));
    Assert.assertTrue(properties.get("someCalculations").getType().equals(crt.getType()));
    Assert.assertTrue(properties.get("someCalculations").getValue().equals(1.5F));
    Assert.assertTrue(properties.get("numCores").getId().equals(ccv2.getId()));
    Assert.assertTrue(properties.get("numCores").getType().equals(ccv2.getType()));
    Assert.assertTrue(properties.get("numCores").getValue().equals(ccv2.getValue()));

    // Verify that everything happened as expected
    EasyMock.verify(tagManagerMock);

  }

  @Test
  public void testGetPropertyValues() {
    // Reset the mock
    EasyMock.reset(tagManagerMock);

    DeviceImpl device = new DeviceImpl(1000L, "test_device", 1L, "test_device_class", tagManagerMock, commandManagerMock);
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
  }

  @Test
  public void testPropertyUpdate() throws RuleFormatException {

    DeviceImpl device = new DeviceImpl(1000L, "test_device", 1L, "test_device_class", tagManagerMock, commandManagerMock);
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

  @Test
  public void testRuleUpdate() throws RuleFormatException, ClassNotFoundException, InterruptedException {
    // Reset the mock
    EasyMock.reset(commandManagerMock);

    final DeviceImpl device = new DeviceImpl(1000L, "test_device", 1L, "test_device_class", tagManagerMock, commandManagerMock);

    Collection<ClientDataTagValue> dataTagValues = new ArrayList<>();
    ClientDataTagImpl cdt1 = new ClientDataTagImpl(234L);
    ClientDataTagImpl cdt2 = new ClientDataTagImpl(345L);
    cdt1.update(DeviceTestUtils.createValidTransferTag(234L, "test_tag_1", 0.5F));
    cdt2.update(DeviceTestUtils.createValidTransferTag(345L, "test_tag_2", 1.5F));
    dataTagValues.add(cdt1);
    dataTagValues.add(cdt2);

    // Expect the device to check if the rule is already subscribed
    EasyMock.expect(tagManagerMock.isSubscribed(EasyMock.<DataTagUpdateListener> anyObject())).andReturn(false);
    // Expect the device to get the tags inside the rule
    EasyMock.expect(tagManagerMock.getDataTags(EasyMock.<Set<Long>> anyObject())).andReturn(dataTagValues);

    // Setup is finished, need to activate the mock
    EasyMock.replay(tagManagerMock);

    final List<PropertyValue> propertyValues = new ArrayList<>();
    propertyValues.add(new PropertyValue("test_property_rule_name", null, "(#234 + #345) / 2", null, "Float"));
    device.setPropertyValues(propertyValues);

    ClientRuleTag rule = (ClientRuleTag) device.getPropertyValue("test_property_rule_name");
    Assert.assertNotNull(rule);
    Assert.assertTrue(rule.isValid());
    Assert.assertTrue((Float) rule.getValue() == 1F);

    // Verify that everything happened as expected
    EasyMock.verify(tagManagerMock);
  }

  @Test
  public void testGetCommandValue() {
    // Reset the mock
    EasyMock.reset(commandManagerMock);

    DeviceImpl device = new DeviceImpl(1000L, "test_device", 1L, "test_device_class", tagManagerMock, commandManagerMock);
    ClientCommandTag cct1 = new ClientCommandTagImpl(-1L);
    ClientCommandTag cct2 = new ClientCommandTagImpl(-2L);

    // Expect the device to call the command tag manager
    EasyMock.expect(commandManagerMock.getCommandTag(EasyMock.<Long> anyObject())).andReturn(cct1);
    EasyMock.expect(commandManagerMock.getCommandTag(EasyMock.<Long> anyObject())).andReturn(cct2);

    // Setup is finished, need to activate the mock
    EasyMock.replay(commandManagerMock);

    List<CommandValue> commandValues = new ArrayList<>();
    commandValues.add(new CommandValue("test_command_1", -1L));
    commandValues.add(new CommandValue("test_command_2", -2L));
    device.setCommandValues(commandValues);

    Assert.assertTrue(device.getCommandValue("test_command_1").getId().equals(-1L));
    Assert.assertTrue(device.getCommandValue("test_command_2").getId().equals(-2L));

    // Verify that everything happened as expected
    EasyMock.verify(commandManagerMock);
  }
}
