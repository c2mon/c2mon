package cern.c2mon.client.core.device;
/******************************************************************************
 * Copyright (C) 2010-2016 CERN. All rights not expressly granted are reserved.
 *
 * This file is part of the CERN Control and Monitoring Platform 'C2MON'.
 * C2MON is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the license.
 *
 * C2MON is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
 * more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with C2MON. If not, see <http://www.gnu.org/licenses/>.
 *****************************************************************************/


import java.util.*;

import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.context.TestPropertySource;

import cern.c2mon.client.common.listener.BaseTagListener;
import cern.c2mon.client.common.tag.Tag;
import cern.c2mon.client.core.device.property.*;
import cern.c2mon.client.core.device.util.DeviceTestUtils;
import cern.c2mon.client.core.service.TagService;
import cern.c2mon.client.core.tag.ClientRuleTag;
import cern.c2mon.client.core.tag.TagController;
import cern.c2mon.client.core.tag.TagImpl;
import cern.c2mon.shared.client.device.DeviceProperty;
import cern.c2mon.shared.rule.RuleFormatException;
import cern.c2mon.shared.rule.SimpleRuleExpression;

/**
 * @author Justin Lewis Salmon
 */
@TestPropertySource(
    properties = {
        "c2mon.client.jms.url=vm://localhost"
    }
)
public class DeviceImplTest {

  /** Mocked components */
  private TagService tagServiceMock;

  @Before
  public void before() {
    tagServiceMock = EasyMock.createMock(TagService.class);
  }

  @Test
  public void testLazyLoadDeviceProperty() throws RuleFormatException, ClassNotFoundException {
    // Reset the mock
    EasyMock.reset(tagServiceMock);

    DeviceImpl device = getTestDevice();

    List<DeviceProperty> deviceProperties = new ArrayList<>();
    deviceProperties.add(new DeviceProperty(1L, "test_property_1", "100000", "tagId", null));

    device.setDeviceProperties(deviceProperties);

    // Expect the device to get a single data tag
    Tag cdt1 = new TagImpl(100000L);
    EasyMock.expect(tagServiceMock.get(EasyMock.<Long> anyObject())).andReturn(cdt1).once();

    // Setup is finished, need to activate the mock
    EasyMock.replay(tagServiceMock);

    Property property = device.getProperty("test_property_1");
    // Manually set the tag manager to the mock one
    ((PropertyImpl) property).setTagManager(tagServiceMock);
    Assert.assertTrue(property.getTagId().equals(cdt1.getId()));
    Assert.assertTrue(property.getTag() != null);
    Assert.assertTrue(property.getFields().size() == 0);
    Assert.assertTrue(property.getCategory() == Category.DATATAG);

    // Verify that everything happened as expected
    EasyMock.verify(tagServiceMock);
  }

  @Test
  public void testGetDeviceProperty() throws RuleFormatException {
    // Reset the mock
    EasyMock.reset(tagServiceMock);

    DeviceImpl device = getTestDevice();
    TagImpl cdt1 = new TagImpl(100000L);
    ClientConstantValue<String> ccv1 = new ClientConstantValue<>("Mr. Administrator", null);
    ClientRuleTag crt = new ClientRuleTag(new SimpleRuleExpression("(#123 + #234) / 2"), Float.class);
    ClientConstantValue ccv2 = new ClientConstantValue(4, Integer.class);

    // Make a map with real values
    Map<String, Tag> deviceProperties = new HashMap<>();
    deviceProperties.put("cpuLoadInPercent", cdt1);
    deviceProperties.put("responsiblePerson", ccv1);
    deviceProperties.put("someCalculations", crt);
    deviceProperties.put("numCores", ccv2);

    device.setDeviceProperties(deviceProperties);

    for (Property property : device.getProperties()) {
      ((PropertyImpl) property).setTagManager(tagServiceMock);
    }

    // Expect the device to check if the rule tag is subscribed, but make no
    // other calls
    EasyMock.expect(tagServiceMock.isSubscribed(EasyMock.<BaseTagListener> anyObject())).andReturn(true).times(2);

    // Setup is finished, need to activate the mock
    EasyMock.replay(tagServiceMock);

    // Attempt to retrieve field value from non-mapped property
    Property property = device.getProperty("nonexistent");
    Assert.assertNull(property);

    property = device.getProperty("cpuLoadInPercent");
    Assert.assertTrue(property.getTagId() == cdt1.getId());
    Assert.assertTrue(property.getTag() != null && property.getTag() instanceof Tag);
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
    EasyMock.verify(tagServiceMock);
  }

  @Test
  public void testLazyLoadDeviceProperties() throws RuleFormatException, ClassNotFoundException {
    // Reset the mock
    EasyMock.reset(tagServiceMock);

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

    TagImpl cdt1 = new TagImpl(10000L);
    TagImpl cdt2 = new TagImpl(10001L);
    ClientConstantValue ccv1 = new ClientConstantValue("Mr. Administrator", null);
    ClientRuleTag crt = new ClientRuleTag(new SimpleRuleExpression("(#123 + #234) / 2"), Float.class);
    ClientConstantValue ccv2 = new ClientConstantValue(4, Integer.class);

    List<Tag> ruleResultTags = new ArrayList<>();
    TagController rrt1 = new TagController(123L);
    TagController rrt2 = new TagController(234L);
    rrt1.update(DeviceTestUtils.createValidTransferTag(123L, "test_tag_1", 1F));
    rrt2.update(DeviceTestUtils.createValidTransferTag(234L, "test_tag_2", 2F));
    ruleResultTags.add(rrt1.getTagImpl());
    ruleResultTags.add(rrt2.getTagImpl());

    // Expect the device to get two data tags (one property, one field)
    EasyMock.expect(tagServiceMock.get(10000L)).andReturn(cdt1);
    EasyMock.expect(tagServiceMock.get(10001L)).andReturn(cdt2);
    // Expect the device to check if the rule tags are subscribed
    EasyMock.expect(tagServiceMock.isSubscribed(EasyMock.<BaseTagListener> anyObject())).andReturn(false).anyTimes();
    // Expect the device to get the tags inside the rule tags
    EasyMock.expect(tagServiceMock.get(EasyMock.<List<Long>> anyObject())).andReturn(ruleResultTags).anyTimes();

    // Setup is finished, need to activate the mock
    EasyMock.replay(tagServiceMock);

    device.setDeviceProperties(deviceProperties);

    for (Property property : device.getProperties()) {
      ((PropertyImpl) property).setTagManager(tagServiceMock);
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
    EasyMock.verify(tagServiceMock);
  }

  @Test
  public void testGetDeviceProperties() {
    // Reset the mock
    EasyMock.reset(tagServiceMock);

    DeviceImpl device = getTestDevice();
    TagImpl cdt1 = new TagImpl(100000L);
    TagImpl cdt2 = new TagImpl(200000L);
    TagImpl cdt3 = new TagImpl(300000L);

    // Make a map with real values
    Map<String, Tag> deviceProperties = new HashMap<>();
    deviceProperties.put("test_property_1", cdt1);
    deviceProperties.put("test_property_2", cdt2);
    deviceProperties.put("test_property_3", cdt3);

    device.setDeviceProperties(deviceProperties);

    // Expect the device to never, ever call the server

    // Setup is finished, need to activate the mock
    EasyMock.replay(tagServiceMock);

    Assert.assertTrue(device.getProperty("test_property_1").getTagId() == cdt1.getId());
    Assert.assertTrue(device.getProperty("test_property_2").getTagId() == cdt2.getId());
    Assert.assertTrue(device.getProperty("test_property_3").getTagId() == cdt3.getId());

    // Verify that everything happened as expected
    EasyMock.verify(tagServiceMock);
  }

  @Test
  public void testRuleUpdate() throws RuleFormatException, ClassNotFoundException, InterruptedException {
    // Reset the mock
    EasyMock.reset(tagServiceMock);

    final DeviceImpl device = getTestDevice();

    Collection<Tag> dataTagValues = new ArrayList<>();
    TagController cdt1 = new TagController(234L);
    TagController cdt2 = new TagController(345L);
    cdt1.update(DeviceTestUtils.createValidTransferTag(234L, "test_tag_1", 0.5F));
    cdt2.update(DeviceTestUtils.createValidTransferTag(345L, "test_tag_2", 1.5F));
    dataTagValues.add(cdt1.getTagImpl());
    dataTagValues.add(cdt2.getTagImpl());

    // Expect the device to check if the rule is already subscribed
    EasyMock.expect(tagServiceMock.isSubscribed(EasyMock.<BaseTagListener> anyObject())).andReturn(false);
    // Expect the device to get the tags inside the rule
    EasyMock.expect(tagServiceMock.get(EasyMock.<Set<Long>> anyObject())).andReturn(dataTagValues);

    // Setup is finished, need to activate the mock
    EasyMock.replay(tagServiceMock);

    final List<DeviceProperty> deviceProperties = new ArrayList<>();
    deviceProperties.add(new DeviceProperty(1L, "test_property_rule_name", "(#234 + #345) / 2", "clientRule", "Float"));
    device.setDeviceProperties(deviceProperties);

    for (Property property : device.getProperties()) {
      ((PropertyImpl) property).setTagManager(tagServiceMock);
    }

    ClientRuleTag rule = (ClientRuleTag) device.getProperty("test_property_rule_name").getTag();
    Assert.assertNotNull(rule);
    Assert.assertTrue(rule.isValid());
    Assert.assertTrue((Float) rule.getValue() == 1F);

    // Verify that everything happened as expected
    EasyMock.verify(tagServiceMock);
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
    EasyMock.reset(tagServiceMock);

    DeviceImpl device = getTestDevice();
    TagImpl cdt1 = new TagImpl(100000L);
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
      ((PropertyImpl) property).setTagManager(tagServiceMock);
    }

    // Expect the device to check if the rule tag is subscribed, but make no
    // other calls
    EasyMock.expect(tagServiceMock.isSubscribed(EasyMock.<BaseTagListener> anyObject())).andReturn(true).times(1);

    // Setup is finished, need to activate the mock
    EasyMock.replay(tagServiceMock);

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
    EasyMock.verify(tagServiceMock);
  }

  @Test
  public void testLazyLoadDevicePropertyFields() throws RuleFormatException, ClassNotFoundException {
    // Reset the mock
    EasyMock.reset(tagServiceMock);

    DeviceImpl device = getTestDevice();

    List<DeviceProperty> deviceFields = new ArrayList<>();
    deviceFields.add(new DeviceProperty(1L, "cpuLoadInPercent", "100000", "tagId", null));
    deviceFields.add(new DeviceProperty(2L, "responsiblePerson", "Mr. Administrator", "constantValue", null));
    deviceFields.add(new DeviceProperty(3L, "someCalculations", "(#123 + #234) / 2", "clientRule", "Float"));
    deviceFields.add(new DeviceProperty(4L, "numCores", "4", "constantValue", "Integer"));

    TagImpl cdt = new TagImpl(100000L);
    ClientConstantValue ccv1 = new ClientConstantValue("Mr. Administrator", null);
    ClientRuleTag crt = new ClientRuleTag(new SimpleRuleExpression("(#123 + #234) / 2"), Float.class);
    ClientConstantValue ccv2 = new ClientConstantValue(4, Integer.class);

    List<Tag> ruleResultTags = new ArrayList<>();
    TagController cdt1 = new TagController(123L);
    TagController cdt2 = new TagController(234L);
    cdt1.update(DeviceTestUtils.createValidTransferTag(123L, "test_tag_1", 1F));
    cdt2.update(DeviceTestUtils.createValidTransferTag(234L, "test_tag_2", 2F));
    ruleResultTags.add(cdt1.getTagImpl());
    ruleResultTags.add(cdt2.getTagImpl());

    // Expect the device to get one data tag
    EasyMock.expect(tagServiceMock.get(EasyMock.<Long> anyObject())).andReturn(cdt).once();
    // Expect the device to check if the rule tag is subscribed
    EasyMock.expect(tagServiceMock.isSubscribed(EasyMock.<BaseTagListener> anyObject())).andReturn(false).anyTimes();
    // Expect the device to get the tags inside the rule tag
    EasyMock.expect(tagServiceMock.get(EasyMock.<List<Long>> anyObject())).andReturn(ruleResultTags).anyTimes();

    // Setup is finished, need to activate the mock
    EasyMock.replay(tagServiceMock);

    List<DeviceProperty> deviceProperties = new ArrayList<>();
    deviceProperties.add(new DeviceProperty(5L, "acquisition", "mappedProperty", deviceFields));
    device.setDeviceProperties(deviceProperties);

    for (Property property : device.getProperties()) {
      ((PropertyImpl) property).setTagManager(tagServiceMock);
    }

    Property fields = device.getProperty("acquisition");

    Tag v = fields.getField("cpuLoadInPercent").getTag();
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
    EasyMock.verify(tagServiceMock);
  }

  private DeviceImpl getTestDevice() {
    return new DeviceImpl(1000L, "test_device", 1L, "test_device_class");
  }
}
