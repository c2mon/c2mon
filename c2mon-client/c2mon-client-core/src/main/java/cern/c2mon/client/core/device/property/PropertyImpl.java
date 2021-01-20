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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cern.c2mon.client.common.tag.Tag;
import cern.c2mon.client.core.service.TagService;

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
   * Constructor for a property whose internal {@link Tag} will
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
   * Constructor for a property whose internal {@link Tag} does
   * not need to be lazily loaded.
   *
   * @param name the name of the property
   * @param category the property category
   * @param dataTag the internal data tag of the property
   */
  public PropertyImpl(String name, Category category, Tag dataTag) {
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
    return new ArrayList<>(fields.keySet());
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
   * @param tagService the tag manager to use
   */
  @Override
  public void setTagManager(TagService tagService) {
    this.tagService = tagService;
    for (Field field : fields.values()) {
      ((FieldImpl) field).setTagManager(tagService);
    }
  }
}
