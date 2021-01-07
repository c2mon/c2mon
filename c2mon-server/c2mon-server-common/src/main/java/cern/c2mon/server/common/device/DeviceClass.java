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

import java.util.List;

import cern.c2mon.shared.client.device.Command;
import cern.c2mon.shared.client.device.Property;
import cern.c2mon.shared.common.Cacheable;

/**
 * This interface describes the methods provided by a DeviceClass object used in
 * the server Device cache.
 *
 * @author Justin Lewis Salmon
 */
public interface DeviceClass extends Cacheable {

  /**
   * Retrieve the unique ID of this device class.
   *
   * @return the device class ID
   */
  @Override
  public Long getId();

  /**
   * Retrieve the name of this device class.
   *
   * @return the device class name
   */
  public String getName();

  /**
   * Retrieve the description of this device class.
   *
   * @return the device class description
   */
  public String getDescription();

  /**
   * Retrieve the list of properties that belong to this device.
   *
   * @return the list of device properties
   */
  public List<Property> getProperties();

  /**
   * Retrieve the list of IDs of the properties that belong to this device.
   *
   * @return the list of device property IDs
   */
  public List<Long> getPropertyIds();

  /**
   * Retrieve the list of commands that belong to this device.
   *
   * @return the list of device commands
   */
  public List<Command> getCommands();

  /**
   * Retrieve the list of IDs of the commands that belong to this device.
   *
   * @return the list of device command IDs
   */
  public List<Long> getCommandIds();

  /**
   * Retrieve the list of names of the properties that belong to this device.
   *
   * @return the list of device property names
   */
  public List<String> getPropertyNames();

  /**
   * Retrieve the list of names of the commands that belong to this device.
   *
   * @return the list of device command names
   */
  public List<String> getCommandNames();

  /**
   * Retrieve the ID of a property by name.
   *
   * @param name the name of the property
   * @return the ID of the property if it exists, null otherwise
   */
  public Long getPropertyId(String name);

  /**
   * Retrieve the ID of a command by name.
   *
   * @param name the name of the command
   * @return the ID of the command if it exists, null otherwise
   */
  public Long getCommandId(String name);

  /**
   * Retrieve a list of field names for a particular property.
   *
   * @param propertyName the name of the property to retrieve fields from
   * @return the list of field names
   */
  public List<String> getFieldNames(String propertyName);

  /**
   * Retrieve a list of field ids for a particular property.
   *
   * @param propertyName the name of the property to retrieve fields from
   * @return the list of field ids
   */
  public List<Long> getFieldIds(String propertyName);
}
