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

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.config.device.DeviceCacheObjectFactory;
import cern.c2mon.server.cache.loading.DeviceDAO;
import cern.c2mon.server.common.device.Device;
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

  /**
   * Default constructor.
   *
   * @param deviceCache              reference to the Device cache.
   * @param deviceDAO                the loader DAO
   * @param deviceCacheObjectFactory reference to the Device DAO bean.
   */
  @Inject
  public DeviceConfigHandler(final C2monCache<Device> deviceCache,
                             final DeviceDAO deviceDAO, final DeviceCacheObjectFactory deviceCacheObjectFactory) {
    super(deviceCache, deviceDAO, deviceCacheObjectFactory, ArrayList::new);
  }

}
