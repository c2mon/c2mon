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
package cern.c2mon.server.common.supervision;

import cern.c2mon.shared.common.Cacheable;
import cern.c2mon.shared.common.supervision.SupervisionConstants.SupervisionEntity;
import cern.c2mon.shared.common.supervision.SupervisionConstants.SupervisionStatus;

import java.sql.Timestamp;

/**
 * Implemented by cache objects that are supervised by the
 * C2MON server core supervision mechanism.
 *
 * @author Mark Brightwell
 *
 */
public interface Supervised extends Cacheable {

  /**
   * Returns the human-readable name of the cache object.
   *
   * @return the object name
   */
  String getName();

  /**
   * Returns the supervision status of this item at
   * the current time.
   *
   * @return the supervision status
   */
  SupervisionStatus getSupervisionStatus();

  /**
   * Returns the id of the state tag used to publish the
   * status to clients.
   *
   * @return the id of the associated state tag
   */
  Long getStateTagId();

  /**
   * Returns the id of the alive tag used for the supervision
   * mechanism.
   *
   * @return the id of the associated alive tag; returns null if
   * no alive tag is defined (so no supervision can take place)
   */
  Long getAliveTagId();

  /**
   * Returns the alive interval specifying how often an alive
   * message is expected.
   *
   * @return the interval in milliseconds
   */
  Integer getAliveInterval();

  /**
   * Returns the entity name of this supervised object.
   * @return the supervision entity name
   */
  SupervisionEntity getSupervisionEntity();

  /**
   * Returns a reason/description of the current status.
   * @return a description of the status
   */
  String getStatusDescription();

  /**
   * Sets the status of this supervised object.
   * @param supervisionStatus new status
   */
  void setSupervisionStatus(SupervisionStatus supervisionStatus);

  /**
   * Sets the description of the status.
   * @param statusDescription a reason for the current status
   */
  void setStatusDescription(String statusDescription);

  /**
   * Setter.
   * @param supervisionTime time of the supervision event
   */
  void setStatusTime(Timestamp supervisionTime);

  /**
   * Getter.
   * @return the time of the last change in the supervision status
   */
  Timestamp getStatusTime();

  /**
   * Returns true if the object is either running or in
   * the start up phase. And false if either DOWN or STOPPED, or
   * if the status is UNCERTAIN.
   *
   * @return true if it is running (or starting up)
   */
  default boolean isRunning(){
    // Assigning it here keeps us safe from concurrent modifications
    SupervisionStatus status = getSupervisionStatus();
    return status != null
      && (status.equals(SupervisionStatus.STARTUP)
      || status.equals(SupervisionStatus.RUNNING)
      || status.equals(SupervisionStatus.RUNNING_LOCAL));
  }

  /**
   * Returns true only if the object is in UNCERTAIN status.
   *
   * @return true if the status is uncertain
   */
  default boolean isUncertain() {
    return getSupervisionStatus() != null && getSupervisionStatus().equals(SupervisionStatus.UNCERTAIN);
  }

  /**
   * Sets the status of this Supervised object to STARTUP,
   * with associated message.
   * <p>
   * <p>Starts the alive timer if not already running.
   *
   * Careful, this does NOT update the cache entry. You need to explicitly {@code put} for that
   */
  default void start(final Timestamp timestamp) {
    setSupervisionStatus(SupervisionStatus.STARTUP);
    setStatusDescription(getSupervisionEntity() + " " + getName() + " was started");
    setStatusTime(timestamp);
  }

  default void stop(final Timestamp timestamp) {
    setSupervisionStatus(SupervisionStatus.DOWN);
    setStatusTime(timestamp);
    setStatusDescription(getSupervisionEntity() + " " + getName() + " was stopped");
  }

  default void resume(final Timestamp timestamp, final String message) {
    setSupervisionStatus(SupervisionStatus.RUNNING);
    setStatusTime(timestamp);
    setStatusDescription(message);
  }

  default void suspend(final Timestamp timestamp, final String message) {
    setSupervisionStatus(SupervisionStatus.DOWN);
    setStatusDescription(message);
    setStatusTime(timestamp);
  }

}
