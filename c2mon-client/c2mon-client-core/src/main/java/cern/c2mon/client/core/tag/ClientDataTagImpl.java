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
import java.util.ArrayList;
import java.util.Collection;

import lombok.Getter;
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
import cern.c2mon.client.common.tag.Tag;
import cern.c2mon.client.core.jms.TopicRegistrationDetails;
import cern.c2mon.client.core.refactoring.CloneableTagBean;
import cern.c2mon.client.core.refactoring.TagBean;
import cern.c2mon.shared.common.datatag.TagQualityStatus;

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
public class ClientDataTagImpl implements TopicRegistrationDetails {

  @Getter
  @Autowired
  private CloneableTagBean cloneableTagBean;

  /**
   * Default description when the object is not yet initialized
   */
  private static final String DEFAULT_DESCRIPTION = "Tag not initialised.";

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

  private void setUnknown() {
    cloneableTagBean.getDataTagQuality().setInvalidStatus(TagQualityStatus.UNDEFINED_TAG, "Tag is not known by the system");
  }

  @org.simpleframework.xml.core.Persist
  public void prepare() {

    if (cloneableTagBean.getRuleExpression() != null)
      ruleExpressionString = cloneableTagBean.getRuleExpression().getExpression();
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
    getCloneableTagBean().getUpdateTagLock().writeLock().lock();
    try {
      if (log.isTraceEnabled()) {
        log.trace("validate() - Removing " + statusToRemove + " quality status from tag " + this.id);
      }
      if (cloneableTagBean.getDataTagQuality().isInvalidStatusSet(statusToRemove)) {
        // remove the quality status
        cloneableTagBean.getDataTagQuality().removeInvalidStatus(statusToRemove);
        clone = this.clone();
      }
    } finally {
      getCloneableTagBean().getUpdateTagLock().writeLock().unlock();
    }

    if (clone != null) {
      getCloneableTagBean().notifyListeners(clone);
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
    getCloneableTagBean().getUpdateTagLock().writeLock().lock();
    try {
      if (log.isTraceEnabled()) {
        log.trace("invalidate() - Invalidating tag " + this.id + " with quality status " + status);
      }
      // Invalidate the object.
      cloneableTagBean.getDataTagQuality().addInvalidStatus(status, description);

      clone = this.clone();
    } finally {
      getCloneableTagBean().getUpdateTagLock().writeLock().unlock();
    }

    if (clone != null) {
      getCloneableTagBean().notifyListeners(clone);
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
    getCloneableTagBean().getListeners().add(listener);

    Tag clone = null;
    getCloneableTagBean().getUpdateTagLock().readLock().lock();
    try {
      boolean sendInitialUpdate = !TagComparator.compare(cloneableTagBean, initialValue);

      if (sendInitialUpdate) {
        clone = this.clone();
      }
    } finally {
      getCloneableTagBean().getUpdateTagLock().readLock().unlock();
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
    return new ArrayList<>(getCloneableTagBean().getListeners());
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
    boolean isRegistered = getCloneableTagBean().getListeners().contains(pListener);
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
    getCloneableTagBean().getListeners().remove(pListener);
  }

  /**
   * Removes all previously registered <code>DataTagUpdateListener</code>
   */
  public void removeAllUpdateListeners() {
    getCloneableTagBean().getListeners().clear();
  }

  /**
   * Returns information whether the tag has any update listeners registered
   * or not
   *
   * @return <code>true</code>, if this <code>Tag</code> instance has
   * update listeners registered.
   */
  public boolean hasUpdateListeners() {
    boolean isEmpty = !getCloneableTagBean().getListeners().isEmpty();
    return isEmpty;
  }

  /* (non-Javadoc)
   * @see cern.c2mon.client.tag.Tag#getTopicName()
   */
  @Override
  public String getTopicName() {
    getCloneableTagBean().getUpdateTagLock().readLock().lock();
    try {
      if (getCloneableTagBean().getTopicName() != null) {
        return getCloneableTagBean().getTopicName();
      }
      return "";
    } finally {
      getCloneableTagBean().getUpdateTagLock().readLock().unlock();
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

  @SuppressWarnings("unchecked")
  @Override
  public CloneableTagBean clone() {
//    getCloneableTagBean().getUpdateTagLock().readLock().lock();
//    try {
//      ClientDataTagImpl clone = (ClientDataTagImpl) super.clone();
//
//      clone.updateTagLock = new ReentrantReadWriteLock();
//
//      // clone the process id map
//      clone.cloneableTagBean.setProcessSupervisionStatus(new HashMap<>(cloneableTagBean.getProcessSupervisionStatus().size()));
//      for (Entry<Long, SupervisionEvent> entry : cloneableTagBean.getProcessSupervisionStatus().entrySet()) {
//        if (entry.getValue() != null) {
//          clone.cloneableTagBean.getProcessSupervisionStatus().put(entry.getKey(), entry.getValue().clone());
//        }
//        else {
//          clone.cloneableTagBean.getProcessSupervisionStatus().put(entry.getKey(), null);
//        }
//      }
//
//      // clone the equipment id map
//      clone.cloneableTagBean.setEquipmentSupervisionStatus(new HashMap<>(cloneableTagBean.getEquipmentSupervisionStatus().size()));
//      for (Entry<Long, SupervisionEvent> entry : cloneableTagBean.getEquipmentSupervisionStatus().entrySet()) {
//        if (entry.getValue() != null) {
//          clone.cloneableTagBean.getEquipmentSupervisionStatus().put(entry.getKey(), entry.getValue().clone());
//        }
//        else {
//          clone.cloneableTagBean.getEquipmentSupervisionStatus().put(entry.getKey(), null);
//        }
//      }
//
//      // clone the sub equipment id map
//      clone.cloneableTagBean.setSubEquipmentSupervisionStatus(new HashMap<>(cloneableTagBean.getSubEquipmentSupervisionStatus().size()));
//      for (Entry<Long, SupervisionEvent> entry : cloneableTagBean.getSubEquipmentSupervisionStatus().entrySet()) {
//        if (entry.getValue() != null) {
//          clone.cloneableTagBean.getSubEquipmentSupervisionStatus().put(entry.getKey(), entry.getValue().clone());
//        }
//        else {
//          clone.cloneableTagBean.getSubEquipmentSupervisionStatus().put(entry.getKey(), null);
//        }
//      }
//
//      // clone the metadata map - alternative:
//      //clone.metadata = (Map<String, Object>) ((HashMap)this.metadata).clone();
//      //clone.metadata.putAll(metadata);
//      clone.metadata = new HashMap<>();
//      for (Entry<String, Object> entry : metadata.entrySet()) {
//        clone.metadata.put(deepClone(entry.getKey()), deepClone(entry.getValue()));
//      }
//
//      // AlarmsValue objects are immutable
//      clone.cloneableTagBean.setAlarms(new ArrayList<>());
//      for (AlarmValue alarm : cloneableTagBean.getAlarms()) {
//        clone.cloneableTagBean.getAlarms().add(alarm.clone());
//      }
//
//      if (cloneableTagBean.getDataTagQuality() != null) {
//        clone.cloneableTagBean.setTagQuality(cloneableTagBean.getDataTagQuality().clone());
//      }
//      if (cloneableTagBean.getTimestamp() != null) {
//        clone.cloneableTagBean.setSourceTimestamp((Timestamp) cloneableTagBean.getTimestamp().clone());
//      }
//      if (cloneableTagBean.getDaqTimestamp() != null) {
//        clone.cloneableTagBean.setDaqTimestamp((Timestamp) cloneableTagBean.getDaqTimestamp().clone());
//      }
//      if (cloneableTagBean.getServerTimestamp() != null) {
//        clone.cloneableTagBean.setServerTimestamp((Timestamp) cloneableTagBean.getServerTimestamp().clone());
//      }
//      if (cloneableTagBean.getRuleExpression() != null) {
//        clone.cloneableTagBean.setRuleExpression((RuleExpression) cloneableTagBean.getRuleExpression().clone());
//      }
//      clone.listeners = new ConcurrentIdentitySet<>();
//
//      return clone;
//    }
//    catch (CloneNotSupportedException cloneException) {
//      log.error(
//              "clone() - Cloning the ClientDataTagImpl object failed! No update sent to the client.");
//      throw new RuntimeException(cloneException);
//    } finally {
//      updateTagLock.readLock().unlock();
//    }

    return cloneableTagBean.clone();
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
