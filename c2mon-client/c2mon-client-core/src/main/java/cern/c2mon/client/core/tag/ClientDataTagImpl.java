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
package cern.c2mon.client.core.tag;

import java.io.*;
import java.sql.Timestamp;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import lombok.extern.slf4j.Slf4j;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.convert.AnnotationStrategy;
import org.simpleframework.xml.core.Persister;
import org.springframework.beans.factory.annotation.Autowired;

import cern.c2mon.client.common.listener.BaseListener;
import cern.c2mon.client.common.listener.DataTagListener;
import cern.c2mon.client.common.listener.DataTagUpdateListener;
import cern.c2mon.client.common.listener.TagUpdateListener;
import cern.c2mon.client.common.tag.Tag;
import cern.c2mon.client.common.util.ConcurrentIdentitySet;
import cern.c2mon.client.core.jms.SupervisionListener;
import cern.c2mon.client.core.jms.TopicRegistrationDetails;
import cern.c2mon.shared.client.alarm.AlarmValue;
import cern.c2mon.shared.client.supervision.SupervisionEvent;
import cern.c2mon.shared.client.tag.TagUpdate;
import cern.c2mon.shared.client.tag.TagValueUpdate;
import cern.c2mon.shared.common.datatag.DataTagQuality;
import cern.c2mon.shared.common.datatag.TagQualityStatus;
import cern.c2mon.shared.common.supervision.SupervisionConstants.SupervisionStatus;
import cern.c2mon.shared.rule.RuleExpression;
import cern.c2mon.shared.rule.RuleFormatException;

/**
 * A client representation of the DataTag object.
 * <code>Tag</code> objects are created by the
 * <code>TagFactory</code>. The object connects to its update topic
 * and receives tag updates from the TIM server.
 * When the Tag value or quality changes it notifies its registered
 * <code>DataTagUpdateListeners</code>
 *
 * @author Matthias Braeger
 * @see TagFactory
 * @see DataTagUpdateListener
 */
@Root(name = "Tag")
@Slf4j
public class ClientDataTagImpl implements TagUpdateListener, TopicRegistrationDetails, SupervisionListener, Cloneable {

  @Autowired
  private ExtendedTagBean extendedTagBean;

  /**
   * Default description when the object is not yet initialized
   */
  private static final String DEFAULT_DESCRIPTION = "Tag not initialised.";

  /**
   * The value of the tag
   */
  @Element(required = false)
  private Object tagValue;

  /**
   * Unique identifier for a DataTag
   */
  @Attribute
  private Long id;

  /**
   * Only used for xml serialization.
   */
  @Element(required = false)
  private String ruleExpressionString;

  /**
   * <code>true</code>, if tag represents an Alive Control tag
   */
  private boolean aliveTagFlag = false;

  /**
   * <code>true</code>, if tag represents a CommFault-, Alive- or Status tag
   */
  private boolean controlTagFlag = false;

  /**
   * String representation of the JMS destination where the DataTag
   * is published on change.
   */
  @Element(required = false)
  private String topicName = null;

  /**
   * Metadata of an Tag object.
   */
  private Map<String, Object> metadata = new HashMap<>();

  /**
   * Concurrent modifiable collection of DataTagUpdateListeners registered for
   * updates on this DataTag
   */
  private Set<BaseListener> listeners = new ConcurrentIdentitySet<>();

  /**
   * Lock to prevent more than one thread at a time to update the value
   */
  private ReentrantReadWriteLock updateTagLock = new ReentrantReadWriteLock();


  /**
   * Protected default constructor that initializes the tag id with -1L
   */
  protected ClientDataTagImpl() {
    this.id = -1L;
  }

  /**
   * Constructor
   * Creates a Tag with a tagID and a javax.jms.TopicSession
   * object to be used for subscriptions.
   * Sets the tag name to "Not.initialized" and the quality to uninitialized.
   *
   * @param tagId the unique identifier for the DataTag
   */
  public ClientDataTagImpl(final Long tagId) {
    id = tagId;
  }

  /**
   * Constructor
   * Creates a Tag with a tagID and a javax.jms.TopicSession
   * object to be used for subscriptions.
   * Sets the tag name to "Not.initialized" and the quality to UNINITIALIZED.
   *
   * @param tagId the unique identifier for the DataTag
   * @param If    true, it will set the quality to UNDEFINED_TAG instead of UNINITIALIZED
   */
  public ClientDataTagImpl(final Long tagId, boolean unknown) {
    id = tagId;

    if (unknown) {
      setUnknown();
    }
  }

  /**
   * Set the field metadata.
   *
   * @param metadata the data to set.
   */
  public void setMetadata(Map<String, Object> metadata) {
    this.metadata = metadata;
  }

  /**
   * Returns the metadata to the corresponding tag.
   *
   * @return the metadata of the object.
   */
  public Map<String, Object> getMetadata() {
    return this.metadata;
  }

  private void setUnknown() {
    extendedTagBean.getDataTagQuality().setInvalidStatus(TagQualityStatus.UNDEFINED_TAG, "Tag is not known by the system");
  }

  @org.simpleframework.xml.core.Persist
  public void prepare() {

    if (extendedTagBean.getRuleExpression() != null)
      ruleExpressionString = extendedTagBean.getRuleExpression().getExpression();
  }


  /* (non-Javadoc)
   * @see cern.c2mon.client.tag.Tag#getId()
   */
  @Override
  public Long getId() {
    return this.id;
  }

  /**
   * Removes the invalid quality status and informs the listeners but only,
   * if the status flag was really being set before.
   *
   * @param statusToRemove The invalid quality status to be removed from this tag.
   */
  public void validate(final TagQualityStatus statusToRemove) {
    Tag clone = null;
    updateTagLock.writeLock().lock();
    try {
      if (log.isTraceEnabled()) {
        log.trace("validate() - Removing " + statusToRemove + " quality status from tag " + this.id);
      }
      if (extendedTagBean.getDataTagQuality().isInvalidStatusSet(statusToRemove)) {
        // remove the quality status
        extendedTagBean.getDataTagQuality().removeInvalidStatus(statusToRemove);
        clone = this.clone();
      }
    } finally {
      updateTagLock.writeLock().unlock();
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
    TagBean clone = null;
    updateTagLock.writeLock().lock();
    try {
      if (log.isTraceEnabled()) {
        log.trace("invalidate() - Invalidating tag " + this.id + " with quality status " + status);
      }
      // Invalidate the object.
      extendedTagBean.getDataTagQuality().addInvalidStatus(status, description);

      clone = this.clone();
    } finally {
      updateTagLock.writeLock().unlock();
    }

    if (clone != null) {
      notifyListeners(clone);
    }
  }

  /**
   * Private method to notify all registered <code>DataTagUpdateListener</code> instances.
   * Please avoid calling this method within a WRITELOCK block since it could be a potential
   * candidate for risking a deadlocks.
   *
   * @param Please only provide a clone of this tag
   */
  private synchronized void notifyListeners(final Tag clone) {
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
   * Adds a <code>DataTagUpdateListener</code> to the Tag and
   * generates an initial update event, in case that the initalValue parameter
   * is not specified (null) or different to the current value.<p>
   * Any change to the Tag value or quality attributes will trigger
   * an update event to all <code>DataTagUpdateListener</code> objects registered.
   *
   * @param listener     the DataTagUpdateListener that will receive value updates message for this tag
   * @param initialValue In case the user subscribed with a {@link DataTagListener} provide here
   *                     the initial value which was sent through {@link DataTagListener#onInitialUpdate(Collection)}
   *                     method. Otherwise, pass {@code null} as parameter, if the initial update shall be sent via the
   *                     {@link DataTagUpdateListener#onUpdate(Tag)}
   *
   * @see #removeUpdateListener(DataTagUpdateListener)
   */
  public void addUpdateListener(final BaseListener<Tag> listener, final Tag initialValue) {
    if (log.isTraceEnabled()) {
      log.trace("addUpdateListener() called.");
    }
    listeners.add(listener);

    Tag clone = null;
    updateTagLock.readLock().lock();
    try {
      boolean sendInitialUpdate = !TagComparator.compare(extendedTagBean, initialValue);

      if (sendInitialUpdate) {
        clone = this.clone();
      }
    } finally {
      updateTagLock.readLock().unlock();
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
   * Adds a <code>DataTagUpdateListener</code> to the Tag and
   * generates an initial update event for that listener. Any change to the
   * Tag value or quality attributes will trigger an update event to
   * all <code>DataTagUpdateListener</code> objects registered.
   *
   * @param listener the DataTagUpdateListener that will receive value updates message for this tag
   *
   * @see #removeUpdateListener(DataTagUpdateListener)
   */
  public void addUpdateListener(final BaseListener listener) {
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
   * @see #removeUpdateListener(DataTagUpdateListener)
   */
  public void addUpdateListeners(final Collection<BaseListener> listeners) {
    for (BaseListener listener : listeners) {
      addUpdateListener(listener);
    }
  }

  /**
   * @return All listeners registered to this data tag
   */
  public Collection<BaseListener> getUpdateListeners() {
    return new ArrayList<>(listeners);
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
  public boolean isUpdateListenerRegistered(final BaseListener<? extends Tag> pListener) {
    boolean isRegistered = listeners.contains(pListener);
    return isRegistered;
  }

  /**
   * Removes (synchronized) a previously registered <code>DataTagUpdateListener</code>
   *
   * @param pListener The listener that shall be unregistered
   *
   * @see #addUpdateListener
   */
  public void removeUpdateListener(final BaseListener<? extends Tag> pListener) {
    listeners.remove(pListener);
  }

  /**
   * Removes all previously registered <code>DataTagUpdateListener</code>
   */
  public void removeAllUpdateListeners() {
    listeners.clear();
  }

  /**
   * Returns information whether the tag has any update listeners registered
   * or not
   *
   * @return <code>true</code>, if this <code>Tag</code> instance has
   * update listeners registered.
   */
  public boolean hasUpdateListeners() {
    boolean isEmpty = !listeners.isEmpty();
    return isEmpty;
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

    if (tagValueUpdate != null && tagValueUpdate.getId().equals(id)) {

      if (tagValueUpdate.getServerTimestamp() == null) {
        return false;
      }

      // Check server cache timestamp
      final long newServerTime = tagValueUpdate.getServerTimestamp().getTime();
      final long oldServerTime = extendedTagBean.getServerTimestamp().getTime();

      if (newServerTime > oldServerTime) {
        return true;
      }

      // Check DAQ timestamp, if configured.
      // This is not the case for server rule tags
      if (newServerTime == oldServerTime && tagValueUpdate.getDaqTimestamp() != null) {
        final long newDaqTime = tagValueUpdate.getDaqTimestamp().getTime();

        if (extendedTagBean.getDaqTimestamp() == null) { // old DAQ timestamp is not set
          return true;
        }

        final long oldDaqTime = extendedTagBean.getDaqTimestamp().getTime();
        if (newDaqTime > oldDaqTime) {
          return true;
        }
        else if (newDaqTime == oldDaqTime && tagValueUpdate.getSourceTimestamp() != null) {
          final long newSourceTime = tagValueUpdate.getSourceTimestamp().getTime();

          if (extendedTagBean.getTimestamp() == null) { // old source timestamp is not set
            return true;
          }

          final long oldSourceTime = extendedTagBean.getTimestamp().getTime();
          if (tagValueUpdate instanceof TagUpdate || newSourceTime != oldSourceTime) {
            // We basically allow non-continuous source timestamps
            return true;
          }
        }
        else if (tagValueUpdate instanceof TagUpdate && newDaqTime == oldDaqTime && extendedTagBean.getTimestamp() == null) {
          // This means we accept a TagUpdate also when server & DAQ time are equals
          // but both source timestamps are not set
          return true;
        }
      }
    }

    return false;
  }

  /**
   * Inner method to update the tag quality without changing the inaccessible states
   * previously set by supervision event updates.
   *
   * @param qualityUpdate The tag quality update
   */
  private void updateTagQuality(final DataTagQuality qualityUpdate) {
    if (!extendedTagBean.getDataTagQuality().isAccessible()) {
      Map<TagQualityStatus, String> oldQualityStates = extendedTagBean.getDataTagQuality().getInvalidQualityStates();
      extendedTagBean.getDataTagQuality().setInvalidStates(qualityUpdate.getInvalidQualityStates());

      if (oldQualityStates.containsKey(TagQualityStatus.PROCESS_DOWN)) {
        extendedTagBean.getDataTagQuality().addInvalidStatus(TagQualityStatus.PROCESS_DOWN, oldQualityStates.get(TagQualityStatus.PROCESS_DOWN));
      }
      else if (oldQualityStates.containsKey(TagQualityStatus.EQUIPMENT_DOWN)) {
        extendedTagBean.getDataTagQuality().addInvalidStatus(TagQualityStatus.EQUIPMENT_DOWN, oldQualityStates.get(TagQualityStatus.EQUIPMENT_DOWN));
      }
      else if (oldQualityStates.containsKey(TagQualityStatus.SUBEQUIPMENT_DOWN)) {
        extendedTagBean.getDataTagQuality().addInvalidStatus(TagQualityStatus.SUBEQUIPMENT_DOWN, oldQualityStates.get(TagQualityStatus.SUBEQUIPMENT_DOWN));
      }
    }
    else {
      extendedTagBean.getDataTagQuality().setInvalidStates(qualityUpdate.getInvalidQualityStates());
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
          extendedTagBean.setRuleExpression(RuleExpression.createExpression(tagUpdate.getRuleExpression()));
        }

        doUpdateValues(tagUpdate);

        // update process map
        Map<Long, SupervisionEvent> updatedProcessMap = new HashMap<Long, SupervisionEvent>();
        for (Long processId : tagUpdate.getProcessIds()) {
          updatedProcessMap.put(processId, extendedTagBean.getProcessSupervisionStatus().get(processId));
        }
        extendedTagBean.setProcessSupervisionStatus(updatedProcessMap);

        // update equipment map
        Map<Long, SupervisionEvent> updatedEquipmentMap = new HashMap<Long, SupervisionEvent>();
        for (Long equipmentId : tagUpdate.getEquipmentIds()) {
          updatedEquipmentMap.put(equipmentId, extendedTagBean.getEquipmentSupervisionStatus().get(equipmentId));
        }
        extendedTagBean.setEquipmentSupervisionStatus(updatedEquipmentMap);

        // update sub equipment map
        Map<Long, SupervisionEvent> updatedSubEquipmentMap = new HashMap<Long, SupervisionEvent>();
        for (Long subEquipmentId : tagUpdate.getSubEquipmentIds()) {
          updatedSubEquipmentMap.put(subEquipmentId, extendedTagBean.getSubEquipmentSupervisionStatus().get(subEquipmentId));
        }
        extendedTagBean.setSubEquipmentSupervisionStatus(updatedSubEquipmentMap);

        extendedTagBean.setTagName(tagUpdate.getName());
        topicName = tagUpdate.getTopicName();
        extendedTagBean.setUnit(tagUpdate.getUnit());

        aliveTagFlag = tagUpdate.isAliveTag();
        controlTagFlag = tagUpdate.isControlTag();
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
   * Inner method for updating the process status of this tag and
   * computing the error message, if one of the linked processes is down.
   */
  private void updateProcessStatus() {
    boolean down = false;
    StringBuilder invalidationMessage = new StringBuilder();
    for (SupervisionEvent event : extendedTagBean.getProcessSupervisionStatus().values()) {
      if (event != null) {
        boolean isDown = false;
        isDown |= event.getStatus().equals(SupervisionStatus.DOWN);
        isDown |= event.getStatus().equals(SupervisionStatus.STOPPED);
        if (isDown) {
          down = true;
          if (invalidationMessage.length() > 0) {
            invalidationMessage.append("; ");
          }
          invalidationMessage.append(event.getMessage());
        }
      }
    }

    if (down) {
      extendedTagBean.getDataTagQuality().addInvalidStatus(TagQualityStatus.PROCESS_DOWN, invalidationMessage.toString());
    }
    else {
      extendedTagBean.getDataTagQuality().removeInvalidStatus(TagQualityStatus.PROCESS_DOWN);
    }
  }

  /**
   * Inner method for updating the equipment status of this tag and
   * computing the error message, if one of the linked equipments is down.
   */
  private void updateEquipmentStatus() {
    boolean down = false;
    StringBuilder invalidationMessage = new StringBuilder();
    for (SupervisionEvent event : extendedTagBean.getEquipmentSupervisionStatus().values()) {
      if (event != null) {
        boolean isDown = false;
        isDown |= event.getStatus().equals(SupervisionStatus.DOWN);
        isDown |= event.getStatus().equals(SupervisionStatus.STOPPED);
        if (isDown) {
          down = true;
          if (invalidationMessage.length() > 0) {
            invalidationMessage.append("; ");
          }
          invalidationMessage.append(event.getMessage());
        }
      }
    }

    if (down) {
      extendedTagBean.getDataTagQuality().addInvalidStatus(TagQualityStatus.EQUIPMENT_DOWN, invalidationMessage.toString());
    }
    else {
      extendedTagBean.getDataTagQuality().removeInvalidStatus(TagQualityStatus.EQUIPMENT_DOWN);
    }
  }

  /**
   * Inner method for updating the sub equipment status of this tag and
   * computing the error message, if one of the linked sub equipments is down.
   */
  private void updateSubEquipmentStatus() {
    boolean down = false;
    StringBuilder invalidationMessage = new StringBuilder();
    for (SupervisionEvent event : extendedTagBean.getSubEquipmentSupervisionStatus().values()) {
      if (event != null) {
        boolean isDown = false;
        isDown |= event.getStatus().equals(SupervisionStatus.DOWN);
        isDown |= event.getStatus().equals(SupervisionStatus.STOPPED);
        if (isDown) {
          down = true;
          if (invalidationMessage.length() > 0) {
            invalidationMessage.append("; ");
          }
          invalidationMessage.append(event.getMessage());
        }
      }
    }

    if (down) {
      extendedTagBean.getDataTagQuality().addInvalidStatus(TagQualityStatus.SUBEQUIPMENT_DOWN, invalidationMessage.toString());
    }
    else {
      extendedTagBean.getDataTagQuality().removeInvalidStatus(TagQualityStatus.SUBEQUIPMENT_DOWN);
    }
  }

  /**
   * Inner method for updating the all value fields from this
   * <code>Tag</code> instance
   *
   * @param tagValueUpdate Reference to the object containing the updates
   */
  private void doUpdateValues(final TagValueUpdate tagValueUpdate) {
    updateTagQuality(tagValueUpdate.getDataTagQuality());

    extendedTagBean.getAlarms().clear();
    extendedTagBean.getAlarms().addAll(tagValueUpdate.getAlarms());

    extendedTagBean.setDescription(tagValueUpdate.getDescription());
    extendedTagBean.setValueDescription(tagValueUpdate.getValueDescription());
    extendedTagBean.setServerTimestamp(tagValueUpdate.getServerTimestamp());
    extendedTagBean.setDaqTimestamp(tagValueUpdate.getDaqTimestamp());
    extendedTagBean.setSourceTimestamp(tagValueUpdate.getSourceTimestamp());
    tagValue = tagValueUpdate.getValue();
    extendedTagBean.setMode(tagValueUpdate.getMode());
    extendedTagBean.setSimulated(tagValueUpdate.isSimulated());
  }

  /* (non-Javadoc)
   * @see cern.c2mon.client.tag.Tag#getTopicName()
   */
  @Override
  public String getTopicName() {
    updateTagLock.readLock().lock();
    try {
      if (topicName != null) {
        return this.topicName;
      }
      return "";
    } finally {
      updateTagLock.readLock().unlock();
    }
  }

  /* (non-Javadoc)
   * @see cern.c2mon.client.tag.Tag#hashCode()
   */
  @Override
  public int hashCode() {
    return this.id.hashCode();
  }

  /* (non-Javadoc)
   * @see cern.c2mon.client.tag.Tag#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object pRight) {
    if (pRight instanceof ClientDataTagImpl) {
      if (this.id.equals(((ClientDataTagImpl) pRight).id)) {
        return true;
      }
    }

    return false;
  }

  private <T> T deepClone(T object) {
    if (object == null) {
      return null;
    }

    try {
      ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
      ObjectOutputStream out = null;
      out = new ObjectOutputStream(byteOut);
      out.writeObject(object);
      out.flush();
      ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(byteOut.toByteArray()));
      return (T) object.getClass().cast(in.readObject());
    }
    catch (IOException | ClassNotFoundException e) {
      throw new RuntimeException("Error cloning metadata: the object is not serializable");
    }
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
  @SuppressWarnings("unchecked")
  @Override
  public ClientDataTagImpl clone() {
    updateTagLock.readLock().lock();
    try {
      ClientDataTagImpl clone = (ClientDataTagImpl) super.clone();

      clone.updateTagLock = new ReentrantReadWriteLock();

      // clone the process id map
      clone.extendedTagBean.setProcessSupervisionStatus(new HashMap<>(extendedTagBean.getProcessSupervisionStatus().size()));
      for (Entry<Long, SupervisionEvent> entry : extendedTagBean.getProcessSupervisionStatus().entrySet()) {
        if (entry.getValue() != null) {
          clone.extendedTagBean.getProcessSupervisionStatus().put(entry.getKey(), entry.getValue().clone());
        }
        else {
          clone.extendedTagBean.getProcessSupervisionStatus().put(entry.getKey(), null);
        }
      }

      // clone the equipment id map
      clone.extendedTagBean.setEquipmentSupervisionStatus(new HashMap<>(extendedTagBean.getEquipmentSupervisionStatus().size()));
      for (Entry<Long, SupervisionEvent> entry : extendedTagBean.getEquipmentSupervisionStatus().entrySet()) {
        if (entry.getValue() != null) {
          clone.extendedTagBean.getEquipmentSupervisionStatus().put(entry.getKey(), entry.getValue().clone());
        }
        else {
          clone.extendedTagBean.getEquipmentSupervisionStatus().put(entry.getKey(), null);
        }
      }

      // clone the sub equipment id map
      clone.extendedTagBean.setSubEquipmentSupervisionStatus(new HashMap<>(extendedTagBean.getSubEquipmentSupervisionStatus().size()));
      for (Entry<Long, SupervisionEvent> entry : extendedTagBean.getSubEquipmentSupervisionStatus().entrySet()) {
        if (entry.getValue() != null) {
          clone.extendedTagBean.getSubEquipmentSupervisionStatus().put(entry.getKey(), entry.getValue().clone());
        }
        else {
          clone.extendedTagBean.getSubEquipmentSupervisionStatus().put(entry.getKey(), null);
        }
      }

      // clone the metadata map - alternative:
      //clone.metadata = (Map<String, Object>) ((HashMap)this.metadata).clone();
      //clone.metadata.putAll(metadata);
      clone.metadata = new HashMap<>();
      for (Entry<String, Object> entry : metadata.entrySet()) {
        clone.metadata.put(deepClone(entry.getKey()), deepClone(entry.getValue()));
      }

      // AlarmsValue objects are immutable
      clone.extendedTagBean.setAlarms(new ArrayList<>());
      for (AlarmValue alarm : extendedTagBean.getAlarms()) {
        clone.extendedTagBean.getAlarms().add(alarm.clone());
      }

      if (extendedTagBean.getDataTagQuality() != null) {
        clone.extendedTagBean.setTagQuality(extendedTagBean.getDataTagQuality().clone());
      }
      if (extendedTagBean.getTimestamp() != null) {
        clone.extendedTagBean.setSourceTimestamp((Timestamp) extendedTagBean.getTimestamp().clone());
      }
      if (extendedTagBean.getDaqTimestamp() != null) {
        clone.extendedTagBean.setDaqTimestamp((Timestamp) extendedTagBean.getDaqTimestamp().clone());
      }
      if (extendedTagBean.getServerTimestamp() != null) {
        clone.extendedTagBean.setServerTimestamp((Timestamp) extendedTagBean.getServerTimestamp().clone());
      }
      if (extendedTagBean.getRuleExpression() != null) {
        clone.extendedTagBean.setRuleExpression((RuleExpression) extendedTagBean.getRuleExpression().clone());
      }
      clone.listeners = new ConcurrentIdentitySet<>();

      return clone;
    }
    catch (CloneNotSupportedException cloneException) {
      log.error(
              "clone() - Cloning the ClientDataTagImpl object failed! No update sent to the client.");
      throw new RuntimeException(cloneException);
    } finally {
      updateTagLock.readLock().unlock();
    }
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
    if (extendedTagBean.isControlTag() && !extendedTagBean.isAliveTag()) {
      return;
    }

    Tag clone = null;

    updateTagLock.writeLock().lock();
    try {
      boolean validUpdate = false;
      validUpdate |= extendedTagBean.getEquipmentSupervisionStatus().containsKey(supervisionEvent.getEntityId());
      validUpdate |= extendedTagBean.getSubEquipmentSupervisionStatus().containsKey(supervisionEvent.getEntityId());
      validUpdate |= extendedTagBean.getProcessSupervisionStatus().containsKey(supervisionEvent.getEntityId());

      if (validUpdate) {
        SupervisionEvent oldEvent;
        switch (supervisionEvent.getEntity()) {
          case PROCESS:
            oldEvent = extendedTagBean.getProcessSupervisionStatus().put(supervisionEvent.getEntityId(), supervisionEvent);
            updateProcessStatus();
            break;
          case EQUIPMENT:
            oldEvent = extendedTagBean.getEquipmentSupervisionStatus().put(supervisionEvent.getEntityId(), supervisionEvent);
            updateEquipmentStatus();
            break;
          case SUBEQUIPMENT:
            oldEvent = extendedTagBean.getSubEquipmentSupervisionStatus().put(supervisionEvent.getEntityId(), supervisionEvent);
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
   * Creates a XML representation of this class by making use of
   * the simpleframework XML library.
   *
   * @return The XML representation of this class
   * @see #fromXml(String)
   */
  public String getXml() {
    Serializer serializer = new Persister(new AnnotationStrategy());
    StringWriter fw = null;
    String result = null;

    try {
      fw = new StringWriter();
      serializer.write(this, fw);
      result = fw.toString();
    }
    catch (Exception e) {
      e.printStackTrace();
    } finally {
      if (fw != null) {
        try {
          fw.close();
        }
        catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
    return result;
  }

  /**
   * Static method for creating a <code>ClientDataTagImpl</code> object
   * from a XML String by making use of the simpleframework XML library.
   *
   * @param xml The XML representation of a <code>ClientDataTagImpl</code> object
   *
   * @return <code>ClientDataTagImpl</code> object created from the given XML String
   * @throws Exception In case of a parsing error or a wrong XML definition
   * @see #getXml()
   */
  public static ClientDataTagImpl fromXml(final String xml) throws Exception {

    ClientDataTagImpl cdt = null;
    StringReader sr = null;
    Serializer serializer = new Persister(new AnnotationStrategy());

    try {
      sr = new StringReader(xml);
      cdt = serializer.read(ClientDataTagImpl.class, new StringReader(xml), false);
    } finally {

      if (sr != null) {
        sr.close();
      }
    }

    return cdt;
  }

  /**
   * @return A XML representation of this class instance.
   */
  @Override
  public String toString() {
    return this.getXml();
  }
}
