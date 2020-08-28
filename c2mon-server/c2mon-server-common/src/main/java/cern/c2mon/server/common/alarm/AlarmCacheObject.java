/******************************************************************************
 * Copyright (C) 2010-2019 CERN. All rights not expressly granted are reserved.
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
package cern.c2mon.server.common.alarm;

import java.sql.Timestamp;
import java.util.LinkedList;

import lombok.Data;

import cern.c2mon.server.common.metadata.Metadata;
import cern.c2mon.shared.client.alarm.condition.AlarmCondition;
import cern.c2mon.shared.common.Cacheable;

/**
 * Alarm object held in the cache.
 * <p/>
 * Note: in TIM1 care was taken to make sure this is "" and not null - be
 * careful when sending to LASER as this may be the reason (?)
 *
 * @author Mark Brightwell
 *
 */
@Data
public class AlarmCacheObject implements Cloneable, Cacheable, Alarm {

  /** Serial version UID */
  private static final long serialVersionUID = 794087757524662419L;

  /**
   * Internal identifier of the AlarmCacheObject.
   */
  private Long id;

  /**
   * Unique identifier of the DataTagCacheObject to which the alarm is attached.
   * The Alarm is activated or terminated depending on the current value of this
   * data tag.
   */
  private Long dataTagId;

  /**
   * Fault family of the alarm.
   **/
  private String faultFamily;

  /**
   * Fault member of the alarm.
   **/
  private String faultMember;

  /**
   * Fault code of the alarm.
   **/
  private int faultCode;

  /**
   * AlarmCondition used to determine the alarm's current state
   **/
  private AlarmCondition condition;

  /**
   * The meta data of the Alarm. The meta data can be arbitrary and of of the
   * type String, Numeric and Boolean. Not every Alarm needs to have a meta
   * data. Also the meta data don't have to be every time the same.
   */
  private Metadata metadata;

  /**
   * <code>true</code>, if the alarm state is active as published to listeners
   * (may be forced to <code>true</code> and silenced in case of oscillation)
   */
  private boolean active = false;

  /** Same as the server timestamp of the tag, that triggered the alarm state change */
  private Timestamp timestamp;

  /** This timestamp is taken from the incoming datatag value update */
  private Timestamp sourceTimestamp;

  /**
   * Optional info property
   **/
  private String info;

  ///////////////////////////////////////////////////////////////////////////////
  /////////// VARIABLES REQUIRED FOR DETECTING ALARM OSCILLATION ////////////////
  ///////////////////////////////////////////////////////////////////////////////

  /**
   * <code>true</code> if the alarm state is active as maintained internally. This state is not exposed to listeners
   * and only used for the purpose of detecting and maintaining oscillation.
   * It always reflect the true state of an alarm, regardless of oscillation status
   * (in contrast with the attribute <b>active</b> which may be forced to <code>true</code> if an oscillation is ongoing).
   */
  private boolean internalActive;

  /** Used to keep the n last source timestamps to calculate the oscillation time range */
  private LinkedList<Long> fifoSourceTimestamps = new LinkedList<>();

  /** Set to <code>true</code>, if alarm starts oscillating */
  private boolean oscillating;


  /**
   * Default constructor.
   */
  public AlarmCacheObject() {
    this.timestamp = new Timestamp(0);
    this.sourceTimestamp = timestamp;
    this.info = "";
  }

  /**
   * Constructor setting Alarm id.
   *
   * @param id
   *          the id of the Alarm
   */
  public AlarmCacheObject(final Long id) {
    this();
    this.id = id;
  }

  /**
   * Create a deep clone of this AlarmCacheObject.
   *
   * @return a deep clone of this AlarmCacheObject
   * @throws CloneNotSupportedException
   *           should never be thrown
   */
  @Override
  public AlarmCacheObject clone() throws CloneNotSupportedException {
    AlarmCacheObject alarmCacheObject = (AlarmCacheObject) super.clone();
    if (this.condition != null) {
      alarmCacheObject.condition = (AlarmCondition) this.condition.clone();
    }
    if (this.metadata != null) {
      alarmCacheObject.metadata = this.metadata.clone();
    }
    if (this.timestamp != null) {
      alarmCacheObject.timestamp = (Timestamp) this.timestamp.clone();
    }
    if (this.sourceTimestamp != null) {
      alarmCacheObject.sourceTimestamp = (Timestamp) this.sourceTimestamp.clone();
    }
    return alarmCacheObject;
  }

  @Override
  public final Metadata getMetadata() {
    if (this.metadata == null) {
      this.metadata = new Metadata();
    }
    return this.metadata;
  }

  /**
   * Getter method.
   *
   * @return the unique identifier of the DataTag to which the alarm is attached
   */
  @Override
  public final Long getTagId() {
    return this.dataTagId;
  }

  @Override
  public String toString() {
    return toString(false);
  }

  /**
   * Convert the object to string, with optional extended debug information.
   *
   * @param extended <code>true</code> to obtain debug information.
   * @return A string representation of the object.
   */
  @Override
  public String toString(boolean extended) {
    StringBuilder str = new StringBuilder();

    str.append(getId())
       .append('\t')
       .append(getTagId())
       .append('\t')
       .append(getTimestamp())
       .append('\t')
       .append(getFaultFamily())
       .append('\t')
       .append(getFaultMember())
       .append('\t')
       .append(getFaultCode())
       .append('\t')
       .append(isActive());
    if (getInfo() != null) {
      str.append('\t');
      str.append(getInfo());
    }
    if (extended) {
      str.append("\tinternalActive: ");
      str.append(this.internalActive);
    }

    return str.toString();
  }
}
