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
package cern.c2mon.shared.client.device;

import java.util.HashMap;
import java.util.Map;

import cern.c2mon.shared.util.json.GsonFactory;

import com.google.gson.Gson;

/**
 * This class implements the {@link TransferDevice} interface which defines the
 * transport object that is transferred to the client layer for initialising a
 * given <code>Device</code> object and its corresponding mapping between
 * properties and data tag IDs.
 *
 * @author Justin Lewis Salmon
 */
public class TransferDeviceImpl implements TransferDevice {

  /** Gson parser singleton */
  private static transient Gson gson = null;

  /** The unique device ID */
  private final Long id;

  /** The device name */
  private final String name;

  /** The device property mapping (property name : tag ID) */
  private Map<String, Long> propertyValues = new HashMap<>();

  /** The device command mapping (command name : tag ID) */
  private Map<String, Long> commandValues = new HashMap<>();

  /**
   * @return The Gson parser singleton instance to serialise/deserialise Json
   *         messages of that class
   */
  public static synchronized Gson getGson() {
    if (gson == null) {
      gson = GsonFactory.createGsonBuilder().create();
    }
    return gson;
  }

  /**
   * Default constructor.
   *
   * @param pDeviceId the unique device ID
   * @param pDeviceName the name of the device
   */
  public TransferDeviceImpl(final Long pDeviceId, final String pDeviceName) {
    this.id = pDeviceId;
    this.name = pDeviceName;
  }

  @Override
  public Long getId() {
    return this.id;
  }

  @Override
  public String getName() {
    return this.name;
  }

  @Override
  public Map<String, Long> getPropertyValues() {
    return this.propertyValues;
  }

  @Override
  public Map<String, Long> getCommandValues() {
    return this.commandValues;
  }

  /**
   * Add a property value (property name : tag ID mapping) to the transfer
   * object.
   *
   * @param propertyName the name of the property
   * @param tagId the ID of the tag to which the property corresponds
   */
  public void addPropertyValue(String propertyName, Long tagId) {
    this.propertyValues.put(propertyName, tagId);
  }

  /**
   * Add several property values (property name : tag ID mappings) to the
   * transfer object.
   *
   * @param propertyValues the map of property values to add
   */
  public void addPropertyValues(Map<String, Long> propertyValues) {
    this.propertyValues.putAll(propertyValues);
  }

  /**
   * Add a command value (command name : tag ID mapping) to the transfer object.
   *
   * @param commandName the name of the command
   * @param tagId the ID of the tag to which the command corresponds
   */
  public void addCommandValue(String commandName, Long tagId) {
    this.commandValues.put(commandName, tagId);
  }

  /**
   * Add several command values (command name : tag ID mappings) to the transfer
   * object.
   *
   * @param commandValues the map of command values to add
   */
  public void addCommandValues(Map<String, Long> commandValues) {
    this.commandValues.putAll(commandValues);
  }

  /**
   * Generates a JSON message out of this class instance.
   *
   * @return The serialised JSON representation of this class instance
   */
  public final String toJson() {
    return getGson().toJson(this);
  }

  /**
   * Deserialises the JSON string into a <code>DeviceTransfer</code> object
   * instance
   *
   * @param json A JSON string representation of a <code>DeviceTransfer</code>
   *          class
   * @return The deserialised <code>DeviceTransfer</code> instance of the JSON
   *         message
   */
  public static TransferDevice fromJson(final String json) {
    return getGson().fromJson(json, TransferDeviceImpl.class);
  }

}
