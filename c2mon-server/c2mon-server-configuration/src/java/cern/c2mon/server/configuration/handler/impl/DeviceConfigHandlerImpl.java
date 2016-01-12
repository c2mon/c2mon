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
package cern.c2mon.server.configuration.handler.impl;

import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.UnexpectedRollbackException;

import cern.c2mon.server.cache.DeviceCache;
import cern.c2mon.server.configuration.handler.DeviceConfigHandler;
import cern.c2mon.server.configuration.handler.transacted.DeviceConfigTransacted;
import cern.c2mon.server.configuration.impl.ProcessChange;
import cern.c2mon.shared.client.configuration.ConfigurationElement;
import cern.c2mon.shared.client.configuration.ConfigurationElementReport;

/**
 * Implementation of {@link DeviceConfigHandler}.
 *
 * @author Justin Lewis Salmon
 */
@Service
public class DeviceConfigHandlerImpl implements DeviceConfigHandler {

  /**
   * Class logger.
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(DeviceConfigHandlerImpl.class);

  /**
   * Transacted bean.
   */
  @Autowired
  private DeviceConfigTransacted deviceConfigTransacted;

  /**
   * Reference to the Device cache.
   */
  private DeviceCache deviceCache;

  /**
   * Default constructor.
   *
   * @param deviceCache autowired reference to the Device cache.
   */
  @Autowired
  public DeviceConfigHandlerImpl(final DeviceCache deviceCache) {
    this.deviceCache = deviceCache;
  }

  @Override
  public ProcessChange createDevice(ConfigurationElement element) throws IllegalAccessException {
    ProcessChange change = deviceConfigTransacted.doCreateDevice(element);
    return change;
  }

  @Override
  public ProcessChange updateDevice(Long id, Properties elementProperties) {
    try {
      ProcessChange processChange = deviceConfigTransacted.doUpdateDevice(id, elementProperties);
      return processChange;

    } catch (UnexpectedRollbackException e) {
      LOGGER.error("Rolling back update in cache");
      // DB transaction is rolled back here: reload the tag
      deviceCache.remove(id);
      deviceCache.loadFromDb(id);
      throw e;
    }
  }

  /*
   * (non-Javadoc)
   *
   * @see
   * cern.c2mon.server.configuration.handler.DeviceConfigHandler#removeDevice()
   *
   * A Device removal will always result in the removal of all device properties
   * and commands.
   */
  @Override
  public ProcessChange removeDevice(Long id, ConfigurationElementReport elementReport) {
    ProcessChange change = deviceConfigTransacted.doRemoveDevice(id, elementReport);

    // will be skipped if rollback exception thrown in do method
    deviceCache.remove(id);
    return change;
  }
}
