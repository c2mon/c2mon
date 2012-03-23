/*******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2004 - 2011 CERN. This program is free software; you can
 * redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version. This program is distributed
 * in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received
 * a copy of the GNU General Public License along with this program; if not,
 * write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA 02111-1307, USA.
 * 
 * Author: TIM team, tim.support@cern.ch
 ******************************************************************************/
package cern.c2mon.client.core.tag;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.log4j.Logger;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.convert.AnnotationStrategy;
import org.simpleframework.xml.core.Persister;

import cern.c2mon.client.common.listener.DataTagUpdateListener;
import cern.c2mon.client.common.tag.ClientDataTag;
import cern.c2mon.client.common.tag.TypeNumeric;
import cern.c2mon.client.common.util.ConcurrentIdentitySet;
import cern.c2mon.client.jms.SupervisionListener;
import cern.c2mon.client.jms.TopicRegistrationDetails;
import cern.c2mon.shared.client.alarm.AlarmValue;
import cern.c2mon.shared.client.supervision.SupervisionEvent;
import cern.c2mon.shared.client.tag.TagMode;
import cern.c2mon.shared.client.tag.TagUpdate;
import cern.c2mon.shared.client.tag.TagValueUpdate;
import cern.tim.shared.common.datatag.DataTagQuality;
import cern.tim.shared.common.datatag.DataTagQualityImpl;
import cern.tim.shared.common.datatag.TagQualityStatus;
import cern.tim.shared.common.supervision.SupervisionConstants.SupervisionStatus;
import cern.tim.shared.rule.RuleExpression;
import cern.tim.shared.rule.RuleFormatException;

/**
 * A client representation of the DataTag object.
 * <code>ClientDataTag</code> objects are created by the
 * <code>TagFactory</code>. The object connects to its update topic
 * and receives tag updates from the TIM server.
 * When the ClientDataTag value or quality changes it notifies its registered
 * <code>DataTagUpdateListeners</code>
 * @see TagFactory
 * @see DataTagUpdateListener
 * @author Matthias Braeger
 */
@Root(name="ClientDataTag")
public class ClientDataTagImpl implements ClientDataTag, TopicRegistrationDetails, SupervisionListener {
  
  /** Log4j instance */
  private static final Logger LOG = Logger.getLogger(ClientDataTagImpl.class);
  
  /** Default description when the object is not yet initialized */
  private static final String DEFAULT_DESCRIPTION = "Tag not initialised.";
  
  /** The value of the tag */
  @Element(required = false)
  private Object tagValue;
  
  /** The current tag mode */
  @Element
  private TagMode mode = TagMode.TEST;
  
  /** 
   * <code>true</code>, if the tag value is currently simulated and not
   * corresponding to a live event.
   */
  @Element
  private boolean simulated = false;

  /** Unique identifier for a DataTag */
  @Attribute
  private Long id;
  
  /** 
   * Containing all process id's which are relevant to compute the
   * final quality status on the C2MON client layer. By definition there
   * is just one id defined. Only rules might have dependencies
   * to multiple processes (DAQs).
   */
  private Map<Long, SupervisionEvent> processSupervisionStatus = new HashMap<Long, SupervisionEvent>();
  
  /** 
   * Containing all equipment id's which are relevant to compute the
   * final quality status on the C2MON client layer. By definition there
   * is just one id defined. Only rules might have dependencies
   * to multiple equipments.
   */
  private Map<Long, SupervisionEvent> equipmentSupervisionStatus = new HashMap<Long, SupervisionEvent>();
  
  /** The unique name of the tag */
  @Element(required = false)
  private String tagName = null;
  
  /** The quality of the tag */
  @Element(required = false)
  private DataTagQuality tagQuality = 
    new DataTagQualityImpl(TagQualityStatus.UNINITIALISED, DEFAULT_DESCRIPTION);
  
  /** The alarm objects associated to this data tag */
  @ElementList
  private ArrayList<AlarmValue> alarms = new ArrayList<AlarmValue>();
  
  /** The source timestamp that indicates when the value change was generated */
  @Element(required = false)
  private Timestamp sourceTimestamp = null;
  
  /** The server timestamp that indicates when the change message passed the server */
  @Element
  private Timestamp serverTimestamp = new Timestamp(0L);

  /** Unit of the tag */
  @Element(required = false)
  private String unit = null;
  
  /** The description of the Tag */
  @Element(required = false)
  private String description = "";
  
  /** The description of the value */
  @Element(required = false)
  private String valueDescription = "";
  
  /**
   * String representation of the JMS destination where the DataTag 
   * is published on change.
   */
  @Element(required = false)
  private String topicName = null;

  /** In case this data tag is a rule this variable contains its rule expression */
  private RuleExpression ruleExpression = null;

  /** Only used for xml serialization. */
  @Element(required = false)
  private String ruleExpressionString;

  /**
   * Concurrent modifiable collection of DataTagUpdateListeners registered for
   * updates on this DataTag
   */
  private Set<DataTagUpdateListener> listeners = new ConcurrentIdentitySet<DataTagUpdateListener>();
  
  /** Lock to prevent more than one thread at a time to update the value */
  private ReentrantReadWriteLock updateTagLock = new ReentrantReadWriteLock();


  protected ClientDataTagImpl() {      
    this.id = -1L;
  }
  
  /**
   * Constructor
   * Creates a ClientDataTag with a tagID and a javax.jms.TopicSession
   * object to be used for subscriptions.
   * Sets the tag name to "Not.initialized" and the quality to uninitialized.
   * @param tagId the unique identifier for the DataTag
   */
  public ClientDataTagImpl(final Long tagId) {
    id = tagId;
  }  
  
  @org.simpleframework.xml.core.Persist
  public void prepare() {
    
     if (ruleExpression != null)
       ruleExpressionString = ruleExpression.getExpression();
  }

  
  /* (non-Javadoc)
   * @see cern.c2mon.client.tag.ClientDataTag#getId()
   */
  @Override
  public Long getId() {
    return this.id;
  }

  /* (non-Javadoc)
   * @see cern.c2mon.client.tag.ClientDataTag#getName()
   */ 
  @Override
  public String getName() {
    updateTagLock.readLock().lock();
    try {
      if (this.tagName == null) {
        return "UNKNOWN";
      }
      else {
        return tagName;
      }
    }
    finally {
      updateTagLock.readLock().unlock();
    }
  }
  
  @Override
  public TagMode getMode() {
    updateTagLock.readLock().lock();
    try {
      return mode;
    }
    finally {
      updateTagLock.readLock().unlock();
    }
  }

  @Override
  public boolean isSimulated() {
    updateTagLock.readLock().lock();
    try {
      return simulated;
    }
    finally {
      updateTagLock.readLock().unlock();
    }
  }

  @Override
  public Object getValue() {
    updateTagLock.readLock().lock();
    try {
      return tagValue;
    }
    finally {
      updateTagLock.readLock().unlock();
    }
  }

  @Override
  public Timestamp getTimestamp() {
    updateTagLock.readLock().lock();
    try { 
      if (sourceTimestamp == null) {
        // Use the server timestamp, because the tag might never been
        // sent by an equipment. In that case the sourceTimestamp is null.
        return serverTimestamp;
      }
      else {
        return sourceTimestamp;
      }
    }
    finally {
      updateTagLock.readLock().unlock();
    }
  }

  @Override
  public Timestamp getServerTimestamp() {
    updateTagLock.readLock().lock();
    try {
      return serverTimestamp;
    }
    finally {
      updateTagLock.readLock().unlock();
    }
  }

  @Override
  public String getUnit() {
    updateTagLock.readLock().lock();
    try {
      if (unit != null) {
        return unit;
      }
      return "";
    }
    finally {
      updateTagLock.readLock().unlock();
    }
  }

  @Override
  public final Class< ? > getType() {
    updateTagLock.readLock().lock();
    try {
      Class< ? > type = null;
      if (this.tagValue != null) {
        type = tagValue.getClass();
      }
      return type;
    }
    finally {
      updateTagLock.readLock().unlock();
    }
  }
  
  @Override
  public TypeNumeric getTypeNumeric() {
    updateTagLock.readLock().lock();
    try {
      Class< ? > type = getType();
      if (type != null) {
        int typeNumeric = type.hashCode();
        for (TypeNumeric t : TypeNumeric.values()) {
          if (t.getCode() == typeNumeric) {
            return t;
          }
        }
      }
    }
    finally {
      updateTagLock.readLock().unlock();
    }
    
    return TypeNumeric.TYPE_UNKNOWN;
  }

  @Override
  public DataTagQuality getDataTagQuality() {
    updateTagLock.readLock().lock();
    try {
      return tagQuality;
    }
    finally {
      updateTagLock.readLock().unlock();
    }
  }
  
  @Override
  public boolean isValid() {
    updateTagLock.readLock().lock();
    try {
      return tagQuality.isValid();
    }
    finally {
      updateTagLock.readLock().unlock();
    }
  }
  
  /**
   * Removes the invalid quality status and informs the listeners but only,
   * if the status flag was really being set before.
   * @param statusToRemove The invalid quality status to be removed from this tag.
   */
  public void validate(final TagQualityStatus statusToRemove) {
    updateTagLock.writeLock().lock();
    try {
      if (LOG.isDebugEnabled()) {
        LOG.debug("validate() - Removing " + statusToRemove + " quality status from tag " + this.id);
      }
      if (tagQuality.isInvalidStatusSet(statusToRemove)) {
        // remove the quality status
        tagQuality.removeInvalidStatus(statusToRemove);
        notifyListeners();
      }
    }
    finally {
      updateTagLock.writeLock().unlock();
    }
  }

  /**
   * Invalidates the tag with {@link TagQualityStatus#INACCESSIBLE} and sets
   * the quality description to <code>pDescription</code>
   * Notifies all registered <code>DataTagUpdateListeners</code> of the change
   * of state.
   * @param status The invalidation status to be added to the tag 
   * @param description the quality description
   */
  public void invalidate(final TagQualityStatus status, final String description) {
    updateTagLock.writeLock().lock();
    try {
      if (LOG.isDebugEnabled()) {
        LOG.debug("invalidate() - Invalidating tag " + this.id + " with quality status " + status);
      }
      // Invalidate the object.
      tagQuality.addInvalidStatus(status, description);
      
      notifyListeners();
    }
    finally {
      updateTagLock.writeLock().unlock();
    }
  }

  /**
   * Private method to notify all registered <code>DataTagUpdateListener</code> instances.
   */
  private void notifyListeners() {
    try {
      final ClientDataTag clone = this.clone();
      for (DataTagUpdateListener updateListener : listeners) { 
        try { 
          updateListener.onUpdate(clone);
        }
        catch (Exception e) {
          LOG.error("notifyListeners() : error notifying DataTagUpdateListeners", e);
        }
      }
    }
    catch (CloneNotSupportedException cloneException) {
      LOG.fatal(
          "notifyListeners() - Cloning the ClientDataTagImpl object failed! No update sent to the client.");
      throw new RuntimeException(cloneException);
    }
  }


  /**
   * Adds a <code>DataTagUpdateListener</code> to the ClientDataTag and 
   * generates an initial update event for that listener.
   * Any change to the ClientDataTag value or quality attributes will trigger
   * an update event to all <code>DataTagUpdateListener</code> objects 
   * registered.
   * @param pListener the DataTagUpdateListener comments
   * @see #removeUpdateListener(DataTagUpdateListener)
   */
  public void addUpdateListener(final DataTagUpdateListener pListener) {
    if (LOG.isDebugEnabled()) {
      LOG.debug("addUpdateListener() called.");
    }
    listeners.add(pListener);
    
    try {
      pListener.onUpdate(this.clone());
    }
    catch (CloneNotSupportedException cloneException) {
      LOG.fatal(
          "addUpdateListener() - Cloning the ClientDataTagImpl object failed! No update sent to the client.");
      throw new RuntimeException(cloneException);
    }
  }
  
  /**
   * Adds all <code>DataTagUpdateListener</code> of the list to the ClientDataTag and 
   * generates an initial update event for those listeners.
   * Any change to the ClientDataTag value or quality attributes will trigger
   * an update event to all <code>DataTagUpdateListener</code> objects 
   * registered.
   * @param pListenerList the DataTagUpdateListener comments
   * @see #removeUpdateListener(DataTagUpdateListener)
   */
  public void addUpdateListeners(final Collection<DataTagUpdateListener> pListenerList) {
    for (DataTagUpdateListener listener : pListenerList) {
      addUpdateListener(listener);
    }
  }
  
  /**
   * @return All listeners registered to this data tag
   */
  public Collection<DataTagUpdateListener> getUpdateListeners() {
    return new ArrayList<DataTagUpdateListener>(listeners);
  }
  
  /**
   * Returns <code>true</code>, if the given listener is registered
   * for receiving updates of that tag.
   * @param pListener the listener to check
   * @return <code>true</code>, if the given listener is registered
   * for receiving updates of that tag.
   */
  public boolean isUpdateListenerRegistered(final DataTagUpdateListener pListener) {
    boolean isRegistered = listeners.contains(pListener);
    return isRegistered;
  }

  /**
   * Removes (synchronized) a previously registered <code>DataTagUpdateListener</code>
   * @see #addUpdateListener
   * @param pListener The listener that shall be unregistered
   */
  public void removeUpdateListener(final DataTagUpdateListener pListener) {
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
   * @return <code>true</code>, if this <code>ClientDataTag</code> instance has
   *         update listeners registered.
   */
  public boolean hasUpdateListeners() {
    boolean isEmpty = !listeners.isEmpty();
    return isEmpty;
  }
  
  /**
   * Checks whether the update is valid or not
   * @param tagValueUpdate The received update
   * @return <code>true</code>, if the update passed all checks
   */
  private boolean isValidUpdate(final TagValueUpdate tagValueUpdate) {
    boolean valid = true;
    valid &= tagValueUpdate != null;
    if (tagValueUpdate != null) 
      valid &= tagValueUpdate.getId().equals(id);
    if (tagQuality.isAccessible()) { // If inaccessible we always allow a refresh
      valid &= tagValueUpdate.getServerTimestamp().after(serverTimestamp);
    }
    
    return valid;
  }
  
  
  /**
   * Inner method to update the tag quality without changing the inaccessible states
   * previously set by supervision event updates.
   * @param qualityUpdate The tag quality update
   */
  private void updateTagQuality(final DataTagQuality qualityUpdate) {
    if (!tagQuality.isAccessible()) {
      Map<TagQualityStatus, String> oldQualityStates = this.tagQuality.getInvalidQualityStates();
      tagQuality.setInvalidStates(qualityUpdate.getInvalidQualityStates());
      
      if (oldQualityStates.containsKey(TagQualityStatus.PROCESS_DOWN)) {
        tagQuality.addInvalidStatus(TagQualityStatus.PROCESS_DOWN, oldQualityStates.get(TagQualityStatus.PROCESS_DOWN));
      }
      else if (oldQualityStates.containsKey(TagQualityStatus.EQUIPMENT_DOWN)) {
        tagQuality.addInvalidStatus(TagQualityStatus.EQUIPMENT_DOWN, oldQualityStates.get(TagQualityStatus.EQUIPMENT_DOWN));
      }
      else if (oldQualityStates.containsKey(TagQualityStatus.SUBEQUIPMENT_DOWN)) {
        tagQuality.addInvalidStatus(TagQualityStatus.SUBEQUIPMENT_DOWN, oldQualityStates.get(TagQualityStatus.SUBEQUIPMENT_DOWN));
      }
    }
    else {
      tagQuality.setInvalidStates(qualityUpdate.getInvalidQualityStates());
    }
  } 

  @Override
  public boolean update(final TagValueUpdate tagValueUpdate) {
    updateTagLock.writeLock().lock();
    try {
      boolean valid = isValidUpdate(tagValueUpdate);

      if (valid) {
        doUpdateValues(tagValueUpdate);
        // Notify all listeners of the update
        notifyListeners();
      }

      return valid;
    }
    finally {
      updateTagLock.writeLock().unlock();
    }
  }
  
  /* (non-Javadoc)
   * @see cern.c2mon.client.tag.ClientDataTag#update(cern.c2mon.shared.client.tag.TransferTag)
   */
  @Override
  public boolean update(final TagUpdate tagUpdate) throws RuleFormatException {
    updateTagLock.writeLock().lock();
    try {
      boolean valid = isValidUpdate(tagUpdate);
    
      if (valid) {
        if (tagUpdate.getRuleExpression() != null) {
          ruleExpression = RuleExpression.createExpression(tagUpdate.getRuleExpression());
        }
        
        doUpdateValues(tagUpdate);
        
        // update process map
        Map<Long, SupervisionEvent> updatedProcessMap = new HashMap<Long, SupervisionEvent>();
        for (Long processId : tagUpdate.getProcessIds()) {
         updatedProcessMap.put(processId, processSupervisionStatus.get(processId)); 
        }
        processSupervisionStatus = updatedProcessMap;
        
        // update equipment map
        Map<Long, SupervisionEvent> updatedEquipmentMap = new HashMap<Long, SupervisionEvent>();
        for (Long equipmentId : tagUpdate.getEquipmentIds()) {
          updatedEquipmentMap.put(equipmentId, equipmentSupervisionStatus.get(equipmentId)); 
        }
        equipmentSupervisionStatus = updatedEquipmentMap;
        
        tagName = tagUpdate.getName();
        topicName = tagUpdate.getTopicName();
        unit = tagUpdate.getUnit();
        
        // Notify all listeners of the update
        notifyListeners();
      }
      
      return valid;
    }
    finally {
      updateTagLock.writeLock().unlock();
    }
  }
  
  /**
   * Inner method for updating the process status of this tag and
   * computing the error message, if one of the linked processes is down.
   */
  private void updateProcessStatus() {
    boolean down = false;
    StringBuilder invalidationMessage = new StringBuilder();
    for (SupervisionEvent event : processSupervisionStatus.values()) {
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
      tagQuality.addInvalidStatus(TagQualityStatus.PROCESS_DOWN, invalidationMessage.toString());
    }
    else {
      tagQuality.removeInvalidStatus(TagQualityStatus.PROCESS_DOWN);
    }
  }
  
  /**
   * Inner method for updating the equipment status of this tag and
   * computing the error message, if one of the linked equipments is down.
   */
  private void updateEquipmentStatus() {
    boolean down = false;
    StringBuilder invalidationMessage = new StringBuilder();
    for (SupervisionEvent event : equipmentSupervisionStatus.values()) {
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
      tagQuality.addInvalidStatus(TagQualityStatus.EQUIPMENT_DOWN, invalidationMessage.toString());
    }
    else {
      tagQuality.removeInvalidStatus(TagQualityStatus.EQUIPMENT_DOWN);
    }
  }
  
  /**
   * Inner method for updating the all value fields from this
   * <code>ClientDataTag</code> instance
   * 
   * @param tagValueUpdate Reference to the object containing the updates 
   */
  private void doUpdateValues(final TagValueUpdate tagValueUpdate) {
    updateTagQuality(tagValueUpdate.getDataTagQuality());
    
    alarms.clear();
    alarms.addAll(tagValueUpdate.getAlarms());
    
    description = tagValueUpdate.getDescription();
    valueDescription = tagValueUpdate.getValueDescription();
    serverTimestamp = tagValueUpdate.getServerTimestamp();
    sourceTimestamp = tagValueUpdate.getSourceTimestamp();
    tagValue = tagValueUpdate.getValue();
    mode = tagValueUpdate.getMode();
    simulated = tagValueUpdate.isSimulated();
  }

  /* (non-Javadoc)
   * @see cern.c2mon.client.tag.ClientDataTag#getTopicName()
   */
  @Override
  public String getTopicName() {
    updateTagLock.readLock().lock();
    try {
      return this.topicName;
    }
    finally {
      updateTagLock.readLock().unlock();
    }
  }

  /* (non-Javadoc)
   * @see cern.c2mon.client.tag.ClientDataTag#isRuleResult()
   */
  @Override
  public boolean isRuleResult() {
    updateTagLock.readLock().lock();
    try {
      return this.ruleExpression != null;
    }
    finally {
      updateTagLock.readLock().unlock();
    }
  }

  /* (non-Javadoc)
   * @see cern.c2mon.client.tag.ClientDataTag#getRuleExpression()
   */
  @Override
  public RuleExpression getRuleExpression() {
    updateTagLock.readLock().lock();
    try {
      return this.ruleExpression;
    }
    finally {
      updateTagLock.readLock().unlock();
    }
  }

  @Override
  public Collection<Long> getAlarmIds() {
    updateTagLock.readLock().lock();
    try {
      Collection<Long> alarmIds = new ArrayList<Long>(alarms.size());
      for (AlarmValue alarmValue : alarms) {
        alarmIds.add(alarmValue.getId());
      }
      
      return alarmIds;
    }
    finally {
      updateTagLock.readLock().unlock();
    }
  }
  
  /* (non-Javadoc)
   * @see cern.c2mon.client.tag.ClientDataTag#getAlarms()
   */
  @Override
  public final Collection<AlarmValue> getAlarms() {
    updateTagLock.readLock().lock();
    try {
      return new ArrayList<AlarmValue>(alarms);
    }
    finally {
      updateTagLock.readLock().unlock();
    }
  }
  
  /* (non-Javadoc)
   * @see cern.c2mon.client.tag.ClientDataTag#hashCode()
   */
  @Override
  public int hashCode() {
    return this.id.hashCode();
  }
  
  /* (non-Javadoc)
   * @see cern.c2mon.client.tag.ClientDataTag#equals(java.lang.Object)
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

  /* (non-Javadoc)
   * @see cern.c2mon.client.tag.ClientDataTag#getDescription()
   */
  @Override
  public String getDescription() {
    updateTagLock.readLock().lock();
    try {
      return this.description;
    }
    finally { updateTagLock.readLock().unlock(); }
  }
  
  @Override
  public String getValueDescription() {
    updateTagLock.readLock().lock();
    try {
      return valueDescription;
    } 
    finally { updateTagLock.readLock().unlock(); }
  }
  
  
  /**
   * Creates a clone of the this object. The only difference is that
   * it does not copy the registered listeners. If you are only interested
   * in the static information of the object you should call after cloning
   * the {@link #clean()} method.
   * @return The clone of this object
   * @throws CloneNotSupportedException Thrown, if one of the field does not support cloning.
   * @see #clean()
   */
  @SuppressWarnings("unchecked")
  @Override
  public ClientDataTagImpl clone() throws CloneNotSupportedException {
    updateTagLock.readLock().lock();
    try {
      ClientDataTagImpl clone = (ClientDataTagImpl) super.clone();
      
      clone.updateTagLock = new ReentrantReadWriteLock();
      
      // clone the process id map
      clone.processSupervisionStatus = new HashMap<Long, SupervisionEvent>(processSupervisionStatus.size());
      for (Entry<Long, SupervisionEvent> entry : processSupervisionStatus.entrySet()) {
        if (entry.getValue() != null) {
          clone.processSupervisionStatus.put(entry.getKey(), entry.getValue().clone());
        }
        else {
          clone.processSupervisionStatus.put(entry.getKey(), null);
        }
      }
      
      // clone the equipment id map
      clone.equipmentSupervisionStatus = new HashMap<Long, SupervisionEvent>(equipmentSupervisionStatus.size());
      for (Entry<Long, SupervisionEvent> entry : equipmentSupervisionStatus.entrySet()) {
        if (entry.getValue() != null) {
          clone.equipmentSupervisionStatus.put(entry.getKey(), entry.getValue().clone());
        }
        else {
          clone.equipmentSupervisionStatus.put(entry.getKey(), null);
        }
      }
      
      // AlarmsValue objects are immutable
      clone.alarms = (ArrayList<AlarmValue>) alarms.clone();
      
      if (tagQuality != null) {
        clone.tagQuality = tagQuality.clone();
      }
      if (sourceTimestamp != null) {
        clone.sourceTimestamp = (Timestamp) sourceTimestamp.clone();
      }
      if (serverTimestamp != null) {
        clone.serverTimestamp = (Timestamp) serverTimestamp.clone();
      }
      if (ruleExpression != null) {
        clone.ruleExpression = (RuleExpression) ruleExpression.clone();
      }
      clone.listeners = new ConcurrentIdentitySet<DataTagUpdateListener>();
      
      return clone;
    }
    finally {
      updateTagLock.readLock().unlock();
    }
  }

  @Override
  public void onUpdate(final TagValueUpdate tagValueUpdate) {
    update(tagValueUpdate);
  }

  @Override
  public Collection<Long> getEquipmentIds() {
    return new ArrayList<Long>(equipmentSupervisionStatus.keySet());
  }

  @Override
  public Collection<Long> getProcessIds() {
    return new ArrayList<Long>(processSupervisionStatus.keySet());
  }

  @Override
  public void clean() {
    updateTagLock.writeLock().lock();
    try {
      this.alarms.clear();
      this.description = DEFAULT_DESCRIPTION;
      this.tagQuality.setInvalidStatus(TagQualityStatus.UNINITIALISED, DEFAULT_DESCRIPTION);
      this.serverTimestamp = new Timestamp(0L);
      this.sourceTimestamp = null;
      this.tagValue = null;
      for (Long id : processSupervisionStatus.keySet()) {
        processSupervisionStatus.put(id, null);
      }
      for (Long id : equipmentSupervisionStatus.keySet()) {
        equipmentSupervisionStatus.put(id, null);
      }
    } finally { updateTagLock.writeLock().unlock(); }
  }

  /**
  * This thread safe method updates the accessible state of the given
  * <code>ClientDataTag</code> object. Once the accessibility has been updated
  * it notifies the registered listener about the update by providing a copy of
  * the <code>ClientDataTag</code> object.
  * 
  * @param supervisionEvent The supervision event which contains the current
  *                         status of the process or the equipment.
  */
  @Override
  public void onSupervisionUpdate(SupervisionEvent supervisionEvent) {
    if (supervisionEvent == null) {
      return;
    }
    updateTagLock.writeLock().lock();
    try {
      boolean validUpdate = false;
      validUpdate |= equipmentSupervisionStatus.containsKey(supervisionEvent.getEntityId());
      validUpdate |= processSupervisionStatus.containsKey(supervisionEvent.getEntityId());

      if (validUpdate) {
        SupervisionEvent oldEvent;
        switch (supervisionEvent.getEntity()) {
          case PROCESS:
            oldEvent = processSupervisionStatus.put(supervisionEvent.getEntityId(), supervisionEvent);
            updateProcessStatus();
            break;
          case EQUIPMENT:
            oldEvent = equipmentSupervisionStatus.put(supervisionEvent.getEntityId(), supervisionEvent);
            updateEquipmentStatus();
            break;
          default:
            String errorMsg = "The supervision event type " + supervisionEvent.getEntity() + " is not supported.";
            LOG.error("update(SupervisionEvent) - " + errorMsg);
            throw new IllegalArgumentException(errorMsg);
        }
        
        if (oldEvent == null || !supervisionEvent.equals(oldEvent)) {
          // Notify all listeners of the update
          notifyListeners();
        }
      }
    }
    finally {
      updateTagLock.writeLock().unlock();
    }
  }
  
  /**
   * Creates a XML representation of this class by making use of
   * the simpleframework XML library.
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
      } catch (Exception e) {
          e.printStackTrace();
      } finally {
          if (fw != null) {
              try {
                  fw.close();
              } catch (IOException e) {
                  e.printStackTrace();
              }
          }
      }
      return result;
  }
  
  /**
   * Static method for creating a <code>ClientDataTagImpl</code> object
   * from a XML String by making use of the simpleframework XML library.
   * @param xml The XML representation of a <code>ClientDataTagImpl</code> object
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
