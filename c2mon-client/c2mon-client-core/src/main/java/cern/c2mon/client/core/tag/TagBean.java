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

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import lombok.Getter;
import lombok.Setter;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;

import cern.c2mon.client.common.tag.ClientDataTagValue;
import cern.c2mon.client.common.tag.Tag;
import cern.c2mon.client.common.tag.TypeNumeric;
import cern.c2mon.shared.client.alarm.AlarmValue;
import cern.c2mon.shared.client.supervision.SupervisionEvent;
import cern.c2mon.shared.client.tag.TagMode;
import cern.c2mon.shared.common.datatag.DataTagQuality;
import cern.c2mon.shared.common.datatag.DataTagQualityImpl;
import cern.c2mon.shared.common.datatag.TagQualityStatus;
import cern.c2mon.shared.rule.RuleExpression;

/**
 * @author Szymon Halastra
 */
public class TagBean implements ClientDataTagValue, Tag {

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
   * The current tag mode
   */
  @Setter
  @Element
  private TagMode mode = TagMode.TEST;

  /**
   * <code>true</code>, if the tag value is currently simulated and not
   * corresponding to a live event.
   */
  @Setter
  @Element
  private boolean simulated = false;

  /**
   * Unique identifier for a DataTag
   */
  @Attribute
  private Long id;

  /**
   * Containing all process id's which are relevant to compute the
   * final quality status on the C2MON client layer. By definition there
   * is just one id defined. Only rules might have dependencies
   * to multiple processes (DAQs).
   */
  @Getter
  @Setter
  private Map<Long, SupervisionEvent> processSupervisionStatus = new HashMap<>();

  /**
   * Containing all equipment id's which are relevant to compute the
   * final quality status on the C2MON client layer. By definition there
   * is just one id defined. Only rules might have dependencies
   * to multiple equipments.
   */
  @Setter
  @Getter
  private Map<Long, SupervisionEvent> equipmentSupervisionStatus = new HashMap<>();

  /**
   * Containing all sub equipment id's which are relevant to compute the
   * final quality status on the C2MON client layer. By definition there
   * is just one id defined. Only rules might have dependencies
   * to multiple sub equipments.
   */
  @Getter
  @Setter
  private Map<Long, SupervisionEvent> subEquipmentSupervisionStatus = new HashMap<>();

  /**
   * The unique name of the tag
   */
  @Setter
  @Element(required = false)
  private String tagName = null;

  /**
   * The quality of the tag
   */
  @Setter
  @Element(required = false)
  private DataTagQuality tagQuality =
          new DataTagQualityImpl(TagQualityStatus.UNINITIALISED, DEFAULT_DESCRIPTION);

  /**
   * <code>true</code>, if tag represents an Alive Control tag
   */
  private boolean aliveTagFlag = false;

  /**
   * <code>true</code>, if tag represents a CommFault-, Alive- or Status tag
   */
  private boolean controlTagFlag = false;

  /**
   * The alarm objects associated to this data tag
   */
  @ElementList
  @Setter
  private ArrayList<AlarmValue> alarms = new ArrayList<>();

  /**
   * The source timestamp that indicates when the value change was generated
   */
  @Element(required = false)
  @Setter
  private java.sql.Timestamp sourceTimestamp = null;

  /**
   * The DAQ timestamp that indicates when the change message passed the DAQ module
   */
  @Element(required = false)
  @Setter
  private java.sql.Timestamp daqTimestamp = null;

  /**
   * The server timestamp that indicates when the change message passed the server
   */
  @Element
  @Setter
  private java.sql.Timestamp serverTimestamp = new java.sql.Timestamp(0L);

  /**
   * Unit of the tag
   */
  @Setter
  @Element(required = false)
  private String unit = null;

  /**
   * The description of the Tag
   */
  @Setter
  @Element(required = false)
  private String description = "";

  /**
   * The description of the value
   */
  @Setter
  @Element(required = false)
  private String valueDescription = "";

  /**
   * In case this data tag is a rule this variable contains its rule expression
   */
  @Setter
  private RuleExpression ruleExpression = null;

  /**
   * Metadata of an Tag object.
   */
  private Map<String, Object> metadata = new HashMap<>();

  /**
   * Lock to prevent more than one thread at a time to update the value
   */
  private ReentrantReadWriteLock updateTagLock = new ReentrantReadWriteLock();

  /* (non-Javadoc)
   * @see cern.c2mon.client.tag.Tag#getId()
   */
  @Override
  public Long getId() {
    return this.id;
  }

  /* (non-Javadoc)
   * @see cern.c2mon.client.tag.Tag#getName()
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
  public String getValueDescription() {
    return this.getLockedString(description);
  }

  @Override
  public String getUnit() {
    return this.getLockedString(unit);
  }

  /* (non-Javadoc)
   * @see cern.c2mon.client.tag.Tag#getAlarms()
   */
  @Override
  public final Collection<AlarmValue> getAlarms() {
    updateTagLock.readLock().lock();
    try {
      return new ArrayList<AlarmValue>(alarms);
    } finally {
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
    } finally {
      updateTagLock.readLock().unlock();
    }
  }

  @Override
  public Collection<Long> getEquipmentIds() {
    return new ArrayList<Long>(equipmentSupervisionStatus.keySet());
  }

  @Override
  public Collection<Long> getSubEquipmentIds() {
    return new ArrayList<Long>(subEquipmentSupervisionStatus.keySet());
  }

  @Override
  public Collection<Long> getProcessIds() {
    return new ArrayList<Long>(processSupervisionStatus.keySet());
  }

  /* (non-Javadoc)
   * @see cern.c2mon.client.tag.Tag#isRuleResult()
   */
  @Override
  public boolean isRuleResult() {
    updateTagLock.readLock().lock();
    try {
      return this.ruleExpression != null;
    } finally {
      updateTagLock.readLock().unlock();
    }
  }

  /* (non-Javadoc)
   * @see cern.c2mon.client.tag.Tag#getRuleExpression()
   */
  @Override
  public RuleExpression getRuleExpression() {
    updateTagLock.readLock().lock();
    try {
      return this.ruleExpression;
    } finally {
      updateTagLock.readLock().unlock();
    }
  }

  /* (non-Javadoc)
     * @see cern.c2mon.client.tag.Tag#getDescription()
     */
  @Override
  public String getDescription() {
    updateTagLock.readLock().lock();
    try {
      if (this.description != null) {
        return this.description;
      }
      return "";
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
  public final Class<?> getType() {
    updateTagLock.readLock().lock();
    try {
      Class<?> type = null;
      if (this.tagValue != null) {
        type = tagValue.getClass();
      }
      return type;
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

  /**
   * Removes all information from the object.
   * This is in particular interesting for the history mode which sometimes just
   * uses the static information from the live tag object.
   */
  public void clean() {
    updateTagLock.writeLock().lock();
    try {
      this.alarms.clear();
      this.description = DEFAULT_DESCRIPTION;
      this.tagQuality.setInvalidStatus(TagQualityStatus.UNINITIALISED, DEFAULT_DESCRIPTION);
      this.serverTimestamp = new Timestamp(0L);
      this.daqTimestamp = null;
      this.sourceTimestamp = null;
      this.tagValue = null;
      for (Long id : processSupervisionStatus.keySet()) {
        processSupervisionStatus.put(id, null);
      }
      for (Long id : equipmentSupervisionStatus.keySet()) {
        equipmentSupervisionStatus.put(id, null);
      }
      for (Long id : subEquipmentSupervisionStatus.keySet()) {
        subEquipmentSupervisionStatus.put(id, null);
      }
    } finally {
      updateTagLock.writeLock().unlock();
    }
  }
}
