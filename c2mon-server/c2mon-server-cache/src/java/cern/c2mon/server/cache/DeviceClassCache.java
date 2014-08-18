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
package cern.c2mon.server.cache;

import cern.c2mon.server.common.device.DeviceClass;

/**
 * The module public interface that should be used to access the
 * <code>DeviceClass</code>es in the server cache.
 *
 * @author Justin Lewis Salmon
 */
public interface DeviceClassCache extends C2monCache<Long, DeviceClass> {

  String cacheInitializedKey = "c2mon.cache.deviceclass.initialized";

  /**
   * Retrieve a particular <code>DeviceClass</code> instance from the cache by
   * specifying its name.
   *
   * @param deviceClassName the name of the device class
   * @return the <code>DeviceClass</code> instance with the specified name, or
   *         null if no instance exists in the cache
   */
  public DeviceClass getDeviceClassByName(String deviceClassName);
}
