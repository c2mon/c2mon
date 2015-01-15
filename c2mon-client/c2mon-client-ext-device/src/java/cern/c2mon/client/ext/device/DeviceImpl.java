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

import org.apache.log4j.Logger;

import cern.c2mon.client.common.tag.ClientCommandTag;
import cern.c2mon.client.common.tag.ClientDataTagValue;
import cern.c2mon.client.core.C2monCommandManager;
import cern.c2mon.client.core.C2monTagManager;
import cern.c2mon.client.core.tag.ClientRuleTag;
import cern.c2mon.client.ext.device.property.Field;
import cern.c2mon.client.ext.device.property.FieldImpl;
import cern.c2mon.client.ext.device.property.Property;
import cern.c2mon.client.ext.device.property.PropertyFactory;
import cern.c2mon.client.ext.device.property.PropertyImpl;
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
public class DeviceImpl implements Device, Cloneable {

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
  private Map<String, Property> deviceProperties = new HashMap<>();

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
  public Property getProperty(String propertyName) {
    PropertyImpl property = (PropertyImpl) deviceProperties.get(propertyName);

    // If we didn't find the property, just return null
    if (property == null) {
      LOG.warn("Property " + propertyName + " does not exist in device.");
      return null;
    }

    return property;
  }

  @Override
  public List<Property> getProperties() {
    List<Property> properties = new ArrayList<>();

    for (Property property : deviceProperties.values()) {
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

    for (Property deviceProperty : deviceProperties.values()) {
      PropertyImpl propertyImpl = (PropertyImpl) deviceProperty;

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

    for (Property deviceProperty : deviceProperties.values()) {
      PropertyImpl propertyImpl = (PropertyImpl) deviceProperty;

      if (propertyImpl.isRuleTag()) {
        ruleTags.add((ClientRuleTag) deviceProperty.getTag());
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
      this.deviceProperties.put(deviceProperty.getName(), PropertyFactory.createProperty(deviceProperty));
    }
  }

  /**
   * Manually set the properties of this device (for testing).
   *
   * @param deviceProperties the properties to set
   */
  protected void setDeviceProperties(Map<String, ClientDataTagValue> deviceProperties) {
    for (Map.Entry<String, ClientDataTagValue> entry : deviceProperties.entrySet()) {
      this.deviceProperties.put(entry.getKey(), PropertyFactory.createProperty(entry.getKey(), entry.getValue()));
    }
  }

  /**
   * Manually set the properties of this device (for testing).
   *
   * @param deviceProperties the properties to set
   */
  protected void setDeviceProperties(HashMap<String, Property> deviceProperties) {
    this.deviceProperties = deviceProperties;
  }

  /**
   * Retrieve the raw map of device properties from this device (for testing)
   *
   * @return the map of raw device properties
   */
  protected Map<String, Property> getDeviceProperties() {
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
   * Update the property that corresponds to the given
   * {@link ClientDataTagValue}.
   *
   * @param tag the updated {@link ClientDataTagValue}
   * @param info a {@link PropertyInfo} object describing the property/field
   *          that was updated
   */
  protected PropertyInfo updateProperty(ClientDataTagValue tag) {
    PropertyInfo info = getPropertyInfoForTag(tag);

    if (info.getPropertyName() != null) {
      if (info.getFieldName() != null) {
        PropertyImpl property = (PropertyImpl) this.deviceProperties.get(info.getPropertyName());
        property.addField(info.getFieldName(), (Field) PropertyFactory.createField(info.getFieldName(), tag));

      } else {
        this.deviceProperties.put(info.getPropertyName(), PropertyFactory.createProperty(info.getPropertyName(), tag));
      }
    } else {
      LOG.warn("updateProperty() called with unmapped tag ID");
    }

    return info;
  }

  /**
   * Search the list of properties of this device to find the one that
   * corresponds to the given {@link ClientDataTagValue}.
   *
   * @param tag the updated {@link ClientDataTagValue}
   * @return info a {@link PropertyInfo} object describing the property/field
   *         that was updated
   */
  private PropertyInfo getPropertyInfoForTag(ClientDataTagValue tag) {
    String propertyName = null;
    String fieldName = null;

    // Need to find the property name corresponding to this tag ID
    for (Entry<String, Property> propertyEntry : deviceProperties.entrySet()) {
      PropertyImpl deviceProperty = (PropertyImpl) propertyEntry.getValue();

      if (deviceProperty.isDataTag() && deviceProperty.getTagId().equals(tag.getId())) {
        propertyName = propertyEntry.getKey();

      } else if (deviceProperty.isMappedProperty()) {
        // Check if the update is on a field
        for (Field field : deviceProperty.getFields()) {
          FieldImpl fieldImpl = (FieldImpl) field;

          if (fieldImpl.isDataTag() && fieldImpl.getTagId().equals(tag.getId())) {
            propertyName = propertyEntry.getKey();
            fieldName = fieldImpl.getName();
          }
        }
      }
    }

    return fieldName == null ? new PropertyInfo(propertyName) : new PropertyInfo(propertyName, fieldName);
  }

  @Override
  protected Device clone() throws CloneNotSupportedException {
    DeviceImpl clone = (DeviceImpl) super.clone();

    clone.deviceProperties = (Map<String, Property>) ((HashMap<String, Property>) deviceProperties).clone();
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
    for (Property property : deviceProperties.values()) {
      ((PropertyImpl) property).setTagManager(tagManager);
    }
  }
}
