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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cern.c2mon.client.common.tag.ClientDataTagValue;
import cern.c2mon.client.core.C2monTagManager;

/**
 * This class implements the {@link Property} interface and provides
 * implementation support for containing fields.
 *
 * @author Justin Lewis Salmon
 */
public class PropertyImpl extends BasePropertyImpl implements Property {

  /**
   * The map of nested property fields (if applicable).
   */
  private Map<String, Field> fields = new HashMap<>();

  /**
   * Constructor for a property whose internal {@link ClientDataTagValue} will
   * be lazily loaded in the future.
   *
   * @param name the name of the property
   * @param category the property category
   * @param tagId the id of the data tag corresponding to this property
   */
  public PropertyImpl(String name, Category category, long tagId) {
    super(name, category, tagId);
  }

  /**
   * Constructor for a property whose internal {@link ClientDataTagValue} does
   * not need to be lazily loaded.
   *
   * @param name the name of the property
   * @param category the property category
   * @param clientDataTag the internal data tag of the property
   */
  public PropertyImpl(String name, Category category, ClientDataTagValue dataTag) {
    super(name, category, dataTag);
  }

  /**
   * Constructor used to create an instance containing nested property fields.
   *
   * @param name the name of the property
   * @param category the category of this property
   * @param fields the property fields to set
   */
  public PropertyImpl(final String name, final Category category, Map<String, Field> fields) {
    super(name, category, (Long) null);
    this.fields = fields;
  }

  @Override
  public Field getField(String fieldName) {
    return fields.get(fieldName);
  }

  @Override
  public List<Field> getFields() {
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

    for (Field field : fields.values()) {
      FieldImpl fieldImpl = (FieldImpl) field;

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
   * @param field the {@link Field} instance of the field
   */
  public void addField(String fieldName, Field field) {
    fields.put(fieldName, field);
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
   * Manually set the reference to the {@link C2monTagManager} on the property
   * and all its fields. Used for testing purposes.
   *
   * @param tagManager the tag manager to use
   */
  @Override
  public void setTagManager(C2monTagManager tagManager) {
    this.tagManager = tagManager;
    for (Field field : fields.values()) {
      ((FieldImpl) field).setTagManager(tagManager);
    }
  }
}
