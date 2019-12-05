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

import cern.c2mon.server.cache.AliveTimerCache;
import cern.c2mon.server.cache.CommFaultTagCache;
import cern.c2mon.server.cache.EquipmentCache;
import cern.c2mon.server.cache.EquipmentFacade;
import cern.c2mon.server.cache.exception.CacheElementNotFoundException;
import cern.c2mon.server.common.equipment.Equipment;
import cern.c2mon.server.configuration.handler.*;
import cern.c2mon.server.configuration.impl.ProcessChange;
import cern.c2mon.shared.client.configuration.ConfigConstants.Action;
import cern.c2mon.shared.client.configuration.ConfigConstants.Entity;
import cern.c2mon.shared.client.configuration.ConfigurationElement;
import cern.c2mon.shared.client.configuration.ConfigurationElementReport;
import cern.c2mon.shared.daq.config.EquipmentUnitRemove;
import cern.c2mon.shared.daq.config.IChange;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;


/**
 * See interface documentation.
 *
 * @author Mark Brightwell
 *
 */
@Slf4j
@Service
public class EquipmentConfigHandlerImpl extends AbstractEquipmentConfigHandler<Equipment> implements EquipmentConfigHandler {

  private EquipmentConfigHandler equipmentConfigTransacted;

  private SubEquipmentConfigHandler subEquipmentConfigHandler;

  private DataTagConfigHandler dataTagConfigHandler;

  private CommandTagConfigHandler commandTagConfigHandler;

  private EquipmentFacade equipmentFacade;

  private ProcessConfigHandler processConfigHandler;

  private EquipmentCache equipmentCache;

  /**
   * Autowired constructor.
   */
  @Autowired
  public EquipmentConfigHandlerImpl(SubEquipmentConfigHandler subEquipmentConfigHandler, DataTagConfigHandler dataTagConfigHandler,
      CommandTagConfigHandler commandTagConfigHandler, EquipmentFacade equipmentFacade, EquipmentCache equipmentCache,
      ControlTagConfigHandler controlTagConfigHandler, EquipmentConfigHandler equipmentConfigTransacted,
      AliveTimerCache aliveTimerCache, CommFaultTagCache commFaultTagCache) {
    super(controlTagConfigHandler, equipmentConfigTransacted, equipmentCache, aliveTimerCache, commFaultTagCache, equipmentFacade);
    this.subEquipmentConfigHandler = subEquipmentConfigHandler;
    this.dataTagConfigHandler = dataTagConfigHandler;
    this.commandTagConfigHandler = commandTagConfigHandler;
    this.equipmentFacade = equipmentFacade;
    this.equipmentCache = equipmentCache;
    this.equipmentConfigTransacted = equipmentConfigTransacted;
  }

  @Override
  public void setProcessConfigHandler(ProcessConfigHandler processConfigHandler) {
    this.processConfigHandler = processConfigHandler;
  }

  @Override
  public ProcessChange remove(final Long equipmentid, final ConfigurationElementReport equipmentReport) {
    log.debug("Removing Equipment " + equipmentid);
    try {
      Equipment equipmentCopy = equipmentCache.getCopy(equipmentid);
      //WARNING: outside equipment lock, as all these use methods that access a Process (to create ProcessChange object)!
      removeEquipmentTags(equipmentCopy, equipmentReport);
      removeEquipmentCommands(equipmentCopy, equipmentReport);
      removeSubEquipments(new ArrayList<Long>(equipmentCopy.getSubEquipmentIds()), equipmentReport);

      equipmentCache.acquireWriteLockOnKey(equipmentid);
      try {
        equipmentConfigTransacted.remove(equipmentid, equipmentReport);
      } finally {
        equipmentCache.releaseWriteLockOnKey(equipmentid);
      }

      // must be removed last as equipment references them; when this returns are removed from cache and DB permanently
      removeEquipmentControlTags(equipmentCopy, equipmentReport);

      // remove alive & commfault after control tags, or could be pulled back in from DB to cache!
      equipmentFacade.removeAliveTimer(equipmentid);
      equipmentFacade.removeCommFault(equipmentid);
      processConfigHandler.removeEquipmentFromProcess(equipmentid, equipmentCopy.getProcessId());
      equipmentCache.remove(equipmentid);
      IChange equipmentUnitRemove = new EquipmentUnitRemove(0L, equipmentid); //id is reset

      return new ProcessChange(equipmentCopy.getProcessId(), equipmentUnitRemove);

    } catch (CacheElementNotFoundException cacheEx) {
      log.debug("Equipment not found in cache - unable to remove it.");
      equipmentReport.setWarning("Equipment not found in cache so cannot be removed.");
      return new ProcessChange();
    }
  }

  @Override
  public List<ProcessChange> create(ConfigurationElement element) throws IllegalAccessException {
    List<ProcessChange> change = equipmentConfigTransacted.create(element);
    equipmentCache.notifyListenersOfUpdate(element.getEntityId());
    return change;
  }

  @Override
  public List<ProcessChange> update(Long equipmentId, Properties elementProperties) throws IllegalAccessException {
    if (elementProperties.containsKey("processId")) {
      log.warn("Attempting to change the parent process id of an equipment - this is not currently supported!");
      elementProperties.remove("processId");
    }
    return commonUpdate(equipmentId, elementProperties);
  }

  /**
   * Removes the subequipments attached to this equipment.
   * Exceptions are caught, added to the report and thrown
   * up to interrupt the equipment removal.
   *
   *<p>Call within Equipment lock.
   *
   * @param subEquipmentIds the equipment ids for which the subequipments should be removed
   * @param equipmentReport the report at the equipment level
   */
  private void removeSubEquipments(Collection<Long> subEquipmentIds, ConfigurationElementReport equipmentReport) {
    for (Long subEquipmentId : new ArrayList<Long>(subEquipmentIds)) {
      ConfigurationElementReport subEquipmentReport = new ConfigurationElementReport(Action.REMOVE, Entity.SUBEQUIPMENT, subEquipmentId);
      equipmentReport.addSubReport(subEquipmentReport);
      try {
        subEquipmentConfigHandler.remove(subEquipmentId, subEquipmentReport);
      } catch (Exception ex) {
        subEquipmentReport.setFailure("Exception caught - aborting removal of subequipment "
            + subEquipmentId , ex);
        throw new RuntimeException("Aborting reconfiguration as unable to remove subequipment.", ex);
      }

    }
  }

  /**
   * Removes the tags for this equipment. The DAQ is not informed as
   * this method is only called when the whole Equipment is removed.
   *
   * <p>Call within equipment lock.
   * @param equipment for which the tags should be removed
   * @throws RuntimeException if fail to remove tag
   */
  private void removeEquipmentTags(Equipment equipment, ConfigurationElementReport equipmentReport) {
    for (Long dataTagId : new ArrayList<>(equipmentFacade.getDataTagIds(equipment.getId()))) { //copy as list is modified by removeDataTag
      ConfigurationElementReport tagReport = new ConfigurationElementReport(Action.REMOVE, Entity.DATATAG, dataTagId);
      equipmentReport.addSubReport(tagReport);
      dataTagConfigHandler.remove(dataTagId, tagReport);
    }
  }

  /**
   * Removes all command tags associated with this equipment.
   * @param equipment reference
   * @param equipmentReport report to add subreports to
   */
  private void removeEquipmentCommands(Equipment equipment, ConfigurationElementReport equipmentReport) {
    for (Long commandTagId : new ArrayList<Long>(equipment.getCommandTagIds())) { //copy as modified when removing command tag
      ConfigurationElementReport commandReport = new ConfigurationElementReport(Action.REMOVE, Entity.COMMANDTAG, commandTagId);
      equipmentReport.addSubReport(commandReport);
      commandTagConfigHandler.removeCommandTag(commandTagId, commandReport);
    }
  }


}
