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

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.server.cache.ControlTagCache;
import cern.c2mon.server.common.equipment.Equipment;
import cern.c2mon.server.common.subequipment.SubEquipment;
import cern.c2mon.server.configuration.handler.ControlTagConfigHandler;
import cern.c2mon.server.configuration.impl.ProcessChange;
import cern.c2mon.shared.client.configuration.ConfigurationElement;
import cern.c2mon.shared.client.configuration.ConfigurationElementReport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.UnexpectedRollbackException;

import java.util.Properties;

/**
 * See interface documentation.
 * <p>Please note that all changes on control tags will require a lock of the equipment. Otherwise
 * it can lead to a deadlock situation in the running server.
 *  
 * <p>TODO: update and remove are same as {@link DataTagConfigHandlerImpl} so could extend it
 * 
 * @author Mark Brightwell
 *
 */
@Service
@Slf4j
public class ControlTagConfigHandlerImpl implements ControlTagConfigHandler {
  
  /** Variable definition in configuration element for equipment ID */
  private static final String EQUIPMENT_ID = "equipmentId";
  
  private final C2monCache<Equipment> equipmentCache;
  
  private final C2monCache<SubEquipment> subEquipmentCache;

  private ControlTagConfigHandler controlTagConfigTransacted;
  
  private ControlTagCache controlTagCache;


  @Autowired  
  public ControlTagConfigHandlerImpl(ControlTagCache controlTagCache,
                                     C2monCache<Equipment> equipmentCache,
                                     C2monCache<SubEquipment> subEquipmentCache, ControlTagConfigHandler controlTagConfigTransacted) {
    this.controlTagCache = controlTagCache;
    this.equipmentCache = equipmentCache;
    this.subEquipmentCache = subEquipmentCache;
    this.controlTagConfigTransacted = controlTagConfigTransacted;
  }
  
  /**
   * Removes the control tag and fills in the passed report in case
   * of failure. The control tag removed is commited if successful.
   * If it fails, a rollback exception is throw and all *local* changes
   * are rolled back (the calling method needs to handle the thrown 
   * UnexpectedRollbackException appropriately.
   *
   * @param id the id of the tag to remove
   * @param tagReport the report for this removal
   * @return a DAQ configuration event if ControlTag is associated to some Equipment or SubEquipment and DAQ
   *          needs informing (i.e. if has Address) , else return null
   */
  @Override
  public ProcessChange remove(Long id, ConfigurationElementReport tagReport) {
    ProcessChange change = controlTagConfigTransacted.remove(id, tagReport);
    controlTagCache.remove(id); //will be skipped if rollback exception thrown in do method
    return change;
  }

  @Override
  public ProcessChange create(ConfigurationElement element) throws IllegalAccessException {
    ProcessChange change;
    Long controlTagId = element.getEntityId();
    acquireEquipmentWriteLockForElement(controlTagId, element.getElementProperties());
    try {
      change = controlTagConfigTransacted.create(element);
    } finally {
      releaseEquipmentWriteLockForElement(controlTagId, element.getElementProperties());
    }
    controlTagCache.notifyListenersOfUpdate(controlTagId);
    return change;
  }

  @Override
  public ProcessChange update(Long id, Properties elementProperties) throws IllegalAccessException {
    acquireEquipmentWriteLockForElement(id, elementProperties);
    try {
      return controlTagConfigTransacted.update(id, elementProperties);
    } catch (UnexpectedRollbackException e) {
      log.error("Rolling back ControlTag update in cache");
      controlTagCache.remove(id);
      controlTagCache.loadFromDb(id);
      throw e;
    } finally {
      releaseEquipmentWriteLockForElement(id, elementProperties);
    }
  }
  
  @Override
  public ProcessChange getCreateEvent(Long configId, Long controlTagId, Long equipmentId, Long processId) {
    return controlTagConfigTransacted.getCreateEvent(configId, controlTagId, equipmentId, processId);
  }
  
  @Override
  public void addAlarmToTag(Long tagId, Long alarmId) {
    controlTagConfigTransacted.addAlarmToTag(tagId, alarmId);
  }

  @Override
  public void addRuleToTag(Long tagId, Long ruleId) {
    controlTagConfigTransacted.addRuleToTag(tagId, ruleId);
  }

  @Override
  public void removeAlarmFromTag(Long tagId, Long alarmId) {
    controlTagConfigTransacted.removeAlarmFromTag(tagId, alarmId);
  }

  @Override
  public void removeRuleFromTag(Long tagId, Long ruleId) {
    controlTagConfigTransacted.removeRuleFromTag(tagId, ruleId);
  }
  
  /**
   * Checks whether the equipment id belongs to the equipment or subequipment
   * cache and locks it.
   * @param equipmentId The id of the equipment of subequipment.
   */
  private void acquireEquipmentWriteLock(final Long equipmentId) {
    if (equipmentCache.hasKey(equipmentId)) {
      equipmentCache.acquireWriteLockOnKey(equipmentId);
    }
    else if (subEquipmentCache.hasKey(equipmentId)) {
      subEquipmentCache.acquireWriteLockOnKey(equipmentId);
    }
    else {
      log.debug("Equipment id " + equipmentId + " unknown in in both equipment and subequipment cache. Do write lock in both caches.");
      equipmentCache.acquireWriteLockOnKey(equipmentId);
      subEquipmentCache.acquireWriteLockOnKey(equipmentId);
    }
  }

  private void acquireEquipmentWriteLockForElement(final Long id, Properties elementProperties) {
    String equipmentIdValue = elementProperties.getProperty(EQUIPMENT_ID);
    if (equipmentIdValue == null || equipmentIdValue.equalsIgnoreCase("")) {
      log.trace("Property '" + EQUIPMENT_ID + "' is not set for Control Tag " + id + " => no write lock on equipment possible.");
    } else{
      Long equipmentId = Long.valueOf(equipmentIdValue);
      acquireEquipmentWriteLock(equipmentId);
    }
  }
  
  /**
   * Releases the write locks on equipment and subequipment cache, if owned by this thread.
   * @param equipmentId The id of the equipment of subequipment.
   */
  private void releaseEquimentWriteLock(final Long equipmentId) {
    if (subEquipmentCache.isWriteLockedByCurrentThread(equipmentId)) {
      subEquipmentCache.releaseWriteLockOnKey(equipmentId);
    }
    if (equipmentCache.isWriteLockedByCurrentThread(equipmentId)) {
      equipmentCache.releaseWriteLockOnKey(equipmentId);
    }
  }
  
  private void releaseEquipmentWriteLockForElement(final Long id, final Properties elementProperties) {
    String equipmentIdValue = elementProperties.getProperty(EQUIPMENT_ID);
    if (equipmentIdValue == null || equipmentIdValue.equalsIgnoreCase("")) {
      log.trace("Property '" + EQUIPMENT_ID + "' is not set for Control Tag " + id + " => no release of equipment write lock needed.");
    } else {
      Long equipmentId = Long.valueOf(equipmentIdValue);
      releaseEquimentWriteLock(equipmentId);
    }
  } 
}
