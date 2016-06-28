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
package cern.c2mon.client.common.admin;

import java.sql.Timestamp;

import cern.c2mon.shared.client.request.ClientRequestResult;

/**
 * An BroadcastMessage is used to send messages to the user, from an administrator.
 * 
 * @author vdeila
 */
public interface BroadcastMessage extends ClientRequestResult, Cloneable {

  /**
   * @return the type of message
   */
  BroadcastMessageType getType();

  /**
   * @return the sender of the message
   */
  String getSender();

  /**
   * @return the message
   */
  String getMessage();

  /**
   * @return the time of when the message was created
   */
  Timestamp getTimestamp();

  /**
   * Clones the instance of that interface
   * 
   * @return A clone of the object
   * @throws CloneNotSupportedException
   *           Thrown in case that one of the fields of the interface
   *           implementation is not cloneable.
   */
  BroadcastMessage clone() throws CloneNotSupportedException;

  /** The types of messages */
  enum BroadcastMessageType {
    /** Only information */
    INFO,
    /** A warning message */
    WARN
  }

}
