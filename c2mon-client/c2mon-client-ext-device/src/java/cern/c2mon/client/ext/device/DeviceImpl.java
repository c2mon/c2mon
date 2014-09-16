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

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;

import cern.c2mon.client.common.listener.DataTagUpdateListener;
import cern.c2mon.client.common.tag.ClientCommandTag;
import cern.c2mon.client.common.tag.ClientDataTagValue;
import cern.c2mon.client.core.C2monCommandManager;
import cern.c2mon.client.core.C2monTagManager;
import cern.c2mon.client.core.tag.ClientRuleTag;
import cern.c2mon.client.ext.device.property.ClientDeviceProperty;
import cern.c2mon.client.ext.device.property.ClientDevicePropertyFactory;
import cern.c2mon.shared.client.device.DeviceCommand;
import cern.c2mon.shared.client.device.DeviceProperty;
import cern.c2mon.shared.rule.RuleFormatException;

/**
 * This class represents the <code>Device</code> aspect of the
 * class/device/property model. A Device is a concrete instance of a
 * <code>DeviceClass</code> and is composed of properties.
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
   * The map of property names -> properties.
   *
   * <p>
   * Note: when retrieving a device via
   * {@link DeviceManager#getAllDevices(String)}, this map does not contain the
   * actual properties; they will be lazily loaded when you access a particular
   * value. However, when you subscribe to a device via
   * {@link DeviceManager#subscribeDevices(java.util.Set, DeviceUpdateListener)}
   * then all the device properties will exist in the map.
   * </p>
   */
  private Map<String, ClientDeviceProperty> deviceProperties = new HashMap<>();

  /**
   * The map of command names -> commands.
   *
   * <p>
   * Note: the behaviour of this map is equivalent to that of
   * {@link DeviceImpl#deviceProperties}.
   * </p>
   */
  private Map<String, ClientCommandTag> deviceCommands = new HashMap<>();

  /**
   * The set of all listeners which are subscribed to this device
   */
  private Set<DeviceUpdateListener> deviceUpdateListeners = new HashSet<DeviceUpdateListener>();

  /**
   * Reference to the <code>TagManager</code> singleton
   */
  private C2monTagManager tagManager;

  /**
   * Reference to the <code>CommandManager</code> singleton
   */
  private C2monCommandManager commandManager;

  /**
   * Default constructor.
   */
  public DeviceImpl(final Long id,
                    final String name,
                    final Long deviceClassId,
                    final String deviceClassName,
                    final C2monTagManager tagManager,
                    final C2monCommandManager commandManager) {
    this.id = id;
    this.name = name;
    this.deviceClassId = deviceClassId;
    this.deviceClassName = deviceClassName;
    this.tagManager = tagManager;
    this.commandManager = commandManager;
  }

  @Override
  public Long getId() {
    return id;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public String getDeviceClassName() {
    return deviceClassName;
  }

  @Override
  public ClientDataTagValue getProperty(String propertyName) {
    ClientDeviceProperty value = deviceProperties.get(propertyName);

    // If the internal map value is a Long, then we lazy load the data tag
    if (value.isDataTag() && !value.isSubscribed()) {
      Long tagId = value.getTagId();
      return tagManager.getDataTag(tagId);
    }

    // If it is a rule tag, we evaluate the rule (if it isn't subscribed)
    else if (value.isRuleTag() && !tagManager.isSubscribed((DataTagUpdateListener) value.getProperty())) {
      ClientRuleTag ruleTag = (ClientRuleTag) value.getProperty();

      // Get the data tag values from inside the rule
      Set<Long> tagIds = ruleTag.getRuleExpression().getInputTagIds();
      Collection<ClientDataTagValue> dataTagValues = tagManager.getDataTags(tagIds);

      // Update the rule tag
      for (ClientDataTagValue tagValue : dataTagValues) {
        ruleTag.onUpdate(tagValue);
      }

      return ruleTag;

    } else {
      return value.getProperty();
    }
  }

  @Override
  public Map<String, ClientDataTagValue> getProperties() {
    Map<String, ClientDataTagValue> deviceProperties = new HashMap<>();

    for (String propertyName : this.deviceProperties.keySet()) {
      deviceProperties.put(propertyName, getProperty(propertyName));
    }

    return deviceProperties;
  }

  @Override
  public ClientCommandTag<?> getCommand(String commandName) {
    return deviceCommands.get(commandName);
  }

  @Override
  public Map<String, ClientCommandTag> getCommands() {
    Map<String, ClientCommandTag> deviceCommands = new HashMap<>();

    for (String commandName : this.deviceCommands.keySet()) {
      deviceCommands.put(commandName, getCommand(commandName));
    }

    return deviceCommands;
  }

  /**
   * Get the ID of the device class to which this device belongs.
   *
   * @return the device class ID
   */
  public Long getDeviceClassId() {
    return deviceClassId;
  }

  /**
   * Retrieve the IDs of all properties of this device that are data tags.
   *
   * @return the data tag property IDs of this device
   */
  protected Set<Long> getPropertyDataTagIds() {
    Set<Long> propertyDataTagIds = new HashSet<>();

    for (ClientDeviceProperty deviceProperty : deviceProperties.values()) {
      if (deviceProperty.isDataTag()) {
        propertyDataTagIds.add(deviceProperty.getTagId());
      }
    }

    return propertyDataTagIds;
  }

  /**
   * Retrieve all properties of this device which are in fact
   * {@link ClientRuleTag}s.
   *
   * @return the set of client rule tags
   */
  protected Set<ClientRuleTag> getRuleTags() {
    Set<ClientRuleTag> ruleTags = new HashSet<>();

    for (ClientDeviceProperty deviceProperty : deviceProperties.values()) {
      if (deviceProperty.isRuleTag()) {
        ruleTags.add((ClientRuleTag) deviceProperty.getProperty());
      }
    }

    return ruleTags;
  }

  /**
   * Manually set the properties of this device.
   *
   * <p>
   * If any of the given properties are client rules, the appropriate
   * {@link ClientRuleTag} object will be created and the rule will be evaluated
   * here (fetching dependent {@link ClientDataTagValue}s if necessary).
   * However, the dependent data tags will not be subscribed to until the entire
   * device is subscribed to (via
   * {@link DeviceManager#subscribeDevices(Set, DeviceUpdateListener)}.
   * </p>
   *
   * @param deviceProperties the properties to set
   *
   * @throws RuleFormatException if a property is a client rule and is malformed
   * @throws ClassNotFoundException if the result type of a property cannot be
   *           found
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  protected void setDeviceProperties(List<DeviceProperty> deviceProperties) throws RuleFormatException, ClassNotFoundException {

    for (DeviceProperty deviceProperty : deviceProperties) {
      this.deviceProperties.put(deviceProperty.getName(), ClientDevicePropertyFactory.createClientDeviceProperty(deviceProperty));
    }
  }

  /**
   * Manually set the properties of this device (for testing).
   *
   * @param deviceProperties the properties to set
   */
  protected void setDeviceProperties(Map<String, ClientDataTagValue> deviceProperties) {
    for (Map.Entry<String, ClientDataTagValue> entry : deviceProperties.entrySet()) {
      this.deviceProperties.put(entry.getKey(), new ClientDeviceProperty(entry.getValue()));
    }
  }

  /**
   * Manually set the commands of this device.
   *
   * @param deviceCommands
   */
  public void setDeviceCommands(List<DeviceCommand> deviceCommands) {
    for (DeviceCommand deviceCommand : deviceCommands) {
      // We don't need to lazy-load command tags, so just get them here
      this.deviceCommands.put(deviceCommand.getName(), commandManager.getCommandTag(deviceCommand.getTagId()));
    }
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
    String propertyName = null;

    // Need to find the property name corresponding to this tag ID
    for (Entry<String, ClientDeviceProperty> entry : deviceProperties.entrySet()) {
      ClientDeviceProperty deviceProperty = entry.getValue();

      if (deviceProperty.isDataTag() && deviceProperty.getTagId().equals(tagUpdate.getId())) {
        propertyName = entry.getKey();
      }
    }

    // Update the property
    if (propertyName != null) {
      this.deviceProperties.put(propertyName, new ClientDeviceProperty(tagUpdate));

    } else {
      LOG.warn("onUpdate() called with unmapped tag ID");
    }

    int numUnsubscribedProperties = 0;
    for (ClientDeviceProperty deviceProperty : deviceProperties.values()) {
      if (deviceProperty.isDataTag() && !deviceProperty.isSubscribed()) {
        numUnsubscribedProperties++;
      }
    }

    // Notify listeners only when all tag values are properly received
    if (numUnsubscribedProperties == 0) {
      for (DeviceUpdateListener listener : deviceUpdateListeners) {
        listener.onUpdate(this, tagUpdate.getName());
      }
    }
  }
}
