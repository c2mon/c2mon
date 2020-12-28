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

import cern.c2mon.client.common.tag.Tag;
import cern.c2mon.client.core.tag.ClientRuleTag;

/**
 * This interface defines the common methods that all properties (properties and
 * fields) must implement.
 *
 * @author Justin Lewis Salmon
 */
public interface BaseProperty {

  /**
   * Retrieve the name of this property. A property name is unique within its
   * device class.
   *
   * @return the name of the property
   */
  String getName();

  /**
   * Retrieve the category of this property (tag id, constant value, etc.) as
   * defined in {@link Category}.
   *
   * @return the category of this property
   */
  Category getCategory();

  /**
   * Retrieve the ID of the {@link ClientDataTag} to which this property
   * corresponds. Not applicable for {@link ClientRuleTag}s or
   * {@link ClientConstantValue}s.
   *
   * @return the tagId the ID of the corresponding tag or null if the property
   *         does not correspond to a {@link Tag}
   */
  Long getTagId();

  /**
   * Retrieve the device property.
   *
   * <p>
   * In the case of data tags/rules/constant values, this method will return you
   * a {@link Tag} object. The field accessor methods (
   * {@link #getField(String)} and {@link #getFields()}) will return null and
   * empty list, respectively.
   * </p>
   *
   * <p>
   * In the case of a property containing fields, this method will return null,
   * and the field accessor methods will become active. Note that the fields
   * themselves are also instances of {@link Property} and can be treated in the
   * same way as regular properties.
   * </p>
   *
   * @return the device property value or null if the property does not
   *         correspond to a {@link Tag}
   *
   * @see #getField(String)
   */
  Tag getTag();
}
