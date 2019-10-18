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
package cern.c2mon.server.common.tag;

import cern.c2mon.server.common.AbstractCacheableImpl;
import cern.c2mon.server.common.metadata.Metadata;
import cern.c2mon.shared.common.datatag.DataTagConstants;
import cern.c2mon.shared.common.datatag.DataTagQuality;
import cern.c2mon.shared.common.datatag.DataTagQualityImpl;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import static cern.c2mon.shared.common.datatag.DataTagConstants.*;

/**
 * Abstract tag used as basis for all tag objects in the server:
 * DataTag, ControlTag and RuleTag.
 *
 * @author Mark Brightwell
 */
@Slf4j
@Data
@EqualsAndHashCode(callSuper = true)
public abstract class AbstractTagCacheObject extends AbstractCacheableImpl implements Serializable {

  // TODO remove UID if not needed
  /**
   * UID since serializable
   */
  private static final long serialVersionUID = 8210576749860621700L;

  /**
   * Maximum length of the value description. If the user tries to send a
   * longer value description, it will be truncated.
   */
  private static final int MAX_DESC_LENGTH = 500;

  /**
   * Unique tag name.
   */
  private String name;

  /**
   * Free-text description of the tag
   */
  private String description;

  /**
   * Expected data type for the tag's value
   */
  private String dataType;

  /**
   * Indicates whether a tag is "in operation", "in maintenance" or "in test".
   */
  private short mode;

  /**
   * Indicates whether a tag has been reconfigured and is awaiting a DAQ restart,
   * or cannot be trusted due to a reconfiguration error.
   */
  private DataTagConstants.Status status;

  /**
   * Indicates whether this tag's value changes shall be logged to the
   * history.
   */
  private boolean logged;

  /**
   * Unit of the tag's value. This parameter is defined at configuration time
   * and doesn't change during run-time. It is mainly used for analogue values
   * that may represent e.g. a flow in "m3", a voltage in "kV" etc.
   */
  private String unit;

  /**
   * The meta data of the Tag. The meta data can be arbitrary and of of the type String, Number and Boolean.
   * Not every Tag needs to have a meta data. Also the meta data don't have to be every time the same.
   */
  private Metadata metadata;

  /**
   * DIP address for tags published on DIP
   */
  private String dipAddress;

  /**
   * JAPC address for tags published on JAPC
   */
  private String japcAddress;

  /**
   * Current value of the datatag (if any, null before first value reception). The value is of type
   * Boolean, Float, String etc. as indicated in the {@link #dataType} field (and is cloneable with a
   * shallow copy).
   */
  private Object value;

  /**
   * Description of the tag's current value (if any)
   */
  private String valueDescription;

  /**
   * Quality of the tag's current value.
   * Assumed non-null within server.
   */
  private DataTagQuality dataTagQuality;

  /**
   * Flag indicating that the current value of this DataTag is the result of a
   * simulation. In that case, the value will neither be logged nor persisted to
   * the entity bean.
   */
  //TODO replace with new OVERRIDDEN mode
  private boolean simulated;

  /**
   * Identifiers of all alarms attached to the datatag
   */
  private Collection<Long> alarmIds;

  /**
   * Identifiers of all rules attached to the datatag
   * (tests will fail if not initialized as this field
   * is set during cache loading from DB).
   */
  private Collection<Long> ruleIds;

  /**
   * String of rules ids obtained from the database;
   * this string is then parsed and saved in the ruleIds
   * field
   */
  private String ruleIdsString;

  /**
   * This private member is set when a DataTagCacheObject is updated or
   * invalidated. It is used when the changes are persisted to the DataTag
   * entity bean in order to avoid updating fields that have not changed.
   *
   * TODO remove these if not used
   */
  protected short tagChange = CHANGE_NONE;

  public static final short CHANGE_NONE = 0;
  public static final short CHANGE_UPDATE = 1;
  public static final short CHANGE_INVALIDATE = 2;
  public static final short CHANGE_CONFIGURATION = 3;


  /**
   * Synchronization locks
   *
   * These are excluded from the equals and hashcode methods, {@code ReentrantReadWriteLock}
   * does NOT provide an equals and hashcode implementation by itself, which causes the
   * {@code CommandTagCacheObject#equals} to always return false
   * The reasoning seems to be similar to this
   * <a href=https://stackoverflow.com/questions/7567502/why-are-two-atomicintegers-never-equal>SO Discussion
   * on why AtomicInteger has no equals</a>
   */
  @EqualsAndHashCode.Exclude
  private ReadLock readLock;
  @EqualsAndHashCode.Exclude
  private WriteLock writeLock;

  /**
   * Default public constructor.
   *
   * Sets the default value of the quality to UNINITIALISED with no
   * description; the description should be added at a later stage
   * with information about this tag creation.
   */
  protected AbstractTagCacheObject() {
    super();
    //TODO check this - done by config loader
    ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    readLock = lock.readLock();
    writeLock = lock.writeLock();

    dataTagQuality = new DataTagQualityImpl();
    alarmIds = new ArrayList<>();
    ruleIds = new ArrayList<>();
    cacheTimestamp = new Timestamp(System.currentTimeMillis());
    metadata = new Metadata();
  }

  /**
   * Constructor used in implementations of the class.
   *
   * @param id
   */
  protected AbstractTagCacheObject(final Long id) {
    this();
    this.id = id;
  }

  /**
   * The clone is provided with <b>new</b> locks: these do not lock access
   * to the object residing in the cache (the clone is no longer in the
   * cache).
   */
  @Override
  public AbstractTagCacheObject clone() {
    AbstractTagCacheObject cacheObject = (AbstractTagCacheObject) super.clone();
    ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    cacheObject.readLock = lock.readLock();
    cacheObject.writeLock = lock.writeLock();
    if (dataTagQuality != null) {
      cacheObject.dataTagQuality = dataTagQuality.clone();
    }
    cacheObject.alarmIds = (ArrayList<Long>) ((ArrayList<Long>) alarmIds).clone();
    cacheObject.ruleIds = (ArrayList<Long>) ((ArrayList<Long>) ruleIds).clone();
    return cacheObject;
  }

  /**
   * Returns an own copy of the list of rules that need evaluating when
   * this tag changes.
   *
   * @return list of rule ids
   */
  public final Collection<Long> getCopyRuleIds() {
    readLock.lock();
    try {
      return new ArrayList<>(ruleIds);
    } finally {
      readLock.unlock();
    }
  }

  /**
   * Add a new rule to the collection of rules that need to be evaluated when
   * THIS tags changes.
   */
  public final boolean addRuleId(final Long pId) {
    if (!this.ruleIds.contains(pId)) {
      this.ruleIds.add(pId);
      this.tagChange = CHANGE_CONFIGURATION;
      return true;
    } else {
      return false;
    }
  }

  /**
   * Remove a rule from the collection of rules that need to be evaluated when
   * THIS tags changes.
   */
  public final boolean removeRuleId(final Long pId) {
    if (this.ruleIds.contains(pId)) {
      this.ruleIds.remove(pId);
      this.tagChange = CHANGE_CONFIGURATION;
      return true;
    } else {
      return false;
    }
  }

  public final boolean isValid() {
    return dataTagQuality.isValid();
  }

  public final boolean isExistingTag() {
    return dataTagQuality.isExistingTag();
  }

  public final Collection<Long> getCopyAlarmIds() {
    readLock.lock();
    try {
      return new ArrayList<>(this.alarmIds);
    } finally {
      readLock.unlock();
    }
  }

  /**
   * Sets the tag name (no longer the topic, which depends on Process)
   *
   * @param name the name to set
   */
  public final void setName(final String name) {
    if (name != null) {
      this.name = name;
    } else {
      throw new IllegalArgumentException("Attempt to set Tag name to null!");
    }
  }

  public final boolean isInOperation() {
    return (mode == MODE_OPERATIONAL);
  }

  public final boolean isInTest() {
    return (mode == MODE_TEST);
  }

  public final boolean isInMaintenance() {
    return (mode == MODE_MAINTENANCE);
  }

  public boolean isInUnconfigured() {
    return (mode == MODE_NOTCONFIGURED);
  }

  public final Metadata getMetadata() {
    if (this.metadata == null) {
      this.metadata = new Metadata();
    }
    return this.metadata;
  }

  public final short getDataTagChange() {
    return this.tagChange;
  }

  /**
   * Sets both the ruleIdsString and ruleIds fields.
   *
   * <p>In this way, the two are always kept consistent
   * with each other. If there are no rules to evaluate,
   * the String field is always null (never empty String)
   * while the Array is an empty array.
   *
   * @param ruleIdsString the ids of rules in which this tag is used
   */
  public void setRuleIdsString(String ruleIdsString) {
    this.ruleIdsString = ruleIdsString;
    try {
      if (ruleIdsString != null && !ruleIdsString.isEmpty()) {
        String[] ruleIdArray = ruleIdsString.split(",");
        setRuleIds(new ArrayList<Long>(ruleIdArray.length));
        for (int i = 0; i != ruleIdArray.length; i++) {
          if (!ruleIdArray[i].equals("")) {
            addRuleId(Long.valueOf(ruleIdArray[i].trim()));
          }
        }
      } else {
        setRuleIds(new ArrayList<>(0));
        this.ruleIdsString = null;
      }
    } catch (Exception e) {
      log.error("Exception caught while parsing the rule string field for tag #{}: " +
          "setting to empty collection", id, e);
      setRuleIds(new ArrayList<>(0));
      this.ruleIdsString = null;
    }

  }

}
