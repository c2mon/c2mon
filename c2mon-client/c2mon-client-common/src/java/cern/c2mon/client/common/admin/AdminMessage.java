/******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2004 - 2011 CERN This program is free software; you can
 * redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version. This program is distributed
 * in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received
 * a copy of the GNU General Public License along with this program; if not,
 * write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA 02111-1307, USA.
 * 
 * Author: TIM team, tim.support@cern.ch
 *****************************************************************************/
package cern.c2mon.client.common.admin;

import java.sql.Timestamp;

import cern.c2mon.shared.client.request.ClientRequestResult;

/**
 * An AdminMessage is used to send messages to the user, from an administrator.
 * 
 * @author vdeila
 */
public interface AdminMessage extends ClientRequestResult, Cloneable {

  /**
   * @return the type of message
   */
  AdminMessageType getType();

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
  AdminMessage clone() throws CloneNotSupportedException;

  /** The types of messages */
  enum AdminMessageType {
    /** Only information */
    INFO,
    /** A warning message */
    WARN
  }

}
