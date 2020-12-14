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
package cern.c2mon.shared.common.datatag.util;

/**
 * Tag Quality status enumeration which is used by the <code>TagQuality</code>
 * interface.
 */
public enum TagQualityStatus {
  /**
   * A tag with this identifier/name is not known to the system.
   */
  UNDEFINED_TAG(0, 4), // former INVALID_TAG flag
  /**
   * The C2MON client API lost the connection to the JMS broker
   */
  JMS_CONNECTION_DOWN(1, 0),
  /**
   * C2MON client API did not receive a valid server heartbeat
   */
  SERVER_HEARTBEAT_EXPIRED(2, 0),
  /**
   * Invalidated because DAQ is down. This should only be used by the server
   * to indicate that the **DAQ** process has been detected as down, such
   * as when it is restarted. This flag will be removed from all tags once
   * the DAQ is detected as running again (when restarted for instance).
   * <p>
   * <b>This quality is only send with a supervision event and has to be
   * added on the C2MON client API to the affected tags.</b>
   */
  PROCESS_DOWN(4, 0),
  /**
   * Used by the supervision mechanism to indicate that the equipment is
   * down.
   * <p>
   * <b>This quality is only send with a supervision event and has to be
   * added on the C2MON client API to the affected tags.</b>
   */
  EQUIPMENT_DOWN(5, 0),
  /**
   * Invalid because the sub-equipment is down (no used so far as tags are
   * never attached to subequipment).
   * <p>
   * <b>This quality is only send with a supervision event and has to be
   * added on the C2MON client API to the affected tags.</b>
   */
  SUBEQUIPMENT_DOWN(6, 0),
  /**
   * A tag's value cannot be determined. This is normally a value cast
   * problem.
   */
  UNDEFINED_VALUE(7, 5), // former VALUE_UNDEFINED flag
  /**
   * The time-to-live of the tag concerned has been reached - the value is
   * considered to be outdated.
   */
  VALUE_EXPIRED(10, 2),
  /**
   * This quality is used for several problems by the DAQs, e.g
   * to inform the server that the data source is unavailable.
   */
  INACCESSIBLE(10, 1),
  /**
   * The value of the tag concerned is outside its defined validity range
   */
  VALUE_OUT_OF_BOUNDS(10, 3),
  /**
   * Reason for invalidity cannot be clearly identified
   */
  UNKNOWN_REASON(10, 6), // former UNKNOWN flag
  /**
   * No value has ever been received for this tag
   */
  UNINITIALISED(99, 0);

  /**
   * The severity of this quality status
   */
  private final transient int severity;

  /**
   * Code used when saving to the DB
   */
  private final transient int code;

  /**
   * Default Constructor
   *
   * @param severity The severity of the quality status. Zero (0) is the
   *                 highest severity number.
   * @param code     Used for storing code to logging DB (as powers of 2)
   *                 with the exception of UNINITIALISED, 0 indicates it is not stored in history
   */
  TagQualityStatus(final int severity, final int code) {
    this.severity = severity;
    this.code = code;
  }

  /**
   * @return The severity of the status. Zero (0) is the
   * highest severity.
   */
  public int getSeverity() {
    return severity;
  }

  /**
   * Getter method.
   *
   * @return the code used in the history for storing the quality status
   */
  public int getCode() {
    return code;
  }

}
