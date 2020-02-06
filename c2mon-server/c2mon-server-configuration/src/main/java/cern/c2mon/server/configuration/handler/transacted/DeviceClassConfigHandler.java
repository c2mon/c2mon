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
package cern.c2mon.server.configuration.handler.transacted;

import cern.c2mon.cache.actions.deviceclass.DeviceClassCacheObjectFactory;
import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.server.cache.loading.DeviceClassDAO;
import cern.c2mon.server.common.device.Device;
import cern.c2mon.server.common.device.DeviceClass;
import cern.c2mon.shared.client.configuration.ConfigConstants.Action;
import cern.c2mon.shared.client.configuration.ConfigConstants.Entity;
import cern.c2mon.shared.client.configuration.ConfigurationElementReport;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;

/**
 * Device and DeviceClass are only configured using the DB, not through the config API
 *
 * @author Alexandros Papageorgiou
 */
@Named
@Slf4j
public class DeviceClassConfigHandler extends BaseConfigHandlerImpl<DeviceClass> {

  private final C2monCache<Device> deviceCache;
  private final DeviceConfigHandler deviceConfigHandler;

  /**
   * Default constructor.
   *
   * @param deviceClassCache reference to the DeviceClass cache.
   * @param deviceClassDAO   reference to the DeviceClass DAO bean.
   */
  @Inject
  public DeviceClassConfigHandler(final C2monCache<DeviceClass> deviceClassCache,
                                  final DeviceClassDAO deviceClassDAO,
                                  final DeviceClassCacheObjectFactory deviceClassCacheObjectFactory,
                                  final C2monCache<Device> deviceCache, final DeviceConfigHandler deviceConfigHandler) {
    super(deviceClassCache, deviceClassDAO, deviceClassCacheObjectFactory, ArrayList::new);
    this.deviceCache = deviceCache;
    this.deviceConfigHandler = deviceConfigHandler;
  }

  @Override
  protected void doPreRemove(DeviceClass deviceClass, ConfigurationElementReport report) {
    deviceCache.query(device -> device.getDeviceClassId().equals(deviceClass.getId()))
      .forEach(device -> {
        ConfigurationElementReport newReport = new ConfigurationElementReport(Action.REMOVE, Entity.DEVICE, device.getId());
        report.addSubReport(newReport);
        // TODO (Alex) Do we want to keep this bidirectional?
        deviceConfigHandler.remove(device.getId(), newReport);
      });
  }
}
