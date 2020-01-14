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

import cern.c2mon.client.common.tag.Tag;
import cern.c2mon.client.common.tag.TypeNumeric;
import cern.c2mon.client.core.jms.TopicRegistrationDetails;
import cern.c2mon.shared.client.alarm.AlarmValue;
import cern.c2mon.shared.client.supervision.SupervisionEvent;
import cern.c2mon.shared.client.tag.TagMode;
import cern.c2mon.shared.common.datatag.DataTagQuality;
import cern.c2mon.shared.common.datatag.DataTagQualityImpl;
import cern.c2mon.shared.common.datatag.TagQualityStatus;
import cern.c2mon.shared.rule.RuleExpression;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.io.*;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

/**
 * @author Szymon Halastra
 */
@Slf4j
@Data
@Root(name = "Tag")
public class TagImpl implements Tag, TopicRegistrationDetails, Cloneable {

  /**
   * Unique identifier for a DataTag
   */
  public static final String DEFAULT_DESCRIPTION = "Tag not initialised.";

  /**
   * The value of the tag
   */
  @Getter(AccessLevel.NONE)
  @Element(required = false)
  private Object tagValue;

  /**
   * The datatype of the tag value
   */
  @Element(required = false)
  private Class<?> type;

  /**
   * The current tag mode
   */
  @Element
  private TagMode mode = TagMode.TEST;

  /**
   * <code>true</code>, if the tag value is currently simulated and not
   * corresponding to a live event.
   */
  @Element
  private boolean simulated = false;

  /**
   * Unique identifier for a DataTag
   */
  @Attribute
  protected long id;

  /**
   * Containing all process id's which are relevant to compute the
   * final quality status on the C2MON client layer. By definition there
   * is just one id defined. Only rules might have dependencies
   * to multiple processes (DAQs).
   */
  private Map<Long, SupervisionEvent> processSupervisionStatus = new HashMap<>();

  /**
   * Containing all equipment id's which are relevant to compute the
   * final quality status on the C2MON client layer. By definition there
   * is just one id defined. Only rules might have dependencies
   * to multiple equipments.
   */
  private Map<Long, SupervisionEvent> equipmentSupervisionStatus = new HashMap<>();

  /**
   * Containing all sub equipment id's which are relevant to compute the
   * final quality status on the C2MON client layer. By definition there
   * is just one id defined. Only rules might have dependencies
   * to multiple sub equipments.
   */
  private Map<Long, SupervisionEvent> subEquipmentSupervisionStatus = new HashMap<>();

  /**
   * The unique name of the tag
   */
  @Getter(AccessLevel.NONE)
  @Element(required = false)
  private String tagName = null;

  /**
   * Only used for xml serialization.
   */
  @Element(required = false)
  private String ruleExpressionString;

  /**
   * The quality of the tag
   */
  @Element(required = false)
  private DataTagQuality tagQuality =
          new DataTagQualityImpl(TagQualityStatus.UNINITIALISED, DEFAULT_DESCRIPTION);

  /**
   * <code>true</code>, if tag represents an Alive Control tag
   */
  protected boolean aliveTagFlag = false;

  /**
   * <code>true</code>, if tag represents a CommFault-, Alive- or Status tag
   */
  protected boolean controlTagFlag = false;


  /**
   * String representation of the JMS destination where the DataTag
   * is published on change.
   */
  @Element(required = false)
  protected String topicName = null;

  /**
   * The alarm objects associated to this data tag
   */
  @ElementList
  private ArrayList<AlarmValue> alarms = new ArrayList<>();

  /**
   * The source timestamp that indicates when the value change was generated
   */
  @Element(required = false)
  private Timestamp sourceTimestamp = null;

  /**
   * The DAQ timestamp that indicates when the change message passed the DAQ module
   */
  @Element(required = false)
  private Timestamp daqTimestamp = null;

  /**
   * The server timestamp that indicates when the change message passed the server
   */
  @Element
  private Timestamp serverTimestamp = new Timestamp(0L);

  /**
   * Unit of the tag
   */
  @Element(required = false)
  private String unit = null;

  /**
   * The description of the Tag
   */
  @Element(required = false)
  private String description = "";

  /**
   * The description of the value
   */
  @Element(required = false)
  private String valueDescription = "";

  /**
   * In case this data tag is a rule this variable contains its rule expression
   */
  private RuleExpression ruleExpression = null;

  /**
   * Metadata of an Tag object.
   */
  private Map<String, Object> metadata = new HashMap<>();

  /**
   * Lock to prevent more than one thread at a time to update the value
   */
  private ReentrantReadWriteLock updateTagLock = new ReentrantReadWriteLock();

  /**
   * Protected default constructor that initializes the tag id with -1L
   */
  protected TagImpl() {
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
  public TagImpl(final Long tagId) {
    this.id = tagId;
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
  public TagImpl(final Long tagId, boolean unknown) {
    id = tagId;

    if (unknown) {
      setUnknown();
    }
  }

  protected void setUnknown() {
    getDataTagQuality().setInvalidStatus(TagQualityStatus.UNDEFINED_TAG, "Tag is not known by the system");
  }

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
    } finally {
      updateTagLock.readLock().unlock();
    }
  }

  @Override
  public TagMode getMode() {
    updateTagLock.readLock().lock();
    try {
      return mode;
    } finally {
      updateTagLock.readLock().unlock();
    }
  }

  @Override
  public boolean isSimulated() {
    updateTagLock.readLock().lock();
    try {
      return simulated;
    } finally {
      updateTagLock.readLock().unlock();
    }
  }

  @Override
  public boolean isValid() {
    updateTagLock.readLock().lock();
    try {
      return tagQuality.isValid();
    } finally {
      updateTagLock.readLock().unlock();
    }
  }

  @Override
  public DataTagQuality getDataTagQuality() {
    updateTagLock.readLock().lock();
    try {
      return tagQuality;
    } finally {
      updateTagLock.readLock().unlock();
    }
  }

  @Override
  public Object getValue() {
    updateTagLock.readLock().lock();
    try {
      return tagValue;
    } finally {
      updateTagLock.readLock().unlock();
    }
  }

  private String getLockedString(String str) {
    updateTagLock.readLock().lock();
    try {
      if (str != null) {
        return str;
      }
      return "";
    } finally {
      updateTagLock.readLock().unlock();
    }
  }

  @Override
  public String getDescription() {
    return getLockedString(description);
  }

  @Override
  public String getValueDescription() {
    return this.getLockedString(valueDescription);
  }

  @Override
  public String getUnit() {
    return this.getLockedString(unit);
  }

  @Override
  public Collection<Long> getAlarmIds() {
    updateTagLock.readLock().lock();
    try {
      return alarms.stream().map(AlarmValue::getId).collect(Collectors.toList());
    } finally {
      updateTagLock.readLock().unlock();
    }
  }

  @Override
  public Collection<Long> getEquipmentIds() {
    return equipmentSupervisionStatus.keySet();
  }

  @Override
  public Collection<Long> getSubEquipmentIds() {
    return subEquipmentSupervisionStatus.keySet();
  }

  @Override
  public Collection<Long> getProcessIds() {
    return processSupervisionStatus.keySet();
  }

  @Override
  public boolean isRuleResult() {
    updateTagLock.readLock().lock();
    try {
      return this.ruleExpression != null;
    } finally {
      updateTagLock.readLock().unlock();
    }
  }

  @Override
  public RuleExpression getRuleExpression() {
    updateTagLock.readLock().lock();
    try {
      return this.ruleExpression;
    } finally {
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
    } finally {
      updateTagLock.readLock().unlock();
    }
  }

  @Override
  public Timestamp getDaqTimestamp() {
    updateTagLock.readLock().lock();
    try {
      return daqTimestamp;
    } finally {
      updateTagLock.readLock().unlock();
    }
  }

  @Override
  public Timestamp getServerTimestamp() {
    updateTagLock.readLock().lock();
    try {
      return serverTimestamp;
    } finally {
      updateTagLock.readLock().unlock();
    }
  }

  @Override
  public TypeNumeric getTypeNumeric() {
    updateTagLock.readLock().lock();
    try {
      Class<?> type = getType();
      if (type != null) {
        int typeNumeric = type.hashCode();
        for (TypeNumeric t : TypeNumeric.values()) {
          if (t.getCode() == typeNumeric) {
            return t;
          }
        }
      }
    } finally {
      updateTagLock.readLock().unlock();
    }

    return TypeNumeric.TYPE_UNKNOWN;
  }

  /**
   * Returns the metadata to the corresponding tag.
   *
   * @return the metadata of the object.
   */
  @Override
  public Map<String, Object> getMetadata() {
    return this.metadata;
  }

  @Override
  public final boolean isAliveTag() {
    return aliveTagFlag;
  }

  @Override
  public final boolean isControlTag() {
    return controlTagFlag;
  }

  @Override
  public String getTopicName() {
    updateTagLock.readLock().lock();
    try {
      if (this.topicName != null) {
        return this.topicName;
      }
      return "";
    } finally {
      updateTagLock.readLock().unlock();
    }
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
  public TagImpl clone() {
    updateTagLock.readLock().lock();
    try {
      TagImpl clone = (TagImpl) super.clone();

      clone.updateTagLock = new ReentrantReadWriteLock();

      // clone the process id map
      clone.processSupervisionStatus = new HashMap<>(processSupervisionStatus.size());
      for (Map.Entry<Long, SupervisionEvent> entry : processSupervisionStatus.entrySet()) {
        if (entry.getValue() != null) {
          clone.processSupervisionStatus.put(entry.getKey(), entry.getValue().clone());
        }
        else {
          clone.processSupervisionStatus.put(entry.getKey(), null);
        }
      }

      // clone the equipment id map
      clone.equipmentSupervisionStatus = new HashMap<>(equipmentSupervisionStatus.size());
      for (Map.Entry<Long, SupervisionEvent> entry : equipmentSupervisionStatus.entrySet()) {
        if (entry.getValue() != null) {
          clone.equipmentSupervisionStatus.put(entry.getKey(), entry.getValue().clone());
        }
        else {
          clone.equipmentSupervisionStatus.put(entry.getKey(), null);
        }
      }

      // clone the sub equipment id map
      clone.subEquipmentSupervisionStatus = new HashMap<>(subEquipmentSupervisionStatus.size());
      for (Map.Entry<Long, SupervisionEvent> entry : subEquipmentSupervisionStatus.entrySet()) {
        if (entry.getValue() != null) {
          clone.subEquipmentSupervisionStatus.put(entry.getKey(), entry.getValue().clone());
        }
        else {
          clone.subEquipmentSupervisionStatus.put(entry.getKey(), null);
        }
      }

      // clone the metadata map - alternative:
      //clone.metadata = (Map<String, Object>) ((HashMap)this.metadata).clone();
      //clone.metadata.putAll(metadata);
      clone.metadata = new HashMap<>();
      for(Map.Entry<String, Object> entry : metadata.entrySet()) {
        clone.metadata.put(deepClone(entry.getKey()), deepClone(entry.getValue()));
      }

      // AlarmsValue objects are immutable
      clone.alarms = new ArrayList<>();
      for(AlarmValue alarm : alarms){
        clone.alarms.add(alarm.clone());
      }

      if (tagQuality != null) {
        clone.tagQuality = tagQuality.clone();
      }
      if (sourceTimestamp != null) {
        clone.sourceTimestamp = (Timestamp) sourceTimestamp.clone();
      }
      if (daqTimestamp != null) {
        clone.daqTimestamp = (Timestamp) daqTimestamp.clone();
      }
      if (serverTimestamp != null) {
        clone.serverTimestamp = (Timestamp) serverTimestamp.clone();
      }
      if (ruleExpression != null) {
        clone.ruleExpression = (RuleExpression) ruleExpression.clone();
      }
//      clone.listeners = new ConcurrentIdentitySet<>();

      return clone;
    }
    catch (CloneNotSupportedException cloneException) {
      log.error(
              "clone() - Cloning the TagImpl object failed! No update sent to the client.");
      throw new RuntimeException(cloneException);
    }
    finally {
      updateTagLock.readLock().unlock();
    }
  }

  @Override
  public int hashCode() {
    return Long.hashCode(id);
  }

  @Override
  public boolean equals(Object pRight) {
    if (pRight instanceof TagImpl) {
      if (this.id == ((TagImpl) pRight).id) {
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
}
