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

import cern.c2mon.shared.client.device.DeviceProperty;

/**
 * Enumeration defining the currently supported property categories.
 *
 * Supporting new property categories should be relatively easy. It should only
 * be necessary to add a new item to this enum and then modify the
 * {@link PropertyFactory#createProperty(DeviceProperty)} method.
 *
 * @author Justin Lewis Salmon
 */
public enum Category {

  /**
   * A property containing a tag id which points to a data tag.
   */
  DATATAG("tagId"),

  /**
   * A property containing a constant value.
   */
  CONSTANT_VALUE("constantValue"),

  /**
   * A property containing a rule expression that will be evaluated on the
   * client.
   */
  CLIENT_RULE("clientRule"),

  /**
   * A property containing a map of nested fields, i.e. sub-properties.
   */
  MAPPED_PROPERTY("mappedProperty");

  /**
   * The actual category string itself, taken from the original configuration.
   */
  private final String configurationCategory;

  /**
   * Constructor.
   *
   * @param category the actual category string itself, taken from the original
   *          configuration.
   */
  Category(String category) {
    this.configurationCategory = category;
  }

  /**
   * Retrieve the category string itself.
   *
   * @return the category string
   */
  public String getCategory() {
    return configurationCategory;
  }
}
