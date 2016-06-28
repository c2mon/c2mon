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
package cern.c2mon.server.configuration;

/**
 * The ConfigurationUpdate bean listens for Configuration updates and passes the
 * result to registered {@link ConfigurationUpdateListener}s.
 * 
 * <p>
 * Standard usage involves wiring it into your class and calling the
 * registerForConfigurationUpdates method to register your listener.
 * 
 * @author vilches
 * 
 */
public interface ConfigurationUpdate {

  /**
   * Register this listener to received configurations update notifications.
   * Notice that supervision changes are not taken into account here. For these,
   * a module should register directly with the cache.
   * 
   * @param configurationUpdateListener
   *          the listener that should be notified
   */
  void registerForConfigurationUpdates(ConfigurationUpdateListener configurationUpdateListener);

}
