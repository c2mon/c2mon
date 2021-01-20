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
package cern.c2mon.client.core.device.cache;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import cern.c2mon.client.core.device.Device;

/**
 * Implementation of the {@link DeviceCache} interface.
 *
 * @author Justin Lewis Salmon
 */
@Service
public class DeviceCacheImpl implements DeviceCache {

  /** The actual cached devices are stored here */
  private final Map<Long, Device> cache = new ConcurrentHashMap<>();

  @Override
  public void add(Device device) {
    cache.put(device.getId(), device);
  }

  @Override
  public Device get(Long deviceId) {
    return cache.get(deviceId);
  }

  @Override
  public Device get(String deviceName) {
    for (Device device : cache.values()) {
      if (device.getName().equals(deviceName)) {
        return device;
      }
    }

    return null;
  }

  @Override
  public List<Device> getAllDevices(String deviceClassName) {
    List<Device> devices = new ArrayList<>();

    // TODO: clone the devices here?
    for (Device device : cache.values()) {
      if (device.getDeviceClassName().equals(deviceClassName)) {
        devices.add(device);
      }
    }

    return devices;
  }

  @Override
  public List<Device> getAllDevices() {
    return new ArrayList<>(cache.values());
  }

  @Override
  public void remove(Device device) {
    cache.remove(device.getId());
  }

}
