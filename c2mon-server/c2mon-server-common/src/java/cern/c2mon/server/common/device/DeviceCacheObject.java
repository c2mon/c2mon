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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * This class implements the <code>Device</code> interface and resides in the
 * server Device cache.
 *
 * <p>
 * Note: the properties and commands are not stored directly as Maps due to a
 * limitation with MyBatis - it cannot load a map containing a key-value pair
 * based on 2 columns in an <association>/<collection> element. So the
 * keys/values are stored as separate lists and combined in the accessor
 * methods.
 * </p>
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
   * TODO
   */
  private List<PropertyValue> propertyValues = new ArrayList<>();

  /**
   *
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
   * TODO
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
  public Map<String, Long> getPropertyValues() {
    Map<String, Long> propertyValues = new HashMap<>();

    for (PropertyValue propertyValue : this.propertyValues) {
      propertyValues.put(propertyValue.getName(), propertyValue.getTagId());
    }

    return propertyValues;
  }

  @Override
  public Map<String, Long> getCommandValues() {
    Map<String, Long> commandValues = new HashMap<>();

    for (CommandValue commandValue : this.commandValues) {
      commandValues.put(commandValue.getName(), commandValue.getTagId());
    }

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
   * Set the map of property values.
   *
   * @param propertyValues the property values to set
   */
  public void setPropertyValues(Map<String, Long> propertyValues) {
    for (Entry<String, Long> e : propertyValues.entrySet()) {
      this.propertyValues.add(new PropertyValue(e.getKey(), e.getValue()));
    }
  }

  /**
   * TODO
   *
   * @param propertyValues
   */
  public void setPropertyValues(List<PropertyValue> propertyValues) {
    this.propertyValues = propertyValues;
  }

  /**
   * @param commandValueList
   */
  public void setCommandValues(List<CommandValue> commandValues) {
    this.commandValues = commandValues;
  }

  /**
   * Set the map of command values.
   *
   * @param commandValues the command values to set
   */
  public void setCommandValues(Map<String, Long> commandValues) {
    for (Entry<String, Long> e : commandValues.entrySet()) {
      this.commandValues.add(new CommandValue(e.getKey(), e.getValue()));
    }
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
