/******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2009 CERN This program is free software; you can redistribute
 * it and/or modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of the License,
 * or (at your option) any later version. This program is distributed in the
 * hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See
 * the GNU General Public License for more details. You should have received a
 * copy of the GNU General Public License along with this program; if not, write
 * to the Free Software Foundation, Inc., 59 Temple Place - Suite 330, Boston,
 * MA 02111-1307, USA.
 * 
 * Author: TIM team, tim.support@cern.ch
 *****************************************************************************/
package cern.c2mon.server.common.alarm;

import java.sql.Timestamp;

import cern.c2mon.shared.common.Cacheable;
import cern.c2mon.server.common.alarm.AlarmCondition;

/**
 * Alarm object held in the cache.
 * 
 * Imported more or less as-is into C2MON.
 * 
 * Note: in TIM1 care was taken to make sure this is "" and not null - be careful when sending to LASER as this may be the reason (?)
 * 
 * @author Mark Brightwell
 * 
 */

public class AlarmCacheObject implements Cloneable, Cacheable, Alarm {

  /** Serial version UID */
  private static final long serialVersionUID = 794087757524662419L;
    
  /**
   * This enum contains all the possible change state values
   * which an alarm object can have.
   */
  public enum AlarmChangeState {
    /** Alarm state hasn't changed since. */
    CHANGE_NONE,
    /** The alarm's state has changed. */
    CHANGE_STATE,
    /** The additional information about the alarm has changed since the last update call. */
    CHANGE_PROPERTIES
  };

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
   * LASER fault family of the alarm.
   **/
  private String faultFamily;

  /**
   * LASER fault member of the alarm.
   **/
  private String faultMember;

  /**
   * LASER fault code of the alarm.
   **/
  private int faultCode;

  /**
   * AlarmCondition used to determine the alarm's current state
   **/
  private AlarmCondition condition;

  /** 
   * The alarm's current state 
   **/
  private String state;

  /**
   * Member indicating whether the alarm's state or properties have changed 
   * since the last update.
   */
  private AlarmChangeState alarmChangeState;

   /**
   * Timestamp of the last state change
   **/
  private Timestamp timestamp;
  
  /**
   * Was the current alarm value published?
   */
  private boolean published = false;

  /**
   * Optional info property. 
   * TODO in TIM1 care was taken to make sure this is "" and not null - be careful when sending to LASER as this may be the reason
   **/
  private String info;
  
  /**
   * Latest state to be published.
   */
  private AlarmPublication lastPublication;

  /**
   * Name of the JMS topic on which the alarm will be distributed to clients.
   * TODO remove because single topic now for alarm publication?
   */
  private String topic;
  
  /**
   * Default constructor.
   */
  public AlarmCacheObject() {
    //topic is the same for all alarms so moved here 
    //TODO could set this as static constant if confirmed as constant!
    this.topic = "tim.alarm";
    
    // Initialise run-time parameters with default values 
    // (overwritten on loading if DB has none null values)
    this.state = AlarmCondition.TERMINATE;
    this.alarmChangeState = AlarmChangeState.CHANGE_NONE;
    this.timestamp = new Timestamp(0);
    this.info = "";    
  }

  /**
   * Constructor setting Alarm id.
   * @param id the id of the Alarm
   */
  public AlarmCacheObject(final Long id) {
    this();
    this.id = id;
  }

  /**
   * Create a deep clone of this AlarmCacheObject.
   * 
   * @return a deep clone of this AlarmCacheObject
   * @throws CloneNotSupportedException should never be thrown
   */
  public Object clone() throws CloneNotSupportedException {    
     AlarmCacheObject alarmCacheObject = (AlarmCacheObject) super.clone();
     if (this.condition != null) {
       alarmCacheObject.condition = (AlarmCondition) this.condition.clone();
     }     
     if (this.timestamp != null){
       alarmCacheObject.timestamp = (Timestamp) this.timestamp.clone();
     }
     if (this.lastPublication != null) {
       alarmCacheObject.lastPublication = (AlarmPublication) lastPublication.clone();
     }
     return alarmCacheObject;
  }

  /**
   * Getter method.
   * @return the alarm's unique identifier
   */
  @Override
  public final Long getId() {
    return this.id;
  }

  /**
   * Getter method.
   * @return the unique identifier of the DataTag to which the alarm is attached
   */
  @Override
  public final Long getTagId() {
    return this.dataTagId;
  }

  /**
   * Getter method.
   * @return the alarm's LASER fault family
   */
  @Override
  public final String getFaultFamily() {
    return this.faultFamily;
  }

  /**
   * Getter method.
   * @return the alarm's LASER fault member
   */
  @Override
  public final String getFaultMember() {
    return this.faultMember;
  }

  /**
   * Getter method.
   * @return the alarm's LASER fault code
   */
  @Override
  public final int getFaultCode() {
    return this.faultCode;
  }

  /**
   * Getter method.
   * @return the alarm's current state.
   */
  @Override
  public final String getState() {
    return this.state;
  }

  /**
   * Getter method.
   * @return the UTC timestamp of the alarm's last state change
   */
  @Override
  public final Timestamp getTimestamp() {
    return this.timestamp;
  }

  /**
   * Getter method.
   * @return the optional additional info on the alarm that is to be sent to
   *         LASER as a "user-defined" fault state property.
   */
  @Override
  public final String getInfo() {
    return this.info;
  }

  /**
   * Get the AlarmCondition object associated with this AlarmCacheObject.
   * 
   * @return the AlarmCondition object associated with this AlarmCacheObject
   */
  @Override
  public final AlarmCondition getCondition() {
    return this.condition;
  }

  /**
   * Getter method.
   * @return the TIM topic to publish to to clients on
   */
  public String getTopic() {
    return this.topic;
  }

  /**
   * Checks if the Alarm state is ACTIVE.
   * @return true if the alarm is currently active.
   */
  public boolean isActive() {
    return this.state != null && this.state.equals(AlarmCondition.ACTIVE);
  }

  /**
   * Setter method.
   * @param id the id to set
   */
  public void setId(final Long id) {
    this.id = id;
  }

  /**
   * Setter method.
   * @param datatagId the datatagId to set
   */
  public void setDataTagId(final Long datatagId) {
    this.dataTagId = datatagId;
  }

  /**
   * Setter method.
   * @param faultFamily the faultFamily to set
   */
  public void setFaultFamily(final String faultFamily) {
    this.faultFamily = faultFamily;
  }

  /**
   * Setter method.
   * @param faultMember the faultMember to set
   */
  public void setFaultMember(final String faultMember) {
    this.faultMember = faultMember;
  }

  /**
   * Setter method.
   * @param faultCode the faultCode to set
   */
  public void setFaultCode(final int faultCode) {
    this.faultCode = faultCode;
  }

  /**
   * Setter method.
   * @param condition the condition to set
   */
  public void setCondition(final AlarmCondition condition) {
    this.condition = condition;
  }

  /**
   * Setter method.
   * @param state the state to set
   */
  public void setState(final String state) {
    this.state = state;
  }

    
  @Override
  public AlarmChangeState getAlarmChangeState() {
    return alarmChangeState;
  }
    
  /**
   * Setter method.
   * @param changeState The new alarm state which should be update whenever changes
   *                    on the cache object are applied.
   */
  public final void setAlarmChangeState(final AlarmChangeState changeState) {
    this.alarmChangeState = changeState;
  }

  /**
   * Setter method.
   * 
   * @param timestamp the timestamp to set
   */
  public void setTimestamp(final Timestamp timestamp) {
    this.timestamp = timestamp;
  }

  /**
   * Setter method.
   * @param info the info to set
   */
  public void setInfo(final String info) {
    this.info = info;
  }

  /**
   * Setter method.
   * @param topic the topic to set
   */
  public void setTopic(final String topic) {
    this.topic = topic;
  }
  
  @Override
  public void hasBeenPublished(Timestamp publicationTime) {
    if (publicationTime == null) {
      throw new NullPointerException("Cannot set publication time to null");
    }
    published = true;
    if (state != null) {
      lastPublication = new AlarmPublication(this.state, this.info, publicationTime);
    }    
  }
  
  @Override
  public void notYetPublished() {
    published = false;
  }

  /**
   * @return the last publication
   */
  @Override
  public AlarmPublication getLastPublication() {
    return lastPublication;
  }

  /**
   * @return the published
   */
  public boolean isPublished() {
    return published;
  }

  /**
   * @param published the published to set
   */
  public void setPublished(boolean published) {
    this.published = published;
  }

  @Override
  public boolean isPublishedToLaser() {
    return isPublished();
  }

}
