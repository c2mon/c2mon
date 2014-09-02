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

import cern.c2mon.server.cache.DeviceClassCache;
import cern.c2mon.server.configuration.handler.DeviceClassConfigHandler;
import cern.c2mon.server.configuration.handler.transacted.DeviceClassConfigTransacted;
import cern.c2mon.server.configuration.impl.ProcessChange;
import cern.c2mon.shared.client.configuration.ConfigurationElement;
import cern.c2mon.shared.client.configuration.ConfigurationElementReport;

/**
 * Implementation of {@link DeviceClassConfigHandler}.
 *
 * @author Justin Lewis Salmon
 */
@Service
public class DeviceClassConfigHandlerImpl implements DeviceClassConfigHandler {

  /**
   * Class logger.
   */
  private static final Logger LOGGER = Logger.getLogger(DeviceClassConfigHandlerImpl.class);

  /**
   * Transacted bean.
   */
  @Autowired
  private DeviceClassConfigTransacted deviceClassConfigTransacted;

  /**
   * Reference to the DeviceClass cache.
   */
  private DeviceClassCache deviceClassCache;

  /**
   * Default constructor.
   *
   * @param deviceClassCache autowired reference to the DeviceClass cache.
   */
  @Autowired
  public DeviceClassConfigHandlerImpl(final DeviceClassCache deviceClassCache) {
    this.deviceClassCache = deviceClassCache;
  }

  @Override
  public ProcessChange createDeviceClass(ConfigurationElement element) throws IllegalAccessException {
    ProcessChange change = deviceClassConfigTransacted.doCreateDeviceClass(element);
    return change;
  }

  @Override
  public ProcessChange updateDeviceClass(Long id, Properties elementProperties) {
    try {
      ProcessChange processChange = deviceClassConfigTransacted.doUpdateDeviceClass(id, elementProperties);
      return processChange;

    } catch (UnexpectedRollbackException e) {
      LOGGER.error("Rolling back update in cache");
      // DB transaction is rolled back here: reload the tag
      deviceClassCache.remove(id);
      deviceClassCache.loadFromDb(id);
      throw e;
    }
  }

  /*
   * (non-Javadoc)
   *
   * @see cern.c2mon.server.configuration.handler.DeviceClassConfigHandler#
   * removeDeviceClass()
   *
   * A Device Class removal will always result in the following actions, in this
   * order:
   *
   * 1. Remove all Devices dependent on this class
   *
   * 2. Remove all Properties and Commands of the device class
   */
  @Override
  public ProcessChange removeDeviceClass(Long id, ConfigurationElementReport elementReport) {
    ProcessChange change = deviceClassConfigTransacted.doRemoveDeviceClass(id, elementReport);

    // will be skipped if rollback exception thrown in do method
    deviceClassCache.remove(id);
    return change;
  }
}
