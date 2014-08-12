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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;

import cern.c2mon.client.common.listener.DataTagUpdateListener;
import cern.c2mon.client.common.tag.ClientCommandTag;
import cern.c2mon.client.common.tag.ClientDataTagValue;
import cern.c2mon.client.core.C2monTagManager;

/**
 * This class represents the <code>Device</code> aspect of the
 * class/device/property model. A Device is a concrete instance of a
 * <code>DeviceClass</code> and is composed of property values.
 *
 * @author Justin Lewis Salmon
 */
public class DeviceImpl implements Device, DataTagUpdateListener {

  /** Log4j logger for this class */
  private static final Logger LOG = Logger.getLogger(DeviceImpl.class);

  /**
   * The unique ID of the device.
   */
  private final Long id;

  /**
   * The name of the device.
   */
  private final String name;

  /**
   * The ID of the class to which this device belongs.
   */
  private final Long deviceClassId;

  /**
   * The name of the class to which this device belongs.
   */
  private final String deviceClassName;

  /**
   * The map of property names -> property values.
   *
   * <p>
   * Note: when retrieving a device via
   * {@link DeviceManager#getAllDevices(String)}, this map does not contain the
   * actual property values; they will be lazily loaded when you access a
   * particular value. However, when you subscribe to a device via
   * {@link DeviceManager#subscribeDevices(java.util.Set, DeviceUpdateListener)}
   * then all the device properties will exist in the map.
   * </p>
   */
  private Map<String, ?> propertyValues;

  /**
   * The map of command names -> command values.
   *
   * <p>
   * Note: the behaviour of this map is equivalent to that of
   * {@link DeviceImpl#propertyValues}.
   * </p>
   */
  private Map<String, ClientCommandTag> commandValues;

  /**
   * The set of all listeners which are subscribed to this device
   */
  private Set<DeviceUpdateListener> deviceUpdateListeners = new HashSet<DeviceUpdateListener>();

  /**
   * Reference to the <code>TagManager</code> singleton
   */
  private C2monTagManager tagManager;

  /**
   * Default constructor.
   */
  public DeviceImpl(final Long id, final String name, final Long deviceClassId, final String deviceClassName, C2monTagManager tagManager) {
    this.id = id;
    this.name = name;
    this.deviceClassId = deviceClassId;
    this.deviceClassName = deviceClassName;
    this.tagManager = tagManager;
  }

  @Override
  public Long getId() {
    return id;
  }

  @Override
  public String getName() {
    return name;
  }

  public Long getDeviceClassId() {
    return deviceClassId;
  }

  @Override
  public String getDeviceClassName() {
    return deviceClassName;
  }

  @Override
  public ClientDataTagValue getPropertyValue(String propertyName) {

    Object value = propertyValues.get(propertyName);

    if (value instanceof Long) {

      // If the internal map value is a Long, then we lazy load the data tag
      Long tagId = (Long) value;
      return tagManager.getDataTag(tagId);

    } else {
      return (ClientDataTagValue) propertyValues.get(propertyName);
    }
  }

  @Override
  public Map<String, ClientDataTagValue> getPropertyValues() {

    Map<String, ClientDataTagValue> propertyValues = new HashMap<>();

    for (String propertyName : this.propertyValues.keySet()) {
      propertyValues.put(propertyName, getPropertyValue(propertyName));
    }

    return propertyValues;
  }

  @Override
  public Map<String, ClientCommandTag> getCommandValues() {
    // TODO: implement this
    return commandValues;
  }

  /**
   * Retrieve the IDs of all properties of this device.
   *
   * @return the property value IDs of this device
   */
  protected Set<Long> getPropertyValueIds() {

    Set<Long> propertyValueIds = new HashSet<>();

    for (Object object : propertyValues.values()) {
      if (object instanceof Long) {
        propertyValueIds.add((Long) object);
      } else {
        propertyValueIds.add(((ClientDataTagValue) object).getId());
      }
    }

    return propertyValueIds;
  }

  /**
   * Manually set the properties of this device.
   *
   * @param propertyValues the property values to set
   */
  protected void setPropertyValues(Map<String, ?> propertyValues) {
    this.propertyValues = propertyValues;
  }

  /**
   * Register a {@link DeviceUpdateListener} that will be notified when a
   * property of this device changes.
   *
   * @param listener the listener to add
   */
  protected void addDeviceUpdateListener(DeviceUpdateListener listener) {
    deviceUpdateListeners.add(listener);
  }

  /**
   * Remove a {@link DeviceUpdateListener} that was previously registered to
   * receive updates about this device.
   *
   * @param listener the listener to remove
   */
  protected void removeDeviceUpdateListener(DeviceUpdateListener listener) {
    if (deviceUpdateListeners.contains(listener)) {
      deviceUpdateListeners.remove(listener);
    } else {
      LOG.debug("Trying to unregister a listener that is not registered: ignoring");
    }
  }

  /**
   * Retrieve all {@link DeviceUpdateListener}s currently registered with this
   * device.
   *
   * @return the set of DeviceUpdateListeners
   */
  protected Set<DeviceUpdateListener> getDeviceUpdateListeners() {
    return deviceUpdateListeners;
  }

  /**
   * Determine whether this device has any registered
   * {@link DeviceUpdateListener}s.
   *
   * @return true if this device has any registered update listeners, false
   *         otherwise
   */
  protected boolean hasUpdateListeners() {
    return deviceUpdateListeners.size() > 0;
  }

  @Override
  public final void onUpdate(ClientDataTagValue tagUpdate) {
    String propertyValueName = null;

    // Need to find the property name corresponding to this tag ID
    for (Entry<String, ?> entry : propertyValues.entrySet()) {
      Long tagId;

      if (entry.getValue() instanceof Long) {
        tagId = (Long) entry.getValue();

      } else {
        tagId = ((ClientDataTagValue) entry.getValue()).getId();
      }

      if (tagId.equals(tagUpdate.getId())) {
        propertyValueName = entry.getKey();
      }
    }

    // Update the property
    if (propertyValueName != null) {
      ((Map<String, ClientDataTagValue>) propertyValues).put(propertyValueName, tagUpdate);

    } else {
      LOG.warn("onUpdate() called with unmapped tag ID");
    }

    int numSparseEntries = 0;
    for (Object object : propertyValues.values()) {
      if (object instanceof Long)
        numSparseEntries++;
    }

    // Notify listeners only when all tag values are properly received
    if (numSparseEntries == 0) {
      for (DeviceUpdateListener listener : deviceUpdateListeners) {
        listener.onUpdate(this, tagUpdate.getName());
      }
    }
  }
}
