/*******************************************************************************
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
 ******************************************************************************/

package cern.c2mon.client.core.refactoring;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import cern.c2mon.client.common.listener.BaseListener;
import cern.c2mon.client.common.listener.TagUpdateListener;
import cern.c2mon.client.common.tag.Tag;
import cern.c2mon.client.common.util.ConcurrentIdentitySet;
import cern.c2mon.client.core.jms.SupervisionListener;
import cern.c2mon.shared.client.supervision.SupervisionEvent;
import cern.c2mon.shared.client.tag.TagUpdate;
import cern.c2mon.shared.client.tag.TagValueUpdate;
import cern.c2mon.shared.common.datatag.DataTagQuality;
import cern.c2mon.shared.common.datatag.TagQualityStatus;
import cern.c2mon.shared.common.supervision.SupervisionConstants;
import cern.c2mon.shared.rule.RuleExpression;
import cern.c2mon.shared.rule.RuleFormatException;

/**
 * @author Szymon Halastra
 */
@Slf4j
public class CloneableTagBean extends TagBean implements TagUpdateListener, SupervisionListener, Cloneable {

  /**
   * Lock to prevent more than one thread at a time to update the value
   */
  @Getter
  private ReentrantReadWriteLock updateTagLock = new ReentrantReadWriteLock();

  /**
   * Concurrent modifiable collection of DataTagUpdateListeners registered for
   * updates on this DataTag
   */
  @Getter
  private Set<BaseListener> listeners = new ConcurrentIdentitySet<>();

  /**
   * Metadata of an Tag object.
   */
  @Getter
  @Setter
  private Map<String, Object> metadata = new HashMap<>();

  @Override
  public boolean onUpdate(final TagValueUpdate tagValueUpdate) {
    return update(tagValueUpdate);
  }

  /**
   * This thread safe method updates the accessible state of the given
   * <code>Tag</code> object. Once the accessibility has been updated
   * it notifies the registered listener about the update by providing a copy of
   * the <code>Tag</code> object.
   *
   * @param supervisionEvent The supervision event which contains the current
   *                         status of the process or the equipment.
   */
  @Override
  public void onSupervisionUpdate(SupervisionEvent supervisionEvent) {
    if (supervisionEvent == null) {
      return;
    }
    // In case of a CommFault- or Status control tag, we ignore supervision events
    if (this.isControlTag() && !this.isAliveTag()) {
      return;
    }

    Tag clone = null;

    updateTagLock.writeLock().lock();
    try {
      boolean validUpdate = false;
      validUpdate |= this.getEquipmentSupervisionStatus().containsKey(supervisionEvent.getEntityId());
      validUpdate |= this.getSubEquipmentSupervisionStatus().containsKey(supervisionEvent.getEntityId());
      validUpdate |= this.getProcessSupervisionStatus().containsKey(supervisionEvent.getEntityId());

      if (validUpdate) {
        SupervisionEvent oldEvent;
        switch (supervisionEvent.getEntity()) {
          case PROCESS:
            oldEvent = this.getProcessSupervisionStatus().put(supervisionEvent.getEntityId(), supervisionEvent);
            updateProcessStatus();
            break;
          case EQUIPMENT:
            oldEvent = this.getEquipmentSupervisionStatus().put(supervisionEvent.getEntityId(), supervisionEvent);
            updateEquipmentStatus();
            break;
          case SUBEQUIPMENT:
            oldEvent = this.getSubEquipmentSupervisionStatus().put(supervisionEvent.getEntityId(), supervisionEvent);
            updateSubEquipmentStatus();
            break;
          default:
            String errorMsg = "The supervision event type " + supervisionEvent.getEntity() + " is not supported.";
            log.error("update(SupervisionEvent) - " + errorMsg);
            throw new IllegalArgumentException(errorMsg);
        }

        if (oldEvent == null || !supervisionEvent.equals(oldEvent)) {
          // Notify all listeners of the update
          clone = this.clone();
        }
      }
    } finally {
      updateTagLock.writeLock().unlock();
    }

    if (clone != null) {
      notifyListeners(clone);
    }
  }

  /**
   * Inner method for updating the process status of this tag and
   * computing the error message, if one of the linked processes is down.
   */
  private void updateProcessStatus() {
    boolean down = false;
    StringBuilder invalidationMessage = new StringBuilder();
    for (SupervisionEvent event : this.getProcessSupervisionStatus().values()) {
      this.invalidateMessage(invalidationMessage, event);
    }

    if (down) {
      this.getDataTagQuality().addInvalidStatus(TagQualityStatus.PROCESS_DOWN, invalidationMessage.toString());
    }
    else {
      this.getDataTagQuality().removeInvalidStatus(TagQualityStatus.PROCESS_DOWN);
    }
  }

  /**
   * Inner method for updating the equipment status of this tag and
   * computing the error message, if one of the linked equipments is down.
   */
  private void updateEquipmentStatus() {
    boolean down = false;
    StringBuilder invalidationMessage = new StringBuilder();
    for (SupervisionEvent event : this.getEquipmentSupervisionStatus().values()) {
      this.invalidateMessage(invalidationMessage, event);
    }

    if (down) {
      this.getDataTagQuality().addInvalidStatus(TagQualityStatus.EQUIPMENT_DOWN, invalidationMessage.toString());
    }
    else {
      this.getDataTagQuality().removeInvalidStatus(TagQualityStatus.EQUIPMENT_DOWN);
    }
  }

  /**
   * Inner method for updating the sub equipment status of this tag and
   * computing the error message, if one of the linked sub equipments is down.
   */
  private void updateSubEquipmentStatus() {
    StringBuilder invalidationMessage = new StringBuilder();
    for (SupervisionEvent event : this.getSubEquipmentSupervisionStatus().values()) {
      this.invalidateMessage(invalidationMessage, event);
    }

    if (invalidationMessage.length() == 0) {
      this.getDataTagQuality().addInvalidStatus(TagQualityStatus.SUBEQUIPMENT_DOWN, invalidationMessage.toString());
    }
    else {
      this.getDataTagQuality().removeInvalidStatus(TagQualityStatus.SUBEQUIPMENT_DOWN);
    }
  }

  private StringBuilder invalidateMessage(StringBuilder stringBuilder, SupervisionEvent event) {
    if (event != null) {
      boolean isDown = false;
      isDown |= event.getStatus().equals(SupervisionConstants.SupervisionStatus.DOWN);
      isDown |= event.getStatus().equals(SupervisionConstants.SupervisionStatus.STOPPED);
      if (isDown) {
        if (stringBuilder.length() > 0) {
          stringBuilder.append("; ");
        }
        stringBuilder.append(event.getMessage());
      }
    }

    return stringBuilder;
  }

  /**
   * Private method to notify all registered <code>DataTagUpdateListener</code> instances.
   * Please avoid calling this method within a WRITELOCK block since it could be a potential
   * candidate for risking a deadlocks.
   *
   * @param Please only provide a clone of this tag
   */
  public synchronized void notifyListeners(final Tag clone) {
    for (BaseListener updateListener : listeners) {
      try {
        updateListener.onUpdate(clone);
      }
      catch (Exception e) {
        log.error("notifyListeners() : error notifying DataTagUpdateListeners", e);
      }
    }
  }

  /**
   * This thread safe method updates the given <code>ClientDataTag</code> object.
   * It copies every single field of the <code>TransferTagValue</code> object and notifies
   * then the registered listener about the update by providing a copy of the
   * <code>ClientDataTag</code> object.
   * <p>
   * Please note that the <code>ClientDataTag</code> gets only updated, if the tag id's
   * matches and if the server time stamp of the update is older than the current time
   * stamp set.
   *
   * @param transferTag The object that contains the updates.
   *
   * @return <code>true</code>, if the update was successful, otherwise
   * <code>false</code>
   */
  public boolean update(final TagValueUpdate tagValueUpdate) {
    Tag clone = null;
    boolean valid = false;

    updateTagLock.writeLock().lock();
    try {
      valid = isValidUpdate(tagValueUpdate);

      if (valid) {
        doUpdateValues(tagValueUpdate);
        // Notify all listeners of the update
        clone = this.clone();
      }
    } finally {
      updateTagLock.writeLock().unlock();
    }

    if (clone != null) {
      notifyListeners(clone);
    }

    return valid;
  }

  /**
   * This thread safe method updates the given <code>ClientDataTag</code> object.
   * It copies every single field of the <code>TransferTag</code> object and notifies
   * then the registered listener about the update by providing a copy of the
   * <code>ClientDataTag</code> object.
   * <p>
   * Please note that the <code>ClientDataTag</code> gets only updated, if the tag id's
   * matches and if the server time stamp
   * of the update is older thatn the current time
   * stamp set.
   *
   * @param tagUpdate The object that contains the updates.
   *
   * @return <code>true</code>, if the update was successful, otherwise
   * <code>false</code>
   * @throws RuleFormatException In case that the <code>TransferTag</code>
   *                             parameter contains a invalid rule expression.
   */
  public boolean update(final TagUpdate tagUpdate) throws RuleFormatException {
    Tag clone = null;
    boolean valid = false;

    updateTagLock.writeLock().lock();
    try {
      valid = isValidUpdate(tagUpdate);

      if (valid) {
        if (tagUpdate.getRuleExpression() != null) {
          this.setRuleExpression(RuleExpression.createExpression(tagUpdate.getRuleExpression()));
        }

        doUpdateValues(tagUpdate);

        // update process map
        Map<Long, SupervisionEvent> updatedProcessMap = new HashMap<>();
        for (Long processId : tagUpdate.getProcessIds()) {
          updatedProcessMap.put(processId, this.getProcessSupervisionStatus().get(processId));
        }
        this.setProcessSupervisionStatus(updatedProcessMap);

        // update equipment map
        Map<Long, SupervisionEvent> updatedEquipmentMap = new HashMap<>();
        for (Long equipmentId : tagUpdate.getEquipmentIds()) {
          updatedEquipmentMap.put(equipmentId, this.getEquipmentSupervisionStatus().get(equipmentId));
        }
        this.setEquipmentSupervisionStatus(updatedEquipmentMap);

        // update sub equipment map
        Map<Long, SupervisionEvent> updatedSubEquipmentMap = new HashMap<>();
        for (Long subEquipmentId : tagUpdate.getSubEquipmentIds()) {
          updatedSubEquipmentMap.put(subEquipmentId, this.getSubEquipmentSupervisionStatus().get(subEquipmentId));
        }
        this.setSubEquipmentSupervisionStatus(updatedSubEquipmentMap);

        this.setTagName(tagUpdate.getName());
        this.setTopicName(tagUpdate.getTopicName());
        this.setUnit(tagUpdate.getUnit());

        this.aliveTagFlag = tagUpdate.isAliveTag();
        this.controlTagFlag = tagUpdate.isControlTag();
        this.metadata = tagUpdate.getMetadata();

        // Notify all listeners of the update
        clone = this.clone();
      }
    } finally {
      updateTagLock.writeLock().unlock();
    }

    if (clone != null) {
      notifyListeners(clone);
    }

    return valid;
  }


  /**
   * Inner method for updating the all value fields from this
   * <code>Tag</code> instance
   *
   * @param tagValueUpdate Reference to the object containing the updates
   */
  private void doUpdateValues(final TagValueUpdate tagValueUpdate) {
    updateTagQuality(tagValueUpdate.getDataTagQuality());

    this.getAlarms().clear();
    this.getAlarms().addAll(tagValueUpdate.getAlarms());

    this.setDescription(tagValueUpdate.getDescription());
    this.setValueDescription(tagValueUpdate.getValueDescription());
    this.setServerTimestamp(tagValueUpdate.getServerTimestamp());
    this.setDaqTimestamp(tagValueUpdate.getDaqTimestamp());
    this.setSourceTimestamp(tagValueUpdate.getSourceTimestamp());
    this.setTagValue(tagValueUpdate.getValue());
    this.setMode(tagValueUpdate.getMode());
    this.setSimulated(tagValueUpdate.isSimulated());
  }

  /**
   * Inner method to update the tag quality without changing the inaccessible states
   * previously set by supervision event updates.
   *
   * @param qualityUpdate The tag quality update
   */
  private void updateTagQuality(final DataTagQuality qualityUpdate) {
    if (!this.getDataTagQuality().isAccessible()) {
      Map<TagQualityStatus, String> oldQualityStates = this.getDataTagQuality().getInvalidQualityStates();
      this.getDataTagQuality().setInvalidStates(qualityUpdate.getInvalidQualityStates());

      if (oldQualityStates.containsKey(TagQualityStatus.PROCESS_DOWN)) {
        this.getDataTagQuality().addInvalidStatus(TagQualityStatus.PROCESS_DOWN, oldQualityStates.get(TagQualityStatus.PROCESS_DOWN));
      }
      else if (oldQualityStates.containsKey(TagQualityStatus.EQUIPMENT_DOWN)) {
        this.getDataTagQuality().addInvalidStatus(TagQualityStatus.EQUIPMENT_DOWN, oldQualityStates.get(TagQualityStatus.EQUIPMENT_DOWN));
      }
      else if (oldQualityStates.containsKey(TagQualityStatus.SUBEQUIPMENT_DOWN)) {
        this.getDataTagQuality().addInvalidStatus(TagQualityStatus.SUBEQUIPMENT_DOWN, oldQualityStates.get(TagQualityStatus.SUBEQUIPMENT_DOWN));
      }
    }
    else {
      this.getDataTagQuality().setInvalidStates(qualityUpdate.getInvalidQualityStates());
    }
  }

  /**
   * Checks whether the received update is valid or not.
   * <p>
   * The following properties are checked (in order)
   * to decide whether an update is valid or not
   * <p>
   * <li> the tag id is the same
   * <li> The tag update is not <code>null</code>
   * <li> The server timestamp is never older or at least equals.
   * <li> The DAQ timestamp.
   * <li> The source timestamp.
   * <p>
   * Checkout issue:
   * http://issues.cern.ch/browse/TIMS-826
   * for more details.
   *
   * @param tagValueUpdate The received update
   *
   * @return <code>true</code>, if the update passed all checks
   */
  protected boolean isValidUpdate(final TagValueUpdate tagValueUpdate) {

    if (tagValueUpdate != null && tagValueUpdate.getId().equals(this.id)) {

      if (tagValueUpdate.getServerTimestamp() == null) {
        return false;
      }

      // Check server cache timestamp
      final long newServerTime = tagValueUpdate.getServerTimestamp().getTime();
      final long oldServerTime = this.getServerTimestamp().getTime();

      if (newServerTime > oldServerTime) {
        return true;
      }

      // Check DAQ timestamp, if configured.
      // This is not the case for server rule tags
      if (newServerTime == oldServerTime && tagValueUpdate.getDaqTimestamp() != null) {
        final long newDaqTime = tagValueUpdate.getDaqTimestamp().getTime();

        if (this.getDaqTimestamp() == null) { // old DAQ timestamp is not set
          return true;
        }

        final long oldDaqTime = this.getDaqTimestamp().getTime();
        if (newDaqTime > oldDaqTime) {
          return true;
        }
        else if (newDaqTime == oldDaqTime && tagValueUpdate.getSourceTimestamp() != null) {
          final long newSourceTime = tagValueUpdate.getSourceTimestamp().getTime();

          if (this.getTimestamp() == null) { // old source timestamp is not set
            return true;
          }

          final long oldSourceTime = this.getTimestamp().getTime();
          if (tagValueUpdate instanceof TagUpdate || newSourceTime != oldSourceTime) {
            // We basically allow non-continuous source timestamps
            return true;
          }
        }
        else if (tagValueUpdate instanceof TagUpdate && newDaqTime == oldDaqTime && this.getTimestamp() == null) {
          // This means we accept a TagUpdate also when server & DAQ time are equals
          // but both source timestamps are not set
          return true;
        }
      }
    }

    return false;
  }

  /**
   * Creates a clone of the this object. The only difference is that
   * it does not copy the registered listeners. If you are only interested
   * in the static information of the object you should call after cloning
   * the {@link #clean()} method.
   *
   * @return The clone of this object
   * @throws CloneNotSupportedException Thrown, if one of the field does not support cloning.
   * @see #clean()
   */
  @Override
  public CloneableTagBean clone() {
    try {
      CloneableTagBean cloneableTagBean = (CloneableTagBean) super.clone();

      return cloneableTagBean;
    }
    catch (CloneNotSupportedException e) {
      log.error("clone() - Cloning the CloneableTagBean object failed! No update send to the client.");
      throw new RuntimeException(e);
    }
  }
}
