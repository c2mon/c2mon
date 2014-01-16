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

import cern.c2mon.server.common.alarm.AlarmCacheObject.AlarmChangeState;
import cern.c2mon.shared.common.Cacheable;
import cern.c2mon.server.common.alarm.AlarmCondition;

/**
 * Interface giving access to the most important attributes of an Alarm
 * 
 * @author Michael Berberich
 */
public interface Alarm extends Cacheable {

  /**
   * Get the alarm's current state.
   * @return the alarm's current state
   * @see AlarmCondition for the ACTIVE and TERMINATED constants
   */
  String getState();
  
  /**
   * Get the change state of the alarm cache object
   * @return The cache state enum constant which describes the change state.
   * @see AlarmChangeState
   */
  AlarmChangeState getAlarmChangeState();
  
  /**
   * Get the UTC timestamp of the alarm's last state change
   * @return the UTC timestamp of the alarm's last state change
   */
  Timestamp getTimestamp();
  
  /**
   * Get the optional additional info on the alarm that is to be sent to
   * LASER as a "user-defined" fault state property.
   * @return the optional additional info on the alarm
   */
  String getInfo();
  
  /**
   * Get the alarm's unique identifier
   * @return the alarm's unique identifier
   */
  Long getId();
  
  /**
   * Get the alarm's LASER fault family
   * @return the alarm's LASER fault family
   */
  String getFaultFamily();
  
  /**
   * Get the alarm's LASER fault member
   * @return the alarm's LASER fault member
   */
  String getFaultMember();
  
  /**
   * Get the alarm's LASER fault code
   * @return the alarm's LASER fault code
   */
  int getFaultCode();
  
  /**
   * Get the unique identifier of the Tag to which the alarm is attached
   * @return the unique identifier of the Tag
   */
  Long getTagId();
  
  /**
   * Get the AlarmCondition object associated with this Alarm
   * @return the AlarmCondition object
   */
  AlarmCondition getCondition();
  
  /**
   * @return true if the alarm is currently active.
   */
  boolean isActive();
  
  /** 
   * @return the JMS topic on which the alarm is to be distributed
   */
  String getTopic();

  /**
   * Has the current value of the Alarm been published to LASER?
   * Used for creating the LASER backup.
   * @return
   */
  boolean isPublishedToLaser();

  /**
   * Call this method when the current alarm has been published to LASER.
   * Call within write lock. Currently set state & info are used to remember the
   * latest publication.
   * 
   * @param laserPublicationTime publication time used as LASER user timestamp 
   */
  void hasBeenPublished(Timestamp laserPublicationTime);

  /**
   * Returns details of the previous publication for this alarm. Can
   * be used.
   * @return
   */
  AlarmPublication getLastPublication();

  /**
   * Call to indicate that this Alarm has been freshly updated and not
   * yet published.
   */
  void notYetPublished();
}
