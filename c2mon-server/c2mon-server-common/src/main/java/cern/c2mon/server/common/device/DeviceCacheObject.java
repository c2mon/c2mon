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

import cern.c2mon.server.common.AbstractCacheableImpl;
import cern.c2mon.shared.client.device.DeviceCommand;
import cern.c2mon.shared.client.device.DeviceProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

/**
 * This class implements the <code>Device</code> interface and resides in the
 * server Device cache.
 *
 * @author Justin Lewis Salmon
 */
@Getter
@Setter
@ToString
@EqualsAndHashCode(callSuper = true)
public class DeviceCacheObject extends AbstractCacheableImpl implements Device {

  /**
   * Serial version UID, since cloneable
   */
  private static final long serialVersionUID = -5756951683926328266L;

  /**
   * The name of this device.
   */
  private String name;

  /**
   * The unique ID of the class to which this device belongs.
   */
  private Long deviceClassId;

  /**
   * The list of properties belonging to this device.
   */
  private List<DeviceProperty> deviceProperties = new ArrayList<>();

  /**
   * The list of commands belonging to this device.
   */
  private List<DeviceCommand> deviceCommands = new ArrayList<>();

  /**
   * Default constructor.
   *
   * @param id the unique ID of this device
   * @param name the name of this device
   * @param deviceClassId the ID of the class to which this device belongs
   */
  public DeviceCacheObject(final Long id, final String name, final Long deviceClassId) {
    super(id);
    this.name = name;
    this.deviceClassId = deviceClassId;
  }

  @SuppressWarnings("unchecked")
  @Override
  public DeviceCacheObject clone() {
    DeviceCacheObject clone = (DeviceCacheObject) super.clone();

    clone.deviceProperties = (List<DeviceProperty>) ((ArrayList<DeviceProperty>) deviceProperties).clone();
    clone.deviceCommands = (List<DeviceCommand>) ((ArrayList<DeviceCommand>) deviceCommands).clone();

    return clone;
  }
}
