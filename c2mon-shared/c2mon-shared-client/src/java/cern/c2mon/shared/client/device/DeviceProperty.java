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
package cern.c2mon.shared.client.device;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
   * The unique name of the property (matches name from parent device class).
   */
  @Attribute
  private String name;

  /**
   * The ID of the data tag that corresponds to this property.
   */
  @Element(required = false, name = "tag-id")
  private Long tagId;

  /**
   * The client rule string of this property.
   */
  @Element(required = false, name = "client-rule")
  private String clientRule;

  /**
   * The constant value of this property.
   */
  @Element(required = false, name = "constant-value")
  private String constantValue;

  /**
   * The result type of this property (for rules and constant values)
   */
  @Element(required = false, name = "result-type")
  private String resultType = "String";

  /**
   * The list of nested fields of this property.
   */
  @ElementList(required = false, entry = "Fields")
  private List<DeviceProperty> fields;

  /**
   * Default constructor. A <code>DeviceProperty</code> can be either a tag ID,
   * a client rule, or a constant value. For client rules and constant values,
   * it is possible to specify the type of the resulting value.
   *
   * @param name the unique name of this property
   * @param tagId the ID of the data tag that corresponds to this property
   * @param clientRule the client rule string of this property
   * @param constantValue the constant value of this property
   * @param resultType the result type of this property (for rules and constant
   *          values). Defaults to {@link String}.
   */
  public DeviceProperty(final String name, final Long tagId, final String clientRule, final String constantValue, final String resultType) {
    this.name = name;
    this.tagId = tagId;
    this.clientRule = clientRule;
    this.constantValue = constantValue;

    if (resultType != null) {
      this.resultType = resultType;
    }
  }

  public DeviceProperty(final String name, final List<DeviceProperty> fields) {
    this.name = name;
    this.fields = fields;
  }

  /**
   * Constructor not used (needed for SimpleXML).
   */
  public DeviceProperty() {
  }

  /**
   * Get the unique name of the property.
   *
   * @return the name of the property
   */
  public String getName() {
    return name;
  }

  /**
   * Get the ID of the data tag corresponding to this property.
   *
   * @return the data tag ID
   */
  public Long getTagId() {
    return tagId;
  }

  /**
   * Get the client rule string of this property.
   *
   * @return the client rule string
   */
  public String getClientRule() {
    return clientRule;
  }

  /**
   * Get the constant value of this property.
   *
   * @return the constant value
   */
  public String getConstantValue() {
    return constantValue;
  }

  /**
   * Retrieve the raw fields of this property (if they exist). Used for testing
   * only.
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
   * Attempts to convert the string representation of the result type into a
   * class object of the corresponding type.
   *
   * @return the class of the result type
   * @throws ClassNotFoundException if the class cannot be created from the
   *           result type string
   */
  public Class<?> getResultType() throws ClassNotFoundException {
    return Class.forName("java.lang." + resultType);
  }
}
