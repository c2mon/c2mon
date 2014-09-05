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
   * Retrieve a particular property of this device.
   *
   * @param propertyName the name of the property to retrieve
   * @return the {@link ClientDataTagValue} corresponding to the requested
   *         property
   */
  public ClientDataTagValue getPropertyValue(String propertyName);

  /**
   * Retrieve all properties of this device.
   *
   * @return the property values map
   */
  public Map<String, ClientDataTagValue> getPropertyValues();

  /**
   * Retrieve a particular command of this device.
   *
   * @param commandName the name of the command to retrieve
   * @return the {@link ClientCommandTag} corresponding to the requested command
   */
  public ClientCommandTag getCommandValue(String commandName);

  /**
   * Return all commands of this device.
   *
   * @return the command values map
   */
  public Map<String, ClientCommandTag> getCommandValues();
}
