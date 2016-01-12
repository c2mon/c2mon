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

import cern.c2mon.shared.client.device.DeviceCommand;
import cern.c2mon.shared.client.device.DeviceProperty;

/**
 * This class implements the <code>Device</code> interface and resides in the
 * server Device cache.
 *
 * @author Justin Lewis Salmon
 */
public class DeviceCacheObject implements Device, Cloneable {

  /**
   * Serial version UID, since cloneable
   */
  private static final long serialVersionUID = -5756951683926328266L;

  /**
   * The unique ID of this device.
   */
  private Long id;

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
    this.id = id;
    this.name = name;
    this.deviceClassId = deviceClassId;
  }

  /**
   * Constructor used when creating a cache object during configuration.
   *
   * @param id the unique ID of this device
   */
  public DeviceCacheObject(final Long id) {
    this.id = id;
  }

  /**
   * Empty constructor, needed for MyBatis.
   */
  public DeviceCacheObject() {
  }

  @Override
  public Long getId() {
    return id;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public Long getDeviceClassId() {
    return deviceClassId;
  }

  @Override
  public List<DeviceProperty> getDeviceProperties() {
    return deviceProperties;
  }

  @Override
  public List<DeviceCommand> getDeviceCommands() {
    return deviceCommands;
  }

  @SuppressWarnings("unchecked")
  @Override
  public Object clone() throws CloneNotSupportedException {
    DeviceCacheObject clone = (DeviceCacheObject) super.clone();

    clone.deviceProperties = (List<DeviceProperty>) ((ArrayList<DeviceProperty>) deviceProperties).clone();
    clone.deviceCommands = (List<DeviceCommand>) ((ArrayList<DeviceCommand>) deviceCommands).clone();

    return clone;
  }

  /**
   * Set the list of device properties.
   *
   * @param deviceProperties the properties to set
   */
  public void setDeviceProperties(List<DeviceProperty> deviceProperties) {
    this.deviceProperties = deviceProperties;
  }

  /**
   * Set the list of device commands.
   *
   * @param deviceCommands the commands to set
   */
  public void setDeviceCommands(List<DeviceCommand> deviceCommands) {
    this.deviceCommands = deviceCommands;
  }

  /**
   * Set the unique ID of the device.
   *
   * @param id the id to set
   */
  public void setId(Long id) {
    this.id = id;
  }

  /**
   * Set the name of the device.
   *
   * @param name the device name to set
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Set the ID of the class to which this device belongs.
   *
   * @param deviceClassId the device class ID to set
   */
  public void setDeviceClassId(Long deviceClassId) {
    this.deviceClassId = deviceClassId;
  }
}
