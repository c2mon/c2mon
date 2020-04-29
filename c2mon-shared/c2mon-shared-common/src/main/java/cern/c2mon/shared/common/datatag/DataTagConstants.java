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
package cern.c2mon.shared.common.datatag;

import javax.jms.Message;

/**
 * Constants used through the TIM system.
 *
 * @author Mark Brightwell
 *
 */
public interface DataTagConstants {

  /**
   * The status of a entity in the server. Currently only indicates
   * that an entity has been reconfigured and requires a DAQ restart
   * to be operational, or that an error was detected during a
   * reconfiguation and needs looking at in more detail.
   *
   * <p>Status flags should be independent of incoming values and
   * related to the state of the system. The flag is currently
   * only applied to Tags.
   *
   * <p>The status of a tag should not be influenced by incoming valid
   * or invalid tags. Rather it should indicate that some action needs
   * to be taken on the server or DAQ level before it can return to status
   * OK. This is to be contrasted with the Quality of a tag, which is
   * revalidated whenever a valid tag is incoming. The Quality should
   * be thought of as a reflection of the quality at source.
   *
   * ??just flag DAQ with restart flag instead, and use in Process state view??
   */
  public enum Status {OK, REBOOT_REQUIRED, RECONFIGURATION_ERROR}

  // ----------------------------------------------------------------------------
  // MODE CONSTANT DEFINITIONS
  // ----------------------------------------------------------------------------
  /**
   * Modes are operator/user set details on the operational status of a
   * point in the system. The value is set at configuration and is not
   * modified at other times by the server.
   */
  public static final short MODE_OPERATIONAL = 0;
  public static final short MODE_MAINTENANCE = 1;
  public static final short MODE_TEST = 2;

  /**
   * Indicates that a tag is not configured in the database
   * and is being forwarded from a DAQ running a local configuration.
   * TODO adjust this: Such tags will be put in the cache and history but not cache-persisted
   * to the database.
   */
  public static final short MODE_NOTCONFIGURED = 3;

  // ----------------------------------------------------------------------------
  // PRIORITY CONSTANT DEFINITIONS
  // ----------------------------------------------------------------------------
  /**
   * Tags with priority set to PRIORITY_LOW can be grouped together in
   * larger DataTagValueUpdate messages in order to decrease the number
   * of DartaTagValueUpdate messages sent to the server queue. This may cause
   * a certain delay in processing.
   * <p>
   * The exact delay is configured is the DAQ properties.
   */
  public static final int PRIORITY_LOW = 2;

  /**
   * Tags with priority set to PRIORITY_MEDIUM can be grouped together in
   * larger DataTagValueUpdate messages in order to decrease the number
   * of DartaTagValueUpdate messages sent to the server queue. This may cause
   * a certain delay in processing.
   * <p>
   * The exact delay is configured is the DAQ properties.
   */
  public static final int PRIORITY_MEDIUM = Message.DEFAULT_PRIORITY;

  /**
   * Tags with priority set to PRIORITY_HIGHEST can be grouped together in
   * larger DataTagValueUpdate messages in order to decrease the number
   * of DartaTagValueUpdate messages sent to the server queue. This may cause
   * a certain delay in processing, but is much less than on {@link DataTagConstants#PRIORITY_LOW}
   * and {@link DataTagConstants#PRIORITY_MEDIUM}
   * <p>
   * The exact delay is configured is the DAQ properties.
   */
  public static final int PRIORITY_HIGH = 7;
  
  /**
   * This flag is reserved for control tags, such as Alive and COMM_FAULT Tags.
   * Tags with priority set to PRIORITY_HIGHEST must be treated by the driver
   * without delay. Value updates must be notified to the server immediately.
   * The JMS priority of the DataTagValueUpdate message must also be set
   * to PRIORITY_HIGHEST.
   */
  public static final int PRIORITY_HIGHEST = 9;

  // ----------------------------------------------------------------------------
  // TRANSFORMATION CONSTANT DEFINITIONS
  // ----------------------------------------------------------------------------
  /**
   * Constant to be used to disable transformation for numeric values.
   * <PRE>setTransformationFactory(TRANSFORMATION_NONE);</PRE>
   */
  public static final float TRANSFORMATION_NONE = 1f;

  // ----------------------------------------------------------------------------
  // TIME-TO-LIVE CONSTANT DEFINITIONS
  // ----------------------------------------------------------------------------
  /**
   * Constant to be used to set an unlimited time-to-live for a
   * DataTagValueUpdate message to be sent to the application server.
   * <PRE>setTimeToLive(TTL_FOREVER);</PRE>
   */
  public static final short TTL_FOREVER = 0;


}
