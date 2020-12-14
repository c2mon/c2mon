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
package cern.c2mon.shared.client.alarm;

import java.sql.Timestamp;
import java.util.Map;

import cern.c2mon.shared.client.alarm.condition.AlarmCondition;
import cern.c2mon.shared.client.request.ClientRequestResult;

/**
 * Alarm value interface
 *
 * @author Matthias Braeger
 */
public interface AlarmValue extends ClientRequestResult {

  /**
   * Get the alarm's unique identifier
   * @return the alarm's unique identifier
   */
  Long getId();

  /**
   * Get the UTC timestamp of the alarm's last state change
   * @return the UTC timestamp of the alarm's last state change
   */
  Timestamp getTimestamp();

  /** UTC timestamp of the alarm's source tag timestamp
   * @return the UTC timestamp of the alarm's source tag timestamp
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
  Long getTagId();

  Map<String, Object> getMetadata();

  /**
   * @return The description of the Tag
   * @deprecated Please use the Tag object instead
   */
  @Deprecated
  String getTagDescription();

  /**
   * @return true if the alarm is currently active.
   */
  boolean isActive();


  /**
   * @return <code>true</code>, if oscillation control got activated for that alarm on the server side
   */
  boolean isOscillating();

  /**
   * XML representation of the {@link AlarmCondition} that was used to evaluate the alarm
   */
  String getAlarmConditionXml();

  /**
   * @return A description for user of the alarm condition, provided by {@link AlarmCondition#getDescription()}
   */
  String getAlarmConditionDescription();

  /**
   * @return true if this.alarm is more recent.
   */
  boolean isMoreRecentThan(AlarmValue alarm);

  /**
   * @return A clone of the object
   * @throws CloneNotSupportedException Thrown, if a field is not clonable
   */
  AlarmValue clone() throws CloneNotSupportedException;
}
