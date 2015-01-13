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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import org.apache.log4j.Logger;

import cern.c2mon.client.common.listener.DataTagUpdateListener;
import cern.c2mon.client.common.tag.ClientCommandTag;
import cern.c2mon.client.common.tag.ClientDataTagValue;
import cern.c2mon.client.core.C2monCommandManager;
import cern.c2mon.client.core.C2monTagManager;
import cern.c2mon.client.core.tag.ClientRuleTag;
import cern.c2mon.client.ext.device.property.ClientDeviceProperty;
import cern.c2mon.client.ext.device.property.ClientDevicePropertyFactory;
import cern.c2mon.client.ext.device.property.ClientDevicePropertyImpl;
import cern.c2mon.client.ext.device.property.PropertyInfo;
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
public class DeviceImpl implements Device, DataTagUpdateListener, Cloneable {

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
   * Countdown latch used to keep track of the number of data tags waiting to be
   * subscribed-to
   */
  private CountDownLatch subscriptionCompletionLatch = new CountDownLatch(0);

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
  public ClientDeviceProperty getProperty(String propertyName) {
    ClientDevicePropertyImpl property = (ClientDevicePropertyImpl) deviceProperties.get(propertyName);

    // If we didn't find the property, just return null
    if (property == null) {
      LOG.warn("Property " + propertyName + " does not exist in device.");
      return null;
    }

    return property;
  }

  @Override
  public List<ClientDeviceProperty> getProperties() {
    List<ClientDeviceProperty> properties = new ArrayList<>();

    for (ClientDeviceProperty property : deviceProperties.values()) {
      properties.add(getProperty(property.getName()));
    }

    return properties;
  }

  @Override
  public List<String> getPropertyNames() {
    return new ArrayList<String>(deviceProperties.keySet());
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
   * Retrieve the IDs of all properties or fields of this device that are data
   * tags.
   *
   * @return the data tag property/field IDs of this device
   */
  protected Set<Long> getPropertyDataTagIds() {
    Set<Long> propertyDataTagIds = new HashSet<>();

    for (ClientDeviceProperty deviceProperty : deviceProperties.values()) {
      ClientDevicePropertyImpl propertyImpl = (ClientDevicePropertyImpl) deviceProperty;

      if (propertyImpl.isDataTag()) {
        propertyDataTagIds.add(propertyImpl.getTagId());
      }

      if (propertyImpl.isMappedProperty()) {
        propertyDataTagIds.addAll(propertyImpl.getFieldDataTagIds());
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
      ClientDevicePropertyImpl propertyImpl = (ClientDevicePropertyImpl) deviceProperty;

      if (propertyImpl.isRuleTag()) {
        ruleTags.add((ClientRuleTag) deviceProperty.getDataTag());
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
   * Manually set the properties of this device.
   *
   * @param deviceProperties the properties to set
   */
  protected void setDeviceProperties(ArrayList<ClientDataTagValue> deviceProperties) {
    Map<String, ClientDeviceProperty> newDeviceProperties = new HashMap<>();

    for (Map.Entry<String, ClientDeviceProperty> property : this.deviceProperties.entrySet()) {
      for (ClientDataTagValue dataTag : deviceProperties) {
        if (dataTag.getId().equals(property.getValue().getTagId())) {
          newDeviceProperties.put(property.getKey(), ClientDevicePropertyFactory.createClientDeviceProperty(property.getValue().getName(), dataTag));
        }
      }
    }
  }

  /**
   * Manually set the properties of this device (for testing).
   *
   * @param deviceProperties the properties to set
   */
  protected void setDeviceProperties(Map<String, ClientDataTagValue> deviceProperties) {
    for (Map.Entry<String, ClientDataTagValue> entry : deviceProperties.entrySet()) {
      this.deviceProperties.put(entry.getKey(), ClientDevicePropertyFactory.createClientDeviceProperty(entry.getKey(), entry.getValue()));
    }
  }

  /**
   * Manually set the properties of this device (for testing).
   *
   * @param deviceProperties the properties to set
   */
  protected void setDeviceProperties(HashMap<String, ClientDeviceProperty> deviceProperties) {
    this.deviceProperties = deviceProperties;
  }

  /**
   * Retrieve the raw map of device properties from this device (for testing)
   *
   * @return the map of raw device properties
   */
  protected Map<String, ClientDeviceProperty> getDeviceProperties() {
    return deviceProperties;
  }

  /**
   * Manually set the commands of this device.
   *
   * @param deviceCommands
   */
  public void setDeviceCommands(List<DeviceCommand> deviceCommands) {
    for (DeviceCommand deviceCommand : deviceCommands) {
      // We don't need to lazy-load command tags, so just get them here
      this.deviceCommands.put(deviceCommand.getName(), commandManager.getCommandTag(Long.valueOf(deviceCommand.getValue())));
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

  /**
   * Initialise the tag subscription latch. This will cause any listeners not to
   * be called until numDataTags data tags have been received from the server.
   * This is used to wait for all tags to arrive in order to return a fully
   * populated device when a client calls
   * {@link DeviceManager#subscribeDevice(Device, DeviceUpdateListener)}.
   *
   * @param numDataTags the number of data tags to wait for
   */
  protected void initSubscriptionLatch(int numDataTags) {
    subscriptionCompletionLatch = new CountDownLatch(numDataTags);
    LOG.trace("Subscription latch init: " + subscriptionCompletionLatch.getCount());
  }

  /**
   * Block and wait for all tags that were subscribed-to to be received.
   */
  protected void awaitCompleteSubscription() {
    if (subscriptionCompletionLatch != null) {
      try {
        LOG.trace("Waiting on subscription completion latch");
        subscriptionCompletionLatch.await();
      } catch (InterruptedException e) {
        LOG.error("Unable to await complete subscription for device " + getName(), e);
        throw new UnsupportedOperationException("Unable to await complete subscription for device " + getName(), e);
      }
    }
  }

  @Override
  public final void onUpdate(ClientDataTagValue tagUpdate) {
    String propertyName = null;
    String fieldName = null;

    // Need to find the property name corresponding to this tag ID
    for (Entry<String, ClientDeviceProperty> propertyEntry : deviceProperties.entrySet()) {
      ClientDevicePropertyImpl deviceProperty = (ClientDevicePropertyImpl) propertyEntry.getValue();

      if (deviceProperty.isDataTag() && deviceProperty.getTagId().equals(tagUpdate.getId())) {
        propertyName = propertyEntry.getKey();

      } else if (deviceProperty.isMappedProperty()) {
        // Check if the update is on a field
        for (ClientDeviceProperty field : deviceProperty.getFields()) {
          ClientDevicePropertyImpl fieldImpl = (ClientDevicePropertyImpl) field;

          if (fieldImpl.isDataTag() && fieldImpl.getTagId().equals(tagUpdate.getId())) {
            propertyName = propertyEntry.getKey();
            fieldName = fieldImpl.getName();
          }
        }
      }
    }

    // Update the property
    if (propertyName != null) {

      if (fieldName != null) {
        ClientDevicePropertyImpl property = (ClientDevicePropertyImpl) this.deviceProperties.get(propertyName);
        property.addField(fieldName, ClientDevicePropertyFactory.createClientDeviceProperty(fieldName, tagUpdate));

      } else {
        this.deviceProperties.put(propertyName, ClientDevicePropertyFactory.createClientDeviceProperty(propertyName, tagUpdate));
      }
    } else {
      LOG.warn("onUpdate() called with unmapped tag ID");
    }

    if (subscriptionCompletionLatch.getCount() > 0) {

      // If there are unsubscribed properties, count down the latch and return
      // without calling onUpdate()
      LOG.trace("Counting down on subscription latch (" + subscriptionCompletionLatch.getCount() + " remaining)");
      subscriptionCompletionLatch.countDown();
      return;
    }

    for (DeviceUpdateListener listener : deviceUpdateListeners) {
      try {
        LOG.trace("Invoking DeviceUpdateListener");
        PropertyInfo propertyInfo = fieldName == null ? new PropertyInfo(propertyName) : new PropertyInfo(propertyName, fieldName);
        listener.onUpdate(this.clone(), propertyInfo);

      } catch (CloneNotSupportedException e) {
        LOG.error("Unable to clone Device with id " + getId(), e);
        throw new UnsupportedOperationException("Unable to clone Device with id " + getId(), e);
      }
    }
  }

  @Override
  protected Device clone() throws CloneNotSupportedException {
    DeviceImpl clone = (DeviceImpl) super.clone();

    clone.deviceProperties = (Map<String, ClientDeviceProperty>) ((HashMap<String, ClientDeviceProperty>) deviceProperties).clone();
    clone.deviceCommands = (Map<String, ClientCommandTag>) ((HashMap<String, ClientCommandTag>) deviceCommands).clone();

    return clone;
  }

  /**
   * Manually set the reference to the {@link C2monTagManager} inside the device
   * and also inside all device properties. Used for testing purposes.
   *
   * @param tagManager the tag manager to use
   */
  public void setTagManager(C2monTagManager tagManager) {
    this.tagManager = tagManager;
    for (ClientDeviceProperty property : deviceProperties.values()) {
      ((ClientDevicePropertyImpl) property).setTagManager(tagManager);
    }
  }
}
