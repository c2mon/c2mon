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
package cern.c2mon.client.core.device.listener;

import java.util.List;

import cern.c2mon.client.core.device.Device;
import cern.c2mon.client.core.device.property.PropertyInfo;

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
   * This method is only called once before the listener gets subscribed to the
   * device properties, in order to provide the initial values.
   *
   * <p>
   * Please note, that this method has to return in order to not block the
   * listener subscription. Only after it returns will the
   * {@link #onUpdate(Device, PropertyInfo)} be called.
   * </p>
   *
   * @param devices the list of devices containing the initial values of the
   *          tags to which the listener subscribed to.
   *
   */
  void onInitialUpdate(List<Device> devices);

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
   * Note: only a single property will change for any given listener invocation,
   * i.e. the method will be invoked multiple times to reflect multiple property
   * changes.
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
