/*******************************************************************************
 * Copyright (C) 2010-2020 CERN. All rights not expressly granted are reserved.
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
 ******************************************************************************/
package cern.c2mon.shared.common.datatag.util;

import javax.jms.Message;

import lombok.AllArgsConstructor;
import lombok.Getter;

import cern.c2mon.shared.common.datatag.DataTagAddress;

/**
 * Definition of the supported JMS message priorities, which are applied when
 * transferring value updates from the DAQ to the C2MON server.
 * <p/>
 * The priority is configurable per DataTag in the {@link DataTagAddress}.
 * <p>
 * Default is {@link #PRIORITY_LOW}
 * 
 * @author Matthias Braeger
 */
@AllArgsConstructor
public enum JmsMessagePriority {
  
  /**
   * Default for all tags.
   * <p/>
   * Tags with priority set to PRIORITY_LOW can be grouped together in
   * larger DataTagValueUpdate messages in order to decrease the number
   * of DartaTagValueUpdate messages sent to the server queue. This may cause
   * a certain delay in processing.
   * <p>
   * The exact delay is configured is the DAQ properties.
   */
  PRIORITY_LOW(2),
  /**
   * Tags with priority set to PRIORITY_MEDIUM can be grouped together in
   * larger DataTagValueUpdate messages in order to decrease the number
   * of DartaTagValueUpdate messages sent to the server queue. This may cause
   * a certain delay in processing.
   * <p>
   * The exact delay is configured is the DAQ properties.
   */
  PRIORITY_MEDIUM(Message.DEFAULT_PRIORITY),
  /**
   * Tags with priority set to PRIORITY_HIGHEST can be grouped together in
   * larger DataTagValueUpdate messages in order to decrease the number
   * of DartaTagValueUpdate messages sent to the server queue. This may cause
   * a certain delay in processing, but is much less than on {@link #PRIORITY_LOW}
   * and {@link #PRIORITY_MEDIUM}
   * <p>
   * The exact delay is configured is the DAQ properties.
   */
  PRIORITY_HIGH(7),
  /**
   * <b>This flag is reserved for control tags, such as Alive and COMM_FAULT Tags.</b>
   * <p/>
   * Tags with priority set to PRIORITY_HIGHEST must be treated by the driver
   * without delay. Value updates must be notified to the server immediately.
   * The JMS priority of the DataTagValueUpdate message must also be set
   * to PRIORITY_HIGHEST.
   */
  PRIORITY_HIGHEST(9);
  
  @Getter
  private final int priority;
  
  /**
   * Returns the message priority for the given priority level
   * @param priorityLevel the JMS priority level
   * @return The corresponding {@link JmsMessagePriority}. Default is {@link #PRIORITY_LOW}
   */
  public static final JmsMessagePriority getJmsMessagePriority(int priorityLevel) {
    for (JmsMessagePriority priority : JmsMessagePriority.values()) {
      if (priority.getPriority() == priorityLevel) {
        return priority;
      }
    }
    
    return JmsMessagePriority.PRIORITY_LOW;
  }
}
