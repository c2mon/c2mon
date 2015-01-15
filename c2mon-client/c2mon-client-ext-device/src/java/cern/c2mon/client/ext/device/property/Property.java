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

import java.util.List;

import cern.c2mon.client.common.tag.ClientDataTag;
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
