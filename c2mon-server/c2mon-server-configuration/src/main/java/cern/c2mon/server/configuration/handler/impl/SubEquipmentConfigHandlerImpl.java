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
import cern.c2mon.server.cache.SubEquipmentCache;
import cern.c2mon.server.cache.SubEquipmentFacade;
import cern.c2mon.server.cache.exception.CacheElementNotFoundException;
import cern.c2mon.server.common.subequipment.SubEquipment;
import cern.c2mon.server.configuration.handler.ControlTagConfigHandler;
import cern.c2mon.server.configuration.handler.DataTagConfigHandler;
import cern.c2mon.server.configuration.handler.SubEquipmentConfigHandler;
import cern.c2mon.server.configuration.impl.ProcessChange;
import cern.c2mon.shared.client.configuration.ConfigConstants.Action;
import cern.c2mon.shared.client.configuration.ConfigConstants.Entity;
import cern.c2mon.shared.client.configuration.ConfigurationElement;
import cern.c2mon.shared.client.configuration.ConfigurationElementReport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.UnexpectedRollbackException;

import java.util.ArrayList;
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
public class SubEquipmentConfigHandlerImpl extends AbstractEquipmentConfigHandler<SubEquipment> implements SubEquipmentConfigHandler {

  private SubEquipmentConfigHandler subEquipmentConfigTransacted;

  private SubEquipmentCache subEquipmentCache;

  private SubEquipmentFacade subEquipmentFacade;

  private DataTagConfigHandler dataTagConfigHandler;

  /**
   * Autowired constructor.
   */
  @Autowired
  public SubEquipmentConfigHandlerImpl(SubEquipmentCache subEquipmentCache,
                                       SubEquipmentFacade subEquipmentFacade,
                                       ControlTagConfigHandler controlTagConfigHandler,
                                       AliveTimerCache aliveTimerCache,
                                       CommFaultTagCache commFaultTagCache,
                                       SubEquipmentConfigHandler subEquipmentConfigTransacted,
                                       DataTagConfigHandler dataTagConfigHandler) {
    super(controlTagConfigHandler, subEquipmentConfigTransacted, subEquipmentCache, aliveTimerCache, commFaultTagCache, subEquipmentFacade);
    this.subEquipmentCache = subEquipmentCache;
    this.subEquipmentFacade = subEquipmentFacade;
    this.subEquipmentConfigTransacted = subEquipmentConfigTransacted;
    this.dataTagConfigHandler = dataTagConfigHandler;
  }

  /**
   * First removes the SubEquipment from the DB and cache. If successful,
   * removes the associated control tags.
   *
   * <p>If an exception is thrown the SubEquipment will be restored in DB (transaction rollback).
   *
   * @param subEquipmentId id
   * @param subEquipmentReport to which subreports may be added
   */
  @Override
  public List<ProcessChange> remove(final Long subEquipmentId, final ConfigurationElementReport subEquipmentReport) {
    log.debug("Removing SubEquipment " + subEquipmentId);
    subEquipmentCache.acquireWriteLockOnKey(subEquipmentId);
    try {
      SubEquipment subEquipment = subEquipmentCache.get(subEquipmentId);

      // TIMS-951: Allow attachment of DataTags to SubEquipments
      List<ProcessChange> changes = removeSubEquipmentTags(subEquipment, subEquipmentReport);

      try {
        changes.addAll(subEquipmentConfigTransacted.remove(subEquipment, subEquipmentReport));

        subEquipmentCache.releaseWriteLockOnKey(subEquipmentId);
        changes.addAll(removeEquipmentControlTags(subEquipment, subEquipmentReport)); //must be after removal of subequipment from DB
        subEquipmentFacade.removeAliveTimer(subEquipmentId);
        subEquipmentFacade.removeCommFault(subEquipmentId);
        subEquipmentCache.remove(subEquipmentId);

        // Remove the SubEquipment from the parent Equipment
        subEquipmentFacade.removeSubEquipmentFromEquipment(subEquipment.getParentId(), subEquipmentId);

        return changes;
      } catch (RuntimeException e) {
        subEquipmentReport.setFailure("Exception caught while removing Sub-equipment " + subEquipmentId);
        throw new UnexpectedRollbackException("Exception caught while removing Sub-equipment", e);
      }
    } catch (CacheElementNotFoundException e) {
      log.debug("SubEquipment not found in cache - unable to remove it.", e);
      subEquipmentReport.setWarning("SubEquipment not found in cache so cannot be removed.");
      return new ArrayList<ProcessChange>();
    } finally {
      if (subEquipmentCache.isWriteLockedByCurrentThread(subEquipmentId)) {
        subEquipmentCache.releaseWriteLockOnKey(subEquipmentId);
      }
    }
  }

  @Override
  public List<ProcessChange> create(ConfigurationElement element) throws IllegalAccessException {
    List<ProcessChange> change = subEquipmentConfigTransacted.create(element);
    subEquipmentCache.notifyListenersOfUpdate(element.getEntityId());
    return change;
  }

  @Override
  public List<ProcessChange> update(Long subEquipmentId, Properties elementProperties) throws IllegalAccessException {
    // TODO: Remove obsolete parent_equip_id property
    if (elementProperties.containsKey("parent_equip_id")) {
      log.warn("Attempting to change the parent equipment id of a subequipment - this is not currently supported!");
      elementProperties.remove("parent_equip_id");
    }

    if (elementProperties.containsKey("equipmentId")) {
      log.warn("Attempting to change the parent equipment id of a subequipment - this is not currently supported!");
      elementProperties.remove("equipmentId");
    }

    return commonUpdate(subEquipmentId, elementProperties);
  }

  /**
   * Removes the tags for this sub-equipment. The DAQ is not informed as this
   * method is only called when the whole Equipment is removed.
   *
   * Call within equipment lock.
   *
   * @param subEquipment for which the tags should be removed
   * @param subEquipmentReport the report which is build based on the removed entities
   * @throws RuntimeException if fail to remove tag
   */
  private List<ProcessChange> removeSubEquipmentTags(SubEquipment subEquipment, ConfigurationElementReport subEquipmentReport) {
    List<ProcessChange> processChanges = new ArrayList<>();

    for (Long dataTagId : subEquipmentFacade.getDataTagIds(subEquipment.getId())) {
      // copy as list is modified by removeDataTag
      ConfigurationElementReport tagReport = new ConfigurationElementReport(Action.REMOVE, Entity.DATATAG, dataTagId);
      subEquipmentReport.addSubReport(tagReport);

      ProcessChange change = dataTagConfigHandler.remove(dataTagId, tagReport);

      if (change.processActionRequired()) {
        change.setNestedSubReport(tagReport);
        processChanges.add(change);
      }
      else {
        subEquipmentReport.addSubReport(tagReport);
      }
    }

    return processChanges;
  }
}
