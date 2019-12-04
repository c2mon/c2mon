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

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.server.common.device.DeviceClass;
import cern.c2mon.server.configuration.handler.DeviceClassConfigHandler;
import cern.c2mon.server.configuration.handler.transacted.DeviceClassConfigTransacted;
import cern.c2mon.server.configuration.impl.ProcessChange;
import cern.c2mon.shared.client.configuration.ConfigurationElement;
import cern.c2mon.shared.client.configuration.ConfigurationElementReport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.UnexpectedRollbackException;

import java.util.Properties;

/**
 * Implementation of {@link DeviceClassConfigHandler}.
 *
 * @author Justin Lewis Salmon
 */
@Service
@Slf4j
public class DeviceClassConfigHandlerImpl implements DeviceClassConfigHandler {

  private DeviceClassConfigTransacted deviceClassConfigTransacted;

  /**
   * Reference to the DeviceClass cache.
   */
  private C2monCache<DeviceClass> deviceClassCache;

  /**
   * Default constructor.
   *
   * @param deviceClassConfigTransacted
   * @param deviceClassCache autowired reference to the DeviceClass cache.
   */
  @Autowired
  public DeviceClassConfigHandlerImpl(final C2monCache<DeviceClass> deviceClassCache,DeviceClassConfigTransacted deviceClassConfigTransacted) {
    this.deviceClassCache = deviceClassCache;
    this.deviceClassConfigTransacted = deviceClassConfigTransacted;
  }

  @Override
  public ProcessChange createDeviceClass(ConfigurationElement element) throws IllegalAccessException {
    return deviceClassConfigTransacted.doCreateDeviceClass(element);
  }

  @Override
  public ProcessChange updateDeviceClass(Long id, Properties elementProperties) {
    try {
      return deviceClassConfigTransacted.doUpdateDeviceClass(id, elementProperties);

    } catch (UnexpectedRollbackException e) {
      log.error("Rolling back update in cache");
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
