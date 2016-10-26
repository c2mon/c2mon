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
package cern.c2mon.client.core;

import java.util.Set;

import cern.c2mon.client.common.tag.CommandTag;
import cern.c2mon.shared.client.command.CommandReport;
import cern.c2mon.shared.client.command.CommandTagValueException;

/**
 * The <code>C2monCommandManager</code> allows to retrieve command
 * information from the server and to send an execute request. Please
 *
 * @deprecated Please use {@link CommandService} instead
 * @author Matthias Braeger
 */
@Deprecated
public interface C2monCommandManager {
  /**
   * Creates a Collection of ClientCommandTags from a Collection of identifiers
   * 
   * @param <T> The value type of the command
   * @param pCommandIds Collection of unique tag identifiers to create
   *        ClientCommandTags from
   * @return Collection of clientCommandTag instances
   **/
  <T> Set<CommandTag<T>> getCommandTags(final Set<Long> pCommandIds);
  
  /**
   * Returns the {@link CommandTag} object for the given
   * command id. If the command is unknown to the system it will
   * nevertheless return a {@link CommandTag} instance but
   * with most of the fields left uninitialized. 
   * @param <T> The value type of the command
   * @param commandId The command tag id
   * @return A copy of the {@link CommandTag} instance in the
   *         command cache.
   */
  <T> CommandTag<T> getCommandTag(final Long commandId);
  
  /**
   * Executes the command and returns a {@link CommandReport} object.
   * 
   * @param userName The name of the user which wants to execute the command
   * @param commandId The id of the command that shall be executed
   * @param value The command value that shall be used for execution 
   * @return the report on the success/failure of the execution
   * @throws CommandTagValueException In case the method is called with a
   *         value object which is not of expected type of the specified
   *         {@link CommandTag}.
   * @see CommandTag#getType()
   */  
  CommandReport executeCommand(String userName, Long commandId, Object value) throws CommandTagValueException;
  
  /**
   * Checks whether the logged user is authorized to execute a given command.
   * @param userName The name of the user that want to execute the command
   * @param commandId the command that shall be ckecked
   * @return <code>true</code>, if a user is logged in and has the priviledges
   *         to execute the command specified by the <code>commandId</code>
   *         parameter.
   * @see C2monSessionManager#isAuthorized(String, cern.c2mon.shared.common.command.AuthorizationDetails)
   * @see C2monSessionManager#getLoggedUserNames()
   */
  boolean isAuthorized(String userName, Long commandId);

  /**
   * Refreshes the entire local command tag cache with the latest
   * command tag information from the C2MON server.
   */
  void refreshCommandCache();
}
