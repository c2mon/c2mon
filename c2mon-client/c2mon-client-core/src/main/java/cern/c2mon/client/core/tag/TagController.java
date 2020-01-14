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

package cern.c2mon.client.core.tag;

import cern.c2mon.client.common.listener.BaseTagListener;
import cern.c2mon.client.common.listener.TagListener;
import cern.c2mon.client.common.tag.Tag;
import cern.c2mon.client.common.util.ConcurrentIdentitySet;
import cern.c2mon.client.core.jms.SupervisionListener;
import cern.c2mon.client.core.listener.TagUpdateListener;
import cern.c2mon.shared.client.supervision.SupervisionEvent;
import cern.c2mon.shared.client.tag.TagUpdate;
import cern.c2mon.shared.client.tag.TagValueUpdate;
import cern.c2mon.shared.common.datatag.DataTagQuality;
import cern.c2mon.shared.common.datatag.TagQualityStatus;
import cern.c2mon.shared.common.supervision.SupervisionStatus;
import cern.c2mon.shared.common.type.TypeConverter;
import cern.c2mon.shared.rule.RuleExpression;
import cern.c2mon.shared.rule.RuleFormatException;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author Szymon Halastra
 */
@Data
@Slf4j
public class TagController implements TagUpdateListener, SupervisionListener {

  private TagImpl tagImpl;

  /**
   * Lock to prevent more than one thread at a time to update the value
   */
  private ReentrantReadWriteLock updateTagLock = new ReentrantReadWriteLock();

  /**
   * Concurrent modifiable collection of DataTagUpdateListeners registered for
   * updates on this DataTag
   */
  private Set<BaseTagListener> listeners = new ConcurrentIdentitySet<>();

  /**
   * Metadata of an Tag object.
   */
  private Map<String, Object> metadata = new HashMap<>();

  public TagController() {
    this.tagImpl = new TagImpl();
  }

  public TagController(long id) {
    this.tagImpl = new TagImpl(id);
  }

  public TagController(long id, boolean unknown) {
    this.tagImpl = new TagImpl(id);

    if (unknown) {
      this.tagImpl.setUnknown();
    }
  }

  public TagController(TagImpl tagImpl) {
    this.tagImpl = tagImpl.clone();
  }

  @org.simpleframework.xml.core.Persist
  public void prepare() {

    if(this.getTagImpl().getRuleExpression() != null)
      this.getTagImpl().setRuleExpressionString(this.getTagImpl().getRuleExpression().getExpression());
  }

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
    if (tagImpl.isControlTag() && !tagImpl.isAliveTag()) {
      return;
    }

    Tag clone = null;

    updateTagLock.writeLock().lock();
    try {
      boolean validUpdate = false;
      validUpdate |= tagImpl.getEquipmentSupervisionStatus().containsKey(supervisionEvent.getEntityId());
      validUpdate |= tagImpl.getSubEquipmentSupervisionStatus().containsKey(supervisionEvent.getEntityId());
      validUpdate |= tagImpl.getProcessSupervisionStatus().containsKey(supervisionEvent.getEntityId());

      if (validUpdate) {
        SupervisionEvent oldEvent;
        switch (supervisionEvent.getEntity()) {
          case PROCESS:
            oldEvent = tagImpl.getProcessSupervisionStatus().put(supervisionEvent.getEntityId(), supervisionEvent);
            updateProcessStatus(supervisionEvent);
            break;
          case EQUIPMENT:
            oldEvent = tagImpl.getEquipmentSupervisionStatus().put(supervisionEvent.getEntityId(), supervisionEvent);
            updateEquipmentStatus(supervisionEvent);
            break;
          case SUBEQUIPMENT:
            oldEvent = tagImpl.getSubEquipmentSupervisionStatus().put(supervisionEvent.getEntityId(), supervisionEvent);
            updateSubEquipmentStatus(supervisionEvent);
            break;
          default:
            String errorMsg = "The supervision event type " + supervisionEvent.getEntity() + " is not supported.";
            log.error("update(SupervisionEvent) - " + errorMsg);
            throw new IllegalArgumentException(errorMsg);
        }

        if (oldEvent == null || !supervisionEvent.equals(oldEvent)) {
          // Notify all listeners of the update
          clone = tagImpl.clone();
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
  private void updateProcessStatus(SupervisionEvent supervisionEvent) {
    StringBuilder invalidationMessage = new StringBuilder();
    for (SupervisionEvent event : tagImpl.getProcessSupervisionStatus().values()) {
      this.invalidateMessage(invalidationMessage, event);
    }

    boolean down = SupervisionStatus.DOWN == supervisionEvent.getStatus();
    if (down) {
      tagImpl.getDataTagQuality().addInvalidStatus(TagQualityStatus.PROCESS_DOWN, invalidationMessage.toString());
    }
    else {
      tagImpl.getDataTagQuality().removeInvalidStatus(TagQualityStatus.PROCESS_DOWN);
    }
  }

  /**
   * Inner method for updating the equipment status of this tag and
   * computing the error message, if one of the linked equipments is down.
   */
  private void updateEquipmentStatus(SupervisionEvent supervisionEvent) {
    StringBuilder invalidationMessage = new StringBuilder();
    for (SupervisionEvent event : tagImpl.getEquipmentSupervisionStatus().values()) {
      this.invalidateMessage(invalidationMessage, event);
    }

    boolean down = SupervisionStatus.DOWN == supervisionEvent.getStatus();
    if (down) {
      tagImpl.getDataTagQuality().addInvalidStatus(TagQualityStatus.EQUIPMENT_DOWN, invalidationMessage.toString());
    }
    else {
      tagImpl.getDataTagQuality().removeInvalidStatus(TagQualityStatus.EQUIPMENT_DOWN);
    }
  }

  /**
   * Inner method for updating the sub equipment status of this tag and
   * computing the error message, if one of the linked sub equipments is down.
   */
  private void updateSubEquipmentStatus(SupervisionEvent supervisionEvent) {
    StringBuilder invalidationMessage = new StringBuilder();
    for (SupervisionEvent event : tagImpl.getSubEquipmentSupervisionStatus().values()) {
      this.invalidateMessage(invalidationMessage, event);
    }

    boolean down = SupervisionStatus.DOWN == supervisionEvent.getStatus();
    if (down) {
      tagImpl.getDataTagQuality().addInvalidStatus(TagQualityStatus.SUBEQUIPMENT_DOWN, invalidationMessage.toString());
    }
    else {
      tagImpl.getDataTagQuality().removeInvalidStatus(TagQualityStatus.SUBEQUIPMENT_DOWN);
    }
  }

  private StringBuilder invalidateMessage(StringBuilder stringBuilder, SupervisionEvent event) {
    if (event != null) {
      boolean isDown = false;
      isDown |= event.getStatus().equals(SupervisionStatus.DOWN);
      isDown |= event.getStatus().equals(SupervisionStatus.STOPPED);
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
    for (BaseTagListener updateListener : listeners) {
      try {
        updateListener.onUpdate(clone);
      }
      catch (Exception e) {
        log.error("notifyListeners() : error notifying DataTagUpdateListeners", e);
      }
    }
  }

  /**
   * This thread safe method updates the given <code>TagImpl</code> object.
   * It copies every single field of the <code>TransferTagValue</code> object and notifies
   * then the registered listener about the update by providing a copy of the
   * <code>TagImpl</code> object.
   * <p>
   * Please note that the <code>TagImpl</code> gets only updated, if the tag id's
   * matches and if the server time stamp of the update is older than the current time
   * stamp set.
   *
   * @param tagValueUpdate The object that contains the updates.
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
        clone = tagImpl.clone();
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
   * This thread safe method updates the given <code>TagImpl</code> object.
   * It copies every single field of the <code>TransferTag</code> object and notifies
   * then the registered listener about the update by providing a copy of the
   * <code>TagImpl</code> object.
   * <p>
   * Please note that the <code>TagImpl</code> gets only updated, if the tag id's
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
          tagImpl.setRuleExpression(RuleExpression.createExpression(tagUpdate.getRuleExpression()));
        }

        doUpdateValues(tagUpdate);

        // update process map
        Map<Long, SupervisionEvent> updatedProcessMap = new HashMap<>();
        for (Long processId : tagUpdate.getProcessIds()) {
          updatedProcessMap.put(processId, tagImpl.getProcessSupervisionStatus().get(processId));
        }
        tagImpl.setProcessSupervisionStatus(updatedProcessMap);

        // update equipment map
        Map<Long, SupervisionEvent> updatedEquipmentMap = new HashMap<>();
        for (Long equipmentId : tagUpdate.getEquipmentIds()) {
          updatedEquipmentMap.put(equipmentId, tagImpl.getEquipmentSupervisionStatus().get(equipmentId));
        }
        tagImpl.setEquipmentSupervisionStatus(updatedEquipmentMap);

        // update sub equipment map
        Map<Long, SupervisionEvent> updatedSubEquipmentMap = new HashMap<>();
        for (Long subEquipmentId : tagUpdate.getSubEquipmentIds()) {
          updatedSubEquipmentMap.put(subEquipmentId, tagImpl.getSubEquipmentSupervisionStatus().get(subEquipmentId));
        }
        tagImpl.setSubEquipmentSupervisionStatus(updatedSubEquipmentMap);

        tagImpl.setTagName(tagUpdate.getName());
        tagImpl.setTopicName(tagUpdate.getTopicName());
        tagImpl.setUnit(tagUpdate.getUnit());

        tagImpl.aliveTagFlag = tagUpdate.isAliveTag();
        tagImpl.controlTagFlag = tagUpdate.isControlTag();
        tagImpl.setMetadata(tagUpdate.getMetadata());

        // Notify all listeners of the update
        clone = tagImpl.clone();
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

    tagImpl.getAlarms().clear();
    tagImpl.getAlarms().addAll(tagValueUpdate.getAlarms());

    tagImpl.setDescription(tagValueUpdate.getDescription());
    tagImpl.setValueDescription(tagValueUpdate.getValueDescription());
    tagImpl.setServerTimestamp(tagValueUpdate.getServerTimestamp());
    tagImpl.setDaqTimestamp(tagValueUpdate.getDaqTimestamp());
    tagImpl.setSourceTimestamp(tagValueUpdate.getSourceTimestamp());
    tagImpl.setTagValue(tagValueUpdate.getValue());
    tagImpl.setType(TypeConverter.getType(tagValueUpdate.getValueClassName()));
    tagImpl.setMode(tagValueUpdate.getMode());
    tagImpl.setSimulated(tagValueUpdate.isSimulated());
  }

  /**
   * Inner method to update the tag quality without changing the inaccessible states
   * previously set by supervision event updates.
   *
   * @param qualityUpdate The tag quality update
   */
  private void updateTagQuality(final DataTagQuality qualityUpdate) {
    if (!tagImpl.getDataTagQuality().isAccessible()) {
      Map<TagQualityStatus, String> oldQualityStates = tagImpl.getDataTagQuality().getInvalidQualityStates();
      tagImpl.getDataTagQuality().setInvalidStates(qualityUpdate.getInvalidQualityStates());

      if (oldQualityStates.containsKey(TagQualityStatus.PROCESS_DOWN)) {
        tagImpl.getDataTagQuality().addInvalidStatus(TagQualityStatus.PROCESS_DOWN, oldQualityStates.get(TagQualityStatus.PROCESS_DOWN));
      }
      else if (oldQualityStates.containsKey(TagQualityStatus.EQUIPMENT_DOWN)) {
        tagImpl.getDataTagQuality().addInvalidStatus(TagQualityStatus.EQUIPMENT_DOWN, oldQualityStates.get(TagQualityStatus.EQUIPMENT_DOWN));
      }
      else if (oldQualityStates.containsKey(TagQualityStatus.SUBEQUIPMENT_DOWN)) {
        tagImpl.getDataTagQuality().addInvalidStatus(TagQualityStatus.SUBEQUIPMENT_DOWN, oldQualityStates.get(TagQualityStatus.SUBEQUIPMENT_DOWN));
      }
    }
    else {
      tagImpl.getDataTagQuality().setInvalidStates(qualityUpdate.getInvalidQualityStates());
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

    if (tagValueUpdate != null && tagValueUpdate.getId().equals(tagImpl.getId())) {

      if (tagValueUpdate.getServerTimestamp() == null) {
        return false;
      }

      // Check server cache timestamp
      final long newServerTime = tagValueUpdate.getServerTimestamp().getTime();
      final long oldServerTime = tagImpl.getServerTimestamp().getTime();

      if (newServerTime > oldServerTime) {
        return true;
      }

      // Check DAQ timestamp, if configured.
      // This is not the case for server rule tags
      if (newServerTime == oldServerTime && tagValueUpdate.getDaqTimestamp() != null) {
        final long newDaqTime = tagValueUpdate.getDaqTimestamp().getTime();

        if (tagImpl.getDaqTimestamp() == null) { // old DAQ timestamp is not set
          return true;
        }

        final long oldDaqTime = tagImpl.getDaqTimestamp().getTime();
        if (newDaqTime > oldDaqTime) {
          return true;
        }
        else if (newDaqTime == oldDaqTime && tagValueUpdate.getSourceTimestamp() != null) {
          final long newSourceTime = tagValueUpdate.getSourceTimestamp().getTime();

          if (tagImpl.getSourceTimestamp() == null) { // old source timestamp is not set
            return true;
          }

          final long oldSourceTime = tagImpl.getSourceTimestamp().getTime();
          if (tagValueUpdate instanceof TagUpdate || newSourceTime != oldSourceTime) {
            // We basically allow non-continuous source timestamps
            return true;
          }
        }
        else if (tagValueUpdate instanceof TagUpdate && newDaqTime == oldDaqTime && tagImpl.getSourceTimestamp() == null) {
          // This means we accept a TagUpdate also when server & DAQ time are equals
          // but both source timestamps are not set
          return true;
        }
      }
    }

    return false;
  }

  /**
   * Returns information whether the tag has any update listeners registered
   * or not
   *
   * @return <code>true</code>, if this <code>Tag</code> instance has
   * update listeners registered.
   */
  public boolean hasUpdateListeners() {
    boolean isEmpty = !this.getListeners().isEmpty();
    return isEmpty;
  }

  /**
   * Adds a <code>DataTagUpdateListener</code> to the Tag and
   * generates an initial update event, in case that the initalValue parameter
   * is not specified (null) or different to the current value.<p>
   * Any change to the Tag value or quality attributes will trigger
   * an update event to all <code>DataTagUpdateListener</code> objects registered.
   *
   * @param listener     the DataTagUpdateListener that will receive value updates message for this tag
   * @param initialValue In case the user subscribed with a {@link TagListener} provide here
   *                     the initial value which was sent through {@link TagListener#onInitialUpdate(Collection)}
   *                     method. Otherwise, pass {@code null} as parameter, if the initial update shall be sent via the
   *                     {@link BaseTagListener#onUpdate(Tag)}
   *
   * @see #removeUpdateListener(BaseTagListener)
   */
  public void addUpdateListener(final BaseTagListener listener, final Tag initialValue) {
    if (log.isTraceEnabled()) {
      log.trace("addUpdateListener() called.");
    }
    this.getListeners().add(listener);

    Tag clone = null;
    this.getTagImpl().getUpdateTagLock().readLock().lock();
    try {
      boolean sendInitialUpdate = !TagComparator.compare(this.getTagImpl(), initialValue);

      if (sendInitialUpdate) {
        clone = this.getTagImpl().clone();
      }
    } finally {
      this.getTagImpl().getUpdateTagLock().readLock().unlock();
    }

    if (clone != null) {
      try {
        listener.onUpdate(clone);
      }
      catch (Exception e) {
        log.error("addUpdateListener() : error notifying listener", e);
      }
    }
  }

  /**
   * Returns <code>true</code>, if the given listener is registered
   * for receiving updates of that tag.
   *
   * @param pListener the listener to check
   *
   * @return <code>true</code>, if the given listener is registered
   * for receiving updates of that tag.
   */
  public boolean isUpdateListenerRegistered(final BaseTagListener pListener) {
    boolean isRegistered = this.getListeners().contains(pListener);
    return isRegistered;
  }

  /**
   * Removes (synchronized) a previously registered <code>DataTagUpdateListener</code>
   *
   * @param pListener The listener that shall be unregistered
   *
   * @see #addUpdateListener
   */
  public void removeUpdateListener(final BaseTagListener pListener) {
    this.getListeners().remove(pListener);
  }

  /**
   * @return All listeners registered to this data tag
   */
  public Collection<BaseTagListener> getUpdateListeners() {
    return new ArrayList<>(this.getListeners());
  }


  /**
   * Adds a <code>DataTagUpdateListener</code> to the Tag and
   * generates an initial update event for that listener. Any change to the
   * Tag value or quality attributes will trigger an update event to
   * all <code>DataTagUpdateListener</code> objects registered.
   *
   * @param listener the DataTagUpdateListener that will receive value updates message for this tag
   *
   * @see #removeUpdateListener(BaseTagListener)
   */
  public void addUpdateListener(final BaseTagListener listener) {
    addUpdateListener(listener, null);
  }

  /**
   * Adds all <code>DataTagUpdateListener</code> of the list to the Tag and
   * generates an initial update event for those listeners.
   * Any change to the Tag value or quality attributes will trigger
   * an update event to all <code>DataTagUpdateListener</code> objects
   * registered.
   *
   * @param listeners                   the DataTagUpdateListeners that will receive value updates message for this tag
   * @param sendInitialValuesToListener if set to <code>true</code>, the listener will receive the
   *                                    current value of the tag.
   *
   * @see #removeUpdateListener(BaseTagListener)
   */
  public void addUpdateListeners(final Collection<BaseTagListener> listeners) {
    for (BaseTagListener listener : listeners) {
      addUpdateListener(listener);
    }
  }

  /**
   * Removes all previously registered <code>DataTagUpdateListener</code>
   */
  public void removeAllUpdateListeners() {
    this.getListeners().clear();
  }

  /**
   * Removes the invalid quality status and informs the listeners but only,
   * if the status flag was really being set before.
   *
   * @param statusToRemove The invalid quality status to be removed from this tag.
   */
  public void validate(final TagQualityStatus statusToRemove) {
    Tag clone = null;

    if (log.isTraceEnabled()) {
      log.trace("validate() - Removing " + statusToRemove + " quality status from tag " + this.getTagImpl().getId());
    }
    if (this.getTagImpl().getDataTagQuality().isInvalidStatusSet(statusToRemove)) {
      // remove the quality status
      this.getTagImpl().getDataTagQuality().removeInvalidStatus(statusToRemove);
      clone = this.getTagImpl().clone();
    }

    if (clone != null) {
      notifyListeners(clone);
    }
  }

  /**
   * Invalidates the tag with {@link TagQualityStatus#INACCESSIBLE} and sets
   * the quality description to <code>pDescription</code>
   * Notifies all registered <code>DataTagUpdateListeners</code> of the change
   * of state.
   *
   * @param status      The invalidation status to be added to the tag
   * @param description the quality description
   */
  public void invalidate(final TagQualityStatus status, final String description) {
    TagImpl clone = null;
    this.getTagImpl().getUpdateTagLock().writeLock().lock();
    try {
      if (log.isTraceEnabled()) {
        log.trace("invalidate() - Invalidating tag " + this.getTagImpl().getId() + " with quality status " + status);
      }
      // Invalidate the object.
      this.getTagImpl().getDataTagQuality().addInvalidStatus(status, description);

      clone = this.getTagImpl().clone();
    } finally {
      this.getTagImpl().getUpdateTagLock().writeLock().unlock();
    }

    if (clone != null) {
      notifyListeners(clone);
    }
  }

  /**
   * Removes all information from the object.
   * This is in particular interesting for the history mode which sometimes just
   * uses the static information from the live tag object.
   */
  public void clean() {
    updateTagLock.writeLock().lock();
    try {
      tagImpl.getAlarms().clear();
      tagImpl.setDescription(tagImpl.DEFAULT_DESCRIPTION);
      tagImpl.getDataTagQuality().setInvalidStatus(TagQualityStatus.UNINITIALISED, tagImpl.DEFAULT_DESCRIPTION);
      tagImpl.setServerTimestamp(new Timestamp(0L));
      tagImpl.setDaqTimestamp(null);
      tagImpl.setSourceTimestamp(null);
      tagImpl.setTagValue(null);
      tagImpl.setType(null);
      for (Long id : tagImpl.getProcessSupervisionStatus().keySet()) {
        tagImpl.getProcessSupervisionStatus().put(id, null);
      }
      for (Long id : tagImpl.getEquipmentSupervisionStatus().keySet()) {
        tagImpl.getEquipmentSupervisionStatus().put(id, null);
      }
      for (Long id : tagImpl.getSubEquipmentSupervisionStatus().keySet()) {
        tagImpl.getSubEquipmentSupervisionStatus().put(id, null);
      }
    } finally {
      updateTagLock.writeLock().unlock();
    }
  }
}
