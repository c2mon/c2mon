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
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.UnexpectedRollbackException;

import cern.c2mon.server.cache.AliveTimerCache;
import cern.c2mon.server.cache.CommFaultTagCache;
import cern.c2mon.server.cache.SubEquipmentCache;
import cern.c2mon.server.cache.SubEquipmentFacade;
import cern.c2mon.server.cache.exception.CacheElementNotFoundException;
import cern.c2mon.server.common.subequipment.SubEquipment;
import cern.c2mon.server.configuration.handler.ControlTagConfigHandler;
import cern.c2mon.server.configuration.handler.DataTagConfigHandler;
import cern.c2mon.server.configuration.handler.SubEquipmentConfigHandler;
import cern.c2mon.server.configuration.handler.transacted.SubEquipmentConfigTransacted;
import cern.c2mon.server.configuration.impl.ProcessChange;
import cern.c2mon.shared.client.configuration.ConfigConstants.Action;
import cern.c2mon.shared.client.configuration.ConfigConstants.Entity;
import cern.c2mon.shared.client.configuration.ConfigurationElement;
import cern.c2mon.shared.client.configuration.ConfigurationElementReport;

/**
 * See interface documentation.
 *
 * @author Mark Brightwell
 *
 */
@Service
public class SubEquipmentConfigHandlerImpl extends AbstractEquipmentConfigHandler<SubEquipment> implements SubEquipmentConfigHandler {

  private static final Logger LOGGER = Logger.getLogger(SubEquipmentConfigHandlerImpl.class);

  private SubEquipmentConfigTransacted subEquipmentConfigTransacted;

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
                                       SubEquipmentConfigTransacted subEquipmentConfigTransacted,
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
  public List<ProcessChange> removeSubEquipment(final Long subEquipmentId, final ConfigurationElementReport subEquipmentReport) {
    LOGGER.debug("Removing SubEquipment " + subEquipmentId);
    subEquipmentCache.acquireWriteLockOnKey(subEquipmentId);
    try {
      SubEquipment subEquipment = subEquipmentCache.get(subEquipmentId);

      // TIMS-951: Allow attachment of DataTags to SubEquipments
      List<ProcessChange> changes = removeSubEquipmentTags(subEquipment, subEquipmentReport);

      try {
        changes.addAll(subEquipmentConfigTransacted.doRemoveSubEquipment(subEquipment, subEquipmentReport));

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
      LOGGER.debug("SubEquipment not found in cache - unable to remove it.", e);
      subEquipmentReport.setWarning("SubEquipment not found in cache so cannot be removed.");
      return new ArrayList<ProcessChange>();
    } finally {
      if (subEquipmentCache.isWriteLockedByCurrentThread(subEquipmentId)) {
        subEquipmentCache.releaseWriteLockOnKey(subEquipmentId);
      }
    }
  }

  @Override
  public List<ProcessChange> createSubEquipment(ConfigurationElement element) throws IllegalAccessException {
    List<ProcessChange> change = subEquipmentConfigTransacted.doCreateSubEquipment(element);
    subEquipmentCache.lockAndNotifyListeners(element.getEntityId());
    return change;
  }

  @Override
  public List<ProcessChange> updateSubEquipment(Long subEquipmentId, Properties elementProperties) throws IllegalAccessException {
    if (elementProperties.containsKey("parent_equip_id")) {
      LOGGER.warn("Attempting to change the parent equipment id of a subequipment - this is not currently supported!");
      elementProperties.remove("parent_equip_id");
    }
    return commonUpdate(subEquipmentId, elementProperties);
  }

  /**
   * Removes the tags for this sub-equipment. The DAQ is not informed as this
   * method is only called when the whole Equipment is removed.
   *
   * Call within equipment lock.
   *
   * @param equipment for which the tags should be removed
   * @throws RuntimeException if fail to remove tag
   */
  private List<ProcessChange> removeSubEquipmentTags(SubEquipment subEquipment, ConfigurationElementReport subEquipmentReport) {
    List<ProcessChange> processChanges = new ArrayList<>();

    for (Long dataTagId : new ArrayList<Long>(subEquipment.getDataTagIds())) {
      // copy as list is modified by removeDataTag
      ConfigurationElementReport tagReport = new ConfigurationElementReport(Action.REMOVE, Entity.DATATAG, dataTagId);
      subEquipmentReport.addSubReport(tagReport);

      ProcessChange change = dataTagConfigHandler.removeDataTag(dataTagId, tagReport);
      
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
