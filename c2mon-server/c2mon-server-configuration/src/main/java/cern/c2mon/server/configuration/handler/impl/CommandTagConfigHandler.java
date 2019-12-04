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

import cern.c2mon.cache.actions.command.CommandTagCacheObjectFactory;
import cern.c2mon.cache.actions.command.CommandTagService;
import cern.c2mon.cache.actions.equipment.EquipmentService;
import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.server.cache.exception.CacheElementNotFoundException;
import cern.c2mon.server.cache.loading.CommandTagDAO;
import cern.c2mon.server.configuration.impl.ProcessChange;
import cern.c2mon.shared.client.configuration.ConfigurationElement;
import cern.c2mon.shared.client.configuration.ConfigurationElementReport;
import cern.c2mon.shared.common.CacheEvent;
import cern.c2mon.shared.common.command.CommandTag;
import cern.c2mon.shared.daq.config.Change;
import cern.c2mon.shared.daq.config.CommandTagAdd;
import cern.c2mon.shared.daq.config.CommandTagRemove;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * See interface documentation.
 *
 * @author Mark Brightwell
 */
@Slf4j
@Service
public class CommandTagConfigHandler {

  private CommandTagService commandTagService;

  private CommandTagDAO commandTagDAO;

  private CommandTagCacheObjectFactory commandTagCacheObjectFactory;

  private C2monCache<CommandTag> commandTagCache;

  private EquipmentService equipmentService;

  public CommandTagConfigHandler(CommandTagService commandTagService, CommandTagDAO commandTagDAO,
                                 CommandTagCacheObjectFactory commandTagCacheObjectFactory, EquipmentService equipmentService) {
    this.commandTagService = commandTagService;
    this.commandTagDAO = commandTagDAO;
    this.commandTagCache = commandTagService.getCache();
    this.commandTagCacheObjectFactory = commandTagCacheObjectFactory;
    this.equipmentService = equipmentService;
  }

  public List<ProcessChange> createCommandTag(ConfigurationElement element) {
    return commandTagCache.executeTransaction(() -> {
      log.trace("Creating CommandTag " + element.getEntityId());
      CommandTag<?> commandTag = commandTagCacheObjectFactory.createCacheObject(element.getEntityId(), element.getElementProperties());
      commandTagDAO.insertCommandTag(commandTag);
      commandTagCache.putQuiet(commandTag.getId(), commandTag);
      equipmentService.addCommandToEquipment(commandTag.getEquipmentId(), commandTag.getId());

      commandTagCache.getCacheListenerManager().notifyListenersOf(CacheEvent.UPDATE_ACCEPTED, commandTag);

      CommandTagAdd commandTagAdd = new CommandTagAdd(element.getSequenceId(),
        commandTag.getEquipmentId(),
        commandTagService.generateSourceCommandTag(commandTag));
      ArrayList<ProcessChange> processChanges = new ArrayList<>();
      processChanges.add(new ProcessChange(equipmentService.getProcessId(commandTag.getEquipmentId()), commandTagAdd));
      return processChanges;
    });
  }

  public List<ProcessChange> updateCommandTag(Long id, Properties properties) {
    log.trace("Updating CommandTag {}", id);
    //reject if trying to change equipment it is attached to - not currently allowed
    if (properties.containsKey("equipmentId")) {
      log.warn("Attempting to change the equipment to which a command is attached - this is not currently supported!");
      properties.remove("equipmentId");
    }
    Long equipmentId = commandTagCache.get(id).getEquipmentId();

    Change commandTagUpdate = commandTagCache.executeTransaction(() -> {
      Change commandTagUpdateInternal;
      CommandTag<?> commandTag = commandTagCache.get(id);
      commandTagUpdateInternal = commandTagCacheObjectFactory.updateConfig(commandTag, properties);
      commandTagDAO.updateCommandTag(commandTag);
      return commandTagUpdateInternal;
    });

    List<ProcessChange> processChanges = new ArrayList<>();

    if (commandTagUpdate.hasChanged()) {
      processChanges.add(new ProcessChange(equipmentService.getProcessId(equipmentId), commandTagUpdate));
    }

    return processChanges;
  }

  /**
   * @param id
   * @param elementReport
   * @return a ProcessChange event to send to the DAQ if no error occurred
   */
  public List<ProcessChange> removeCommandTag(final Long id, final ConfigurationElementReport elementReport) {
    log.trace("Removing CommandTag " + id);
    ArrayList<ProcessChange> processChanges = new ArrayList<>();

    commandTagCache.executeTransaction(() -> {
      try {
        CommandTag<?> commandTag = commandTagCache.get(id);
        commandTagDAO.deleteCommandTag(commandTag.getId());
        commandTagCache.remove(commandTag.getId());
        Long equipmentId = commandTag.getEquipmentId();

        //unlock before accessing equipment
        equipmentService.removeCommandFromEquipment(commandTag.getEquipmentId(), commandTag.getId());
        CommandTagRemove removeEvent = new CommandTagRemove();
        removeEvent.setCommandTagId(id);
        removeEvent.setEquipmentId(equipmentId);
        processChanges.add(new ProcessChange(equipmentService.getProcessId(commandTag.getEquipmentId()), removeEvent));
      } catch (CacheElementNotFoundException e) {
        log.warn("Attempting to remove a non-existent Command - no action taken.");
        elementReport.setWarning("Attempting to remove a non-existent CommandTag");
      } catch (Exception ex) {
        elementReport.setFailure("Exception caught while removing a commandtag.", ex);
        log.error("Exception caught while removing a commandtag (id: " + id + ")", ex);
        throw new RuntimeException(ex);
      }
    });
    return processChanges;
  }

}
