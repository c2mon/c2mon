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

import java.util.List;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import cern.c2mon.shared.client.device.DeviceCommand;

/**
 * Simple XML mapper bean representing a list of device commands. Used when
 * deserialising device commands during configuration.
 *
 * @author Justin Lewis Salmon
 */
@Root(name = "DeviceCommands")
public class DeviceCommandList {

  @ElementList(entry = "DeviceCommand", inline = true)
  private List<DeviceCommand> deviceCommands;

  public DeviceCommandList(List<DeviceCommand> deviceCommand) {
    this.deviceCommands = deviceCommand;
  }

  public DeviceCommandList() {
    super();
  }

  public List<DeviceCommand> getDeviceCommands() {
    return deviceCommands;
  }
}
