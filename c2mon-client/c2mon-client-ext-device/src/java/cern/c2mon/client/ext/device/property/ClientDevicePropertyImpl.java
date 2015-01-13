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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import cern.c2mon.client.common.listener.DataTagUpdateListener;
import cern.c2mon.client.common.tag.ClientDataTag;
import cern.c2mon.client.common.tag.ClientDataTagValue;
import cern.c2mon.client.core.C2monServiceGateway;
import cern.c2mon.client.core.C2monTagManager;
import cern.c2mon.client.core.tag.ClientRuleTag;

/**
 * Implementation of {@link ClientDeviceProperty}.
 *
 * @author Justin Lewis Salmon
 */
public class ClientDevicePropertyImpl implements ClientDeviceProperty {

  private static final Logger LOG = Logger.getLogger(ClientDevicePropertyImpl.class);

  /**
   * The name of the property. A property is unique within a device class.
   */
  private String name;

  /**
   * The category of this property, e.g. tag id, constant, etc.
   */
  private Category category;

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
  private Map<String, ClientDeviceProperty> fields = new HashMap<>();

  /**
   * Reference to the {@link C2monTagManager}, used to lazy-load data tags.
   */
  private C2monTagManager tagManager;

  /**
   * Constructor used to create an instance containing only a tag ID, to be
   * lazily loaded later.
   *
   * @param name the name of the property
   * @param category the category of this property
   * @param tagId the ID of the {@link ClientDataTag} corresponding to this
   *          property
   */
  public ClientDevicePropertyImpl(final String name, final Category category, final Long tagId) {
    this.name = name;
    this.category = category;
    this.tagId = tagId;
    this.tagManager = C2monServiceGateway.getTagManager();
  }

  /**
   * Constructor used to create an instance containing a device property that
   * will not be lazily loaded ({@link ClientRuleTag} or
   * {@link ClientConstantValue}).
   *
   * @param name the name of the property
   * @param category the category of this property
   * @param clientDataTag the client device property to set
   */
  public ClientDevicePropertyImpl(final String name, final Category category, final ClientDataTagValue clientDataTag) {
    this.name = name;
    this.category = category;
    this.value = clientDataTag;
    if (isDataTag()) {
      this.tagId = clientDataTag.getId();
    }
    this.tagManager = C2monServiceGateway.getTagManager();
  }

  /**
   * Constructor used to create an instance containing nested property fields.
   *
   * @param name the name of the property
   * @param category the category of this property
   * @param fields the property fields to set
   */
  public ClientDevicePropertyImpl(final String name, final Category category, Map<String, ClientDeviceProperty> fields) {
    this.name = name;
    this.category = category;
    this.fields = fields;
    this.tagManager = C2monServiceGateway.getTagManager();
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public Category getCategory() {
    return category;
  }

  @Override
  public Long getTagId() {
    return tagId;
  }

  @Override
  public ClientDataTagValue getDataTag() {
    // Load the value (either lazy-load the data tag or evaluate the rule)
    setValue(loadValue());
    return this.value;
  }

  @Override
  public ClientDeviceProperty getField(String fieldName) {
    return fields.get(fieldName);
  }

  @Override
  public List<ClientDeviceProperty> getFields() {
    return new ArrayList<>(fields.values());
  }

  @Override
  public List<String> getFieldNames() {
    return new ArrayList<String>(fields.keySet());
  }

  /**
   * Retrieve the tag ids of all fields inside this property.
   *
   * @return the list of tag ids
   */
  public List<Long> getFieldDataTagIds() {
    List<Long> tagIds = new ArrayList<>();

    for (ClientDeviceProperty field : fields.values()) {
      ClientDevicePropertyImpl fieldImpl = (ClientDevicePropertyImpl) field;

      if (fieldImpl.isDataTag()) {
        tagIds.add(fieldImpl.getTagId());
      }
    }

    return tagIds;
  }

  /**
   * Insert/update a field into this property.
   *
   * @param fieldName the name of the field to be added
   * @param field the {@link ClientDeviceProperty} instance of the field
   */
  public void addField(String fieldName, ClientDeviceProperty field) {
    fields.put(fieldName, field);
  }

  /**
   * Check if this property points to a {@link ClientDataTag}.
   *
   * @return true if this property is a data tag, false otherwise
   */
  public boolean isDataTag() {
    return value instanceof ClientDataTag || tagId != null;
  }

  /**
   * Check if this property points to a {@link ClientRuleTag}.
   *
   * @return true if this property is a rule tag, false otherwise.
   */
  public boolean isRuleTag() {
    return value instanceof ClientRuleTag;
  }

  /**
   * Check if the data tag corresponding to this property has been subscribed to
   * (not applicable for tags other than {@link ClientDataTag}.
   *
   * @return true if the property tag has been subscribed to, false otherwise
   */
  public boolean isValueLoaded() {
    return isDataTag() && value != null;
  }

  /**
   * Check if the property is a mapped property, i.e. if it has nested fields.
   *
   * @return true if the property is a mapped property, false otherwise
   */
  public boolean isMappedProperty() {
    return fields != null;
  }

  /**
   * Set the internal {@link ClientDataTagValue}.
   *
   * @param value the {@link ClientDataTagValue} to set
   */
  protected void setValue(ClientDataTagValue value) {
    this.value = value;
  }

  /**
   * Perform the necessary tasks to fully instantiate the value of the property.
   * In the case of a data tag, this means lazy-loading its
   * {@link ClientDataTagValue} from the server (if it hasn't already been
   * subscribed to). In the case of a {@link ClientRuleTag}, the dependent tags
   * will be retrieved from the server and the rule will be evaluated.
   *
   * @return the new value
   */
  private ClientDataTagValue loadValue() {
    ClientDataTagValue value = this.value;

    // If the internal value is a Long, then we lazy load the data tag
    if (isDataTag() && !isValueLoaded()) {
      value = tagManager.getDataTag(getTagId());
    }

    // If it is a rule tag, we evaluate the rule (if it isn't subscribed)
    else if (isRuleTag() && !tagManager.isSubscribed((DataTagUpdateListener) this.value)) {

      // Get the data tag values from inside the rule
      Set<Long> tagIds = value.getRuleExpression().getInputTagIds();
      Collection<ClientDataTagValue> dataTagValues = tagManager.getDataTags(tagIds);

      // Update the rule tag
      for (ClientDataTagValue tagValue : dataTagValues) {
        ((ClientRuleTag<?>) value).onUpdate(tagValue);
      }
    }

    return value;
  }

  /**
   * Manually set the reference to the {@link C2monTagManager} on the property
   * and all its fields. Used for testing purposes.
   *
   * @param tagManager the tag manager to use
   */
  public void setTagManager(C2monTagManager tagManager) {
    this.tagManager = tagManager;
    for (ClientDeviceProperty field : fields.values()) {
      ((ClientDevicePropertyImpl) field).setTagManager(tagManager);
    }
  }
}
