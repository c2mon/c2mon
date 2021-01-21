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

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;

/**
 * Simple XML mapper bean representing a device command. Used when deserialising
 * device commands during configuration.
 *
 * @author Justin Lewis Salmon
 */
public class DeviceCommand implements Cloneable, Serializable, DeviceElement {

  private static final long serialVersionUID = 7331198531306903558L;

  /**
   * The unique ID of the command (matches ID from parent device class command).
   */
  @Attribute
  private Long id;

  /**
   * The unique name of the command (matches name from parent device class).
   */
  @Attribute
  private String name;

  /**
   * The actual value of this command.
   */
  @Element(required = false, name = "value")
  private String value;

  /**
   * The category of this command (usually just "commandTagId").
   */
  @Element(required = false, name = "category")
  private String category;

  /**
   * The result type of this command.
   */
  @Element(required = false, name = "result-type")
  private String resultType = "String";

  /**
   * Default constructor. A <code>DeviceCommand</code> is usually just a command
   * tag ID.
   *
   * @param id the unique ID of the command
   * @param name the unique name of this command
   * @param value the actual value of this command
   * @param category the category of this command
   * @param resultType the result type of this command. Defaults to
   *          {@link String}.
   */
  public DeviceCommand(final Long id, final String name, final String value, final String category, final String resultType) {
    this.id = id;
    this.name = name;
    this.value = value;
    this.category = category;

    if (resultType != null) {
      this.resultType = resultType;
    }
  }
  /**
   * Constructor to use during command creation requests. A <code>DeviceCommand</code> is usually just a command
   * tag ID.
   *
   * @param name the unique name of this command
   * @param value the actual value of this command
   * @param category the category of this command
   * @param resultType the result type of this command. Defaults to
   *          {@link String}.
   */
  public DeviceCommand(final String name, final String value, final String category, final String resultType) {
    this.name = name;
    this.value = value;
    this.category = category;

    if (resultType != null) {
      this.resultType = resultType;
    }
  }

  /**
   * Constructor not used (needed for SimpleXML).
   */
  public DeviceCommand() {
  }

  @Override
  public Long getId() {
    return id;
  }

  @Override
  public void setId(Long id) {
    this.id = id;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public void setName(String name) {
    this.name = name;
  }

  @Override
  public String getValue() {
    return value;
  }

  @Override
  public String getCategory() {
    return category;
  }

  @Override
  public String getResultType() {
    return resultType;
  }
}
