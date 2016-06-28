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

import java.util.List;

/**
 * Mapper for accessing Server lifecycle events stored
 * in STL.
 * 
 * @author Mark Brightwell
 *
 */
public interface ServerLifecycleMapper {

  /**
   * Log this event in the server lifecycle STL.
   * 
   * @param serverLifecycleEvent event to log
   */
  void logEvent(ServerLifecycleEvent serverLifecycleEvent);
  
  /**
   * Retrieves all events for a given server, ordered by the time
   * of the event.
   * @param serverName the name of the server
   * @return an ordered list of events, as returned by the iterator
   */
  List<ServerLifecycleEvent> getEventsForServer(String serverName);
  
  /**
   * Delete all lifecycle events in the DB for this server.
   * Used in tests.
   * @param serverName name of the server
   */
  void deleteAllForServer(String serverName);
}
