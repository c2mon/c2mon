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
package cern.c2mon.client.ext.device.property;

import java.util.HashMap;
import java.util.Map;

import cern.c2mon.client.common.tag.ClientDataTagValue;
import cern.c2mon.client.core.tag.ClientRuleTag;
import cern.c2mon.shared.client.device.DeviceProperty;
import cern.c2mon.shared.rule.RuleExpression;
import cern.c2mon.shared.rule.RuleFormatException;

/**
 * Factory class to create an appropriate {@link ClientDeviceProperty} instance
 * from a {@link DeviceProperty} received from the server.
 *
 * @author Justin Lewis Salmon
 */
public class ClientDevicePropertyFactory {

  /**
   * Factory method to create an appropriate {@link ClientDeviceProperty}
   * instance from a {@link DeviceProperty}, based on its category.
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
  public static ClientDeviceProperty createClientDeviceProperty(DeviceProperty deviceProperty) throws ClassNotFoundException, RuleFormatException {

    // If the property has nested fields, create them all here
    if ((deviceProperty.getCategory() == null && !deviceProperty.getFields().isEmpty())
        || deviceProperty.getCategory().equals(Category.MAPPED_PROPERTY.getCategory())) {
      Map<String, DeviceProperty> fields = deviceProperty.getFields();
      Map<String, ClientDeviceProperty> clientFields = new HashMap<>();

      for (DeviceProperty field : fields.values()) {
        clientFields.put(field.getName(), createClientDeviceProperty(field));
      }

      return new ClientDevicePropertyImpl(deviceProperty.getName(), Category.MAPPED_PROPERTY, clientFields);
    }

    // If we have a tag ID, it takes priority.
    if (deviceProperty.getCategory().equals(Category.TAG_ID.getCategory())) {
      return new ClientDevicePropertyImpl(deviceProperty.getName(), Category.TAG_ID, Long.parseLong(deviceProperty.getValue()));
    }

    // If we have a client rule, that comes next in the hierarchy.
    else if (deviceProperty.getCategory().equals(Category.CLIENT_RULE.getCategory())) {
      ClientRuleTag ruleTag = new ClientRuleTag(RuleExpression.createExpression(deviceProperty.getValue()), deviceProperty.getResultTypeClass());
      return new ClientDevicePropertyImpl(deviceProperty.getName(), Category.CLIENT_RULE, ruleTag);
    }

    // If we have a constant value, it comes last in the hierarchy.
    else if (deviceProperty.getCategory().equals(Category.CONSTANT_VALUE.getCategory())) {
      ClientConstantValue constantValueTag = new ClientConstantValue(deviceProperty.getValue(), deviceProperty.getResultTypeClass());
      return new ClientDevicePropertyImpl(deviceProperty.getName(), Category.CONSTANT_VALUE, constantValueTag);
    }

    else {
      throw new RuntimeException("Property \"" + deviceProperty.getName() + "\" must specify at least one of (tagId, clientRule, constantValue)");
    }
  }

  /**
   * Factory method to create an appropriate {@link ClientDeviceProperty}
   * instance from a {@link ClientDataTagValue}, based on its type.
   *
   * @param name the name of the property
   * @param dataTag the data tag
   * @return the appropriate client device property
   */
  public static ClientDeviceProperty createClientDeviceProperty(String name, ClientDataTagValue dataTag) {
    Category category;

    if (dataTag instanceof ClientRuleTag<?>) {
      category = Category.CLIENT_RULE;
    } else if (dataTag instanceof ClientConstantValue<?>) {
      category = Category.CONSTANT_VALUE;
    } else {
      category = Category.TAG_ID;
    }

    return new ClientDevicePropertyImpl(name, category, dataTag);
  }
}
