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

/**
 * Callback interface for a device. An update event is fired when a property on
 * a subscribed-to device changes.
 *
 * @see {@link Device}
 *
 * @author Justin Lewis Salmon
 */
public interface DeviceUpdateListener {

  /**
   * When a particular property on a device changes, this method will be called
   * with the name of the property that has changed.
   *
   * <p>
   * Note: when you initially subscribe to a device, this method will be called
   * with the device object containing all properties and with propertyName as
   * null. Thereafter, a single property will change for any given listener
   * invocation, i.e. the method will be invoked multiple times to reflect
   * multiple property changes.
   * </p>
   *
   * @param device the device in which a property has changed
   * @param propertyName the name of the property that has changed
   */
  void onUpdate(Device device, String propertyName);
}
