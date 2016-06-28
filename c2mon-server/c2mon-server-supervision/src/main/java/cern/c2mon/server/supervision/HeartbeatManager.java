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
package cern.c2mon.server.supervision;

/**
 * The HeartbeatManager bean generates regular server heartbeats, which
 * can then be used by clients for monitoring the alive status of the
 * server.
 * 
 * <p>The heartbeat is published directly on a JMS topic. Server modules can
 * also register as listeners for internal heatbeat notifications.
 * 
 * @author Mark Brightwell
 *
 */
public interface HeartbeatManager {

  /**
   * Register to be notified of heartbeats (all notifications are
   * on the same thread!)
   * 
   * @param heartbeatListener the listener that will be called
   */
  void registerToHeartbeat(HeartbeatListener heartbeatListener);
  
}
