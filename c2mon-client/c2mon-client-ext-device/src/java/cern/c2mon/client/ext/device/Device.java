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
package cern.c2mon.client.ext.device;

import java.util.List;
import java.util.Map;

import cern.c2mon.client.common.tag.ClientCommandTag;
import cern.c2mon.client.common.tag.ClientDataTagValue;
import cern.c2mon.client.ext.device.property.Property;

/**
 * This interface describes the methods which are provided by a C2MON Device
 * object. A device belongs to a particular device class, and can have multiple
 * properties.
 *
 * @author Justin Lewis Salmon
 */
public interface Device {

  /**
   * Retrieve the unique ID of this device.
   *
   * @return the device id
   */
  Long getId();

  /**
   * Retrieve the name of this device.
   *
   * @return the device name
   */
  String getName();

  /**
   * Retrieve the name of the class to which this device belongs.
   *
   * @return the device class name
   */
  String getDeviceClassName();

  /**
   * Retrieve a particular property of this device.
   *
   * <p>
   * Properties can take a number of different forms. They can be data tags,
   * client rules, constant values, or they can contain a list of sub-properties
   * known as fields. The {@link Property#getCategory()} method exists to help
   * you determine the property type.
   * </p>
   *
   * <p>
   * In the case of data tags/rules/constant values, the
   * {@link Property#getTag()} method will return you a
   * {@link ClientDataTagValue} object. The field accessor methods (
   * {@link Property#getField(String)} and {@link Property#getFields()}) will
   * return null and empty list, respectively.
   * </p>
   *
   * <p>
   * In the case of a property containing fields, {@link Property#getTag()}
   * will return null, and the field accessor methods will become active. Note
   * that the fields themselves are also instances of {@link Property} and can
   * be treated in the same way as regular properties.
   * </p>
   *
   * @param propertyName the name of the property you wish to retrieve
   *
   * @return the {@link Property} instance, or null if the property was not
   *         found
   *
   * @see Property
   */
  Property getProperty(String propertyName);

  /**
   * Retrieve all properties of this device.
   *
   * @return the list of {@link Property} instances, or an empty list if the
   *         device contains no properties
   *
   * @see Property
   */
  List<Property> getProperties();

  /**
   * Retrieve the names of all properties of this device.
   *
   * @return the list of device property names, or an empty list if the device
   *         contains no properties
   */
  List<String> getPropertyNames();

  /**
   * Retrieve a particular command of this device.
   *
   * @param commandName the name of the command to retrieve
   * @return the {@link ClientCommandTag} corresponding to the requested command
   *
   * @see ClientCommandTag
   */
  ClientCommandTag getCommand(String commandName);

  /**
   * Return all commands of this device.
   *
   * @return the map of command names -> {@link ClientCommandTag} instances
   *
   * @see ClientCommandTag
   */
  Map<String, ClientCommandTag> getCommands();
}
