/******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2005-2010 CERN.
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * 
 * Author: TIM team, tim.support@cern.ch
 *****************************************************************************/
package cern.c2mon.server.command.impl;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cern.c2mon.server.cache.CommandTagCache;
import cern.c2mon.server.cache.exception.CacheElementNotFoundException;
import cern.c2mon.server.command.CommandExecutionManager;
import cern.c2mon.server.command.CommandPersistenceListener;
import cern.c2mon.server.daqcommunication.out.ProcessCommunicationManager;
import cern.c2mon.shared.client.command.CommandExecuteRequest;
import cern.c2mon.shared.client.command.CommandExecutionStatus;
import cern.c2mon.shared.client.command.CommandReport;
import cern.c2mon.shared.client.command.CommandReportImpl;
import cern.c2mon.shared.client.command.CommandTagHandle;
import cern.c2mon.shared.client.command.CommandTagHandleImpl;
import cern.c2mon.shared.client.command.CommandTagHandleImpl.Builder;
import cern.c2mon.shared.client.command.RbacAuthorizationDetails;
import cern.c2mon.shared.daq.command.CommandExecutionDetails;
import cern.c2mon.shared.daq.command.CommandTag;

/**
 * Implementation of the CommandExecutionManager for TIM.
 * 
 * @author Mark Brightwell
 *
 */
@Service
public class CommandExecutionManagerImpl implements CommandExecutionManager {

  /**
   * Private class logger.
   */
  private static final Logger LOGGER = Logger.getLogger(CommandExecutionManagerImpl.class);
  
  /**
   * Reference to the bean for sending to the DAQ layer.
   */
  private ProcessCommunicationManager processCommunicationManager;
  
  /**
   * Reference to the CommandTag cache.
   */
  private CommandTagCache commandTagCache;
  
  /**
   * Reference to a listener responsible for persisting the command
   * tags to the DB. Must be registered using the provided
   * registration method (<code>registerAsPersistenceListener()</code>).
   */
  private CommandPersistenceListener commandPersistenceListener;  
  
  /**
   * Autowired constructor.
   * @param processCommunicationManager the singleton ProcessCommunicationManager
   * @param commandTagCache the singleton Command cache
   */    
  @Autowired
  public CommandExecutionManagerImpl(final ProcessCommunicationManager processCommunicationManager, 
                                     final CommandTagCache commandTagCache) {
    super();
    this.processCommunicationManager = processCommunicationManager;
    this.commandTagCache = commandTagCache;
  }

  @Override
  public <T> CommandReport execute(final CommandExecuteRequest<T> request) {
    CommandReport report = null;

    if (request == null) {
      String message = "execute() : called with null parameter.";
      LOGGER.error(message);
      throw new NullPointerException(message);
    }
    
    // TODO add RBAC check once token is passed with JMS call      

    /* Try to execute the command */       
    try {
      CommandTag<T> commandTag = commandTagCache.getCopy(request.getId());
      CommandExecutionDetails<T> details = new CommandExecutionDetails<T>();
      details.setExecutionStartTime(new Timestamp(System.currentTimeMillis()));
      details.setValue(request.getValue());
      details.setUsername(request.getUsername());
      details.setHost(request.getHost());
      report = processCommunicationManager.executeCommand(commandTag, request.getValue());
      details.setExecutionEndTime(new Timestamp(System.currentTimeMillis()));
      //log if listener registered
      commandTag.setCommandExecutionDetails(details); // only for logging
      if (commandPersistenceListener != null) {
        if (LOGGER.isDebugEnabled()) {
          LOGGER.debug("execute() : logging command tag.");
        }            
        try {
          commandPersistenceListener.log(commandTag, report);                
        } catch (Exception e) {
          LOGGER.error("Error while logging commands to DB", e);          
        }        
      }

    } catch (CacheElementNotFoundException cacheEx) {
      LOGGER.error("Unable to locate CommandTag with id " + request.getId() + " in the cache.", cacheEx);
      report = new CommandReportImpl(request.getId(), 
                                  CommandExecutionStatus.STATUS_EXECUTION_FAILED, 
                                   "Unable to locate the Command tag in the server cache.");
    } catch (Exception e) {
      LOGGER.error("Exception caught while executing command", e);
      report = new CommandReportImpl(request.getId(), 
                                  CommandExecutionStatus.STATUS_EXECUTION_FAILED, 
                                   e.getMessage());
    }
        
//TODO re-enable once RBAC check is done on server        
//      } else {
//        if (LOGGER.isDebugEnabled()) {
//          LOGGER.debug("execute() : not authorized.");
//        }
//        report = 
//            new CommandReport(handle.getId(), 
//                              CommandReport.STATUS_AUTHORISATION_FAILED, 
//                              "This CommandTagHandle has not been authorised by the server");
//      }      
     
    return report;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> Collection<CommandTagHandle<T>> processRequest(final Collection<Long> commandIds) {     
    //String hostname = request.getHostName();    
    CommandTag<T> commandTagCopy = null;
    Collection<CommandTagHandle<T>> commandTagHandles = new ArrayList<CommandTagHandle<T>>();
    
    if (commandIds.isEmpty()) {
      LOGGER.warn("processRequest(Collection<Long> commandIds) method called with an empty collection argument - returning empty result");
      return commandTagHandles;
    }

    Iterator<Long> it = commandIds.iterator();
    Long id;
    while (it.hasNext()) {
      id = it.next();
      if (id == null) {
        LOGGER.warn("Received request for command with null id - ignoring this request. Check your client code!");
      } else {
        try {
          commandTagCopy = commandTagCache.getCopy(id);
          Builder<T> builder = new Builder<>(id);
          builder.name(commandTagCopy.getName())
                 .description(commandTagCopy.getDescription())
                 .dataType(commandTagCopy.getDataType())
                 .clientTimeout(commandTagCopy.getClientTimeout())
                 .hardwareAddress(commandTagCopy.getHardwareAddress())
                 .minValue(commandTagCopy.getMinimum())
                 .maxValue(commandTagCopy.getMaximum())
                 .rbacAuthorizationDetails((RbacAuthorizationDetails) commandTagCopy.getAuthorizationDetails())
                 .processId(commandTagCopy.getProcessId())
                 .equipmentId(commandTagCopy.getEquipmentId());
          commandTagHandles.add(new CommandTagHandleImpl<T>(builder));
        } catch (CacheElementNotFoundException cacheEx) {
          /* The specified CommandTag is not defined in the system. */
          commandTagHandles.add(new CommandTagHandleImpl<T>(id, null));
          LOGGER.warn("Unable to locate requested command in the cache (Id is " + id + ") - will not be returned to client. ", cacheEx);      
        }
      }      
    }
    return commandTagHandles;
  }

  @Override
  public void registerAsPersistenceListener(final CommandPersistenceListener commandPersistenceListener) {
    if (commandPersistenceListener == null) {
      throw new NullPointerException("Attempt at registering a null CommandPersistenceListener with the CommandExecutionManager.");
    }
    this.commandPersistenceListener = commandPersistenceListener;    
  }

}
