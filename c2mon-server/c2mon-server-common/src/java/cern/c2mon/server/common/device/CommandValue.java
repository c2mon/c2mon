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
package cern.c2mon.server.common.device;

import java.io.Serializable;

import org.simpleframework.xml.Attribute;

/**
 * Simple XML mapper bean representing a device command value. Used when
 * deserialising device command values during configuration.
 *
 * @author Justin Lewis Salmon
 */
public class CommandValue implements Cloneable, Serializable {

  private static final long serialVersionUID = 7331198531306903558L;

  @Attribute
  private String name;

  @Attribute(name = "command-tag-id")
  private Long tagId;

  /**
   * TODO
   *
   * @param name
   * @param tagId
   */
  public CommandValue(String name, Long tagId) {
    this.name = name;
    this.tagId = tagId;
  }

  public CommandValue() {
  }

  public String getName() {
    return name;
  }

  public Long getTagId() {
    return tagId;
  }
}