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

import java.util.ArrayList;
import java.util.List;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

/**
 * Simple XML mapper bean representing a list of device class commands. Used
 * when deserialising device class commands during configuration.
 *
 * @author Justin Lewis Salmon
 */
@Root(name = "Commands")
public class CommandList {

  @ElementList(entry = "Command", inline = true, required = false)
  private List<Command> commands = new ArrayList<>();

  public CommandList(List<Command> commands) {
    this.commands = commands;
  }

  public CommandList() {
    super();
  }

  public List<Command> getCommands() {
    return commands;
  }

  public static class Command {

    @Attribute
    private String name;

    @Element
    private String description;

    public String getName() {
      return name;
    }

    public String getDescription() {
      return description;
    }
  }
}
