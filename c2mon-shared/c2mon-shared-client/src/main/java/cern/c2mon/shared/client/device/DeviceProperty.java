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

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Simple XML mapper bean representing a device property. Used when
 * deserialising device properties during configuration.
 *
 * @author Justin Lewis Salmon
 */
@NoArgsConstructor
@Data
public class DeviceProperty implements Cloneable, Serializable {

  /**
   * ID, since serializable.
   */
  private static final long serialVersionUID = -3714996315363505073L;

  /**
   * The unique ID of the property (matches ID from parent device class property).
   */
  @Attribute
  @Setter(AccessLevel.NONE)
  private Long id;

  /**
   * The unique name of the property (matches name from parent device class).
   */
  @Attribute
  @Setter(AccessLevel.NONE)
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
  @Setter(AccessLevel.NONE)
  private String category;

  /**
   * The result type of this property (for rules and constant values)
   */
  @Element(required = false, name = "result-type")
  @Setter(AccessLevel.NONE)
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
   * Retrieve the raw fields of this property (if they exist).
   *
   * @return the property fields if they exist, null otherwise
   */
  public Map<String, DeviceProperty> getFields() {
    if (this.fields == null) {
      return null;
    } else
      return this.fields.stream()
        .collect(Collectors.toMap(i -> i.name, Function.identity()));
  }

  /**
   * Set a field of this property.
   *
   * @param field the field to set
   */
  public void setFields(DeviceProperty field) {
    this.fields.add(field);
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
