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

import java.util.Map;

import cern.c2mon.client.common.tag.ClientCommandTag;
import cern.c2mon.client.common.tag.ClientDataTagValue;
import cern.c2mon.client.ext.device.exception.MappedPropertyException;
import cern.c2mon.client.ext.device.property.Category;
import cern.c2mon.client.ext.device.property.PropertyInfo;

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
  public Long getId();

  /**
   * Retrieve the name of this device.
   *
   * @return the device name
   */
  public String getName();

  /**
   * Retrieve the name of the class to which this device belongs.
   *
   * @return the device class name
   */
  public String getDeviceClassName();

  /**
   * Retrieve a particular property/field of this device.
   *
   * <p>
   * The given {@link PropertyInfo} object should contain the name of the
   * property to be retrieved. In the case of the property being a mapped
   * property (i.e. containing nested fields) then it should also contain the
   * name of the field to be retrieved. You will then get the
   * {@link ClientDataTagValue} corresponding to the field value, NOT the entire
   * field map.
   * </p>
   *
   * @param propertyInfo the info object containing the name of the
   *          property/field to retrieve
   * @return the {@link ClientDataTagValue} corresponding to the requested
   *         property/field, or null if the property/field was not found
   *
   * @throws MappedPropertyException if an attempt is made to retrieve a field
   *           value from a non mapped property, or if a field name is not
   *           specified for a mapped property
   *
   * @see PropertyInfo
   */
  public ClientDataTagValue getProperty(PropertyInfo propertyInfo) throws MappedPropertyException;

  /**
   * Retrieve all properties of this device, except mapped properties.
   *
   * <p>
   * Note: this method will not return mapped properties.
   * </p>
   *
   * @return the properties map
   */
  public Map<String, ClientDataTagValue> getProperties();

  /**
   * Check if the property is a mapped property, i.e. is a property containing
   * nested fields.
   *
   * @param propertyName the name of the property to check
   * @return true if the property is a mapped property, false otherwise
   */
  public boolean isMappedProperty(String propertyName);

  /**
   * Retrieve a mapped property from the device, i.e. one containing nested
   * fields.
   *
   * @param propertyName the name of the mapped property to retrieve
   * @return the map of fields
   *
   * @throws MappedPropertyException if the requested property is not a mapped
   *           property
   */
  public Map<String, ClientDataTagValue> getMappedProperty(String propertyName) throws MappedPropertyException;

  /**
   * Retrieve the category of a given property/field, as defined in
   * {@link Category}.
   *
   * @param propertyInfo the object specifying the property/field you wish to
   *          determine the category of
   * @return the category of the property/field, or null if the property was not
   *         found
   *
   * @throws MappedPropertyException if an attempt is made to retrieve a field
   *           value from a non mapped property, or if a field name is not
   *           specified for a mapped property
   *
   * @see Category
   */
  public Category getCategoryForProperty(PropertyInfo propertyInfo) throws MappedPropertyException;

  /**
   * Retrieve a particular command of this device.
   *
   * @param commandName the name of the command to retrieve
   * @return the {@link ClientCommandTag} corresponding to the requested command
   */
  public ClientCommandTag getCommand(String commandName);

  /**
   * Return all commands of this device.
   *
   * @return the commands map
   */
  public Map<String, ClientCommandTag> getCommands();
}
