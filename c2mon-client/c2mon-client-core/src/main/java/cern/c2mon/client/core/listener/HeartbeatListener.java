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
package cern.c2mon.client.core.listener;

import cern.c2mon.shared.client.supervision.Heartbeat;

/**
 * Interface to be implemented by HeartbeatListeners
 */

public interface HeartbeatListener {
  /**
   * This method is called whenever a Heartbeat is received from the server.
   * @param pHeartbeat heartbeat received from the server
   */
  public void onHeartbeatReceived(final Heartbeat pHeartbeat);
  /**
   * This method is called when the last Heartbeat from the server expires.
   * @param pHeartbeat last heartbeat received BEFORE the timer expired.
   */
  public void onHeartbeatExpired(final Heartbeat pHeartbeat);

  /**
   * This method is called when a Heartbeat is received from the server after
   * a Heartbeat expiration.
   * @param pHeartbeat heartbeat received from the server
   */
  public void onHeartbeatResumed(final Heartbeat pHeartbeat);
}
