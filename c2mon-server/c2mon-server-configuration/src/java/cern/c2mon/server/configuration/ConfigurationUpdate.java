/******************************************************************************
 * This file is part of the CERN Control and Monitoring (C2MON) platform.
 * 
 * See http://cern.ch/c2mon
 * 
 * Copyright (C) 2005-2014 CERN.
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * 
 * Author: C2MON team, c2mon-support@cern.ch
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
