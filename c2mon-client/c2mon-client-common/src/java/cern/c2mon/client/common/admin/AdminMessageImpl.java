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

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Past;

import cern.c2mon.shared.util.json.GsonFactory;

import com.google.gson.Gson;

/**
 * Implementation of the AdminMessage which is used to send messages to the
 * user, from the administrator
 * 
 * @author vdeila
 * 
 */
public class AdminMessageImpl implements AdminMessage {

  /** Gson parser singleton for that class */
  private static transient Gson gson = null;

  /** the type of message */
  @NotNull
  private AdminMessageType type;

  /** the sender of the message */
  @NotNull
  private String sender;

  /** the message */
  @NotNull
  private String message;

  /** the time of when the message was created */
  @NotNull
  @Past
  private Timestamp timestamp;

  /**
   * Constructor used by Gson
   */
  @SuppressWarnings("unused")
  private AdminMessageImpl() {
    // Do nothing
  };

  /**
   * Constructor
   * 
   * @param type
   *          the type of message
   * @param sender
   *          the sender of the message
   * @param message
   *          the message
   * @param timestamp
   *          the time of when the message was created
   */
  public AdminMessageImpl(final AdminMessageType type, final String sender, final String message, final Timestamp timestamp) {
    this.type = type;
    this.sender = sender;
    this.message = message;
    this.timestamp = timestamp;
  }
  
  /**
   * Copy constructor
   * 
   * @param adminMessage the admin message to copy
   */
  public AdminMessageImpl(final AdminMessage adminMessage) {
    this.type = adminMessage.getType();
    this.sender = adminMessage.getSender();
    this.message = adminMessage.getMessage();
    if (adminMessage.getTimestamp() != null) {
      this.timestamp = (Timestamp) adminMessage.getTimestamp().clone();
    }
    else {
      this.timestamp = null;
    }
  }
  
  @Override
  public AdminMessage clone() throws CloneNotSupportedException {
    AdminMessageImpl clone = (AdminMessageImpl) super.clone();
    if (timestamp != null) {
      clone.timestamp = (Timestamp) timestamp.clone();
    }
    return clone;
  }

  @Override
  public AdminMessageType getType() {
    return this.type;
  }

  @Override
  public String getSender() {
    return this.sender;
  }

  @Override
  public String getMessage() {
    return this.message;
  }

  @Override
  public Timestamp getTimestamp() {
    return this.timestamp;
  }

  @Override
  public String toString() {
    return String.format("%s [message=%s, sender=%s, timestamp=%s, type=%s]", 
        AdminMessageImpl.class.getSimpleName(),
        message, 
        sender, 
        timestamp, 
        type);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((message == null) ? 0 : message.hashCode());
    result = prime * result + ((sender == null) ? 0 : sender.hashCode());
    result = prime * result + ((timestamp == null) ? 0 : timestamp.hashCode());
    result = prime * result + ((type == null) ? 0 : type.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (!(obj instanceof AdminMessageImpl))
      return false;
    AdminMessageImpl other = (AdminMessageImpl) obj;
    if (message == null) {
      if (other.message != null)
        return false;
    }
    else if (!message.equals(other.message))
      return false;
    if (sender == null) {
      if (other.sender != null)
        return false;
    }
    else if (!sender.equals(other.sender))
      return false;
    if (timestamp == null) {
      if (other.timestamp != null)
        return false;
    }
    else if (!timestamp.equals(other.timestamp))
      return false;
    if (type == null) {
      if (other.type != null)
        return false;
    }
    else if (!type.equals(other.type))
      return false;
    return true;
  }
  
  /*
   * GSon methods
   */

  /**
   * @return The Gson parser singleton instance to serialize/deserialize Json
   *         messages of that class
   */
  private static synchronized Gson getGson() {
    if (gson == null) {
      gson = GsonFactory.createGson();
    }
    return gson;
  }

  /**
   * Generates out of this class instance a JSON message
   * 
   * @return The serialized JSON representation of this class instance
   */
  public final String toJson() {
    return getGson().toJson(this);
  }

  /**
   * Deserialized the JSON string into a {@link AdminMessage} object instance
   * 
   * @param json
   *          A JSON string representation of a {@link AdminMessageImpl} class
   * @return The deserialized <code>AdminMessage</code> instance of the JSON
   *         message
   */
  public static final AdminMessage fromJson(final String json) {
    return getGson().fromJson(json, AdminMessageImpl.class);
  }
}
