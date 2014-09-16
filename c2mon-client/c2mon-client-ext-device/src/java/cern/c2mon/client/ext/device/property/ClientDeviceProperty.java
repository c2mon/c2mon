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

import cern.c2mon.client.common.tag.ClientDataTag;
import cern.c2mon.client.common.tag.ClientDataTagValue;
import cern.c2mon.client.core.tag.ClientRuleTag;
import cern.c2mon.client.ext.device.tag.ClientConstantValueTag;

/**
 * Wrapper class to manage a client device property. A property can be either a
 * straightforward {@link ClientDataTag}, a {@link ClientRuleTag} or a
 * {@link ClientConstantValueTag}.
 *
 * @author Justin Lewis Salmon
 */
public class ClientDeviceProperty {

  /**
   * The ID of the {@link ClientDataTag} (if applicable).
   */
  private Long tagId;

  /**
   * The actual value (may be null if the property is a {@link ClientDataTag}
   * and has not yet been lazily loaded).
   */
  private ClientDataTagValue clientDataTagValue;

  /**
   * Constructor used to create an instance containing only a tag ID, to be
   * lazily loaded later.
   *
   * @param tagId the ID of the {@link ClientDataTag} corresponding to this
   *          property
   */
  public ClientDeviceProperty(final Long tagId) {
    this.tagId = tagId;
  }

  /**
   * Constructor used to create an instance containing a device property that
   * will not be lazily loaded ({@link ClientRuleTag} or
   * {@link ClientConstantValueTag}).
   *
   * @param clientDataTag the client device property to set
   */
  public ClientDeviceProperty(final ClientDataTagValue clientDataTag) {
    this.clientDataTagValue = clientDataTag;
    if (isDataTag()) {
      this.tagId = clientDataTag.getId();
    }
  }

  /**
   * Retrieve the ID of the {@link ClientDataTag} to which this property
   * corresponds. Not applicable for {@link ClientRuleTag}s or
   * {@link ClientConstantValueTag}s.
   *
   * @return the tagId the ID of the corresponding tag
   */
  public Long getTagId() {
    return tagId;
  }

  /**
   * Retrieve the device property. May be null if the property points to a
   * {@link ClientDataTag} and the actual tag has not yet been loaded.
   *
   * @return the device property value
   */
  public ClientDataTagValue getProperty() {
    return clientDataTagValue;
  }

  /**
   * Check if this property points to a {@link ClientDataTag}.
   *
   * @return true if this property is a data tag, false otherwise
   */
  public boolean isDataTag() {
    return clientDataTagValue instanceof ClientDataTag || tagId != null;
  }

  /**
   * Check if this property points to a {@link ClientRuleTag}.
   *
   * @return true if this property is a rule tag, false otherwise.
   */
  public boolean isRuleTag() {
    return clientDataTagValue instanceof ClientRuleTag;
  }

  /**
   * Check if the data tag corresponding to this property has been subscribed to
   * (not applicable for tags other than {@link ClientDataTag}.
   *
   * @return true if the property tag has been subscribed to, false otherwise
   */
  public boolean isSubscribed() {
    return isDataTag() && clientDataTagValue != null;
  }
}
