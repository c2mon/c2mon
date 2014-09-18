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

import java.util.Map;

import cern.c2mon.client.common.tag.ClientDataTag;
import cern.c2mon.client.common.tag.ClientDataTagValue;
import cern.c2mon.client.core.tag.ClientRuleTag;

/**
 * Wrapper class to manage a client device property. A property can be either a
 * straightforward {@link ClientDataTag}, a {@link ClientRuleTag} or a
 * {@link ClientConstantValue}.
 *
 * @author Justin Lewis Salmon
 */
public interface ClientDeviceProperty {

  /**
   * Retrieve the ID of the {@link ClientDataTag} to which this property
   * corresponds. Not applicable for {@link ClientRuleTag}s or
   * {@link ClientConstantValue}s.
   *
   * @return the tagId the ID of the corresponding tag
   */
  public Long getTagId();

  /**
   * Retrieve the device property. May be null if the property points to a
   * {@link ClientDataTag} and the actual tag has not yet been loaded.
   *
   * @return the device property value
   */
  public ClientDataTagValue getDataTag();

  /**
   * Check if this property points to a {@link ClientDataTag}.
   *
   * @return true if this property is a data tag, false otherwise
   */
  public boolean isDataTag();

  /**
   * Check if this property points to a {@link ClientRuleTag}.
   *
   * @return true if this property is a rule tag, false otherwise.
   */
  public boolean isRuleTag();

  /**
   * Check if the data tag corresponding to this property has been subscribed to
   * (not applicable for tags other than {@link ClientDataTag}.
   *
   * @return true if the property tag has been subscribed to, false otherwise
   */
  public boolean isSubscribed();

  /**
   * Check if the property is a mapped property, i.e. if it has nested fields.
   *
   * @return true if the property is a mapped property, false otherwise
   */
  public boolean isMappedProperty();

  /**
   * Retrieve the nested fields of this property.
   *
   * @return the property fields, or null if this property is not a mapped
   *         property
   *
   * @see ClientDeviceProperty#isMappedProperty()
   */
  public Map<String, ClientDeviceProperty> getFields();
}
