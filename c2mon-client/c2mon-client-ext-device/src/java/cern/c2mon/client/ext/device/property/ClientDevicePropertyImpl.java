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
 * Implementation of {@link ClientDeviceProperty}.
 *
 * @author Justin Lewis Salmon
 */
public class ClientDevicePropertyImpl implements ClientDeviceProperty {

  /**
   * The ID of the {@link ClientDataTag} (if applicable).
   */
  private Long tagId;

  /**
   * The actual value (may be null if the property is a {@link ClientDataTag}
   * and has not yet been lazily loaded).
   */
  private ClientDataTagValue value;

  /**
   * The map of nested property fields (if applicable).
   */
  private Map<String, ClientDeviceProperty> fields;

  /**
   * Constructor used to create an instance containing only a tag ID, to be
   * lazily loaded later.
   *
   * @param tagId the ID of the {@link ClientDataTag} corresponding to this
   *          property
   */
  public ClientDevicePropertyImpl(final Long tagId) {
    this.tagId = tagId;
  }

  /**
   * Constructor used to create an instance containing a device property that
   * will not be lazily loaded ({@link ClientRuleTag} or
   * {@link ClientConstantValue}).
   *
   * @param clientDataTag the client device property to set
   */
  public ClientDevicePropertyImpl(final ClientDataTagValue clientDataTag) {
    this.value = clientDataTag;
    if (isDataTag()) {
      this.tagId = clientDataTag.getId();
    }
  }

  /**
   * Constructor used to create an instance containing nested property fields.
   *
   * @param fields the property fields to set
   */
  public ClientDevicePropertyImpl(Map<String, ClientDeviceProperty> fields) {
    this.fields = fields;
  }

  @Override
  public Long getTagId() {
    return tagId;
  }

  @Override
  public ClientDataTagValue getDataTag() {
    return value;
  }

  @Override
  public boolean isDataTag() {
    return value instanceof ClientDataTag || tagId != null;
  }

  @Override
  public boolean isRuleTag() {
    return value instanceof ClientRuleTag;
  }

  @Override
  public boolean isSubscribed() {
    return isDataTag() && value != null;
  }

  @Override
  public boolean isMappedProperty() {
    return fields != null;
  }

  @Override
  public Map<String, ClientDeviceProperty> getFields() {
    return fields;
  }

  /**
   * Set the internal {@link ClientDataTagValue}.
   *
   * @param value the {@link ClientDataTagValue} to set
   */
  public void setDataTag(ClientDataTagValue value) {
    this.value = value;
  }
}
