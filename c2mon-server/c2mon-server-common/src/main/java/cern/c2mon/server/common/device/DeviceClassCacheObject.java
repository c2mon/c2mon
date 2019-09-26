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

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * This class implements the <code>DeviceClass</code> interface and resides in
 * the server DeviceClass cache.
 *
 * @author Justin Lewis Salmon
 */
@Data
@NoArgsConstructor
public class DeviceClassCacheObject implements DeviceClass {

  /**
   * Serial version UID, since cloneable
   */
  private static final long serialVersionUID = 5797114724330150853L;

  /**
   * The unique ID of the device class.
   */
  private Long id;

  /**
   * The name of the device class.
   */
  private String name;

  /**
   * The textual description of the device class.
   */
  private String description;

  /**
   * The list of properties that belong to this device class.
   */
  private List<Property> properties = new ArrayList<>();

  /**
   * The list of commands that belong o this device class.
   */
  private List<Command> commands = new ArrayList<>();

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

  @SuppressWarnings("unchecked")
  @Override
  public DeviceClassCacheObject clone() throws CloneNotSupportedException {
    DeviceClassCacheObject clone = (DeviceClassCacheObject) super.clone();

    clone.properties = (List<Property>) ((ArrayList<Property>) properties).clone();
    clone.commands = (List<Command>) ((ArrayList<Command>) commands).clone();
    clone.deviceIds = (List<Long>) ((ArrayList<Long>) deviceIds).clone();

    return clone;
  }

  @Override
  public List<Long> getPropertyIds() {
    List<Long> propertyIds = new ArrayList<>();

    for (Property property : properties) {
      if (property.getId() != null) {
        propertyIds.add(property.getId());
      }
    }

    return propertyIds;
  }

  @Override
  public List<Long> getCommandIds() {
    List<Long> commandIds = new ArrayList<>();

    for (Command command : commands) {
      if (command.getId() != null) {
        commandIds.add(command.getId());
      }
    }

    return commandIds;
  }

  @Override
  public List<String> getPropertyNames() {
    List<String> propertyNames = new ArrayList<>();

    for (Property property : properties) {
      propertyNames.add(property.getName());
    }

    return propertyNames;
  }

  @Override
  public List<String> getCommandNames() {
    List<String> commandNames = new ArrayList<>();

    for (Command command : commands) {
      commandNames.add(command.getName());
    }

    return commandNames;
  }

  @Override
  public Long getPropertyId(String name) {
    for (Property property : properties) {
      if (property.getName().equals(name)) {
        return property.getId();
      }
    }

    return null;
  }

  @Override
  public Long getCommandId(String name) {
    for (Command command : commands) {
      if (command.getName().equals(name)) {
        return command.getId();
      }
    }

    return null;
  }

  @Override
  public List<String> getFieldNames(String propertyName) {
    List<String> fieldNames = new ArrayList<>();

    for (Property property : properties) {
      if (property.getName().equals(propertyName)) {
        if (property.getFields() != null) {
          for (Property field : property.getFields()) {
            fieldNames.add(field.getName());
          }
          break;
        }
      }
    }

    return fieldNames;
  }

  @Override
  public List<Long> getFieldIds(String propertyName) {
    List<Long> fieldIds = new ArrayList<>();

    for (Property property : properties) {
      if (property.getName().equals(propertyName)) {
        if (property.getFields() != null) {
          for (Property field : property.getFields()) {
            fieldIds.add(field.getId());
          }
          break;
        }
      }
    }

    return fieldIds;
  }
}
