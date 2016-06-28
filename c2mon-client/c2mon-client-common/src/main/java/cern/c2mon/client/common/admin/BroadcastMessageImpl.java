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

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Past;

import cern.c2mon.shared.util.json.GsonFactory;

import com.google.gson.Gson;

/**
 * Implementation of the BroadcastMessage which is used to send messages to the
 * user, from the administrator
 * 
 * @author vdeila
 * 
 */
public class BroadcastMessageImpl implements BroadcastMessage {

  /** Gson parser singleton for that class */
  private static transient Gson gson = null;

  /** the type of message */
  @NotNull
  private BroadcastMessageType type;

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
  private BroadcastMessageImpl() {
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
  public BroadcastMessageImpl(final BroadcastMessageType type, final String sender, final String message, final Timestamp timestamp) {
    this.type = type;
    this.sender = sender;
    this.message = message;
    this.timestamp = timestamp;
  }
  
  /**
   * Copy constructor
   * 
   * @param broadcastMessage the admin message to copy
   */
  public BroadcastMessageImpl(final BroadcastMessage broadcastMessage) {
    this.type = broadcastMessage.getType();
    this.sender = broadcastMessage.getSender();
    this.message = broadcastMessage.getMessage();
    if (broadcastMessage.getTimestamp() != null) {
      this.timestamp = (Timestamp) broadcastMessage.getTimestamp().clone();
    }
    else {
      this.timestamp = null;
    }
  }
  
  @Override
  public BroadcastMessage clone() throws CloneNotSupportedException {
    BroadcastMessageImpl clone = (BroadcastMessageImpl) super.clone();
    if (timestamp != null) {
      clone.timestamp = (Timestamp) timestamp.clone();
    }
    return clone;
  }

  @Override
  public BroadcastMessageType getType() {
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
        BroadcastMessageImpl.class.getSimpleName(),
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
    if (!(obj instanceof BroadcastMessageImpl))
      return false;
    BroadcastMessageImpl other = (BroadcastMessageImpl) obj;
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
   * Deserialized the JSON string into a {@link BroadcastMessage} object instance
   * 
   * @param json
   *          A JSON string representation of a {@link BroadcastMessageImpl} class
   * @return The deserialized <code>BroadcastMessage</code> instance of the JSON
   *         message
   */
  public static final BroadcastMessage fromJson(final String json) {
    return getGson().fromJson(json, BroadcastMessageImpl.class);
  }
}
