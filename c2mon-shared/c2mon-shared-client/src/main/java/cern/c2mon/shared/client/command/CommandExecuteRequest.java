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
package cern.c2mon.shared.client.command;

import java.io.Serializable;

/**
 * Command execution request sent by the C2MON Client API.
 * @param <T> The Object type of the command value
 *
 * @author Matthias Braeger, Mark Brightwell
 */
public interface CommandExecuteRequest<T> extends Serializable {

  /**
   * @return The id of the command that shall be executed
   */
  Long getId();
  
  /**
   * @return The value that shall be used to execute the command.
   */
  T getValue();
  
  /**
   * @return the client timeout for this command (in milliseconds)
   */
  int getTimeout();

  /**
   * @return the name of the user executing the command
   */
  String getUsername();

  /**
   * @return the host the execute request is made from
   */
  String getHost();
}
