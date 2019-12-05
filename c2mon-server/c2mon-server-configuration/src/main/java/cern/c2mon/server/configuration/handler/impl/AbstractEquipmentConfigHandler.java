/******************************************************************************
 * Copyright (C) 2010-2018 CERN. All rights not expressly granted are reserved.
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
import cern.c2mon.server.cache.C2monCache;
import cern.c2mon.server.cache.CommFaultTagCache;
import cern.c2mon.server.cache.equipment.CommonEquipmentFacade;
import cern.c2mon.server.common.equipment.AbstractEquipment;
import cern.c2mon.server.configuration.handler.ControlTagConfigHandler;
import cern.c2mon.server.configuration.handler.transacted.CommonEquipmentConfigTransacted;
import cern.c2mon.server.configuration.impl.ProcessChange;
import cern.c2mon.shared.client.configuration.ConfigConstants.Action;
import cern.c2mon.shared.client.configuration.ConfigConstants.Entity;
import cern.c2mon.shared.client.configuration.ConfigurationElementReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Common part of Equipment-SubEquipment handler.
 *
 * @author Mark Brightwell
 *
 * @param <T> type of Equipment
 */
public abstract class AbstractEquipmentConfigHandler<T extends AbstractEquipment> {

  private static final Logger LOGGER = LoggerFactory.getLogger(AbstractEquipmentConfigHandler.class);

  private ControlTagConfigHandler controlTagConfigHandler;

  private CommonEquipmentConfigTransacted<T> abstractEquipmentConfigTransacted;

  private C2monCache<Long, T> abstractEquipmentCache;

  private AliveTimerCache aliveTimerCache;

  private CommFaultTagCache commFaultTagCache;

  private CommonEquipmentFacade<T> commonEquipmentFacade;

  /**
   * Constructor called from implementation and setting required beans.
   */
  public AbstractEquipmentConfigHandler(ControlTagConfigHandler controlTagConfigHandler,
                                          CommonEquipmentConfigTransacted<T> abstractEquipmentConfigTransacted,
                                          C2monCache<Long, T> abstractEquipmentCache,
                                          AliveTimerCache aliveTimerCache,
                                          CommFaultTagCache commFaultTagCache,
                                          CommonEquipmentFacade<T> commonEquipmentFacade) {
    super();
    this.controlTagConfigHandler = controlTagConfigHandler;
    this.abstractEquipmentConfigTransacted = abstractEquipmentConfigTransacted;
    this.abstractEquipmentCache = abstractEquipmentCache;
    this.aliveTimerCache = aliveTimerCache;
    this.commFaultTagCache = commFaultTagCache;
    this.commonEquipmentFacade = commonEquipmentFacade;
  }

  /**
   * Removes the control tags for this equipment. Notice that if this fails, the
   * equipment object will still be removed: this is to prevent the situation of
   * not being able to remove the equipment because of the control tags (say if another
   * equipment is also using them by mistake) and not being able to remove the
   * control tags because of the equipment.
   *
   * <p>Notice that in case of failure, only part of the control tags could remain; they
   * are removed in the following order: Alive tag, CommFaultTag, State tag.
   *
   * @param abstractEquipment the AbstracEquipment to remove
   * @param equipmentReport for adding the sub-reports to
   */
  protected List<ProcessChange> removeEquipmentControlTags(final T abstractEquipment, final ConfigurationElementReport equipmentReport) {
    List<ProcessChange> changes = new ArrayList<>();

    LOGGER.debug("Removing (Sub-)Equipment control tags.");
    Long aliveTagId = abstractEquipment.getAliveTagId();

    if (aliveTagId != null) {
      ConfigurationElementReport tagReport = new ConfigurationElementReport(Action.REMOVE, Entity.CONTROLTAG, aliveTagId);

      ProcessChange change = controlTagConfigHandler.remove(aliveTagId, tagReport);
      if (change.processActionRequired()) {
        change.setNestedSubReport(tagReport);
        changes.add(change);
      }
      else {
        equipmentReport.addSubReport(tagReport);
      }
    }

    Long commTagId = abstractEquipment.getCommFaultTagId();
    if (commTagId != null) {
      ConfigurationElementReport tagReport = new ConfigurationElementReport(Action.REMOVE, Entity.CONTROLTAG, commTagId);

      ProcessChange change = controlTagConfigHandler.remove(commTagId, tagReport);
      if (change.processActionRequired()) {
        change.setNestedSubReport(tagReport);
        changes.add(change);
      }
      else {
        equipmentReport.addSubReport(tagReport);
      }
    }

    Long stateTagId = abstractEquipment.getStateTagId();
    ConfigurationElementReport tagReport = new ConfigurationElementReport(Action.REMOVE, Entity.CONTROLTAG, stateTagId);

    ProcessChange change = controlTagConfigHandler.remove(stateTagId, tagReport);
    if (change.processActionRequired()) {
      change.setNestedSubReport(tagReport);
      changes.add(change);
    }
    else {
      equipmentReport.addSubReport(tagReport);
    }
    return changes;
  }

  /**
   * Common part of (Sub-)Equipment update method. Mainly deals
   * with rollback of other cache changes in case of failure.
   *
   * @param abstractEquipmentId id of (sub)equipment
   * @param elementProperties properties with update details
   * @return changes to be sent to the DAQ layer
   * @throws IllegalAccessException if thrown when updating fields
   */
  protected List<ProcessChange> commonUpdate(Long abstractEquipmentId, Properties elementProperties) throws IllegalAccessException {
    LOGGER.debug("Updating (sub-)equipment {}", abstractEquipmentId);
    // TODO or not todo: warning: can still update commfault, alive and state
    // tag id to non-existent tags (id is NOT checked and exceptions will be
    // thrown!)

    // do not allow id changes! (they would not be applied in any case)
    if (elementProperties.containsKey("id")) {
      LOGGER.warn("Attempting to change the equipment/subequipment id - this is not currently supported!");
      elementProperties.remove("id");
    }
    boolean aliveConfigure = false;
    if (elementProperties.containsKey("aliveInterval") || elementProperties.containsKey("aliveTagId")) {
      aliveConfigure = true;
    }
    boolean commFaultConfigure = false;
    if (elementProperties.containsKey("commFaultTagId")) {
      commFaultConfigure = true;
    }
    abstractEquipmentCache.acquireWriteLockOnKey(abstractEquipmentId);
    try {
      T abstractEquipmentCopy = abstractEquipmentCache.getCopy(abstractEquipmentId);
      try {
        Long oldAliveId = abstractEquipmentCopy.getAliveTagId();
        Long oldCommFaultId = abstractEquipmentCopy.getCommFaultTagId();
        List<ProcessChange> processChanges = abstractEquipmentConfigTransacted.doUpdateAbstractEquipment(abstractEquipmentCopy, elementProperties);

        // commit local changes back to the cache
        abstractEquipmentCache.putQuiet(abstractEquipmentCopy);
        abstractEquipmentCache.releaseWriteLockOnKey(abstractEquipmentId);

        if (aliveConfigure) {
          if (oldAliveId != null)
            commonEquipmentFacade.removeAliveDirectly(oldAliveId);
          if (abstractEquipmentCopy.getAliveTagId() != null)
            commonEquipmentFacade.loadAndStartAliveTag(abstractEquipmentCopy.getId());
        }
        if (commFaultConfigure && abstractEquipmentCopy.getCommFaultTagId() != null) {
          if (oldCommFaultId != null)
            commFaultTagCache.remove(oldCommFaultId);
          if (abstractEquipmentCopy.getCommFaultTagId() != null)
            commFaultTagCache.loadFromDb(abstractEquipmentCopy.getCommFaultTagId());
        }

        return processChanges;
      } catch (RuntimeException ex) {
        LOGGER.error("Exception caught while updating (sub-)equipment - rolling back changes", ex);
        //reload all potentially updated cache elements now DB changes are rolled back
        if (abstractEquipmentCache.isWriteLockedByCurrentThread(abstractEquipmentId)) {
          abstractEquipmentCache.releaseWriteLockOnKey(abstractEquipmentId);
        }
        commFaultTagCache.remove(abstractEquipmentCopy.getCommFaultTagId());
        aliveTimerCache.remove(abstractEquipmentCopy.getAliveTagId());
        abstractEquipmentCache.remove(abstractEquipmentId);
        T oldAbstractEquipment = abstractEquipmentCache.get(abstractEquipmentId);
        commFaultTagCache.loadFromDb(oldAbstractEquipment.getCommFaultTagId());
        commonEquipmentFacade.loadAndStartAliveTag(abstractEquipmentId); //reloads alive from DB
        throw ex;
      }
    } finally {
      if (abstractEquipmentCache.isWriteLockedByCurrentThread(abstractEquipmentId))
        abstractEquipmentCache.releaseWriteLockOnKey(abstractEquipmentId);
    }
  }

}
