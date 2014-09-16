/*******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 *
 * Copyright (C) 2004 - 2014 CERN. This program is free software; you can
 * redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version. This program is distributed
 * in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received
 * a copy of the GNU General Public License along with this program; if not,
 * write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA 02111-1307, USA.
 *
 * Author: TIM team, tim.support@cern.ch
 ******************************************************************************/
package cern.c2mon.server.configuration.handler.impl;

import java.util.Properties;

import org.apache.log4j.Logger;
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
  private static final Logger LOGGER = Logger.getLogger(DeviceConfigHandlerImpl.class);

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
