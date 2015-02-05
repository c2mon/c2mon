/*******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 *
 * Copyright (C) 2004 - 2015 CERN. This program is free software; you can
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
package cern.c2mon.client.ext.device.listener;

import java.util.HashSet;

import cern.c2mon.client.ext.device.Device;

/**
 * This class is a simple wrapper around a {@link DeviceUpdateListener} and a
 * set of {@link Device} objects, which is used to keep track of whether a
 * subscription to the set of devices has received its initial update on the
 * listener.
 *
 * <p>
 * Wrapping the listener in this way also allows the same set of devices to be
 * subscribed to with another listener.
 * </p>
 *
 * @author Justin Lewis Salmon
 */
public class ListenerWrapper {

  /**
   * Reference to the {@link DeviceUpdateListener}.
   */
  private DeviceUpdateListener listener;

  /**
   * Reference to the set of {@link Device} objects.
   */
  private HashSet<Device> devices;

  /**
   * Flag indicating whether this listener/device set combo has received its
   * initial update.
   */
  private boolean initialUpdateRequired = true;

  /**
   * Constructor.
   *
   * @param listener the {@link DeviceUpdateListener} to wrap
   * @param devices the set of {@link Device} objects to wrap
   */
  public ListenerWrapper(DeviceUpdateListener listener, HashSet<Device> devices) {
    this.listener = listener;
    this.devices = devices;
  }

  /**
   * Retrieve the wrapped {@link DeviceUpdateListener}.
   *
   * @return the listener
   */
  public DeviceUpdateListener getListener() {
    return listener;
  }

  /**
   * Retrieve the wrapped set of {@link Device} objects.
   *
   * @return the devices
   */
  public HashSet<Device> getDevices() {
    return devices;
  }

  /**
   * Check whether this listener has received its initial update.
   *
   * @return true if the
   *         {@link DeviceUpdateListener#onInitialUpdate(java.util.List)} method
   *         has not yet been called, false otherwise
   */
  public boolean isInitialUpdateRequired() {
    return initialUpdateRequired;
  }

  /**
   * Set the flag indicating whether this listener has received its initial
   * update.
   *
   * @param initialUpdateRequired the value to set
   */
  public void setInitialUpdateRequired(boolean initialUpdateRequired) {
    this.initialUpdateRequired = initialUpdateRequired;
  }
}
