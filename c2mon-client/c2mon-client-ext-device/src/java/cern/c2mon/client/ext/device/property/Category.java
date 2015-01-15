/*******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 *
 * Copyright (C) 2004 - 2015 CERN. This program is free software; you can
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
  TAG_ID("tagId"),

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
  private final String category;

  /**
   * Constructor.
   *
   * @param category the actual category string itself, taken from the original
   *          configuration.
   */
  Category(String category) {
    this.category = category;
  }

  /**
   * Retrieve the category string itself.
   *
   * @return the category string
   */
  public String getCategory() {
    return category;
  }
}
