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

/**
 * Simple bean used for retrieving properties/fields from a device.
 *
 * @author Justin Lewis Salmon
 */
public class PropertyInfo {

  private String propertyName;

  private String fieldName;

  /**
   * Constructor. Creates a <code>PropertyInfo</code> representing a simple
   * (i.e. non-mapped) property.
   *
   * @param propertyName the name of the property
   */
  public PropertyInfo(String propertyName) {
    this.propertyName = propertyName;
  }

  /**
   * Constructor. Creates a <code>PropertyInfo</code> representing a field
   * within a mapped property.
   *
   * @param propertyName
   * @param fieldName
   */
  public PropertyInfo(String propertyName, String fieldName) {
    this.propertyName = propertyName;
    this.fieldName = fieldName;
  }

  /**
   * Retrieve the name of the property.
   *
   * @return the property name
   */
  public String getPropertyName() {
    return propertyName;
  }

  /**
   * Retrieve the name of the nested field within the mapped property.
   *
   * @return the field name, or {@code null}, if the property has no fields defined.
   */
  public String getFieldName() {
    return fieldName;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((fieldName == null) ? 0 : fieldName.hashCode());
    result = prime * result + ((propertyName == null) ? 0 : propertyName.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof PropertyInfo)) {
      return false;
    }
    PropertyInfo other = (PropertyInfo) obj;
    if (fieldName == null) {
      if (other.fieldName != null) {
        return false;
      }
    }
    else if (!fieldName.equals(other.fieldName)) {
      return false;
    }
    if (propertyName == null) {
      if (other.propertyName != null) {
        return false;
      }
    }
    else if (!propertyName.equals(other.propertyName)) {
      return false;
    }
    return true;
  }
}
