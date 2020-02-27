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

import cern.c2mon.cache.actions.device.DeviceService;
import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.config.deviceclass.DeviceClassCacheObjectFactory;
import cern.c2mon.server.cache.loading.DeviceClassDAO;
import cern.c2mon.server.common.device.DeviceClass;
import cern.c2mon.server.configuration.impl.ProcessChange;
import cern.c2mon.shared.client.configuration.ConfigConstants.Action;
import cern.c2mon.shared.client.configuration.ConfigConstants.Entity;
import cern.c2mon.shared.client.configuration.ConfigurationElementReport;
import cern.c2mon.shared.common.Cacheable;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.List;

/**
 * Device and DeviceClass are only configured using the DB, not through the config API
 *
 * @author Alexandros Papageorgiou
 */
@Named
@Slf4j
public class DeviceClassConfigHandler extends BaseConfigHandlerImpl<DeviceClass> {

  private final DeviceService deviceService;
  private final DeviceConfigHandler deviceConfigHandler;

  /**
   * Default constructor.
   *
   * @param deviceClassCache reference to the DeviceClass cache.
   * @param deviceClassDAO   reference to the DeviceClass DAO bean.
   * @param deviceClassCacheObjectFactory
   * @param deviceConfigHandler
   * @param deviceService
   */
  @Inject
  public DeviceClassConfigHandler(final C2monCache<DeviceClass> deviceClassCache,
                                  final DeviceClassDAO deviceClassDAO,
                                  final DeviceClassCacheObjectFactory deviceClassCacheObjectFactory,
                                  final DeviceService deviceService, final DeviceConfigHandler deviceConfigHandler) {
    super(deviceClassCache, deviceClassDAO, deviceClassCacheObjectFactory, ArrayList::new);
    this.deviceService = deviceService;
    this.deviceConfigHandler = deviceConfigHandler;
  }

  /**
   * Removes the device class with the given id and cascades onto the devices
   *
   * Due to FK constraints, the devices with the given device class id
   * are removed first, even if the device class is not found afterwards.
   * If you would like to change this behaviour, you can use the
   * {@link super#doPreRemove(Cacheable, ConfigurationElementReport)} hook.
   *
   * @param id the device class id to remove
   * @param report the ConfigurationElementReport to collect the results
   * @return
   */
  @Override
  public List<ProcessChange> remove(Long id, ConfigurationElementReport report) {
    deviceService.getByDeviceClassId(id)
      .forEach(device -> {
        ConfigurationElementReport newReport = new ConfigurationElementReport(Action.REMOVE, Entity.DEVICE, device.getId());
        report.addSubReport(newReport);
        deviceConfigHandler.remove(device.getId(), newReport);
      });
    // Returns an empty list of processChanges
     return super.remove(id, report);
  }
}
