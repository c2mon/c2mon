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

import java.io.Serializable;

/**
 * This class is used as a descriptor to describe a device based on its name and
 * the name of its parent class.
 *
 * @author Justin Lewis Salmon
 */
public class DeviceInfo implements Serializable {

  private static final long serialVersionUID = 942307042388462294L;

  /**
   * The name of the class to which this device belongs.
   */
  private final String className;

  /**
   * The name of the device.
   */
  private final String deviceName;

  /**
   * Constructor.
   *
   * @param className the name of the class to which this device belongs
   * @param deviceName the name of the device
   */
  public DeviceInfo(String className, String deviceName) {
    this.className = className.trim();
    this.deviceName = deviceName.trim();
  }

  /**
   * Retrieve the name of the class to which this device belongs.
   *
   * @return the class name
   */
  public final String getClassName() {
    return className;
  }

  /**
   * Retrieve the name of this device.
   *
   * @return the device name
   */
  public final String getDeviceName() {
    return deviceName;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((className == null) ? 0 : className.hashCode());
    result = prime * result + ((deviceName == null) ? 0 : deviceName.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof DeviceInfo)) {
      return false;
    }
    DeviceInfo other = (DeviceInfo) obj;

    if (className == null) {
      if (other.className != null) {
        return false;
      }
    } else if (!className.equalsIgnoreCase(other.className.trim())) {
      return false;
    }
    if (deviceName == null) {
      if (other.deviceName != null) {
        return false;
      }
    } else if (!deviceName.equalsIgnoreCase(other.deviceName.trim())) {
      return false;
    }
    return true;
  }
}
