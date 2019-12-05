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
package cern.c2mon.server.configuration.handler.impl;

import cern.c2mon.cache.actions.process.ProcessService;
import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.server.cache.exception.CacheElementNotFoundException;
import cern.c2mon.server.common.process.Process;
import cern.c2mon.server.configuration.config.ConfigurationProperties;
import cern.c2mon.server.configuration.handler.ControlTagConfigHandler;
import cern.c2mon.server.configuration.handler.EquipmentConfigHandler;
import cern.c2mon.server.configuration.handler.ProcessConfigHandler;
import cern.c2mon.server.configuration.impl.ProcessChange;
import cern.c2mon.server.daq.JmsContainerManager;
import cern.c2mon.shared.client.configuration.ConfigConstants.Action;
import cern.c2mon.shared.client.configuration.ConfigConstants.Entity;
import cern.c2mon.shared.client.configuration.ConfigurationElement;
import cern.c2mon.shared.client.configuration.ConfigurationElementReport;
import cern.c2mon.shared.common.CacheEvent;
import cern.c2mon.shared.common.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.UnexpectedRollbackException;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;

/**
 * See interface documentation.
 *
 * @author Mark Brightwell
 */
@Component
public class ProcessConfigHandlerImpl implements ProcessConfigHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(ProcessConfigHandlerImpl.class);

  private ProcessConfigHandler processConfigTransacted;

  private EquipmentConfigHandler equipmentConfigHandler;

  private ControlTagConfigHandler controlTagConfigHandler;

  private C2monCache<Process> processCache;

  private ProcessService processService;

  /**
   * Flag indicating if Process removal is allowed when the Process
   * is running.
   */
  private boolean allowRunningProcessRemoval;

  /**
   * Reference to the bean managing DAQ-in JMS connections.
   */
  private JmsContainerManager jmsContainerManager;

  @Autowired
  public ProcessConfigHandlerImpl(EquipmentConfigHandler equipmentConfigHandler,
                                  ControlTagConfigHandler controlTagConfigHandler,
                                  ProcessService processService,
                                  JmsContainerManager jmsContainerManager,
                                  ConfigurationProperties properties,
                                  ProcessConfigHandler processConfigTransacted) {
    super();
    this.equipmentConfigHandler = equipmentConfigHandler;
    this.controlTagConfigHandler = controlTagConfigHandler;
    this.processCache = processService.getCache();
    this.processService = processService;
    this.jmsContainerManager = jmsContainerManager;
    this.allowRunningProcessRemoval = properties.isAllowRunningProcessRemoval();
    this.processConfigTransacted = processConfigTransacted;
  }

  @PostConstruct
  public void init() {
    equipmentConfigHandler.setProcessConfigHandler(this);
  }

  /**
   * Tries to remove the process and all its descendents. The process
   * itself is only completely removed if all the equipments, subequipments
   * and associated tags, commands are all removed successfully.
   *
   * <p>In the case of a failure, the removal is interrupted and the process
   * remains with whatever child objects remain at the point of failure.
   *
   * @param processId     id of process
   * @param processReport the element report for the removal of the process, to which
   *                      subreports can be attached
   */
  @Override
  public ProcessChange remove(final Long processId, final ConfigurationElementReport processReport) {
    LOGGER.debug("Removing process with id " + processId);
    ProcessChange processChange;
    try {
      Process process = processCache.get(processId);
      try {
        Collection<Long> equipmentIds = new ArrayList<>(process.getEquipmentIds());
        if (process.isRunning() && !allowRunningProcessRemoval) {
          String message = "Unable to remove Process " + process.getName() + " as currently running - please stop it first.";
          LOGGER.warn(message);
          processReport.setFailure(message);
          processChange = new ProcessChange();
        } else {
          //remove all associated equipment from system
          for (Long equipmentId : equipmentIds) {
            ConfigurationElementReport childElementReport = new ConfigurationElementReport(Action.REMOVE, Entity.EQUIPMENT, equipmentId);
            try {
              processReport.addSubReport(childElementReport);
              equipmentConfigHandler.remove(equipmentId, childElementReport);
            } catch (RuntimeException ex) {
              LOGGER.error("Exception caught while applying the configuration change (Action, Entity, Entity id) = ("
                + Action.REMOVE + "; " + Entity.EQUIPMENT + "; " + equipmentId + ")", ex);
              childElementReport.setFailure("Exception caught while applying the configuration change.", ex);
              throw new UnexpectedRollbackException("Unexpected exception caught while removing an Equipment.", ex);
            }
          }
          processChange = processCache.executeTransaction(() -> {
            ProcessChange processChangeResult = processConfigTransacted.remove(processId, processReport);
            removeProcessControlTags(process, processReport);
            return processChangeResult;
          });

          //remove alive out of lock (in fact no longer necessary); always after removing control tags, or could be pulled back in from DB to cache
          processService.removeAliveTimerBySupervisedId(processId);
          jmsContainerManager.unsubscribe(process);
          processCache.remove(processId);
        }
        return processChange;
      } catch (RuntimeException ex) {
        LOGGER.error("Exception caught when attempting to remove a process - rolling back DB changes: {}", ex.getMessage());
        throw new UnexpectedRollbackException("Unexpected exception caught while removing Process.", ex);
      }
    } catch (CacheElementNotFoundException cacheEx) {
      LOGGER.warn("Process not found in cache - unable to remove it.", cacheEx);
      processReport.setWarning("Process not found in cache so cannot be removed.");
      return new ProcessChange();
    }
  }

  @Override
  public ProcessChange create(final ConfigurationElement element) throws IllegalAccessException {
    LOGGER.debug("Creating process with id " + element.getEntityId());
    if (processCache.containsKey(element.getEntityId())) {
      throw new ConfigurationException(ConfigurationException.ENTITY_EXISTS, "Attempting to create a process with an already existing id: "
        + element.getEntityId());
    }
    Process process = null;
    try {
      ProcessChange change = processConfigTransacted.create(element);
      process = processCache.get(element.getEntityId());
      jmsContainerManager.subscribe(process);
      processService.startAliveTimerBySupervisedId(element.getEntityId());
      processCache.getCacheListenerManager().notifyListenersOf(CacheEvent.UPDATE_ACCEPTED, process);
      return change;
    } catch (RuntimeException ex) {
      LOGGER.error("Exception caught while creating a new Process - rolling back DB changes and removing from cache.");
      processCache.remove(element.getEntityId());
      if (process != null) {
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
  public ProcessChange update(final Long processId,
                              final Properties elementProperties) throws IllegalAccessException {

    if (elementProperties.containsKey("id")) {
      LOGGER.warn("Attempting to change the process id - this is not currently supported!");
      elementProperties.remove("id");
    }

    if (elementProperties.containsKey("name")) {
      LOGGER.warn("Attempting to change the process name - this is not currently supported!");
      elementProperties.remove("name");
    }

    boolean aliveConfigure = false;
    if (elementProperties.containsKey("aliveInterval") || elementProperties.containsKey("aliveTagId")) {
      aliveConfigure = true;
    }

    ProcessChange processChange = new ProcessChange(processId);
    try {
      processConfigTransacted.update(processId, elementProperties);

      //stop old, start new - transaction is committed here
      if (aliveConfigure) {
        processService.startAliveTimerBySupervisedId(processId);

        // TODO (Alex) Is this call correct? Looks like maybe they wanted to setReboot instead?
        processChange.requiresReboot();
      }

    } catch (CacheElementNotFoundException cacheEx) {
      LOGGER.warn("Unable to locate Process " + processId + " in cache so unable to update it.");
      throw cacheEx;
    } catch (RuntimeException e) {
      LOGGER.error("Exception caught while updating Process " + processId + " - rolling back DB and cache changes for this Process.");
      throw new UnexpectedRollbackException("Unexpected exception caught while updating Process " + processId, e);
    }

    return processChange;
  }

  /**
   * Setter.
   *
   * @param allowRunningProcessRemoval the allowRunningProcessRemoval to set
   */
  public void setAllowRunningProcessRemoval(final boolean allowRunningProcessRemoval) {
    this.allowRunningProcessRemoval = allowRunningProcessRemoval;
  }

  /**
   * Removes process alive and state tags (from DB and cache).
   *
   * @param process
   * @param processReport
   */
  private void removeProcessControlTags(Process process, ConfigurationElementReport processReport) {
    LOGGER.debug("Removing Process control tags for process " + process.getId());
    Long aliveTagId = process.getAliveTagId();
    if (aliveTagId != null) {
      ConfigurationElementReport tagReport = new ConfigurationElementReport(Action.REMOVE, Entity.CONTROLTAG, aliveTagId);
      processReport.addSubReport(tagReport);
      controlTagConfigHandler.remove(aliveTagId, tagReport);
    }
    Long stateTagId = process.getStateTagId();
    ConfigurationElementReport tagReport = new ConfigurationElementReport(Action.REMOVE, Entity.CONTROLTAG, stateTagId);
    processReport.addSubReport(tagReport);
    controlTagConfigHandler.remove(stateTagId, tagReport);
  }
}
