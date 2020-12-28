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
package cern.c2mon.client.core.device.listener;

import java.util.List;

import cern.c2mon.client.core.service.DeviceService;
import cern.c2mon.shared.client.device.DeviceInfo;

/**
 * @author Justin Lewis Salmon
 */
public interface DeviceInfoUpdateListener extends DeviceUpdateListener {

  /**
   * This method will potentially be called if any devices requested via the
   * {@link DeviceService#subscribeDevice(DeviceInfo, DeviceInfoUpdateListener)}
   * or
   * {@link DeviceService#subscribeDevices(java.util.Set, DeviceInfoUpdateListener)}
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
