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

package cern.c2mon.server.common.alarm;

import java.sql.Timestamp;

import cern.c2mon.server.common.metadata.Metadata;
import cern.c2mon.shared.common.Cacheable;

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
  Timestamp getTimestamp();
  
  /**
   * Get the UTC timestamp of the data tag's source
   */
  Timestamp getSourceTimestamp();

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
  @Override
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
   * Get the alarm's metadata.
   *
   * @return The metadata
   */
  Metadata getMetadata();

  
  /**
   * @return true if the alarm is oscillating.
   */
  boolean isOscillating();
}
