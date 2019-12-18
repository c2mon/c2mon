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
package cern.c2mon.server.configuration.impl;

import cern.c2mon.server.configuration.ConfigurationUpdate;
import cern.c2mon.server.configuration.ConfigurationUpdateListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

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
  private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationUpdateImpl.class);

  /**
   * List of registered listeners.
   */
    private List<ConfigurationUpdateListener> listeners = new ArrayList<>();

  @Override
  public void registerForConfigurationUpdates(ConfigurationUpdateListener configurationUpdateListener) {
    if (LOGGER.isTraceEnabled()) {
      LOGGER.trace("registerForConfigurationUpdates - new Listener added to the list");
    }
    listeners.add(configurationUpdateListener);
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
