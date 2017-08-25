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
package cern.c2mon.server.common.device;

import java.util.ArrayList;
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
public class DeviceCommandList implements AbstractList {

  @ElementList(entry = "DeviceCommand", inline = true, required = false)
  private List<DeviceCommand> deviceCommands = new ArrayList<>();

  public DeviceCommandList(List<DeviceCommand> deviceCommand) {
    this.deviceCommands = deviceCommand;
  }

  public DeviceCommandList() {
    super();
  }

  //TODO: remove this method and replace with getList
  public List<DeviceCommand> getDeviceCommands() {
    return deviceCommands;
  }


  @Override
  public List<DeviceCommand> getList() {
    return deviceCommands;
  }
}
