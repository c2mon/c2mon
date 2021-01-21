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
package cern.c2mon.shared.client.device;

import java.util.*;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

/**
 * Simple XML mapper bean representing a list of device properties. Used when
 * deserialising device properties during configuration.
 *
 * @author Justin Lewis Salmon
 */
@Root(name = "DeviceProperties")
public class DevicePropertyList implements DeviceClassOrDeviceSerializableElement {

  @ElementList(entry = "DeviceProperty", inline = true, required = false)
  private Collection<DeviceProperty> deviceProperties = new HashSet<>();

  /**
   * Create a new wrapper bean for the referenced device properties
   * @param deviceProperties the device properties to wrap
   */
  public DevicePropertyList(Collection<DeviceProperty> deviceProperties) {
    this.deviceProperties = deviceProperties;
  }

  /**
   * Default constructor used during deserialization
   */
  public DevicePropertyList() {
    super();
  }

  /**
   * Get the unwrapped device properties in a mutable list
   * @return the unwrapped device properties in a mutable list
   */
  public List<DeviceProperty> getDeviceProperties() {
    return new ArrayList<>(deviceProperties);
  }
}
