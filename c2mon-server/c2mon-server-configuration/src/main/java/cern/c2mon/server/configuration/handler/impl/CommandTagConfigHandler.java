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

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cern.c2mon.server.cache.CommandTagCache;
import cern.c2mon.server.cache.CommandTagFacade;
import cern.c2mon.server.cache.EquipmentFacade;
import cern.c2mon.server.cache.exception.CacheElementNotFoundException;
import cern.c2mon.server.cache.loading.CommandTagDAO;
import cern.c2mon.server.common.command.CommandTagCacheObject;
import cern.c2mon.server.configuration.impl.ProcessChange;
import cern.c2mon.shared.client.configuration.ConfigurationElement;
import cern.c2mon.shared.client.configuration.ConfigurationElementReport;
import cern.c2mon.shared.common.command.CommandTag;
import cern.c2mon.shared.daq.config.Change;
import cern.c2mon.shared.daq.config.CommandTagAdd;
import cern.c2mon.shared.daq.config.CommandTagRemove;

/**
 * See interface documentation.
 *
 * @author Mark Brightwell
 *
 */
@Slf4j
@Service
public class CommandTagConfigHandler {

  @Autowired
  private CommandTagFacade commandTagFacade;

  @Autowired
  private CommandTagDAO commandTagDAO;

  @Autowired
  private CommandTagCache commandTagCache;

  @Autowired
  private EquipmentFacade equipmentFacade;

  public List<ProcessChange> createCommandTag(ConfigurationElement element) throws IllegalAccessException {
    commandTagCache.acquireWriteLockOnKey(element.getEntityId());
    try {
      log.trace("Creating CommandTag " + element.getEntityId());
      CommandTag<?> commandTag = commandTagFacade.createCacheObject(element.getEntityId(), element.getElementProperties());
      commandTagDAO.insertCommandTag(commandTag);
      commandTagCache.putQuiet(commandTag);
      equipmentFacade.addCommandToEquipment(commandTag.getEquipmentId(), commandTag.getId());

      commandTagCache.notifyListenersOfUpdate(commandTag.getId());

      CommandTagAdd commandTagAdd = new CommandTagAdd(element.getSequenceId(),
                                                      commandTag.getEquipmentId(),
                                                      commandTagFacade.generateSourceCommandTag(commandTag));
      ArrayList<ProcessChange> processChanges = new ArrayList<>();
      processChanges.add(new ProcessChange(equipmentFacade.getProcessIdForAbstractEquipment(commandTag.getEquipmentId()), commandTagAdd));
      return processChanges;
    } finally {
      commandTagCache.releaseWriteLockOnKey(element.getEntityId());
    }
  }

  public List<ProcessChange> updateCommandTag(Long id, Properties properties) throws IllegalAccessException {
    log.trace("Updating CommandTag {}", id);
    //reject if trying to change equipment it is attached to - not currently allowed
    if (properties.containsKey("equipmentId")) {
      log.warn("Attempting to change the equipment to which a command is attached - this is not currently supported!");
      properties.remove("equipmentId");
    }
    Change commandTagUpdate = null;
    Long equipmentId = commandTagCache.get(id).getEquipmentId();
    commandTagCache.acquireWriteLockOnKey(id);

    try {
      CommandTag<?> commandTag = commandTagCache.get(id);
      commandTagUpdate = commandTagFacade.updateConfig(commandTag, properties);
      commandTagDAO.updateCommandTag(commandTag);
    } finally {
      commandTagCache.releaseWriteLockOnKey(id);
    }

    List<ProcessChange> processChanges = new ArrayList<>();

    if (commandTagUpdate.hasChanged()) {
      processChanges.add(new ProcessChange(equipmentFacade.getProcessIdForAbstractEquipment(equipmentId), commandTagUpdate));
    }

    return processChanges;
  }

  /**
   *
   * @param id
   * @param elementReport
   * @return a ProcessChange event to send to the DAQ if no error occurred
   */
  public List<ProcessChange> removeCommandTag(final Long id, final ConfigurationElementReport elementReport) {
    log.trace("Removing CommandTag " + id);
    ArrayList<ProcessChange> processChanges = new ArrayList<>();
    Long equipmentId;
    commandTagCache.acquireWriteLockOnKey(id);
    try {
      CommandTag<?> commandTag = commandTagCache.get(id);
      equipmentId = commandTag.getEquipmentId();
      commandTagDAO.deleteCommandTag(commandTag.getId());
      commandTagCache.remove(commandTag.getId());
      commandTagCache.releaseWriteLockOnKey(id);
      //unlock before accessing equipment
      equipmentFacade.removeCommandFromEquipment(commandTag.getEquipmentId(), commandTag.getId());
      CommandTagRemove removeEvent = new CommandTagRemove();
      removeEvent.setCommandTagId(id);
      removeEvent.setEquipmentId(equipmentId);
      processChanges.add(new ProcessChange(equipmentFacade.getProcessIdForAbstractEquipment(commandTag.getEquipmentId()), removeEvent));
    } catch (CacheElementNotFoundException e) {
      log.warn("Attempting to remove a non-existent Command - no action taken.");
      elementReport.setWarning("Attempting to remove a non-existent CommandTag");
    } catch (Exception ex) {
      elementReport.setFailure("Exception caught while removing a commandtag.", ex);
      log.error("Exception caught while removing a commandtag (id: " + id + ")", ex);
      throw new RuntimeException(ex);
    } finally {
      if (commandTagCache.isWriteLockedByCurrentThread(id)) {
        commandTagCache.releaseWriteLockOnKey(id);
      }
    }
    return processChanges;
  }

}
