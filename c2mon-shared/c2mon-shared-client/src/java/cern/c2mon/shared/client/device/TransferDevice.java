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
package cern.c2mon.shared.client.device;

import java.util.List;

import cern.c2mon.shared.client.request.ClientRequestResult;

/**
 * This interface defines the transport object that is transferred to the client
 * layer for initialising a given <code>Device</code> object and its
 * corresponding mapping between properties and data tag IDs.
 *
 * @author Justin Lewis Salmon
 */
public interface TransferDevice extends ClientRequestResult {

  /**
   * Retrieve the unique device ID.
   *
   * @return the device ID
   */
  public Long getId();

  /**
   * Retrieve the device name.
   *
   * @return the device name
   */
  public String getName();

  /**
   * Retrieve the ID of the device class.
   *
   * @return the device class ID
   */
  public Long getDeviceClassId();

  /**
   * Retrieve the property values of this device.
   *
   * @return the list of property values
   */
  public List<PropertyValue> getPropertyValues();

  /**
   * Retrieve the command values of this device.
   *
   * @return the list of command values
   */
  public List<CommandValue> getCommandValues();
}
