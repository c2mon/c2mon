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
package cern.c2mon.server.common.device;

import java.util.ArrayList;
import java.util.List;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import cern.c2mon.shared.client.device.DeviceProperty;

/**
 * Simple XML mapper bean representing a list of device properties. Used when
 * deserialising device properties during configuration.
 *
 * @author Justin Lewis Salmon
 */
@Root(name = "DeviceProperties")
public class DevicePropertyList implements AbstractList {

  @ElementList(entry = "DeviceProperty", inline = true, required = false)
  private List<DeviceProperty> deviceProperties = new ArrayList<>();

  public DevicePropertyList(List<DeviceProperty> deviceProperties) {
    this.deviceProperties = deviceProperties;
  }

  public DevicePropertyList() {
    super();
  }

  //TODO: remove this method and replace with getList
  public List<DeviceProperty> getDeviceProperties() {
    return deviceProperties;
  }

  @Override
  public List getList() {
    return deviceProperties;
  }
}
