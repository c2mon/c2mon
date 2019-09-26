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

import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;

import java.io.Serializable;

/**
 * This class is used as a descriptor to describe a device based on its name and
 * the name of its parent class.
 *
 * @author Justin Lewis Salmon
 */
@Data
@Setter(AccessLevel.NONE)
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
}
