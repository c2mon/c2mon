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

import java.util.List;

import cern.c2mon.client.core.tag.ClientRuleTag;

/**
 * This class represents the <code>Property</code> aspect of the
 * class/device/property model. A property can be either a {@link ClientDataTag}
 * , a {@link ClientRuleTag}, a {@link ClientConstantValue} , or it can contain
 * a list of fields (i.e. sub-properties).
 *
 * @author Justin Lewis Salmon
 */
public interface Property extends BaseProperty {

  /**
   * Retrieve a field from this property.
   *
   * <p>
   * Note that fields are themselves simply instances of {@link Property} and
   * can be treated in the same way as regular properties.
   * </p>
   *
   * @param fieldName the name of the field you wish to retrieve
   * @return the {@link Property} instance, or null if the field was not found
   *         in the property or the property does not contain fields
   */
  public Field getField(String fieldName);

  /**
   * Retrieve all fields of this property.
   *
   * @return the list of {@link Property} instances, or an empty list if this
   *         property does not contain fields
   */
  public List<Field> getFields();

  /**
   * Retrieve the names of all fields of this property.
   *
   * @return the list of field names
   */
  public List<String> getFieldNames();
}
