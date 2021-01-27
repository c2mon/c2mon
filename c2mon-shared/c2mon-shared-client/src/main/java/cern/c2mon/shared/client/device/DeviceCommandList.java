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

import java.util.*;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

/**
 * Simple XML mapper bean representing a list of device commands. Used when
 * deserialising device commands during configuration.
 *
 * @author Justin Lewis Salmon
 */
@Root(name = "DeviceCommands")
public class DeviceCommandList implements SerializableDeviceElement {

  @ElementList(entry = "DeviceCommand", inline = true, required = false)
  private Collection<DeviceCommand> deviceCommands = new HashSet<>();

  /**
   * Create a new wrapper bean for the referenced device commands
   * @param deviceCommands the device commands to wrap
   */
  public DeviceCommandList(Collection<DeviceCommand> deviceCommands) {
    this.deviceCommands = deviceCommands;
  }

  /**
   * Default constructor used during deserialization
   */
  public DeviceCommandList() {
    super();
  }

  /**
   * Get the unwrapped device commands in a mutable list
   * @return the unwrapped device commands in a mutable list
   */
  public List<DeviceCommand> getDeviceCommands() {
    return new ArrayList<>(deviceCommands);
  }
}
