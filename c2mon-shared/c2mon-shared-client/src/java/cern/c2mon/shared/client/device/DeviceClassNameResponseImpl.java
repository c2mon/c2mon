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

/**
 * @author Justin Lewis Salmon
 */
public class DeviceClassNameResponseImpl implements DeviceClassNameResponse {

  /** The unique name of the device class */
  public String deviceClassName;

  /**
   * Default constructor.
   *
   * @param deviceClassName the unique name of the device class
   */
  public DeviceClassNameResponseImpl(String deviceClassName) {
    this.deviceClassName = deviceClassName;
  }

  @Override
  public String getDeviceClassName() {
    return deviceClassName;
  }

  /**
   * @param deviceClassName the device class name to set
   */
  public void setDeviceClassName(String deviceClassName) {
    this.deviceClassName = deviceClassName;
  }
}
