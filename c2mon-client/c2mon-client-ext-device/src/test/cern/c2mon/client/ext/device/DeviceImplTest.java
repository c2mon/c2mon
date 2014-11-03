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
import cern.c2mon.client.ext.device.property.ClientConstantValue;
import cern.c2mon.client.ext.device.property.ClientDeviceProperty;
import cern.c2mon.client.ext.device.property.ClientDevicePropertyImpl;
import cern.c2mon.client.ext.device.property.MappedPropertyException;
import cern.c2mon.client.ext.device.property.PropertyInfo;
import cern.c2mon.client.ext.device.util.DeviceTestUtils;
import cern.c2mon.shared.client.device.DeviceCommand;
import cern.c2mon.shared.client.device.DeviceProperty;
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
  public void testLazyLoadDeviceProperty() throws RuleFormatException, ClassNotFoundException {
    // Reset the mock
    EasyMock.reset(tagManagerMock);

    DeviceImpl device = getTestDevice();

    List<DeviceProperty> deviceProperties = new ArrayList<>();
    deviceProperties.add(new DeviceProperty(1L, "test_property_1", "100000", "tagId", null));

    device.setDeviceProperties(deviceProperties);

    // Expect the device to get a single data tag
    ClientDataTagImpl cdt1 = new ClientDataTagImpl(100000L);
    EasyMock.expect(tagManagerMock.getDataTag(EasyMock.<Long> anyObject())).andReturn(cdt1).once();

    // Setup is finished, need to activate the mock
    EasyMock.replay(tagManagerMock);

    ClientDataTagValue property = device.getProperty(new PropertyInfo("test_property_1"));
    Assert.assertTrue(property.getId() == cdt1.getId());

    // Verify that everything happened as expected
    EasyMock.verify(tagManagerMock);
  }

  @Test
  public void testGetDeviceProperty() throws RuleFormatException {
    // Reset the mock
    EasyMock.reset(tagManagerMock);

    DeviceImpl device = getTestDevice();
    ClientDataTagImpl cdt1 = new ClientDataTagImpl(100000L);
    ClientConstantValue ccv1 = new ClientConstantValue("Mr. Administrator", null);
    ClientRuleTag crt = new ClientRuleTag(new SimpleRuleExpression("(#123 + #234) / 2"), Float.class);
    ClientConstantValue ccv2 = new ClientConstantValue(4, Integer.class);

    // Make a map with real values
    Map<String, ClientDataTagValue> deviceProperties = new HashMap<String, ClientDataTagValue>();
    deviceProperties.put("cpuLoadInPercent", cdt1);
    deviceProperties.put("responsiblePerson", ccv1);
    deviceProperties.put("someCalculations", crt);
    deviceProperties.put("numCores", ccv2);

    device.setDeviceProperties(deviceProperties);

    // Expect the device to check if the rule tag is subscribed, but make no
    // other calls
    EasyMock.expect(tagManagerMock.isSubscribed(EasyMock.<DataTagUpdateListener> anyObject())).andReturn(true);

    // Setup is finished, need to activate the mock
    EasyMock.replay(tagManagerMock);

    try {
      // Attempt to retrieve field value from non-mapped property - should throw exception
      ClientDataTagValue property = device.getProperty(new PropertyInfo("cpuLoadInPercent", "field"));
      Assert.fail("Exception not thrown");
    } catch (MappedPropertyException e) {
    }

    ClientDataTagValue property = device.getProperty(new PropertyInfo("cpuLoadInPercent"));
    Assert.assertTrue(property.getId() == cdt1.getId());
    property = device.getProperty(new PropertyInfo("responsiblePerson"));
    Assert.assertTrue(property.getId() == ccv1.getId());
    property = device.getProperty(new PropertyInfo("someCalculations"));
    Assert.assertTrue(property.getId() == crt.getId());
    property = device.getProperty(new PropertyInfo("numCores"));
    Assert.assertTrue(property.getId() == ccv2.getId());

    // Verify that everything happened as expected
    EasyMock.verify(tagManagerMock);
  }

  @Test
  public void testLazyLoadDeviceProperties() throws RuleFormatException, ClassNotFoundException {
    // Reset the mock
    EasyMock.reset(tagManagerMock);

    DeviceImpl device = getTestDevice();

    List<DeviceProperty> deviceProperties = new ArrayList<>();
    deviceProperties.add(new DeviceProperty(1L, "cpuLoadInPercent", "10000", "tagId", null));
    deviceProperties.add(new DeviceProperty(2L, "responsiblePerson", "Mr. Administrator", "constantValue", null));
    deviceProperties.add(new DeviceProperty(3L, "someCalculations", "(#123 + #234) / 2", "clientRule", "Float"));
    deviceProperties.add(new DeviceProperty(4L, "numCores", "4", "constantValue", "Integer"));

    List<DeviceProperty> fields = new ArrayList<>();
    fields.add(new DeviceProperty(1L, "cpuLoadInPercent2", "10001", "tagId", null));
    fields.add(new DeviceProperty(2L, "responsiblePerson2", "Mr. Administrator", "constantValue", null));
    fields.add(new DeviceProperty(3L, "someCalculations2", "(#123 + #234) / 2", "clientRule", "Float"));
    fields.add(new DeviceProperty(4L, "numCores2", "4", "constantValue", "Integer"));

    DeviceProperty propertyWithFields = new DeviceProperty(5L, "acquisition", "mappedProperty", fields);
    deviceProperties.add(propertyWithFields);

    ClientDataTagImpl cdt1 = new ClientDataTagImpl(10000L);
    ClientDataTagImpl cdt2 = new ClientDataTagImpl(10001L);
    ClientConstantValue ccv1 = new ClientConstantValue("Mr. Administrator", null);
    ClientRuleTag crt = new ClientRuleTag(new SimpleRuleExpression("(#123 + #234) / 2"), Float.class);
    ClientConstantValue ccv2 = new ClientConstantValue(4, Integer.class);

    List<ClientDataTagValue> ruleResultTags = new ArrayList<>();
    ClientDataTagImpl rrt1 = new ClientDataTagImpl(123L);
    ClientDataTagImpl rrt2 = new ClientDataTagImpl(234L);
    rrt1.update(DeviceTestUtils.createValidTransferTag(123L, "test_tag_1", 1F));
    rrt2.update(DeviceTestUtils.createValidTransferTag(234L, "test_tag_2", 2F));
    ruleResultTags.add(rrt1);
    ruleResultTags.add(rrt2);

    // Expect the device to get two data tags (one property, one field)
    EasyMock.expect(tagManagerMock.getDataTag(10000L)).andReturn(cdt1);
    EasyMock.expect(tagManagerMock.getDataTag(10001L)).andReturn(cdt2);
    // Expect the device to check if the rule tags are subscribed
    EasyMock.expect(tagManagerMock.isSubscribed(EasyMock.<DataTagUpdateListener> anyObject())).andReturn(false).times(2);
    // Expect the device to get the tags inside the rule tags
    EasyMock.expect(tagManagerMock.getDataTags(EasyMock.<List<Long>> anyObject())).andReturn(ruleResultTags).times(2);

    // Setup is finished, need to activate the mock
    EasyMock.replay(tagManagerMock);

    device.setDeviceProperties(deviceProperties);

    Map<String, ClientDataTagValue> properties = device.getProperties();

    Assert.assertTrue(properties.get("cpuLoadInPercent").getId().equals(cdt1.getId()));
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
    Assert.assertTrue(properties.get("acquisition") == null);

    Map<String, ClientDataTagValue> acquisition = device.getMappedProperty("acquisition");
    Assert.assertTrue(acquisition.get("cpuLoadInPercent2").getId().equals(cdt2.getId()));
    Assert.assertTrue(acquisition.get("responsiblePerson2").getId().equals(ccv1.getId()));
    Assert.assertTrue(acquisition.get("responsiblePerson2").getType() == String.class);
    Assert.assertTrue(acquisition.get("responsiblePerson2").getValue().equals(ccv1.getValue()));
    Assert.assertTrue(acquisition.get("someCalculations2").getId().equals(crt.getId()));
    Assert.assertTrue(acquisition.get("someCalculations2").getRuleExpression().getExpression().equals(crt.getRuleExpression().getExpression()));
    Assert.assertTrue(acquisition.get("someCalculations2").getType().equals(crt.getType()));
    Assert.assertTrue(acquisition.get("someCalculations2").getValue().equals(1.5F));
    Assert.assertTrue(acquisition.get("numCores2").getId().equals(ccv2.getId()));
    Assert.assertTrue(acquisition.get("numCores2").getType().equals(ccv2.getType()));
    Assert.assertTrue(acquisition.get("numCores2").getValue().equals(ccv2.getValue()));

    // Verify that everything happened as expected
    EasyMock.verify(tagManagerMock);
  }

  @Test
  public void testGetDeviceProperties() {
    // Reset the mock
    EasyMock.reset(tagManagerMock);

    DeviceImpl device = getTestDevice();
    ClientDataTagImpl cdt1 = new ClientDataTagImpl(100000L);
    ClientDataTagImpl cdt2 = new ClientDataTagImpl(200000L);
    ClientDataTagImpl cdt3 = new ClientDataTagImpl(300000L);

    // Make a map with real values
    Map<String, ClientDataTagValue> deviceProperties = new HashMap<String, ClientDataTagValue>();
    deviceProperties.put("test_property_1", cdt1);
    deviceProperties.put("test_property_2", cdt2);
    deviceProperties.put("test_property_3", cdt3);

    device.setDeviceProperties(deviceProperties);

    // Expect the device to never, ever call the server

    // Setup is finished, need to activate the mock
    EasyMock.replay(tagManagerMock);

    Map<String, ClientDataTagValue> properties = device.getProperties();
    Assert.assertTrue(properties.get("test_property_1").getId() == cdt1.getId());
    Assert.assertTrue(properties.get("test_property_2").getId() == cdt2.getId());
    Assert.assertTrue(properties.get("test_property_3").getId() == cdt3.getId());

    // Verify that everything happened as expected
    EasyMock.verify(tagManagerMock);
  }

  @Test
  public void testPropertyUpdate() throws RuleFormatException {

    DeviceImpl device = getTestDevice();
    ClientDataTagImpl cdt1 = new ClientDataTagImpl(100000L);
    cdt1.update(DeviceTestUtils.createValidTransferTag(cdt1.getId(), "test_tag_name", 1L));

    Map<String, ClientDataTagValue> deviceProperties = new HashMap<String, ClientDataTagValue>();
    deviceProperties.put("test_property_name", cdt1);
    device.setDeviceProperties(deviceProperties);

    device.addDeviceUpdateListener(new DeviceUpdateListener() {
      @Override
      public void onUpdate(Device device, PropertyInfo propertyInfo) {
        // do nothing
      }
    });

    // Update the tag value
    ClientDataTagImpl cdt2 = new ClientDataTagImpl(100000L);
    cdt2.update(DeviceTestUtils.createValidTransferTag(cdt1.getId(), cdt1.getName(), 2L));
    device.onUpdate(cdt2);

    // Check that the device stored the new update properly
    Assert.assertTrue(((Long) device.getProperty(new PropertyInfo("test_property_name")).getValue()) == 2L);
  }

  @Test
  public void testRuleUpdate() throws RuleFormatException, ClassNotFoundException, InterruptedException {
    // Reset the mock
    EasyMock.reset(tagManagerMock);

    final DeviceImpl device = getTestDevice();

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

    final List<DeviceProperty> deviceProperties = new ArrayList<>();
    deviceProperties.add(new DeviceProperty(1L, "test_property_rule_name", "(#234 + #345) / 2", "clientRule", "Float"));
    device.setDeviceProperties(deviceProperties);

    ClientRuleTag rule = (ClientRuleTag) device.getProperty(new PropertyInfo("test_property_rule_name"));
    Assert.assertNotNull(rule);
    Assert.assertTrue(rule.isValid());
    Assert.assertTrue((Float) rule.getValue() == 1F);

    // Verify that everything happened as expected
    EasyMock.verify(tagManagerMock);
  }

  @Test
  public void testGetDeviceCommand() {
    // Reset the mock
    EasyMock.reset(commandManagerMock);

    DeviceImpl device = getTestDevice();
    ClientCommandTag cct1 = new ClientCommandTagImpl(-1L);
    ClientCommandTag cct2 = new ClientCommandTagImpl(-2L);

    // Expect the device to call the command tag manager
    EasyMock.expect(commandManagerMock.getCommandTag(EasyMock.<Long> anyObject())).andReturn(cct1);
    EasyMock.expect(commandManagerMock.getCommandTag(EasyMock.<Long> anyObject())).andReturn(cct2);

    // Setup is finished, need to activate the mock
    EasyMock.replay(commandManagerMock);

    List<DeviceCommand> deviceCommands = new ArrayList<>();
    deviceCommands.add(new DeviceCommand(1L, "test_command_1", "-1", "commandTagId", null));
    deviceCommands.add(new DeviceCommand(2L, "test_command_2", "-2", "commandTagId", null));
    device.setDeviceCommands(deviceCommands);

    Assert.assertTrue(device.getCommand("test_command_1").getId().equals(-1L));
    Assert.assertTrue(device.getCommand("test_command_2").getId().equals(-2L));

    // Verify that everything happened as expected
    EasyMock.verify(commandManagerMock);
  }

  @Test
  public void testGetNonexistentProperty() {
    DeviceImpl device = getTestDevice();
    ClientDataTagValue value = device.getProperty(new PropertyInfo("nonexistent"));
    Assert.assertNull(value);
  }

  @Test
  public void testGetDevicePropertyField() throws RuleFormatException {
    // Reset the mock
    EasyMock.reset(tagManagerMock);

    DeviceImpl device = getTestDevice();
    ClientDataTagImpl cdt1 = new ClientDataTagImpl(100000L);
    ClientConstantValue ccv1 = new ClientConstantValue("Mr. Administrator", null);
    ClientRuleTag crt = new ClientRuleTag(new SimpleRuleExpression("(#123 + #234) / 2"), Float.class);
    ClientConstantValue ccv2 = new ClientConstantValue(4, Integer.class);

    Map<String, ClientDeviceProperty> fields = new HashMap<>();
    fields.put("cpuLoadInPercent", new ClientDevicePropertyImpl(cdt1));
    fields.put("responsiblePerson", new ClientDevicePropertyImpl(ccv1));
    fields.put("someCalculations", new ClientDevicePropertyImpl(crt));
    fields.put("numCores", new ClientDevicePropertyImpl(ccv2));

    HashMap<String, ClientDeviceProperty> properties = new HashMap<>();
    properties.put("acquisition", new ClientDevicePropertyImpl(fields));

    device.setDeviceProperties(properties);

    // Expect the device to check if the rule tag is subscribed, but make no
    // other calls
    EasyMock.expect(tagManagerMock.isSubscribed(EasyMock.<DataTagUpdateListener> anyObject())).andReturn(true).times(2);

    // Setup is finished, need to activate the mock
    EasyMock.replay(tagManagerMock);

    try {
      // Field name not specified for mapped property - should throw exception
      ClientDataTagValue property = device.getProperty(new PropertyInfo("acquisition"));
      Assert.fail("Exception not thrown");
    } catch (MappedPropertyException e) {
    }

    ClientDataTagValue property = device.getProperty(new PropertyInfo("acquisition", "cpuLoadInPercent"));
    Assert.assertTrue(property.getId() == cdt1.getId());
    property = device.getProperty(new PropertyInfo("acquisition", "responsiblePerson"));
    Assert.assertTrue(property.getValue() == ccv1.getValue());
    property = device.getProperty(new PropertyInfo("acquisition", "someCalculations"));
    Assert.assertTrue(property.getRuleExpression().getExpression() == crt.getRuleExpression().getExpression());
    property = device.getProperty(new PropertyInfo("acquisition", "numCores"));
    Assert.assertTrue(property.getValue() == ccv2.getValue());

    HashMap<String, ClientDataTagValue> propertyFields = device.getMappedProperty("acquisition");
    property = propertyFields.get("cpuLoadInPercent");
    Assert.assertTrue(property.getId() == cdt1.getId());
    property = propertyFields.get("responsiblePerson");
    Assert.assertTrue(property.getValue() == ccv1.getValue());
    property = propertyFields.get("someCalculations");
    Assert.assertTrue(property.getRuleExpression().getExpression() == crt.getRuleExpression().getExpression());
    property = propertyFields.get("numCores");
    Assert.assertTrue(property.getValue() == ccv2.getValue());

    // Verify that everything happened as expected
    EasyMock.verify(tagManagerMock);
  }

  @Test
  public void testLazyLoadDevicePropertyFields() throws RuleFormatException, ClassNotFoundException {
    // Reset the mock
    EasyMock.reset(tagManagerMock);

    DeviceImpl device = getTestDevice();

    List<DeviceProperty> deviceFields = new ArrayList<>();
    deviceFields.add(new DeviceProperty(1L, "cpuLoadInPercent", "10000", "tagId", null));
    deviceFields.add(new DeviceProperty(2L, "responsiblePerson", "Mr. Administrator", "constantValue", null));
    deviceFields.add(new DeviceProperty(3L, "someCalculations", "(#123 + #234) / 2", "clientRule", "Float"));
    deviceFields.add(new DeviceProperty(4L, "numCores", "4", "constantValue", "Integer"));

    ClientDataTagImpl cdt = new ClientDataTagImpl(100000L);
    ClientConstantValue ccv1 = new ClientConstantValue("Mr. Administrator", null);
    ClientRuleTag crt = new ClientRuleTag(new SimpleRuleExpression("(#123 + #234) / 2"), Float.class);
    ClientConstantValue ccv2 = new ClientConstantValue(4, Integer.class);

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

    List<DeviceProperty> deviceProperties = new ArrayList<>();
    deviceProperties.add(new DeviceProperty(5L, "acquisition", "mappedProperty", deviceFields));
    device.setDeviceProperties(deviceProperties);

    Map<String, ClientDataTagValue> properties = device.getMappedProperty("acquisition");

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

  private DeviceImpl getTestDevice() {
    return new DeviceImpl(1000L, "test_device", 1L, "test_device_class", tagManagerMock, commandManagerMock);
  }
}
