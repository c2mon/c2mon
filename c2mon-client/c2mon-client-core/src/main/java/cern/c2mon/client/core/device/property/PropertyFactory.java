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
package cern.c2mon.client.core.device.property;

import java.util.HashMap;
import java.util.Map;

import cern.c2mon.client.common.tag.Tag;
import cern.c2mon.client.core.tag.ClientRuleTag;
import cern.c2mon.shared.client.device.DeviceProperty;
import cern.c2mon.shared.rule.RuleExpression;
import cern.c2mon.shared.rule.RuleFormatException;

/**
 * Factory class to create appropriate {@link Property} and {@link Field}
 * instances from {@link DeviceProperty} instances received from the server.
 *
 * @author Justin Lewis Salmon
 */
public class PropertyFactory {

  /**
   * Factory method to create an appropriate {@link Property} instance from a
   * {@link DeviceProperty}, based on its category.
   *
   * @param deviceProperty the property object received from the server
   * @return the appropriate client device property
   *
   * @throws ClassNotFoundException if the {@link DeviceProperty} contains an
   *           invalid result type field
   * @throws RuleFormatException if the {@link DeviceProperty} contains an
   *           invalid client rule field
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public static Property createProperty(DeviceProperty deviceProperty) throws ClassNotFoundException, RuleFormatException {

    // If the property has nested fields, create them all here
    if ((deviceProperty.getCategory() == null && !deviceProperty.getFields().isEmpty())
        || deviceProperty.getCategory().equals(Category.MAPPED_PROPERTY.getCategory())) {
      Map<String, DeviceProperty> fields = deviceProperty.getFields();
      Map<String, Field> clientFields = new HashMap<>();

      for (DeviceProperty field : fields.values()) {
        clientFields.put(field.getName(), createField(createProperty(field)));
      }

      return new PropertyImpl(deviceProperty.getName(), Category.MAPPED_PROPERTY, clientFields);
    }

    // If we have a tag ID, it takes priority.
    if (deviceProperty.getCategory().equals(Category.DATATAG.getCategory())) {
      return new PropertyImpl(deviceProperty.getName(), Category.DATATAG, Long.parseLong(deviceProperty.getValue()));
    }

    // If we have a client rule, that comes next in the hierarchy.
    else if (deviceProperty.getCategory().equals(Category.CLIENT_RULE.getCategory())) {
      ClientRuleTag ruleTag = new ClientRuleTag(RuleExpression.createExpression(deviceProperty.getValue()), deviceProperty.getResultTypeClass());
      return new PropertyImpl(deviceProperty.getName(), Category.CLIENT_RULE, ruleTag);
    }

    // If we have a constant value, it comes last in the hierarchy.
    else if (deviceProperty.getCategory().equals(Category.CONSTANT_VALUE.getCategory())) {
      ClientConstantValue constantValueTag = new ClientConstantValue(deviceProperty.getValue(), deviceProperty.getResultTypeClass());
      return new PropertyImpl(deviceProperty.getName(), Category.CONSTANT_VALUE, constantValueTag);
    }

    else {
      throw new RuntimeException("Property \"" + deviceProperty.getName() + "\" must specify at least one of (tagId, clientRule, constantValue)");
    }
  }

  /**
   * Factory method to create an appropriate {@link Property} instance from a
   * {@link Tag}, based on its type.
   *
   * @param name the name of the property
   * @param tag the data tag
   * @return the appropriate client device property
   */
  public static Property createProperty(String name, Tag tag) {
    Category category;

    if (tag instanceof ClientRuleTag<?>) {
      category = Category.CLIENT_RULE;
    } else if (tag instanceof ClientConstantValue<?>) {
      category = Category.CONSTANT_VALUE;
    } else {
      category = Category.DATATAG;
    }

    return new PropertyImpl(name, category, tag);
  }

  /**
   * Factory method to create an appropriate {@link Field} instance from a
   * {@link Tag}, based on its type.
   *
   * @param name the name of the field
   * @param tag the data tag
   * @return the newly created {@link Field} instance
   */
  public static Field createField(String name, Tag tag) {
    Property property = createProperty(name, tag);
    return new FieldImpl(property.getName(), property.getCategory(), ((BasePropertyImpl) property).getValue());
  }

  /**
   * Private internal method to convert a {@link Property} into a {@link Field}.
   *
   * @param property the property to convert
   * @return the newly converted {@link Field} instance
   */
  private static Field createField(Property property) {
    if (property.getCategory().equals(Category.DATATAG)) {
      return new FieldImpl(property.getName(), property.getCategory(), property.getTagId());
    } else {
      return new FieldImpl(property.getName(), property.getCategory(), ((BasePropertyImpl) property).getValue());
    }
  }
}
