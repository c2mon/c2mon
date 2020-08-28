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
package cern.c2mon.server.cache.alarm;

/**
 * This interface is used to register a listener alarm & tag update 
 * notifications
 * 
 * <p>Standard usage involves wiring it into your class and
 * calling the {@link #registerForTagUpdates(AlarmAggregatorListener)} 
 * method to register your listener.
 * 
 * <p>Listeners are notified on the cache notification threads
 * (i.e. this aggregator does not create any extra threads).
 * 
 * @author Mark Brightwell
 *
 */
public interface AlarmAggregatorRegistration {

  /**
   * Register this listener to received alarm & tag update notifications.
   * Notice that supervision changes are not taken into account here. For
   * these, a module should register directly with the cache.
   * 
   * @param aggregatorListener the listener that should be notified
   */
  void registerForTagUpdates(AlarmAggregatorListener aggregatorListener);
  
}
