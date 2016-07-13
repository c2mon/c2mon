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
package cern.c2mon.client.core.jms;

/**
 * Service monitoring the health of the client application. A client application
 * should register for health-state callbacks via the {@link C2monSupervisionManager}.
 * 
 * <p>Can only register a given listener once (duplicate registrations ignored).
 * 
 * <p>Notice that listeners will only be notified of a problem once, since they
 * are expected to take immediate action.
 * 
 * @author Mark Brightwell
 *
 */
public interface ClientHealthMonitor {

  /**
   * Register a listener for health-state callbacks, in particular on slow client consumption
   * of incoming data.
   * 
   * @param clientHealthListener listener that will be called when problems are detected
   */
  void addHealthListener(ClientHealthListener clientHealthListener);

  /**
   * Unregister the listener.
   * 
   * @param clientHealthListener listener
   */
  void removeHealthListener(ClientHealthListener clientHealthListener);
}
