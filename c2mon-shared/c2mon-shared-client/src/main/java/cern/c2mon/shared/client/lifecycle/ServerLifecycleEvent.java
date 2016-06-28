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
package cern.c2mon.shared.client.lifecycle;

import java.sql.Timestamp;

/**
 * Server start/stop event.
 * 
 * @author Mark Brightwell
 *
 */
public class ServerLifecycleEvent {
  
  /**
   * Time of event.
   */
  private Timestamp eventTime;
  
  /**
   * Name of server.
   */
  private String serverName;
  
  /**
   * Type of this event.
   */
  private LifecycleEventType eventType;

  /**
   * Default constructor.
   */
  public ServerLifecycleEvent() {
    super();
  }

  /**
   * Constructor.
   * @param eventTime time of event
   * @param serverName name of server
   * @param eventType type of event
   */
  public ServerLifecycleEvent(final Timestamp eventTime, final String serverName, final LifecycleEventType eventType) {
    super();
    this.eventTime = eventTime;
    this.serverName = serverName;
    this.eventType = eventType;
  }

  /**
   * @return the eventTime
   */
  public Timestamp getEventTime() {
    return eventTime;
  }

  /**
   * @param eventTime the eventTime to set
   */
  public void setEventTime(final Timestamp eventTime) {
    this.eventTime = eventTime;
  }

  /**
   * @return the serverName
   */
  public String getServerName() {
    return serverName;
  }

  /**
   * @param serverName the serverName to set
   */
  public void setServerName(final String serverName) {
    this.serverName = serverName;
  }

  /**
   * @return the eventType
   */
  public LifecycleEventType getEventType() {
    return eventType;
  }

  /**
   * @param eventType the eventType to set
   */
  public void setEventType(final LifecycleEventType eventType) {
    this.eventType = eventType;
  }
  
}
