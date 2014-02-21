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
package cern.c2mon.server.configuration.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import cern.c2mon.server.configuration.ConfigurationUpdate;
import cern.c2mon.server.configuration.ConfigurationUpdateListener;

/**
 * Implementation of the {@link ConfigurationUpdate} (a singleton bean in the
 * server context).
 * 
 * <p>
 * This implementation registers for synchronous notifications from the cache
 * (i.e. on original JMS update thread). These calls are passed through to the
 * client module on the same thread (may need adjusting).
 * 
 * @author vilches
 * 
 */
@Service
public class ConfigurationUpdateImpl implements ConfigurationUpdate {
  /**
   * Class logger.
   */
  private static final Logger LOGGER = Logger.getLogger(ConfigurationUpdateImpl.class);

  /**
   * List of registered listeners.
   */
  private List<ConfigurationUpdateListener> listeners = new ArrayList<ConfigurationUpdateListener>();

  @Override
  public void registerForConfigurationUpdates(ConfigurationUpdateListener configurationUpdateListener) {
    if (LOGGER.isTraceEnabled()) {
      LOGGER.trace("registerForConfigurationUpdates - new Listener added to the list");
    }
    this.listeners.add(configurationUpdateListener);
  }

  /**
   * Notify the listeners of a configuration update.
   * 
   * @param tag
   *          the Tag id that has updated his configuration
   */
  public void notifyListeners(final Long tagId) {
    if (LOGGER.isTraceEnabled()) {
      LOGGER.trace("notifyListeners - notify the Tag with id " + tagId + " has changed. Number of listeners: " + this.listeners.size());
    }
    for (ConfigurationUpdateListener listener : this.listeners) {
      listener.notifyOnConfigurationUpdate(tagId);
    }
  }

  /**
   * 
   * @return
   */
  public List<ConfigurationUpdateListener> getListeners() {
    return this.listeners;
  }

}
