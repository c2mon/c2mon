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
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jms.JMSException;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cern.c2mon.client.common.listener.DataTagListener;
import cern.c2mon.client.common.tag.ClientDataTagValue;
import cern.c2mon.client.core.C2monCommandManager;
import cern.c2mon.client.core.C2monTagManager;
import cern.c2mon.client.core.cache.BasicCacheHandler;
import cern.c2mon.client.core.tag.ClientRuleTag;
import cern.c2mon.client.ext.device.cache.DeviceCache;
import cern.c2mon.client.ext.device.property.PropertyInfo;
import cern.c2mon.client.ext.device.request.DeviceRequestHandler;
import cern.c2mon.shared.client.device.DeviceClassNameResponse;
import cern.c2mon.shared.client.device.DeviceInfo;
import cern.c2mon.shared.client.device.TransferDevice;
import cern.c2mon.shared.rule.RuleFormatException;

/**
 * This class implements the {@link C2monDeviceManager} interface and provides a
 * concrete class for interacting with the C2MON server following the
 * class/device/property model.
 *
 * @author Justin Lewis Salmon
 */
@Service
public class DeviceManager implements C2monDeviceManager, DataTagListener {

  /** Log4j logger for this class */
  private static final Logger LOG = Logger.getLogger(DeviceManager.class);

  /** Reference to the <code>TagManager</code> singleton */
  private final C2monTagManager tagManager;

  /** Reference to the <code>TagManager</code> singleton */
  private final C2monCommandManager commandManager;

  /** Reference to the <code>BasicCacheHandler</code> singleton */
  private final BasicCacheHandler dataTagCache;

  /** Reference to the <code>DeviceCache</code> singleton */
  private final DeviceCache deviceCache;

  /** Provides methods for requesting information from the C2MON server */
  private final DeviceRequestHandler requestHandler;

  /**
   * Map of {@link DeviceUpdateListener}s to a set of {@link Device}s.
   */
  private Map<DeviceUpdateListener, Set<Device>> deviceUpdateListeners = new HashMap<>();

  /**
   * Default Constructor, used by Spring to instantiate the Singleton service.
   *
   * @param pTagManager the TagManager singleton reference
   * @param pDataTagCache the data tag cache handler singleton reference
   * @param pDeviceCache the device cache handler singleton reference
   * @param pRequestHandler provides methods for requesting information from the
   *          C2MON server
   */
  @Autowired
  protected DeviceManager(final C2monTagManager pTagManager,
                          final BasicCacheHandler pDataTagCache,
                          final DeviceCache pDeviceCache,
                          final DeviceRequestHandler pRequestHandler,
                          final C2monCommandManager pCommandManager) {
    this.tagManager = pTagManager;
    this.dataTagCache = pDataTagCache;
    this.deviceCache = pDeviceCache;
    this.requestHandler = pRequestHandler;
    this.commandManager = pCommandManager;
  }

  @Override
  public List<String> getAllDeviceClassNames() {
    List<String> deviceClassNames = new ArrayList<>();

    // Ask the server for all class names via RequestHandler
    try {
      Collection<DeviceClassNameResponse> serverResponse = requestHandler.getAllDeviceClassNames();

      for (DeviceClassNameResponse className : serverResponse) {
        deviceClassNames.add(className.getDeviceClassName());
      }
    } catch (JMSException e) {
      LOG.error("getAllDeviceClassNames() - JMS connection lost -> Could not retrieve device class names from the C2MON server.", e);
    }

    return deviceClassNames;
  }

  @Override
  public List<Device> getAllDevices(String deviceClassName) {

    // Try to get the devices out of the cache
    // TODO: what if only a subset of devices of this class are cached?
    List<Device> devices = deviceCache.getAllDevices(deviceClassName);
    if (devices.isEmpty()) {

      // Didn't find any devices of that class in the cache, so call the
      // server
      try {
        Collection<TransferDevice> serverResponse = requestHandler.getAllDevices(deviceClassName);

        for (TransferDevice transferDevice : serverResponse) {
          Device device = createClientDevice(transferDevice, deviceClassName);
          devices.add(device);
        }
      } catch (JMSException e) {
        LOG.error("getAllDevices() - JMS connection lost -> Could not retrieve devices from the C2MON server.", e);
      }
    }

    // Add the devices to the cache
    // TODO: purge the device cache after a certain amount of time?
    for (Device device : devices) {
      deviceCache.add(device);
    }

    return devices;
  }

  @Override
  public void subscribeDevice(Device device, final DeviceUpdateListener listener) {
    subscribeDevices(new HashSet<>(Arrays.asList(device)), listener);
  }

  @Override
  public void subscribeDevices(Set<Device> devices, final DeviceUpdateListener listener) {
    Set<Long> dataTagIds = new HashSet<>();

    for (Device device : devices) {
      DeviceImpl deviceImpl = (DeviceImpl) device;

      // Here, just get the tag IDs to avoid calling the server
      dataTagIds.addAll(deviceImpl.getPropertyDataTagIds());

      deviceUpdateListeners.put(listener, new HashSet<>(devices));

      // If the device contains properties that are client rules, also subscribe
      // to the tags contained within those rules
      for (ClientRuleTag<?> ruleTag : deviceImpl.getRuleTags()) {
        tagManager.subscribeDataTags(ruleTag.getRuleExpression().getInputTagIds(), ruleTag);
      }
    }

    // Use TagManager to subscribe to all properties of the device
    tagManager.subscribeDataTags(dataTagIds, this);
  }

  @Override
  public void subscribeDevice(DeviceInfo deviceInfo, DeviceInfoUpdateListener listener) {
    subscribeDevices(new HashSet<>(Arrays.asList(deviceInfo)), listener);
  }

  @Override
  public void subscribeDevices(Set<DeviceInfo> deviceInfoList, DeviceInfoUpdateListener listener) {
    try {
      // Ask the server for the devices
      Collection<TransferDevice> serverResponse = requestHandler.getDevices(deviceInfoList);
      Set<Device> devices = new HashSet<>();
      List<DeviceInfo> unknownDevices = new ArrayList<>();

      // Convert the response objects into client devices. If any were not
      // returned, add them to the list of unknown devices.
      for (DeviceInfo deviceInfo : deviceInfoList) {
        Device device = null;

        for (TransferDevice transferDevice : serverResponse) {
          if (transferDevice.getDeviceClassName().equals(deviceInfo.getClassName()) && transferDevice.getName().equals(deviceInfo.getDeviceName())) {
            device = createClientDevice(transferDevice, deviceInfo.getClassName());
            devices.add(device);
          }
        }

        if (device == null) {
          LOG.info("Unknown device (class: " + deviceInfo.getClassName() + " name: " + deviceInfo.getDeviceName() + " requested.");
          unknownDevices.add(deviceInfo);
        }
      }

      // If there were unknown devices in the request list, we need to notify
      // the client by invoking the onDeviceNotFound() method of the listener.
      if (unknownDevices.size() > 0) {
        listener.onDevicesNotFound(unknownDevices);
      }

      // TODO: Caching of devices
      // TODO: Device reconfiguration should somehow trigger client update
      for (Device device : devices) {
        deviceCache.add(device);
      }

      if (devices.size() > 0) {
        // Make the subscription
        subscribeDevices(devices, listener);
      }

    } catch (JMSException e) {
      LOG.error("subscribeDevices() - JMS connection lost -> Could not retrieve devices from the C2MON server.", e);
    }
  }

  @Override
  public void onInitialUpdate(Collection<ClientDataTagValue> initialValues) {
    List<Device> devices = deviceCache.getAllDevices();
    Set<Device> updatedDevices = new HashSet<>();

    for (ClientDataTagValue tag : initialValues) {
      // Find the device(s) that use this tag
      for (Device device : devices) {
        DeviceImpl deviceImpl = (DeviceImpl) device;

        if (deviceImpl.getPropertyDataTagIds().contains(tag.getId())) {
          deviceImpl.updateProperty(tag);
          updatedDevices.add(device);
        }
      }
    }

    // Invoke the listeners that are interested in these tags/devices
    for (Map.Entry<DeviceUpdateListener, Set<Device>> entry : deviceUpdateListeners.entrySet()) {
      if (updatedDevices.equals(entry.getValue())) {
        entry.getKey().onInitialUpdate(new ArrayList<Device>(updatedDevices));
      }
    }
  }

  @Override
  public void onUpdate(ClientDataTagValue tag) {
    List<Device> devices = deviceCache.getAllDevices();

    // Find the device(s) that use this tag
    for (Device device : devices) {
      DeviceImpl deviceImpl = (DeviceImpl) device;

      if (deviceImpl.getPropertyDataTagIds().contains(tag.getId())) {
        PropertyInfo propertyInfo = deviceImpl.updateProperty(tag);

        // Invoke the listeners that are interested in these tags/devices
        for (Map.Entry<DeviceUpdateListener, Set<Device>> entry : deviceUpdateListeners.entrySet()) {
          if (entry.getValue().contains(device)) {
            entry.getKey().onUpdate(device, propertyInfo);
          }
        }
      }
    }
  }

  @Override
  public void unsubscribeDevice(Device device, DeviceUpdateListener listener) {
    unsubscribeDevices(new HashSet<Device>(Arrays.asList(device)), listener);
  }

  @Override
  public void unsubscribeDevices(Set<Device> devices, final DeviceUpdateListener listener) {
    Set<Long> dataTagIds = new HashSet<>();

    for (Device device : devices) {
      DeviceImpl deviceImpl = (DeviceImpl) device;
      dataTagIds.addAll(deviceImpl.getPropertyDataTagIds());

      // Remove the device from the listener
      deviceUpdateListeners.get(listener).remove(device);

      // Remove the device from the cache if nobody is listening for updates
      if (!isSubscribed(device)) {
        deviceCache.remove(device);
      }
    }

    // If no devices are left in the set, remove the listener itself
    if (deviceUpdateListeners.get(listener).isEmpty()) {
      deviceUpdateListeners.remove(listener);
    }

    // Use TagManager to unsubscribe from all properties of the device
    tagManager.unsubscribeDataTags(dataTagIds, this);
  }

  @Override
  public void unsubscribeAllDevices(final DeviceUpdateListener listener) {
    Set<Device> allDevices = new HashSet<Device>(deviceCache.getAllDevices());
    unsubscribeDevices(allDevices, listener);
  }

  /**
   * Check if the given device is subscribed to by any listener.
   *
   * @param device the device to be checked
   * @return true if the device is subscribed to, false otherwise
   */
  public boolean isSubscribed(Device device) {
    for (Set<Device> devices : deviceUpdateListeners.values()) {
      if (devices.contains(device)) {
        return true;
      }
    }

    return false;
  }

  /**
   * Return all {@link DeviceUpdateListener} instances.
   *
   * @return the set of {@link DeviceUpdateListener}s
   */
  public Set<DeviceUpdateListener> getDeviceUpdateListeners() {
    return deviceUpdateListeners.keySet();
  }

  /**
   * Create a client {@link Device} object from a {@link TransferDevice}.
   *
   * @param transferDevice the transfer device to create a client device for
   * @param deviceClassName the name of the device class to which the device
   *          belongs
   *
   * @return the newly created device
   */
  private Device createClientDevice(TransferDevice transferDevice, String deviceClassName) {
    DeviceImpl device = new DeviceImpl(transferDevice.getId(), transferDevice.getName(), transferDevice.getDeviceClassId(), deviceClassName, tagManager,
        commandManager);

    try {
      // Set the properties
      device.setDeviceProperties(transferDevice.getDeviceProperties());

    } catch (RuleFormatException e) {
      LOG.error("getAllDevices() - Received property containing incorrect rule tag from the server. Please check device with id " + device.getId(), e);
      throw new RuntimeException("Received property containing incorrect rule tag from the server for device id " + device.getId());

    } catch (ClassNotFoundException e) {
      LOG.error("getAllDevices() - Received property containing incorrect result type from the server. Please check device with id " + device.getId(), e);
      throw new RuntimeException("Received property containing incorrect result type from the server for device id " + device.getId());
    }

    // Set the commands
    device.setDeviceCommands(transferDevice.getDeviceCommands());

    return device;
  }
}
