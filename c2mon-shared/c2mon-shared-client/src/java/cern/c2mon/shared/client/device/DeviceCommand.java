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

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;

/**
 * Simple XML mapper bean representing a device command. Used when deserialising
 * device commands during configuration.
 *
 * @author Justin Lewis Salmon
 */
public class DeviceCommand implements Cloneable, Serializable {

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
   * Constructor not used (needed for SimpleXML).
   */
  public DeviceCommand() {
  }

  /**
   * Get the unique ID of the command.
   *
   * @return the name of the command
   */
  public Long getId() {
    return id;
  }

  /**
   * Get the unique name of the command.
   *
   * @return the name of the command
   */
  public String getName() {
    return name;
  }

  /**
   * Get the value of this command.
   *
   * @return the client rule string
   */
  public String getValue() {
    return value;
  }

  /**
   * Get the category of this command.
   *
   * @return the constant value
   */
  public String getCategory() {
    return category;
  }

  /**
   * Get the raw result type string of this command.
   *
   * @return the result type
   */
  public String getResultType() {
    return resultType;
  }
}