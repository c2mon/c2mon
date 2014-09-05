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

import cern.c2mon.shared.client.device.CommandValue;
import cern.c2mon.shared.client.device.PropertyValue;

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
   * The list of property values belonging to this device.
   */
  private List<PropertyValue> propertyValues = new ArrayList<>();

  /**
   * The lsit of command values belonging to this device.
   */
  private List<CommandValue> commandValues = new ArrayList<>();

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
  public List<PropertyValue> getPropertyValues() {
    return propertyValues;
  }

  @Override
  public List<CommandValue> getCommandValues() {
    return commandValues;
  }

  @SuppressWarnings("unchecked")
  @Override
  public Object clone() throws CloneNotSupportedException {
    DeviceCacheObject clone = (DeviceCacheObject) super.clone();

    clone.propertyValues = (List<PropertyValue>) ((ArrayList<PropertyValue>) propertyValues).clone();
    clone.commandValues = (List<CommandValue>) ((ArrayList<CommandValue>) commandValues).clone();

    return clone;
  }

  /**
   * Set the list of property values.
   *
   * @param propertyValues the property values to set
   */
  public void setPropertyValues(List<PropertyValue> propertyValues) {
    this.propertyValues = propertyValues;
  }

  /**
   * Set the list of command values.
   *
   * @param commandValueList the command values to set
   */
  public void setCommandValues(List<CommandValue> commandValues) {
    this.commandValues = commandValues;
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
