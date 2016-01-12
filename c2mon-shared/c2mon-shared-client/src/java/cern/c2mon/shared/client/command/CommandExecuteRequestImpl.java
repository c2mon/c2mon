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

import javax.validation.constraints.NotNull;

/**
 * This class implements the {@link CommandExecuteRequest} interface and is used
 * on the client side for sending an EXECUTE_COMMAND request to the C2MON server.
 * @param <T> The Object type of the command value
 *
 * @author Matthias Braeger, Mark Brightwell
 */
public class CommandExecuteRequestImpl<T> implements CommandExecuteRequest<T> {

  /** This object is serialized when sent to the server */
  private static final long serialVersionUID = 717165763310657558L;

  /** The id of the command */
  @NotNull
  private final Long commandId;
  
  /** The value that shall be used for the execution of the command */
  private final T commandValue;
  
  /** Timeout in milliseconds the client waits for a response from the server */
  private final int clientTimeout;
  
  /** Name of the user wishing to execute the command */
  private final String username;
  
  /** The host the execute request is made from */
  private final String host;
  
  /**
   * Default Constructor
   * @param commandId The id of the command
   * @param commandValue The value that shall be used for the execution of the command
   * @param clientTimeout timeout the client waits for a response (in milliseconds)
   * @param username the user who is trying to execute the command
   * @param host the host the execute request is made from
   * @throws IllegalArgumentException In case the <code>commandId</code> parameter is
   *         <code>null</code>
   */
  public CommandExecuteRequestImpl(final Long commandId, final T commandValue, final int clientTimeout, final String username, final String host) {
    if (commandId == null) {
      throw new IllegalArgumentException("Command id cannot be left null");
    }
    this.commandId = commandId;
    this.commandValue = commandValue;
    this.clientTimeout = clientTimeout;
    this.username = username;
    this.host = host;
  }
  
  @Override
  public Long getId() {
    return commandId;
  }

  @Override
  public T getValue() {
    return commandValue;
  }

  @Override
  public int getTimeout() {
    return clientTimeout;
  }

  @Override
  public String getUsername() {
    return username;
  }
  
  @Override
  public String getHost() {
    return host;
  }
}
