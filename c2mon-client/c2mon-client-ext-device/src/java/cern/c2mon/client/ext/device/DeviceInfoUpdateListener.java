/*******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 *
 * Copyright (C) 2004 - 2015 CERN. This program is free software; you can
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
package cern.c2mon.client.ext.device;

import java.util.List;

import cern.c2mon.shared.client.device.DeviceInfo;

/**
 * @author Justin Lewis Salmon
 */
public interface DeviceInfoUpdateListener extends DeviceUpdateListener {

  /**
   * This method will potentially be called if any devices requested via the
   * {@link C2monDeviceManager#subscribeDevice(DeviceInfo, DeviceInfoUpdateListener)}
   * or
   * {@link C2monDeviceManager#subscribeDevices(java.util.Set, DeviceInfoUpdateListener)}
   * methods were not found on the server. In the former case, the
   * unknownDevices parameter will be a single-item list.
   *
   * <p>
   * Note: if this method is to be called, it will be called before any other
   * callback methods of this listener.
   * </p>
   *
   * @param unknownDevices the list of {@link DeviceInfo} objects that were not
   *          found on the server.
   */
  void onDevicesNotFound(List<DeviceInfo> unknownDevices);
}
