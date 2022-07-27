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
package cern.c2mon.shared.client.device;

import java.util.ArrayList;
import java.util.List;

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

  /** The device class ID */
  private final Long deviceClassId;

  /** The device class name */
  private final String deviceClassName;

  /** The device properties */
  private final List<DeviceProperty> deviceProperties = new ArrayList<>();

  /** The device commands */
  private final List<DeviceCommand> deviceCommands = new ArrayList<>();

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
   * @param pDeviceClassId the ID of the device class
   * @param pDeviceClassName the name of the device class
   */
  public TransferDeviceImpl(final Long pDeviceId, final String pDeviceName, final Long pDeviceClassId, final String pDeviceClassName) {
    this.id = pDeviceId;
    this.name = pDeviceName;
    this.deviceClassId = pDeviceClassId;
    this.deviceClassName = pDeviceClassName;
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
  public Long getDeviceClassId() {
    return this.deviceClassId;
  }

  @Override
  public String getDeviceClassName() {
    return this.deviceClassName;
  }

  @Override
  public List<DeviceProperty> getDeviceProperties() {
    return this.deviceProperties;
  }

  @Override
  public List<DeviceCommand> getDeviceCommands() {
    return this.deviceCommands;
  }

  /**
   * Add a device property to the transfer object.
   *
   * @param deviceProperty the property to add
   */
  public void addDeviceProperty(DeviceProperty deviceProperty) {
    this.deviceProperties.add(deviceProperty);
  }

  /**
   * Add several device properties to the transfer object.
   *
   * @param deviceProperties the list of properties to add
   */
  public void addDeviceProperties(List<DeviceProperty> deviceProperties) {
    this.deviceProperties.addAll(deviceProperties);
  }

  /**
   * Add a device command to the transfer object.
   *
   * @param deviceCommand the command to add
   */
  public void addDeviceCommand(DeviceCommand deviceCommand) {
    this.deviceCommands.add(deviceCommand);
  }

  /**
   * Add several device commands to the transfer object.
   *
   * @param deviceCommands the list of commands to add
   */
  public void addDeviceCommands(List<DeviceCommand> deviceCommands) {
    this.deviceCommands.addAll(deviceCommands);
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
