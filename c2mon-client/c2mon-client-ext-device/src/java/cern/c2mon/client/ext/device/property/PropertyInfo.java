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
   * @return the field name
   */
  public String getFieldName() {
    return fieldName;
  }
}
