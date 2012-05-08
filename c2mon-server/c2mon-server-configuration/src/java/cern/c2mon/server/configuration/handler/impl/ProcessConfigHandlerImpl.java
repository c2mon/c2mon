/******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2005-2011 CERN.
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
package cern.c2mon.server.configuration.handler.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.UnexpectedRollbackException;

import cern.c2mon.server.configuration.handler.ControlTagConfigHandler;
import cern.c2mon.server.configuration.handler.EquipmentConfigHandler;
import cern.c2mon.server.configuration.handler.ProcessConfigHandler;
import cern.c2mon.server.configuration.handler.transacted.ProcessConfigTransacted;
import cern.c2mon.server.configuration.impl.ProcessChange;
import cern.tim.server.cache.ProcessCache;
import cern.tim.server.cache.ProcessFacade;
import cern.tim.server.cache.exception.CacheElementNotFoundException;
import cern.tim.server.common.process.Process;
import cern.tim.server.daqcommunication.in.JmsContainerManager;
import cern.tim.shared.client.configuration.ConfigurationElement;
import cern.tim.shared.client.configuration.ConfigurationElementReport;
import cern.tim.shared.client.configuration.ConfigConstants.Action;
import cern.tim.shared.client.configuration.ConfigConstants.Entity;
import cern.tim.shared.common.ConfigurationException;
import cern.tim.shared.daq.config.Change;

/**
 * See interface documentation.
 * 
 * @author Mark Brightwell
 *
 */
public class ProcessConfigHandlerImpl implements ProcessConfigHandler {

  private static final Logger LOGGER = Logger.getLogger(ProcessConfigHandlerImpl.class);
  
  /**
   * Transacted bean.
   */
  @Autowired
  private ProcessConfigTransacted processConfigTransacted;
  
  private EquipmentConfigHandler equipmentConfigHandler;
  
  private ControlTagConfigHandler controlTagConfigHandler;
  
  /**
   * Cache.
   */
  private ProcessCache processCache;
  
  private ProcessFacade processFacade;
  
  /**
   * Flag indicating if Process removal is allowed when the Process
   * is running.
   */
  private boolean allowRunningProcessRemoval = false;
  
  /**
   * Reference to the bean managing DAQ-in JMS connections.
   */
  private JmsContainerManager jmsContainerManager;
    
  
  @Autowired
  public ProcessConfigHandlerImpl(EquipmentConfigHandler equipmentConfigHandler, ControlTagConfigHandler controlTagConfigHandler, ProcessCache processCache,
      ProcessFacade processFacade, JmsContainerManager jmsContainerManager) {
    super();
    this.equipmentConfigHandler = equipmentConfigHandler;
    this.controlTagConfigHandler = controlTagConfigHandler;
    this.processCache = processCache;
    this.processFacade = processFacade;    
    this.jmsContainerManager = jmsContainerManager;
  }

  /**
   * Tries to remove the process and all its descendents. The process
   * itself is only completely removed if all the equipments, subequipments
   * and associated tags, commands are all removed successfully.
   * 
   * <p>In the case of a failure, the removal is interrupted and the process
   * remains with whatever child objects remain at the point of failure.
   * @param processId id of process
   * @param processReport the element report for the removal of the process, to which 
   *                          subreports can be attached
   */
  @Override
  public ProcessChange removeProcess(final Long processId, final ConfigurationElementReport processReport) {    
    LOGGER.debug("Removing process with id " + processId);
    ProcessChange processChange;
    try {
      Process process = processCache.get(processId);
      try {                
        Collection<Long> equipmentIds = process.getCopyEquipmentIds();
        if (processFacade.isRunning(process) && !allowRunningProcessRemoval) {
          String message = "Unable to remove Process " + process.getName() + " as currently running - please stop it first.";
          LOGGER.warn(message); 
          processReport.setFailure(message);
          processChange = new ProcessChange();
        } else {
          //remove all associated equipment from system   
          for (Long equipmentId : new ArrayList<Long>(equipmentIds)) {
            ConfigurationElementReport childElementReport = new ConfigurationElementReport(Action.REMOVE, Entity.EQUIPMENT, equipmentId);
            try {        
              processReport.addSubReport(childElementReport);
              equipmentConfigHandler.removeEquipment(equipmentId, childElementReport);
            } catch (RuntimeException ex) {
              LOGGER.error("Exception caught while applying the configuration change (Action, Entity, Entity id) = (" 
                  + Action.REMOVE + "; " + Entity.EQUIPMENT + "; " + equipmentId + ")", ex);
              childElementReport.setFailure("Exception caught while applying the configuration change.", ex);          
              throw new UnexpectedRollbackException("Unexpected exception caught while removing an Equipment.", ex);
            }      
          }
          process.getWriteLock().lock();
          processChange = processConfigTransacted.doRemoveProcess(process, processReport);
          removeProcessControlTags(process, processReport);          
          process.getWriteLock().unlock();
          //remove alive out of lock (in fact no longer necessary); always after removing control tags, or could be pulled back in from DB to cache
          processFacade.removeAliveTimer(processId);
          jmsContainerManager.unsubscribe(process);
          processCache.remove(processId);              
         }        
        return processChange;
      } catch (RuntimeException ex) {                  
        LOGGER.error("Exception caught when attempting to remove a process - rolling back DB changes.", ex);        
        throw new UnexpectedRollbackException("Unexpected exception caught while removing Process.", ex);
      } finally {
        if (process.getWriteLock().isHeldByCurrentThread()) {
          process.getWriteLock().unlock();
        }        
      } 
    } catch (CacheElementNotFoundException cacheEx) {
      LOGGER.warn("Process not found in cache - unable to remove it.", cacheEx);
      processReport.setWarning("Process not found in cache so cannot be removed.");
      return new ProcessChange();
    }    
  }
  
  @Override
  public ProcessChange createProcess(final ConfigurationElement element) throws IllegalAccessException {
    LOGGER.debug("Creating process with id " + element.getEntityId());
    if (processCache.hasKey(element.getEntityId())) {
      throw new ConfigurationException(ConfigurationException.ENTITY_EXISTS, "Attempting to create a process with an already existing id: "
          + element.getEntityId());
    }
    Process process = null;
    try {
      ProcessChange change = processConfigTransacted.doCreateProcess(element);
      process = processCache.get(element.getEntityId());
      jmsContainerManager.subscribe(process);
      processFacade.loadAndStartAliveTag(element.getEntityId());
      return change;
    } catch (RuntimeException ex) {
      LOGGER.error("Exception caught while creating a new Process - rolling back DB changes and removing from cache.");
      processCache.remove(element.getEntityId());     
      if (process != null){
        jmsContainerManager.unsubscribe(process);
      }      
      throw new UnexpectedRollbackException("Unexpected error while creating a new Process.", ex);     
    }
    
  }

  @Override
  public void removeEquipmentFromProcess(final Long equipmentId, final Long processId) {
    processConfigTransacted.removeEquipmentFromProcess(equipmentId, processId);
  }

  @Override
  public ProcessChange updateProcess(final Long processId, 
                                    final Properties elementProperties) throws IllegalAccessException {
    if (elementProperties.containsKey("id")) {
      throw new ConfigurationException(ConfigurationException.UNDEFINED, "Attempting to change the process id - this is not currently supported!");
    }
    if (elementProperties.containsKey("name")) {
      throw new ConfigurationException(ConfigurationException.UNDEFINED, "Attempting to change the process name - this is not currently supported!");
    }
    boolean aliveConfigure = false;
    if (elementProperties.containsKey("aliveInterval") || elementProperties.containsKey("aliveTagId")) {
      aliveConfigure = true;
    }    
    try {      
      Change processUpdate; //not used so far, as no change sent to DAQ
      Process process = processCache.get(processId);
      Long oldAliveId = process.getAliveTagId();
      process.getWriteLock().lock();
      try {        
        processUpdate = processFacade.updateConfig(process, elementProperties);
        processConfigTransacted.doUpdateProcess(processId, elementProperties);
        //stop old, start new - transaction is committed here   
        if (aliveConfigure) {
          processFacade.removeAliveDirectly(oldAliveId);
          processFacade.loadAndStartAliveTag(process.getId());
        }
      } catch (RuntimeException e) {
        LOGGER.error("Exception caught while updating a new Process - rolling back DB and cache changes for this Process.");
        //remove newly configured alive directly (process in cache may have been reloaded from DB)
        if (aliveConfigure) {
          processFacade.removeAliveDirectly(process.getAliveTagId());        
        }       
        //reload old cache object
        processCache.remove(processId);
        processCache.loadFromDb(processId);
        //reload old alive
        if (aliveConfigure) {
          processFacade.loadAndStartAliveTag(processId);
        }
        throw new UnexpectedRollbackException("Unexpected exception caught while updating a Process configuration.", e);
      } finally {               
        process.getWriteLock().unlock();        
      }
    } catch (CacheElementNotFoundException e) {
      LOGGER.warn("Unable to locate Process " + processId + " in cache so unable to update it.");
      throw e;
    }    
    return new ProcessChange(processId);
  }

  /**
   * Setter.
   * @param allowRunningProcessRemoval the allowRunningProcessRemoval to set
   */
  public void setAllowRunningProcessRemoval(final boolean allowRunningProcessRemoval) {
    this.allowRunningProcessRemoval = allowRunningProcessRemoval;
  }
  
  /**
   * Removes process alive and state tags (from DB and cache).
   * @param process
   * @param processReport
   */
  private void removeProcessControlTags(Process process, ConfigurationElementReport processReport) {
    LOGGER.debug("Removing Process control tags for process " + process.getId());     
    Long aliveTagId = process.getAliveTagId();
    if (aliveTagId != null) {
      ConfigurationElementReport tagReport = new ConfigurationElementReport(Action.REMOVE, Entity.CONTROLTAG, aliveTagId);
      processReport.addSubReport(tagReport);
      controlTagConfigHandler.removeControlTag(aliveTagId, tagReport);      
    }          
    Long stateTagId = process.getStateTagId();
    ConfigurationElementReport tagReport = new ConfigurationElementReport(Action.REMOVE, Entity.CONTROLTAG, stateTagId);
    processReport.addSubReport(tagReport);  
    controlTagConfigHandler.removeControlTag(stateTagId, tagReport);    
  }

  /**
   * Used for testing.
   * @param processConfigTransacted the processConfigTransacted to set
   */
  public void setProcessConfigTransacted(ProcessConfigTransacted processConfigTransacted) {
    this.processConfigTransacted = processConfigTransacted;
  }
  
}
