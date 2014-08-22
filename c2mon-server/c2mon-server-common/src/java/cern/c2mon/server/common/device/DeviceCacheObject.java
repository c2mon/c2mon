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
  private final Long id;

  /**
   * The name of this device.
   */
  private final String name;

  /**
   * The unique ID of the class to which this device belongs.
   */
  private final Long deviceClassId;

  /**
   * The list of names of properties belonging to this device. This has a 1:1
   * mapping with the propertyTagIds field.
   */
  private List<String> propertyNames = new ArrayList<>();

  /**
   * The list of IDs of the tags (property values) belonging to this device.
   * This has a 1:1 mapping with the propertyNames field.
   */
  private List<Long> propertyTagIds = new ArrayList<>();

  /**
   * The list of names of commands belonging to this device. This has a 1:1
   * mapping with the commandTagIds field.
   */
  private List<String> commandNames = new ArrayList<>();

  /**
   * The list of IDs of the tags (command values) belonging to this device. This
   * has a 1:1 mapping with the commandNames field.
   */
  private List<Long> commandTagIds = new ArrayList<>();

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

    // Potential bug alert: the propertyNames and propertyTagIds lists must be
    // the same length. Although they should be, as both come directly from
    // database columns marked as NOT NULL.
    for (int i = 0; i < propertyNames.size(); i++) {
      try {
        propertyValues.put(propertyNames.get(i), propertyTagIds.get(i));
      } catch (IndexOutOfBoundsException e) {
        propertyValues.put(propertyNames.get(i), null);
      }
    }

    return propertyValues;
  }

  @Override
  public Map<String, Long> getCommandValues() {
    Map<String, Long> commandValues = new HashMap<>();

    // Potential bug alert: the commandNames and commandTagIds lists must be the
    // same length. Although they should be, as both come directly from
    // database columns marked as NOT NULL.
    for (int i = 0; i < commandNames.size(); i++) {
      try {
        commandValues.put(commandNames.get(i), commandTagIds.get(i));
      } catch (IndexOutOfBoundsException e) {
        commandValues.put(commandNames.get(i), null);
      }
    }

    return commandValues;
  }

  @Override
  public DeviceCacheObject clone() {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * Set the map of property values.
   *
   * @param propertyValues the property values to set
   */
  public void setPropertyValues(Map<String, Long> propertyValues) {
    for (Entry<String, Long> e : propertyValues.entrySet()) {
      propertyNames.add(e.getKey());
      propertyTagIds.add(e.getValue());
    }
  }

  /**
   * Set the map of command values.
   *
   * @param commandValues the command values to set
   */
  public void setCommandValues(Map<String, Long> commandValues) {
    for (Entry<String, Long> e : commandValues.entrySet()) {
      commandNames.add(e.getKey());
      commandTagIds.add(e.getValue());
    }
  }

  /**
   * Get the list of property names.
   *
   * @return the property names
   */
  public List<String> getPropertyNames() {
    return propertyNames;
  }

  /**
   * Set the list of property names.
   *
   * @param propertyNames the property names to set
   */
  public void setPropertyNames(List<String> propertyNames) {
    this.propertyNames = propertyNames;
  }

  /**
   * Get the list of property tag IDs
   * @return the property tag IDs
   */
  public List<Long> getPropertyTagIds() {
    return propertyTagIds;
  }

  /**
   * Set the list of property tag IDs.
   *
   * @param propertyTagIds the tag IDs to set
   */
  public void setPropertyTagIds(List<Long> propertyTagIds) {
    this.propertyTagIds = propertyTagIds;
  }

  /**
   * Get the list of command names
   *
   * @return the command names
   */
  public List<String> getCommandNames() {
    return commandNames;
  }

  /**
   * Set the list of command names.
   *
   * @param commandNames the command names to set
   */
  public void setCommandNames(List<String> commandNames) {
    this.commandNames = commandNames;
  }

  /**
   * Get the list of command tag IDs
   *
   * @return the command tag IDs
   */
  public List<Long> getCommandTagIds() {
    return commandTagIds;
  }

  /**
   * Set the list of command tag IDs.
   *
   * @param commandTagIds the command tag IDs to set
   */
  public void setCommandTagIds(List<Long> commandTagIds) {
    this.commandTagIds = commandTagIds;
  }
}
