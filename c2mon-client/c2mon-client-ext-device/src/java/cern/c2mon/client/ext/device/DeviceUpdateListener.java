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

import cern.c2mon.client.ext.device.property.PropertyInfo;

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
   * When a particular property on a device changes, this method will be called.
   * A reference to the device itself will be given, along with a
   * {@link PropertyInfo} object which will contain information about the
   * property that has changed.
   *
   * <p>
   * If the updated property is a simple property (i.e. an atomic value, not a
   * mapped property) then the {@link PropertyInfo} object will contain the
   * property name and have a null field name.
   *
   * If it is a mapped property (i.e. contains nested fields) then the field
   * name will not be null; it will point to the field within the property that
   * was updated.
   * </p>
   *
   * <p>
   * Note: when you initially subscribe to a device, this method will be called
   * once with the device object containing all properties and with
   * <code>propertyInfo</code> as null. Thereafter, a single property will
   * change for any given listener invocation, i.e. the method will be invoked
   * multiple times to reflect multiple property changes.
   * </p>
   *
   * @param device the device in which a property has changed
   * @param propertyInfo the {@link PropertyInfo} object containing information
   *          about the property/field that has changed
   *
   * @see PropertyInfo
   */
  void onUpdate(Device device, PropertyInfo propertyInfo);
}
