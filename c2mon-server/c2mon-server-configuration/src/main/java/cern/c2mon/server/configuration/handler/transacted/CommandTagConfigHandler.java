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
package cern.c2mon.server.configuration.handler.transacted;

import cern.c2mon.cache.actions.command.CommandTagService;
import cern.c2mon.cache.actions.equipment.EquipmentService;
import cern.c2mon.cache.config.command.CommandTagCacheObjectFactory;
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

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * See interface documentation.
 *
 * @author Mark Brightwell
 */
@Slf4j
@Named
public class CommandTagConfigHandler extends BaseConfigHandlerImpl<CommandTag> {

  private CommandTagService commandTagService;

  private EquipmentService equipmentService;

  @Inject
  public CommandTagConfigHandler(CommandTagService commandTagService, CommandTagDAO commandTagDAO,
                                 CommandTagCacheObjectFactory commandTagCacheObjectFactory, EquipmentService equipmentService) {
    super(commandTagService.getCache(), commandTagDAO, commandTagCacheObjectFactory, ArrayList::new);
    this.commandTagService = commandTagService;
    this.equipmentService = equipmentService;
  }

  @Override
  protected void doPostCreate(CommandTag commandTag) {
    super.doPostCreate(commandTag);
    equipmentService.addCommandToEquipment(commandTag.getEquipmentId(), commandTag.getId());
    cache.getCacheListenerManager().notifyListenersOf(CacheEvent.INSERTED, commandTag);
  }

  @Override
  protected List<ProcessChange> createReturnValue(CommandTag commandTag, ConfigurationElement element) {
    CommandTagAdd commandTagAdd = new CommandTagAdd(
      element.getSequenceId(),
      commandTag.getEquipmentId(),
      commandTagService.generateSourceCommandTag(commandTag)
    );

    ArrayList<ProcessChange> processChanges = new ArrayList<>();
    processChanges.add(new ProcessChange(equipmentService.getProcessId(commandTag.getEquipmentId()), commandTagAdd));
    return processChanges;
  }

  public List<ProcessChange> updateCommandTag(Long id, Properties properties) {
    //reject if trying to change equipment it is attached to - not currently allowed
    if (properties.containsKey("equipmentId")) {
      log.warn("Attempting to change the equipment to which a command is attached - this is not currently supported!");
      properties.remove("equipmentId");
    }
    return super.update(id, properties);
  }

  @Override
  protected List<ProcessChange> updateReturnValue(CommandTag commandTag, Change commandTagUpdate, Properties properties) {
    List<ProcessChange> processChanges = new ArrayList<>();

    if (commandTagUpdate.hasChanged()) {
      processChanges.add(new ProcessChange(equipmentService.getProcessId(commandTag.getEquipmentId()), commandTagUpdate));
    }

    return processChanges;
  }

  @Override
  protected List<ProcessChange> removeReturnValue(CommandTag commandTag, ConfigurationElementReport report) {
    ArrayList<ProcessChange> processChanges = new ArrayList<>();

    try {
      equipmentService.removeCommandFromEquipment(commandTag.getEquipmentId(), commandTag.getId());
      CommandTagRemove removeEvent = new CommandTagRemove();
      removeEvent.setCommandTagId(commandTag.getId());
      removeEvent.setEquipmentId(commandTag.getEquipmentId());
      processChanges.add(new ProcessChange(equipmentService.getProcessId(commandTag.getEquipmentId()), removeEvent));
    } catch (Exception ex) {
      report.setFailure("Exception caught while removing a commandtag.", ex);
      log.error("Exception caught while removing a commandtag (id: " + commandTag.getId() + ")", ex);
      throw new RuntimeException(ex);
    }

    return processChanges;
  }

}
