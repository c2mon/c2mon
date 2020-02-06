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

import cern.c2mon.cache.actions.device.DeviceCacheObjectFactory;
import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.server.cache.loading.DeviceDAO;
import cern.c2mon.server.common.device.Device;
import cern.c2mon.server.common.device.DeviceClass;
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
public class DeviceConfigHandler extends BaseConfigHandlerImpl<Device> {

  private C2monCache<DeviceClass> deviceClassCache;

  /**
   * Default constructor.
   *
   * @param deviceCache              reference to the Device cache.
   * @param deviceDAO
   * @param deviceCacheObjectFactory reference to the Device DAO bean.
   * @param deviceClassCache         reference to the DeviceClass cache.
   */
  @Inject
  public DeviceConfigHandler(final C2monCache<Device> deviceCache,
                             final DeviceDAO deviceDAO, final DeviceCacheObjectFactory deviceCacheObjectFactory,
                             final C2monCache<DeviceClass> deviceClassCache) {
    super(deviceCache, deviceDAO, deviceCacheObjectFactory, ArrayList::new);
    this.deviceClassCache = deviceClassCache;
  }

  @Override
  protected void doPostCreate(Device element) {
    // Update the cacheObject class so that it knows about the new cacheObject
    // TODO (Alex) Create loadFromDb
//    deviceClassCache.loadFromDb(element.getDeviceClassId());
  }

}
