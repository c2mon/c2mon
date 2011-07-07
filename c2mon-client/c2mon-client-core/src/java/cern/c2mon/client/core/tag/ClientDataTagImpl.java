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

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.log4j.Logger;

import cern.c2mon.client.core.DataTagUpdateListener;
import cern.c2mon.shared.client.alarm.AlarmValue;
import cern.c2mon.shared.client.tag.TransferTag;
import cern.c2mon.shared.client.tag.TransferTagValue;
import cern.tim.shared.common.datatag.DataTagQuality;
import cern.tim.shared.common.datatag.DataTagQualityImpl;
import cern.tim.shared.common.datatag.TagQualityStatus;
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
public class ClientDataTagImpl implements ClientDataTag, Cloneable {//, ServerMessageListener, {
  
  /** The value of the tag */
  private Object tagValue;

  /** Unique identifier for a DataTag */
  private final Long id;
  
  /** 
   * Containing all process id's which are relevant to compute the
   * final quality status on the C2MON client layer. By definition there
   * is just one id defined. Only rules might have dependencies
   * to multiple processes (DAQs).
   */
  private HashSet<Long> processIds = new HashSet<Long>();
  
  /** 
   * Containing all equipment id's which are relevant to compute the
   * final quality status on the C2MON client layer. By definition there
   * is just one id defined. Only rules might have dependencies
   * to multiple equipments.
   */
  private HashSet<Long> equipmentIds = new HashSet<Long>();
  
  /** The unique name of the tag */
  private String tagName = null;
  
  /** The quality of the tag */
  private DataTagQuality tagQuality = 
    new DataTagQualityImpl(TagQualityStatus.UNINITIALISED, "DataTag not initialised from server.");
  
  /** The alarm objects associated to this data tag */
  private ArrayList<AlarmValue> alarms = new ArrayList<AlarmValue>();
  
  /** The source timestamp that indicates when the value change was generated */
  private Timestamp sourceTimestamp = null;
  
  /** The server timestamp that indicates when the change message passed the server */
  private Timestamp serverTimestamp = null;

  /** Unit of the tag */
  private String unit = null;
  
  /** The current tag value description */
  private String description = "DataTag not initialised from server.";
  
  /**
   * String representation of the JMS destination where the DataTag 
   * is published on change.
   */
  private String topicName = null;

  /** In case this data tag is a rule this variable contains its rule expression */
  private RuleExpression ruleExpression = null;


  /**
   * List of DataTagUpdateListeners registered for updates on this DataTag
   */
  private List<DataTagUpdateListener> listeners = new ArrayList<DataTagUpdateListener>();

  /** Thread lock for access to the <code>DataTagUpdateListener</code> list */
  private final ReentrantReadWriteLock listenersLock = new ReentrantReadWriteLock();
  
  /** Lock to prevent more than one thread at a time to update the value */
  private final ReentrantReadWriteLock updateTagLock = new ReentrantReadWriteLock();
  
  /** Log4j instance */
  private static final Logger LOG = Logger.getLogger(ClientDataTagImpl.class);


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

  
  /* (non-Javadoc)
   * @see cern.c2mon.client.tag.ClientDataTag#getId()
   */  
  public Long getId() {
    return this.id;
  }

  /* (non-Javadoc)
   * @see cern.c2mon.client.tag.ClientDataTag#getName()
   */  
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

  /* (non-Javadoc)
   * @see cern.c2mon.client.tag.ClientDataTag#getValue()
   */  
  public Object getValue() {
    updateTagLock.readLock().lock();
    try {
      return tagValue;
    }
    finally {
      updateTagLock.readLock().unlock();
    }
  }


  /* (non-Javadoc)
   * @see cern.c2mon.client.tag.ClientDataTag#getSourceTimestamp()
   */
  @Override
  public Timestamp getSourceTimestamp() {
    updateTagLock.readLock().lock();
    try { 
      if (sourceTimestamp == null) {
        return new Timestamp(0);
      }
      else {
        return sourceTimestamp;
      }
    }
    finally {
      updateTagLock.readLock().unlock();
    }
  }
  
  
  /* (non-Javadoc)
   * @see cern.c2mon.client.tag.ClientDataTag#getServerTimestamp()
   */
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

  
  /* (non-Javadoc)
   * @see cern.c2mon.client.tag.ClientDataTag#getUnit()
   */
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

  /* (non-Javadoc)
   * @see cern.c2mon.client.tag.ClientDataTag#getType()
   */
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
  
  /* (non-Javadoc)
   * @see cern.c2mon.client.tag.ClientDataTag#getTypeNumeric()
   */
  @Override
  public long getTypeNumeric() {
    updateTagLock.readLock().lock();
    try {
      Class< ? > type = getType();
      if (type == null) {
        return ClientDataTagValue.TYPE_UNKNOWN;
      }
      else {
        return type.hashCode();
      }
    }
    finally {
      updateTagLock.readLock().unlock();
    }
  }


  /* (non-Javadoc)
   * @see cern.c2mon.client.tag.ClientDataTag#getDataTagQuality()
   */
  public DataTagQuality getDataTagQuality() {
    updateTagLock.readLock().lock();
    try {
      return tagQuality;
    }
    finally {
      updateTagLock.readLock().unlock();
    }
  }


  /* (non-Javadoc)
   * @see cern.c2mon.client.tag.ClientDataTag#invalidate(java.lang.String)
   */  
  public void invalidate(final String pDescription) {
    try {
      updateTagLock.writeLock().lock();
      
      if (LOG.isDebugEnabled()) {
        LOG.debug("invalidate() called for tag " + this.id);
      }
      // Invalidate the object.
      tagQuality.addInvalidStatus(TagQualityStatus.INACCESSIBLE, pDescription);
      
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
    listenersLock.readLock().lock();
    try {
      ClientDataTag clone = this.clone();
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
      LOG.error(
          "notifyListeners() - Cloning the ClientDataTagImpl object failed! No update sent to the client.",
          cloneException);
    }
    finally {
      listenersLock.readLock().unlock();
    }
  }


  /* (non-Javadoc)
   * @see cern.c2mon.client.tag.ClientDataTag#addUpdateListener(cern.c2mon.client.tag.DataTagUpdateListener)
   */
  public void addUpdateListener(final DataTagUpdateListener pListener) {
    if (LOG.isDebugEnabled()) {
      LOG.debug("addUpdateListener() called.");
    }
    try {
      listenersLock.writeLock().lock();
      boolean isRegistered = false;
      // Search for pListener by reference
      for (DataTagUpdateListener listener : listeners) {
        if (listener == pListener) {
          isRegistered = true;
          break;
        }
      }
      
      if (!isRegistered) {
        listeners.add(pListener);
      }
    }
    finally {
      listenersLock.writeLock().unlock();
    }
    
    try {
      this.updateTagLock.readLock().lock();
      // TODO: uncomment
//      pListener.onUpdate(this);
    }
    finally {
      this.updateTagLock.readLock().unlock();
    }
  }
  
  /* (non-Javadoc)
   * @see cern.c2mon.client.tag.ClientDataTag#getUpdateListeners()
   */
  @Override
  public Collection<DataTagUpdateListener> getUpdateListeners() {
    try {
      listenersLock.readLock().lock();
      return new ArrayList<DataTagUpdateListener>(listeners);
    }
    finally {
      listenersLock.readLock().unlock();
    }
  }
  
  /* (non-Javadoc)
   * @see cern.c2mon.client.tag.ClientDataTag#isUpdateListenerRegistered(cern.c2mon.client.tag.DataTagUpdateListener)
   */
  public boolean isUpdateListenerRegistered(DataTagUpdateListener pListener) {
    boolean isRegistered = false;
    try {
      listenersLock.readLock().lock();
      isRegistered = listeners.contains(pListener);
    }
    finally {
      listenersLock.readLock().unlock();
    }
    
    return isRegistered;
  }

  /* (non-Javadoc)
   * @see cern.c2mon.client.tag.ClientDataTag#removeUpdateListener(cern.c2mon.client.tag.DataTagUpdateListener)
   */
  public void removeUpdateListener(final DataTagUpdateListener pListener) {
    try {
      listenersLock.writeLock().lock();
      listeners.remove(pListener);
    }
    finally {
      listenersLock.writeLock().unlock();
    }
  }


  
  /* (non-Javadoc)
   * @see cern.c2mon.client.tag.ClientDataTag#hasUpdateListeners()
   */
  public boolean hasUpdateListeners() {
    boolean isEmpty = false;
    try {
      listenersLock.readLock().lock();
      isEmpty = !listeners.isEmpty();
    }
    finally {
      listenersLock.readLock().unlock();
    }
    return isEmpty;
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
  

  /* (non-Javadoc)
   * @see cern.c2mon.client.tag.ClientDataTag#update(cern.c2mon.shared.client.tag.TransferTagValue)
   */
  @Override
  public boolean update(final TransferTagValue transferTagValue) {
    if (transferTagValue != null && transferTagValue.getId().equals(id)) {
      updateTagLock.writeLock().lock();
      try {
        doUpdateValues(transferTagValue);
        // Notify all listeners of the update
        notifyListeners();
      }
      finally {
        updateTagLock.writeLock().unlock();
      }
      return true;
    }
    else {
      return false;
    }
  }
  
  /* (non-Javadoc)
   * @see cern.c2mon.client.tag.ClientDataTag#update(cern.c2mon.shared.client.tag.TransferTag)
   */
  @Override
  public boolean update(final TransferTag transferTag) throws RuleFormatException {
    if (transferTag != null && transferTag.getId().equals(id)) {
      updateTagLock.writeLock().lock();
      try {
        if (transferTag.getRuleExpression() != null) {
          ruleExpression = RuleExpression.createExpression(transferTag.getRuleExpression());
        }
        
        doUpdateValues(transferTag);
        
        processIds.clear();
        processIds.addAll(transferTag.getProcessIds());
        
        equipmentIds.clear();
        equipmentIds.addAll(transferTag.getEquipmentIds());
        
        tagName = transferTag.getName();
        topicName = transferTag.getTopicName();
        unit = transferTag.getUnit();
        
        // Notify all listeners of the update
        notifyListeners();
      }
      finally {
        updateTagLock.writeLock().unlock();
      }
      
      return true;
    }
    else {
      return false;
    }
  }
  
  
  private void doUpdateValues(final TransferTagValue transferTagValue) {
    updateTagQuality(transferTagValue.getDataTagQuality());
    
    alarms.clear();
    alarms.addAll(transferTagValue.getAlarms());
    
    description = transferTagValue.getDescription();
    serverTimestamp = transferTagValue.getServerTimestamp();
    sourceTimestamp = transferTagValue.getSourceTimestamp();
    tagValue = transferTagValue.getValue();
  }
  

// TODO: Refactor!
  /* (non-Javadoc)
   * @see cern.c2mon.client.tag.ClientDataTag#onMessageReceived(java.lang.Object)
   */
//  @Override
  public void onMessageReceived(final Object pPayload) {
//    if (pPayload instanceof DataTag) {
//      this.update((DataTag) pPayload);
//    }
//    else if (pPayload instanceof DataTagValue) {
//      this.update((DataTagValue) pPayload);
//    }
//    else  {
//      LOG.warn("onMessageReceived() : Unable to handle message payload of type : " + pPayload.getClass().getName());
//    }
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
    finally {
      updateTagLock.readLock().unlock();
    }
  }
  
  @SuppressWarnings("unchecked")
  @Override
  public ClientDataTag clone() throws CloneNotSupportedException {
    ClientDataTagImpl clone = (ClientDataTagImpl) super.clone();
    
    clone.processIds = (HashSet<Long>) processIds.clone();
    clone.equipmentIds = (HashSet<Long>) equipmentIds.clone();
    
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
    clone.listeners = new ArrayList<DataTagUpdateListener>();
    
    return clone;    
  }
}

