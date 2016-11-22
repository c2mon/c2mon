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
package cern.c2mon.server.daqcommunication.in;

import cern.c2mon.server.common.process.Process;

/**
 * Manages the JMS containers listening for incoming updates.
 * Allows the adding and removal of containers dedicated
 * to listening for updates from a particular Process.
 *
 * <p>At startup, the manager will subscribe to those Processes found
 * in the cache.
 *
 * @author Mark Brightwell
 */
public interface JmsContainerManager {

  /**
   * Exclusive config lock located in ClusterCache.
   *
   * <p>Distributed lock used to get exclusive configuration access to the server
   * (applying configurations is forced to be sequential).
   */
  String CONFIG_LOCK_KEY = "c2mon.configuration.configlock";

  /**
   * Adds a JMS container listening for process update messages. If one is already
   * registered for this process, logs a warning.
   *
   * @param process the Process to receive messages for
   */
  void subscribe(Process process);

  /**
   * Removes the container listening to updates for the given
   * process. If no subscription can be found for this process,
   * logs a warning.
   *
   * @param process stop receving message for this Process
   * @throws NullPointerException if the specified Process is null
   */
  void unsubscribe(Process process);
}
