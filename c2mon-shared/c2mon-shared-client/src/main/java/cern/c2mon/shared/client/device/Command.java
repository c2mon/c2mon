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

import lombok.NoArgsConstructor;

/**
 * Simple XML mapper bean representing a device class command. Used when
 * deserialising device class commands during configuration.
 *
 * @author Justin Lewis Salmon
 */
@NoArgsConstructor
public class Command extends DeviceClassElement implements Serializable {

  private static final long serialVersionUID = -6943334662697273304L;

  /**
   * Constructor to use during command creation requests.
   *
   * @param name the name of the command
   * @param description the command description
   */
  public Command(final String name, final String description) {
    this.name = name;
    this.description = description;
  }

  /**
   * Create a new Command
   *
   * @param id the unique ID of the command
   * @param name the name of the command
   * @param description the command description
   */
  public Command(final Long id, final String name, final String description) {
    this.id = id;
    this.name = name;
    this.description = description;
  }

}
