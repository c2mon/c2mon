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

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.convert.AnnotationStrategy;
import org.simpleframework.xml.core.Persister;

/**
 * Simple XML mapper bean representing a list of device class commands. Used
 * when deserialising device class commands during configuration.
 *
 * @author Justin Lewis Salmon
 */
@Root(name = "Commands")
public class CommandList {

  @ElementList(entry = "Command", inline = true, required = false)
  private Set<Command> commands = new HashSet<>();

  public CommandList(Set<Command> commands) {
    this.commands = commands;
  }

  public CommandList() {
    super();
  }

  public List<Command> getCommands() {
    return new ArrayList<>(commands);
  }

  public String toConfigXml() throws Exception {
    Persister serializer = new Persister(new AnnotationStrategy());
    try (StringWriter fw = new StringWriter()) {
      serializer.write(this, fw);
      return fw.toString();
    }
  }
}
