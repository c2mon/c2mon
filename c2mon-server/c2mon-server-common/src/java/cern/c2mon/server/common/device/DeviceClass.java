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

import java.util.List;

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
