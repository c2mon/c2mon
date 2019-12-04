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
import cern.c2mon.server.common.device.Device;
import cern.c2mon.server.configuration.handler.DeviceConfigHandler;
import cern.c2mon.server.configuration.handler.transacted.DeviceConfigTransacted;
import cern.c2mon.server.configuration.impl.ProcessChange;
import cern.c2mon.shared.client.configuration.ConfigurationElement;
import cern.c2mon.shared.client.configuration.ConfigurationElementReport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.UnexpectedRollbackException;

import java.util.Properties;

/**
 * Implementation of {@link DeviceConfigHandler}.
 *
 * @author Justin Lewis Salmon
 */
@Service
@Slf4j
public class DeviceConfigHandlerImpl implements DeviceConfigHandler {

  private DeviceConfigTransacted deviceConfigTransacted;

  /**
   * Reference to the Device cache.
   */
  private C2monCache<Device> deviceCache;

  /**
   * Default constructor.
   *
   * @param deviceCache autowired reference to the Device cache.
   */
  @Autowired
  public DeviceConfigHandlerImpl(final C2monCache<Device> deviceCache, final DeviceConfigTransacted deviceConfigTransacted) {
    this.deviceCache = deviceCache;
    this.deviceConfigTransacted = deviceConfigTransacted;
  }

  @Override
  public ProcessChange createDevice(ConfigurationElement element) throws IllegalAccessException {
    return deviceConfigTransacted.doCreateDevice(element);
  }

  @Override
  public ProcessChange updateDevice(Long id, Properties elementProperties) {
    try {
      return deviceConfigTransacted.doUpdateDevice(id, elementProperties);

    } catch (UnexpectedRollbackException e) {
      log.error("Rolling back update in cache");
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
