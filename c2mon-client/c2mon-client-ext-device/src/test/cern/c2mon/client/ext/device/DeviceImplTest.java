/*******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 *
 * Copyright (C) 2004 - 2014 CERN. This program is free software; you can
 * redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version. This program is distributed
 * in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received
 * a copy of the GNU General Public License along with this program; if not,
 * write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA 02111-1307, USA.
 *
 * Author: TIM team, tim.support@cern.ch
 ******************************************************************************/
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
import cern.c2mon.client.common.tag.ClientDataTagValue;
import cern.c2mon.client.core.C2monCommandManager;
import cern.c2mon.client.core.C2monTagManager;
import cern.c2mon.client.core.tag.ClientDataTagImpl;
import cern.c2mon.client.core.tag.ClientRuleTag;
import cern.c2mon.client.ext.device.property.Category;
import cern.c2mon.client.ext.device.property.ClientConstantValue;
import cern.c2mon.client.ext.device.property.Field;
import cern.c2mon.client.ext.device.property.FieldImpl;
import cern.c2mon.client.ext.device.property.Property;
import cern.c2mon.client.ext.device.property.PropertyImpl;
import cern.c2mon.client.ext.device.util.DeviceTestUtils;
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

    Property property = device.getProperty("test_property_1");
    // Manually set the tag manager to the mock one
    ((PropertyImpl) property).setTagManager(tagManagerMock);
    Assert.assertTrue(property.getTagId().equals(cdt1.getId()));
    Assert.assertTrue(property.getTag() != null);
    Assert.assertTrue(property.getFields().size() == 0);
    Assert.assertTrue(property.getCategory() == Category.DATATAG);

    // Verify that everything happened as expected
    EasyMock.verify(tagManagerMock);
  }

  @Test
  public void testGetDeviceProperty() throws RuleFormatException {
    // Reset the mock
    EasyMock.reset(tagManagerMock);

    DeviceImpl device = getTestDevice();
    ClientDataTagImpl cdt1 = new ClientDataTagImpl(100000L);
    ClientConstantValue<String> ccv1 = new ClientConstantValue<>("Mr. Administrator", null);
    ClientRuleTag crt = new ClientRuleTag(new SimpleRuleExpression("(#123 + #234) / 2"), Float.class);
    ClientConstantValue ccv2 = new ClientConstantValue(4, Integer.class);

    // Make a map with real values
    Map<String, ClientDataTagValue> deviceProperties = new HashMap<String, ClientDataTagValue>();
    deviceProperties.put("cpuLoadInPercent", cdt1);
    deviceProperties.put("responsiblePerson", ccv1);
    deviceProperties.put("someCalculations", crt);
    deviceProperties.put("numCores", ccv2);

    device.setDeviceProperties(deviceProperties);

    for (Property property : device.getProperties()) {
      ((PropertyImpl) property).setTagManager(tagManagerMock);
    }

    // Expect the device to check if the rule tag is subscribed, but make no
    // other calls
    EasyMock.expect(tagManagerMock.isSubscribed(EasyMock.<DataTagUpdateListener> anyObject())).andReturn(true).times(2);

    // Setup is finished, need to activate the mock
    EasyMock.replay(tagManagerMock);

    // Attempt to retrieve field value from non-mapped property
    Property property = device.getProperty("nonexistent");
    Assert.assertNull(property);

    property = device.getProperty("cpuLoadInPercent");
    Assert.assertTrue(property.getTagId() == cdt1.getId());
    Assert.assertTrue(property.getTag() != null && property.getTag() instanceof ClientDataTagValue);
    Assert.assertTrue(property.getFields().size() == 0);
    Assert.assertTrue(property.getCategory() == Category.DATATAG);
    property = device.getProperty("responsiblePerson");
    Assert.assertNull(property.getTagId());
    Assert.assertTrue(property.getCategory() == Category.CONSTANT_VALUE);
    Assert.assertTrue(property.getTag() != null && property.getTag() instanceof ClientConstantValue<?>);
    property = device.getProperty("someCalculations");
    Assert.assertNull(property.getTagId());
    Assert.assertTrue(property.getCategory() == Category.CLIENT_RULE);
    Assert.assertTrue(property.getTag() != null && property.getTag() instanceof ClientRuleTag<?>);
    property = device.getProperty("numCores");
    Assert.assertNull(property.getTagId());
    Assert.assertTrue(property.getCategory() == Category.CONSTANT_VALUE);
    Assert.assertTrue(property.getTag() != null && property.getTag() instanceof ClientConstantValue<?>);

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
    EasyMock.expect(tagManagerMock.isSubscribed(EasyMock.<DataTagUpdateListener> anyObject())).andReturn(false).anyTimes();
    // Expect the device to get the tags inside the rule tags
    EasyMock.expect(tagManagerMock.getDataTags(EasyMock.<List<Long>> anyObject())).andReturn(ruleResultTags).anyTimes();

    // Setup is finished, need to activate the mock
    EasyMock.replay(tagManagerMock);

    device.setDeviceProperties(deviceProperties);

    for (Property property : device.getProperties()) {
      ((PropertyImpl) property).setTagManager(tagManagerMock);
    }

    Assert.assertTrue(device.getProperty("cpuLoadInPercent").getTag().getId().equals(cdt1.getId()));
    Assert.assertTrue(device.getProperty("responsiblePerson").getTag().getId().equals(ccv1.getId()));
    Assert.assertTrue(device.getProperty("responsiblePerson").getTag().getType() == String.class);
    Assert.assertTrue(device.getProperty("responsiblePerson").getTag().getValue().equals(ccv1.getValue()));
    Assert.assertTrue(device.getProperty("someCalculations").getTag().getId().equals(crt.getId()));
    Assert.assertTrue(device.getProperty("someCalculations").getTag().getRuleExpression().getExpression().equals(crt.getRuleExpression().getExpression()));
    Assert.assertTrue(device.getProperty("someCalculations").getTag().getType().equals(crt.getType()));
    Assert.assertTrue(device.getProperty("someCalculations").getTag().getValue().equals(1.5F));
    Assert.assertTrue(device.getProperty("numCores").getTag().getId().equals(ccv2.getId()));
    Assert.assertTrue(device.getProperty("numCores").getTag().getType().equals(ccv2.getType()));
    Assert.assertTrue(device.getProperty("numCores").getTag().getValue().equals(ccv2.getValue()));

    Property acquisition = device.getProperty("acquisition");
    Assert.assertTrue(acquisition.getField("cpuLoadInPercent2").getTag().getId().equals(cdt2.getId()));
    Assert.assertTrue(acquisition.getField("responsiblePerson2").getTag().getId().equals(ccv1.getId()));
    Assert.assertTrue(acquisition.getField("responsiblePerson2").getTag().getType() == String.class);
    Assert.assertTrue(acquisition.getField("responsiblePerson2").getTag().getValue().equals(ccv1.getValue()));
    Assert.assertTrue(acquisition.getField("someCalculations2").getTag().getId().equals(crt.getId()));
    Assert.assertTrue(acquisition.getField("someCalculations2").getTag().getRuleExpression().getExpression()
        .equals(crt.getRuleExpression().getExpression()));
    Assert.assertTrue(acquisition.getField("someCalculations2").getTag().getType().equals(crt.getType()));
    Assert.assertTrue(acquisition.getField("someCalculations2").getTag().getValue().equals(1.5F));
    Assert.assertTrue(acquisition.getField("numCores2").getTag().getId().equals(ccv2.getId()));
    Assert.assertTrue(acquisition.getField("numCores2").getTag().getType().equals(ccv2.getType()));
    Assert.assertTrue(acquisition.getField("numCores2").getTag().getValue().equals(ccv2.getValue()));

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

    Assert.assertTrue(device.getProperty("test_property_1").getTagId() == cdt1.getId());
    Assert.assertTrue(device.getProperty("test_property_2").getTagId() == cdt2.getId());
    Assert.assertTrue(device.getProperty("test_property_3").getTagId() == cdt3.getId());

    // Verify that everything happened as expected
    EasyMock.verify(tagManagerMock);
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

    for (Property property : device.getProperties()) {
      ((PropertyImpl) property).setTagManager(tagManagerMock);
    }

    ClientRuleTag rule = (ClientRuleTag) device.getProperty("test_property_rule_name").getTag();
    Assert.assertNotNull(rule);
    Assert.assertTrue(rule.isValid());
    Assert.assertTrue((Float) rule.getValue() == 1F);

    // Verify that everything happened as expected
    EasyMock.verify(tagManagerMock);
  }

  @Test
  public void testGetNonexistentProperty() {
    DeviceImpl device = getTestDevice();
    Property value = device.getProperty("nonexistent");
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

    Map<String, Field> fields = new HashMap<>();
    fields.put("cpuLoadInPercent", new FieldImpl("cpuLoadInPercent", Category.DATATAG, cdt1));
    fields.put("responsiblePerson", new FieldImpl("responsiblePerson", Category.CONSTANT_VALUE, ccv1));
    fields.put("someCalculations", new FieldImpl("someCalculations", Category.CLIENT_RULE, crt));
    fields.put("numCores", new FieldImpl("numCores", Category.CONSTANT_VALUE, ccv2));

    HashMap<String, Property> properties = new HashMap<>();
    properties.put("acquisition", new PropertyImpl("acquisition", Category.MAPPED_PROPERTY, fields));

    device.setDeviceProperties(properties);

    for (Property property : device.getProperties()) {
      ((PropertyImpl) property).setTagManager(tagManagerMock);
    }

    // Expect the device to check if the rule tag is subscribed, but make no
    // other calls
    EasyMock.expect(tagManagerMock.isSubscribed(EasyMock.<DataTagUpdateListener> anyObject())).andReturn(true).times(1);

    // Setup is finished, need to activate the mock
    EasyMock.replay(tagManagerMock);

    Property property = device.getProperty("acquisition");
    Field field = property.getField("cpuLoadInPercent");
    Assert.assertTrue(field.getTagId() == cdt1.getId());
    field = property.getField("responsiblePerson");
    Assert.assertTrue(field.getTag().getValue() == ccv1.getValue());
    field = property.getField("someCalculations");
    Assert.assertTrue(field.getTag().getRuleExpression().getExpression() == crt.getRuleExpression().getExpression());
    field = property.getField("numCores");
    Assert.assertTrue(field.getTag().getValue() == ccv2.getValue());

    // Verify that everything happened as expected
    EasyMock.verify(tagManagerMock);
  }

  @Test
  public void testLazyLoadDevicePropertyFields() throws RuleFormatException, ClassNotFoundException {
    // Reset the mock
    EasyMock.reset(tagManagerMock);

    DeviceImpl device = getTestDevice();

    List<DeviceProperty> deviceFields = new ArrayList<>();
    deviceFields.add(new DeviceProperty(1L, "cpuLoadInPercent", "100000", "tagId", null));
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
    EasyMock.expect(tagManagerMock.isSubscribed(EasyMock.<DataTagUpdateListener> anyObject())).andReturn(false).anyTimes();
    // Expect the device to get the tags inside the rule tag
    EasyMock.expect(tagManagerMock.getDataTags(EasyMock.<List<Long>> anyObject())).andReturn(ruleResultTags).anyTimes();

    // Setup is finished, need to activate the mock
    EasyMock.replay(tagManagerMock);

    List<DeviceProperty> deviceProperties = new ArrayList<>();
    deviceProperties.add(new DeviceProperty(5L, "acquisition", "mappedProperty", deviceFields));
    device.setDeviceProperties(deviceProperties);

    for (Property property : device.getProperties()) {
      ((PropertyImpl) property).setTagManager(tagManagerMock);
    }

    Property fields = device.getProperty("acquisition");

    ClientDataTagValue v = fields.getField("cpuLoadInPercent").getTag();
    Assert.assertTrue(fields.getField("cpuLoadInPercent").getTag().getId().equals(cdt.getId()));
    Assert.assertTrue(fields.getField("responsiblePerson").getTag().getId().equals(ccv1.getId()));
    Assert.assertTrue(fields.getField("responsiblePerson").getTag().getType() == String.class);
    Assert.assertTrue(fields.getField("responsiblePerson").getTag().getValue().equals(ccv1.getValue()));
    Assert.assertTrue(fields.getField("someCalculations").getTag().getId().equals(crt.getId()));
    Assert.assertTrue(fields.getField("someCalculations").getTag().getRuleExpression().getExpression().equals(crt.getRuleExpression().getExpression()));
    Assert.assertTrue(fields.getField("someCalculations").getTag().getType().equals(crt.getType()));
    Assert.assertTrue(fields.getField("someCalculations").getTag().getValue().equals(1.5F));
    Assert.assertTrue(fields.getField("numCores").getTag().getId().equals(ccv2.getId()));
    Assert.assertTrue(fields.getField("numCores").getTag().getType().equals(ccv2.getType()));
    Assert.assertTrue(fields.getField("numCores").getTag().getValue().equals(ccv2.getValue()));

    // Verify that everything happened as expected
    EasyMock.verify(tagManagerMock);
  }

  private DeviceImpl getTestDevice() {
    return new DeviceImpl(1000L, "test_device", 1L, "test_device_class");
  }
}
