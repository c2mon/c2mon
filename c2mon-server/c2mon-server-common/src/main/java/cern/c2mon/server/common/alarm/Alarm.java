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

import cern.c2mon.server.common.metadata.Metadata;
import cern.c2mon.shared.client.alarm.condition.AlarmCondition;
import cern.c2mon.shared.common.Cacheable;

import java.sql.Timestamp;

/**
 * Interface giving access to the most important attributes of an Alarm
 *
 * @author Michael Berberich
 */
public interface Alarm extends Cacheable {

  String ALARM_INFO_OSC = "[OSC]";


  /**
   * Get the UTC timestamp of the alarm's last state change
   * @return the UTC timestamp of the alarm's last state change
   */
  Timestamp getTriggerTimestamp();

  /**
   * Get the UTC timestamp of the data tag's source (as opposed to the cache timestamp).
   * @return the UTC timestamp of the alarm's source data tag timestamp
   */
  Timestamp getSourceTimestamp();

  /**
   * Get the optional additional info on the alarm that is to be sent to
   * LASER as a "user-defined" fault state property.
   * @return the optional additional info on the alarm
   */
  String getInfo();

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
  Long getDataTagId();

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
   * Get the alarm's metadata.
   *
   * @return The metadata
   */
  Metadata getMetadata();


  /**
   * @return true if the alarm is oscillating.
   */
  boolean isOscillating();

  /**
   * A string representation of the alarm object.
   * This is mainly used for debugging purposes. When calling {@link #toString()} it will call this method
   * with <code>extended=false</code>.
   *
   * @param extended if set to <code>true</code>, the string will contain more information
   *
   * @return The alarm object as string.
   * @see #toString()
   */
  String toString(boolean extended);
}
