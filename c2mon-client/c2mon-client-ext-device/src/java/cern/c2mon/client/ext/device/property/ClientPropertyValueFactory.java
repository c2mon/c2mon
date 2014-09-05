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

import cern.c2mon.client.core.tag.ClientRuleTag;
import cern.c2mon.client.ext.device.tag.ClientConstantValueTag;
import cern.c2mon.shared.client.device.PropertyValue;
import cern.c2mon.shared.rule.RuleExpression;
import cern.c2mon.shared.rule.RuleFormatException;

/**
 * Factory class to create an appropriate {@link ClientPropertyValue} instance
 * from a {@link PropertyValue} received from the server.
 *
 * @author Justin Lewis Salmon
 */
public class ClientPropertyValueFactory {

  /**
   * Factory method to create an appropriate {@link ClientPropertyValue}
   * instance.
   *
   * @param propertyValue the property value object received from the server
   * @return the appropriate client property value
   *
   * @throws ClassNotFoundException if the {@link PropertyValue} contains an
   *           invalid result type field
   * @throws RuleFormatException if the {@link PropertyValue} contains an
   *           invalid client rule field
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public static ClientPropertyValue createClientPropertyValue(PropertyValue propertyValue) throws ClassNotFoundException, RuleFormatException {

    // If we have a tag ID, it takes priority.
    if (propertyValue.getTagId() != null) {
      return new ClientPropertyValue(propertyValue.getTagId());
    }

    // If we have a client rule, that comes next in the hierarchy.
    else if (propertyValue.getClientRule() != null) {
      ClientRuleTag ruleTag = new ClientRuleTag(RuleExpression.createExpression(propertyValue.getClientRule()), propertyValue.getResultType());
      return new ClientPropertyValue(ruleTag);
    }

    // If we have a constant value, it comes last in the hierarchy.
    else if (propertyValue.getConstantValue() != null) {
      ClientConstantValueTag constantValueTag = new ClientConstantValueTag(propertyValue.getConstantValue(), propertyValue.getResultType());
      return new ClientPropertyValue(constantValueTag);
    }

    else {
      throw new RuntimeException("Property \"" + propertyValue.getName() + "\" must specify at least one of (tagId, clientRule, constantValue)");
    }
  }
}
