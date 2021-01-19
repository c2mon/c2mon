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

package cern.c2mon.client.core.service;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import cern.c2mon.client.core.device.Device;
import cern.c2mon.client.core.device.exception.DeviceNotFoundException;
import cern.c2mon.client.core.device.listener.DeviceInfoUpdateListener;
import cern.c2mon.client.core.device.listener.DeviceUpdateListener;
import cern.c2mon.client.core.device.property.PropertyInfo;
import cern.c2mon.shared.client.device.DeviceInfo;

/**
 * This interface describes the methods which are provided by the C2MON device
 * manager singleton. The device manager handles communication with the C2MON
 * server to retrieve information following the class/device/property pattern.
 *
 * @author Justin Lewis Salmon
 */
public interface DeviceService {

  /**
   * This method allows you to retrieve a list of the names of all device
   * classes. You can then call {@link #getAllDevices(String)} to retrieve a
   * particular device.
   *
   * @return the list of all device class names
   */
  List<String> getAllDeviceClassNames();

  /**
   * Retrieve a single device.
   *
   * <p>
   * Note: retrieving a device using this method does not automatically
   * subscribe to that device. Accessing a property of the device will fetch the
   * value from the server only once. To receive updates about property changes,
   * use one of the provided subscription methods.
   * </p>
   *
   * @param info the {@link DeviceInfo} object describing the device you wish to
   *          subscribe to
   * @return the requested device
   *
   * @throws DeviceNotFoundException if no device was found that matches the
   *           given {@link DeviceInfo} description
   */
  Device getDevice(DeviceInfo info) throws DeviceNotFoundException;

  /**
   * Retrieve all devices of a particular class.
   *
   * <p>
   * Note: retrieving devices using this method does not automatically subscribe
   * to those devices. Accessing a property of a particular device will fetch
   * the value from the server only once. To receive updates about property
   * changes, use {@link #subscribeDevices(Set, DeviceUpdateListener)}.
   * </p>
   *
   * @param deviceClassName the name of the class of devices to retrieve
   * @return the list of devices of the specified class
   */
  List<Device> getAllDevices(final String deviceClassName);

  /**
   * Subscribe to retrieve updates of property changes of a device.
   *
   * <p>
   * Subscribing to a device means subscribing to all the properties of that
   * device. When a particular property changes, the given
   * {@link DeviceUpdateListener#onUpdate(Device, PropertyInfo)} method will be
   * called with the device itself and the name of the property that has
   * changed.
   * </p>
   *
   * <p>
   * When a device is first subscribed to, the {@link
   * DeviceUpdateListener#onInitialUpdate(List<Device>)} method is passed a
   * single-item list containing a reference to the fully initialised device.
   * Note that the {@link DeviceUpdateListener#onUpdate(Device, PropertyInfo)}
   * method is guaranteed not to be called until this method returns.
   * </p>
   *
   * <p>
   * Note: only a single property will change for any given listener invocation.
   * The listener will be invoked multiple times to reflect multiple property
   * changes.
   * </p>
   *
   * @param device the device you want to subscribe to
   * @param listener the callback listener that will be notified when the device
   *          is done initialising, and when a device property changes
   *
   * @see DeviceUpdateListener
   */
  void subscribeDevice(final Device device, final DeviceUpdateListener listener);

  /**
   * Subscribe to retrieve updates of property changes of a set of devices.
   *
   * <p>
   * Subscribing to a device means subscribing to all the properties of that
   * device. When a particular property changes, the given
   * {@link DeviceUpdateListener#onUpdate(Device, PropertyInfo)} method will be
   * called with the device itself and the name of the property that has
   * changed.
   * </p>
   *
   * <p>
   * When a device is first subscribed to, the {@link
   * DeviceUpdateListener#onInitialUpdate(List<Device>))} method is passed a
   * list containing references to the fully initialised devices. Note that the
   * {@link DeviceUpdateListener#onUpdate(Device, PropertyInfo)} method is
   * guaranteed not to be called until this method returns.
   * </p>
   *
   * <p>
   * Note: only a single property will change for any given listener invocation.
   * The listener will be invoked multiple times to reflect multiple property
   * changes.
   * </p>
   *
   * @param devices the set of devices you want to subscribe to
   * @param listener the callback listener that will be notified when a device
   *          property changes
   */
  void subscribeDevices(final Set<Device> devices, final DeviceUpdateListener listener);

  /**
   * Subscribe to retrieve updates of property changes of a device by name.
   *
   * <p>
   * Subscribing to a device means subscribing to all the properties of that
   * device. When a particular property changes, the given
   * {@link DeviceUpdateListener#onUpdate(Device, PropertyInfo)} method will be
   * called with the device itself and the name of the property that has
   * changed.
   * </p>
   *
   * <p>
   * When a device is first subscribed to, the {@link
   * DeviceUpdateListener#onInitialUpdate(List<Device>))} method is passed a
   * single-item list containing a reference to the fully initialised device.
   * Note that the {@link DeviceUpdateListener#onUpdate(Device, PropertyInfo)}
   * method is guaranteed not to be called until this method returns.
   * </p>
   *
   * <p>
   * If the device is not found on the server, the
   * {@link DeviceInfoUpdateListener#onDevicesNotFound(List)} method of the listener
   * will be called.
   * </p>
   *
   * <p>
   * Note: only a single property will change for any given listener invocation.
   * The listener will be invoked multiple times to reflect multiple property
   * changes.
   * </p>
   *
   * @param info the {@link DeviceInfo} object describing the device you wish to
   *          subscribe to
   * @param listener the callback listener that will be notified when a device
   *          property changes
   *
   * @see DeviceInfo
   */
  void subscribeDevice(DeviceInfo info, final DeviceInfoUpdateListener listener);

  /**
   * Subscribe to retrieve updates of property changes of a set of devices by
   * name.
   *
   * <p>
   * Subscribing to a device means subscribing to all the properties of that
   * device. When a particular property changes, the given
   * {@link DeviceUpdateListener#onUpdate(Device, PropertyInfo)} method will be
   * called with the device itself and the name of the property that has
   * changed.
   * </p>
   *
   * <p>
   * When a device is first subscribed to, the {@link
   * DeviceUpdateListener#onInitialUpdate(List<Device>))} method is passed a
   * list containing references to the fully initialised devices. Note that the
   * {@link DeviceUpdateListener#onUpdate(Device, PropertyInfo)} method is
   * guaranteed not to be called until this method returns.
   * </p>
   *
   * <p>
   * This method takes a set of {@link DeviceInfo} objects, each of which
   * describes a device by its class and its name. If any devices described are
   * not found by the server, then the
   * {@link DeviceUpdateListener#onDevicesNotFound(List)} method will be called,
   * where its argument contains a list of {@link DeviceInfo} objects that were
   * not found.
   * </p>
   *
   * <p>
   * Note: only a single property will change for any given listener invocation.
   * The listener will be invoked multiple times to reflect multiple property
   * changes.
   * </p>
   *
   * @param deviceInfoList the list of {@link DeviceInfo} objects describing the
   *          devices you wish to subscribe to
   * @param listener the callback listener that will be notified when a device
   *          property changes, and also possibly when unknown devices were
   *          requested
   *
   * @see DeviceInfo
   */
  void subscribeDevices(final Set<DeviceInfo> deviceInfoList, final DeviceInfoUpdateListener listener);

  /**
   * Unsubscribe from a previously subscribed-to device.
   *
   * @param device the device you want to unsubscribe from
   * @param listener the listener that was previously registered
   */
  void unsubscribeDevice(final Device device, final DeviceUpdateListener listener);

  /**
   * Unsubscribe from a previously subscribed-to set of devices.
   *
   * @param devices the set of devices you want to unsubscribe from
   * @param listener the listener that was previously registered
   */
  void unsubscribeDevices(final Set<Device> devices, final DeviceUpdateListener listener);

  /**
   * Unsubscribe from all previously subscribed-to devices.
   *
   * @param listener the listener that was previously registered
   */
  void unsubscribeAllDevices(final DeviceUpdateListener listener);

  /**
   * Retrieve all devices that are subscribed to receive updates on a particular
   * {@link DeviceUpdateListener}.
   *
   * @param listener the listener that was previously used to make a
   *          subscription
   * @return the list of devices that were subscribed to using the given
   *         listener
   */
  Collection<Device> getAllSubscribedDevices(DeviceUpdateListener listener);
}
