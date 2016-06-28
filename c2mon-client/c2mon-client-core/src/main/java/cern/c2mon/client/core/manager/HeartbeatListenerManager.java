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
package cern.c2mon.client.core.manager;

import cern.c2mon.client.core.listener.HeartbeatListener;

/**
 * This interface is implemented by the <code>HeartbeatManger</code>.
 * It allows the <code>SessionManager</code> to delegate heartbeat
 * listener subscriptions and unsubscriptions to the
 * <code>HeartbeatManager</code>.
 *
 * @author Matthias Braeger
 */
interface HeartbeatListenerManager {

  /**
   * Register a new HeartbeatListener for receiving heartbeat events
   * @param listener The listener to be registered
   */
  void addHeartbeatListener(HeartbeatListener listener);

  /**
   * Unregister an existing HeartbeatListener
   * @param listener The listener to be removed
   */
  void removeHeartbeatListener(HeartbeatListener listener);
}
