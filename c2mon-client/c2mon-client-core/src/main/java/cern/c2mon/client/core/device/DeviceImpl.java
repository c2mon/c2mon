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
package cern.c2mon.client.core.device;

import java.util.*;
import java.util.Map.Entry;

import lombok.extern.slf4j.Slf4j;

import cern.c2mon.client.common.tag.CommandTag;
import cern.c2mon.client.common.tag.Tag;
import cern.c2mon.client.core.device.listener.DeviceUpdateListener;
import cern.c2mon.client.core.device.property.*;
import cern.c2mon.client.core.tag.ClientRuleTag;
import cern.c2mon.shared.client.device.DeviceProperty;
import cern.c2mon.shared.rule.RuleFormatException;

/**
 * This class represents the <code>Device</code> aspect of the
 * class/device/property model. A Device is a concrete instance of a
 * <code>DeviceClass</code> and is composed of properties.
 *
 * @author Justin Lewis Salmon
 */
@Slf4j
public class DeviceImpl implements Device, Cloneable {

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
   * Note: the behaviour of this map is not equivalent to that of
   * {@link DeviceImpl#deviceProperties}. Commands will not be lazy-loaded, but
   * instead fully instantiated when the device is initially retrieved.
   * </p>
   */
  private Map<String, CommandTag<?>> deviceCommands = new HashMap<>();

  /**
   * Default constructor.
   * @param id the unique ID of the device
   * @param name the he name of the device (unique within the device class)
   * @param deviceClassId the ID of the class to which this device belongs
   * @param deviceClassName the unique name of the class to which this device belongs
   */
  public DeviceImpl(final Long id,
                    final String name,
                    final Long deviceClassId,
                    final String deviceClassName) {
    this.id = id;
    this.name = name;
    this.deviceClassId = deviceClassId;
    this.deviceClassName = deviceClassName;
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
      log.warn("Property " + propertyName + " does not exist in device.");
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
    return new ArrayList<>(deviceProperties.keySet());
  }

  @Override
  public CommandTag<?> getCommand(String commandName) {
    return deviceCommands.get(commandName);
  }

  @Override
  public Map<String, CommandTag<?>> getCommands() {
    Map<String, CommandTag<?>> commands = new HashMap<>();

    for (String commandName : this.deviceCommands.keySet()) {
      commands.put(commandName, getCommand(commandName));
    }

    return commands;
  }

  @Override
  public void addCommand(String name, CommandTag<?> command) {
    this.deviceCommands.put(name, command);
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
  protected Set<ClientRuleTag<?>> getRuleTags() {
    Set<ClientRuleTag<?>> ruleTags = new HashSet<>();

    for (Property deviceProperty : deviceProperties.values()) {
      PropertyImpl propertyImpl = (PropertyImpl) deviceProperty;

      if (propertyImpl.isRuleTag()) {
        ruleTags.add((ClientRuleTag<?>) deviceProperty.getTag());
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
   * here (fetching dependent {@link Tag}s if necessary).
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
  protected void setDeviceProperties(Map<String, Tag> deviceProperties) {
    for (Map.Entry<String, Tag> entry : deviceProperties.entrySet()) {
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
   * @param commands
   */
  public void setDeviceCommands(Map<String, CommandTag<?>> commands) {
    this.deviceCommands = commands;
  }

  /**
   * Update the property that corresponds to the given
   * {@link Tag}.
   *
   * @param tag the updated {@link Tag}
   * @param info a {@link PropertyInfo} object describing the property/field
   *          that was updated
   */
  protected PropertyInfo updateProperty(Tag tag) {
    PropertyInfo info = getPropertyInfoForTag(tag);

    if (info.getPropertyName() != null) {
      if (info.getFieldName() != null) {
        PropertyImpl property = (PropertyImpl) this.deviceProperties.get(info.getPropertyName());
        property.addField(info.getFieldName(), PropertyFactory.createField(info.getFieldName(), tag));

      } else {
        this.deviceProperties.put(info.getPropertyName(), PropertyFactory.createProperty(info.getPropertyName(), tag));
      }
    } else {
      log.warn("updateProperty() called with unmapped tag ID");
    }

    return info;
  }

  /**
   * Search the list of properties of this device to find the one that
   * corresponds to the given {@link Tag}.
   *
   * @param tag the updated {@link Tag}
   * @return info a {@link PropertyInfo} object describing the property/field
   *         that was updated
   */
  private PropertyInfo getPropertyInfoForTag(Tag tag) {
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

  @SuppressWarnings("unchecked")
  @Override
  protected Device clone() throws CloneNotSupportedException {
    DeviceImpl clone = (DeviceImpl) super.clone();

    clone.deviceProperties = (Map<String, Property>) ((HashMap<String, Property>) deviceProperties).clone();
    clone.deviceCommands = (Map<String, CommandTag<?>>) ((HashMap<String, CommandTag<?>>) deviceCommands).clone();

    return clone;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((deviceClassId == null) ? 0 : deviceClassId.hashCode());
    result = prime * result + ((id == null) ? 0 : id.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof DeviceImpl)) {
      return false;
    }
    DeviceImpl other = (DeviceImpl) obj;
    if (deviceClassId == null) {
      if (other.deviceClassId != null) {
        return false;
      }
    } else if (!deviceClassId.equals(other.deviceClassId)) {
      return false;
    }
    if (id == null) {
      if (other.id != null) {
        return false;
      }
    } else if (!id.equals(other.id)) {
      return false;
    }
    return true;
  }
}
