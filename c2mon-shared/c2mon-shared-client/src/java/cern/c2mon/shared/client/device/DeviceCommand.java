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
   * The unique name of this command (matches name from parent device class).
   */
  @Attribute
  private String name;

  /**
   * The ID of the command tag to which this command corresponds.
   */
  @Element(name = "command-tag-id")
  private Long tagId;

  /**
   * Default constructor.
   *
   * @param name the unique name of this command.
   * @param tagId the ID of the command tag to which this command corresponds.
   */
  public DeviceCommand(String name, Long tagId) {
    this.name = name;
    this.tagId = tagId;
  }

  /**
   * Constructor not used (needed for SimpleXML).
   */
  public DeviceCommand() {
  }

  /**
   * Retrieve the name of this command.
   *
   * @return the command name
   */
  public String getName() {
    return name;
  }

  /**
   * Retrieve the ID of the command tag to which this command corresponds.
   *
   * @return the command tag ID
   */
  public Long getTagId() {
    return tagId;
  }
}