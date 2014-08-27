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
package cern.c2mon.server.common.device;

import java.util.ArrayList;
import java.util.List;

/**
 * This class implements the <code>DeviceClass</code> interface and resides in
 * the server DeviceClass cache.
 *
 * @author Justin Lewis Salmon
 */
public class DeviceClassCacheObject implements DeviceClass, Cloneable {

  /**
   * Serial version UID, since cloneable
   */
  private static final long serialVersionUID = 5797114724330150853L;

  /**
   * The unique ID of the device class.
   */
  private final Long id;

  /**
   * The name of the device class.
   */
  private String name;

  /**
   * The textual description of the device class.
   */
  private String description;

  /**
   * The list of property names that belong to this device class.
   */
  private List<String> properties = new ArrayList<>();

  /**
   * The list of command names that belong o this device class.
   */
  private List<String> commands = new ArrayList<>();

  /**
   * List of IDs of devices that are instances of this class.
   */
  private List<Long> deviceIds = new ArrayList<>();

  /**
   * Default constructor.
   *
   * @param id the unique ID of the device class
   * @param name the name of the device class
   * @param description the textual description of the device class
   */
  public DeviceClassCacheObject(final Long id, final String name, final String description) {
    this.id = id;
    this.name = name;
    this.description = description;
  }

  /**
   * Constructor used when creating a cache object during configuration.
   *
   * @param id the unique ID of the device class
   */
  public DeviceClassCacheObject(final Long id) {
    this.id = id;
    this.name = null;
    this.description = null;
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
  public String getDescription() {
    return description;
  }

  @Override
  public List<String> getProperties() {
    return properties;
  }

  @Override
  public List<String> getCommands() {
    return commands;
  }

  @SuppressWarnings("unchecked")
  @Override
  public Object clone() throws CloneNotSupportedException {
    DeviceClassCacheObject clone = (DeviceClassCacheObject) super.clone();

    clone.properties = (List<String>) ((ArrayList<String>) properties).clone();
    clone.commands = (List<String>) ((ArrayList<String>) commands).clone();
    clone.deviceIds = (List<Long>) ((ArrayList<Long>) deviceIds).clone();

    return clone;
  }

  /**
   * Set the name of the device class.
   *
   * @param name the device class name to set
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Set the description of the device class.
   *
   * @param description the device class description to set
   */
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * Retrieve the list of IDs corresponding to Device instances that are members
   * of this device class.
   *
   * @return the list of device IDs
   */
  public List<Long> getDeviceIds() {
    return deviceIds;
  }

  /**
   * Set the list of IDs corresponding to Device instances that are members of
   * this device class.
   *
   * @param deviceIds the list of device IDs to set
   */
  public void setDeviceIds(List<Long> deviceIds) {
    this.deviceIds = deviceIds;
  }

  /**
   * Set the list of property names that belong to this device.
   *
   * @param properties the properties to set
   */
  public void setProperties(List<String> properties) {
    this.properties = properties;
  }

  /**
   * Set the list of command names that belong to this device.
   *
   * @param commands the commands to set
   */
  public void setCommands(List<String> commands) {
    this.commands = commands;
  }
}
