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
package cern.c2mon.server.client.request;

import java.util.ArrayList;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cern.c2mon.server.command.CommandExecutionManager;
import cern.c2mon.shared.client.command.CommandExecuteRequest;
import cern.c2mon.shared.client.command.CommandReport;
import cern.c2mon.shared.client.request.ClientRequest;
import cern.c2mon.shared.client.request.ClientRequestResult;

/**
 * Helper class for {@link ClientRequestDelegator} to handle
 * command requests.
 *
 * @author Matthias Braeger
 */
@Service
class ClientCommandRequestHandler {
  
  /** Private class logger */
  private static final Logger LOG = LoggerFactory.getLogger(ClientCommandRequestHandler.class);
  
  
  /** Reference to the CommandExecutionManager */
  private final CommandExecutionManager commandExecutionManager;
  
  @Autowired
  public ClientCommandRequestHandler(final CommandExecutionManager commandExecutionManager) {
    this.commandExecutionManager = commandExecutionManager;
  }
  
  /**
   * Inner method which handles the CommandTagHandle Requests
   *
   * @param commandRequest The command request sent from the client
   * @return a Collection of CommandTagHandles
   */
  Collection<? extends ClientRequestResult> handleCommandHandleRequest(final ClientRequest commandRequest) {

    switch (commandRequest.getResultType()) {
    case TRANSFER_COMMAND_HANDLES_LIST:

      return commandExecutionManager.processRequest(commandRequest.getTagIds());
    default:
      LOG.error("handleCommandHandleRequest() - Could not generate response message. Unknown enum ResultType " + commandRequest.getResultType());
    }
    return null;
  }

  /**
   * Inner method which handles the Execute Command Request
   *
   * @param executeCommandRequest The command request send from the client
   * @return A command report
   */
  Collection<? extends ClientRequestResult> handleExecuteCommandRequest(final ClientRequest executeCommandRequest) {

    final Collection<CommandReport> commandReports = new ArrayList<CommandReport>(1);
    commandReports.add(commandExecutionManager.execute((CommandExecuteRequest<?>) executeCommandRequest.getObjectParameter()));
    if (LOG.isDebugEnabled()) {
      LOG.debug("Finished executing command - returning report.");
    }
    return commandReports;
  }
}
