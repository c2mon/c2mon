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
package cern.c2mon.shared.client.device;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cern.c2mon.shared.client.configuration.api.util.IgnoreProperty;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;

/**
 * Simple XML mapper bean representing a device property. Used when
 * deserialising device properties during configuration.
 *
 * @author Justin Lewis Salmon
 */
public class DeviceProperty implements Cloneable, Serializable {

  /**
   * ID, since serializable.
   */
  private static final long serialVersionUID = -3714996315363505073L;

  /**
   * The unique ID of the property (matches ID from parent device class property).
   */
  @Attribute
  private Long id;

  /**
   * The unique name of the property (matches name from parent device class).
   */
  @Attribute
  private String name;

  /**
   * The actual value of this property.
   */
  @Element(required = false, name = "value")
  private String value;

  /**
   * The category of this property (e.g. "tagId", "clientRule", "constantValue").
   */
  @Element(required = false, name = "category")
  private String category;

  /**
   * The result type of this property (for rules and constant values)
   */
  @Element(required = false, name = "result-type")
  private String resultType = "String";

  /**
   * The list of nested fields of this property.
   */
  @ElementList(required = false, name = "PropertyFields")
  private List<DeviceProperty> fields = new ArrayList<>();

  /**
   * Default constructor. A <code>DeviceProperty</code> can be a tag ID, a
   * client rule, a constant value, or something else. For client rules and
   * constant values, it is possible to specify the type of the resulting value.
   *
   * @param id the unique ID of the property
   * @param name the unique name of this property
   * @param value the actual value of this property
   * @param category the category of this property (e.g. "tagId", "clientRule",
   *          "constantValue")
   * @param resultType the result type of this property (for rules and constant
   *          values). Defaults to {@link String}.
   */
  public DeviceProperty(final Long id, final String name, final String value, final String category, final String resultType) {
    this.id = id;
    this.name = name;
    this.value = value;
    this.category = category;

    if (resultType != null) {
      this.resultType = resultType;
    }
  }
  /**
   * Constructor to use during command creation requests. A <code>DeviceProperty</code> can be a tag ID, a client rule,
   * a constant value, or something else. For client rules and constant values, it is possible to specify the type of
   * the resulting value.
   *
   * @param id the unique ID of the property
   * @param name the unique name of this property
   * @param value the actual value of this property
   * @param category the category of this property (e.g. "tagId", "clientRule",
   *          "constantValue")
   * @param resultType the result type of this property (for rules and constant
   *          values). Defaults to {@link String}.
   */
  public DeviceProperty(final String name, final String value, final String category, final String resultType) {
    this.name = name;
    this.value = value;
    this.category = category;

    if (resultType != null) {
      this.resultType = resultType;
    }
  }

  /**
   * Constructor that creates a mapped property.
   *
   * @param id the unique ID of the property
   * @param name name the unique name of this property
   * @param category category the category of this property (should be "mappedProperty")
   * @param fields the nested property fields
   */
  public DeviceProperty(final Long id, final String name, final String category, final List<DeviceProperty> fields) {
    this.id = id;
    this.name = name;
    this.category = category;
    this.fields = fields;
  }
  /**
   * Constructor to use during command creation requests that creates a mapped property.
   *
   * @param name name the unique name of this property
   * @param category category the category of this property (should be "mappedProperty")
   * @param fields the nested property fields
   */
  public DeviceProperty(final String name, final String category, final List<DeviceProperty> fields) {
    this.name = name;
    this.category = category;
    this.fields = fields;
  }

  /**
   * Constructor not used (needed for SimpleXML).
   */
  public DeviceProperty() {
  }

  /**
   * Get the unique ID of the property.
   *
   * @return the id of the property
   */
  public Long getId() {
    return id;
  }

  /**
   * Set the unique ID of the property. This should only be done during element creation requests.
   *
   * @param id the id of the property
   */
  public void setId(Long id) { this.id = id; }

  /**
   * Get the unique name of the property.
   *
   * @return the name of the property
   */
  public String getName() {
    return name;
  }

  /**
   * Get the value of this property.
   *
   * @return the property value
   */
  public String getValue() {
    return value;
  }

  /**
   * Set the value of this property.
   *
   * @param value the value to set
   */
  public void setValue(String value) {
    this.value = value;
  }

  /**
   * Get the category of this property.
   *
   * @return the property category
   */
  public String getCategory() {
    return category;
  }

  /**
   * Retrieve the raw fields of this property (if they exist).
   *
   * @return the property fields if they exist, null otherwise
   */
  public Map<String, DeviceProperty> getFields() {
    if (this.fields == null) {
      return null;
    }

    Map<String, DeviceProperty> fields = new HashMap<>();

    for (DeviceProperty field : this.fields) {
      fields.put(field.getName(), field);
    }

    return fields;
  }

  /**
   * Set a field of this property.
   *
   * @param field the field to set
   */
  public void setFields(DeviceProperty field) {
    if (field.getName() != null || field.getId() != null) {
      this.fields.add(field);
    }
  }

  /**
   * Get the raw result type string of this property.
   *
   * @return the result type
   */
  public String getResultType() {
    return resultType;
  }

  /**
   * Attempts to convert the string representation of the result type into a
   * class object of the corresponding type.
   *
   * @return the class of the result type
   * @throws ClassNotFoundException if the class cannot be created from the
   *           result type string
   */
  public Class<?> getResultTypeClass() throws ClassNotFoundException {
    return Class.forName("java.lang." + resultType);
  }
}
