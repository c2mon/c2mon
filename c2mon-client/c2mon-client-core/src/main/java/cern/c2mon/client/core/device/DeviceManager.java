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
import java.util.stream.Collectors;

import javax.jms.JMSException;

import cern.c2mon.client.core.device.exception.ImproperDeviceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

import cern.c2mon.client.common.listener.TagListener;
import cern.c2mon.client.common.tag.CommandTag;
import cern.c2mon.client.common.tag.Tag;
import cern.c2mon.client.common.util.ConcurrentSet;
import cern.c2mon.client.core.device.cache.DeviceCache;
import cern.c2mon.client.core.device.exception.DeviceNotFoundException;
import cern.c2mon.client.core.device.listener.DeviceInfoUpdateListener;
import cern.c2mon.client.core.device.listener.DeviceUpdateListener;
import cern.c2mon.client.core.device.listener.ListenerWrapper;
import cern.c2mon.client.core.device.property.PropertyInfo;
import cern.c2mon.client.core.device.request.DeviceRequestHandler;
import cern.c2mon.client.core.service.DeviceService;
import cern.c2mon.client.core.service.CommandService;
import cern.c2mon.client.core.service.TagService;
import cern.c2mon.client.core.tag.ClientRuleTag;
import cern.c2mon.shared.client.device.DeviceClassNameResponse;
import cern.c2mon.shared.client.device.DeviceCommand;
import cern.c2mon.shared.client.device.DeviceInfo;
import cern.c2mon.shared.client.device.TransferDevice;

/**
 * This class implements the {@link DeviceService} interface and provides a
 * concrete class for interacting with the C2MON server following the
 * class/device/property model.
 *
 * @author Justin Lewis Salmon
 */
@Service
@Slf4j
public class DeviceManager implements DeviceService, TagListener {

  /** Reference to the <code>TagManager</code> singleton */
  private final TagService tagService;

  /** Reference to the <code>CommandService</code> singleton */
  private final CommandService commandService;

  /** Reference to the <code>DeviceCache</code> singleton */
  private final DeviceCache deviceCache;

  /** Provides methods for requesting information from the C2MON server */
  private final DeviceRequestHandler requestHandler;

  /**
   * Set of {@link ListenerWrapper} objects which wrap a
   * {@link DeviceUpdateListener} and a set of {@link Device}s.
   */
  private final Set<ListenerWrapper> deviceUpdateListeners = new ConcurrentSet<>();

  /**
   * Default Constructor, used by Spring to instantiate the Singleton service.
   *
   * @param tagService the TagService singleton reference
   * @param pDeviceCache the device cache handler singleton reference
   * @param pRequestHandler provides methods for requesting information from the
   *          C2MON server
   * @param commandService the command service singleton, allows to retrieve command information from the server
   */
  @Autowired
  protected DeviceManager(final TagService tagService,
                          final DeviceCache pDeviceCache,
                          final DeviceRequestHandler pRequestHandler,
                          final CommandService commandService) {
    this.tagService = tagService;
    this.deviceCache = pDeviceCache;
    this.requestHandler = pRequestHandler;
    this.commandService = commandService;
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
      log.error("getAllDeviceClassNames() - JMS connection lost -> Could not retrieve device class names from the C2MON server.", e);
    }

    return deviceClassNames;
  }

  @Override
  public Device getDevice(DeviceInfo info) throws DeviceNotFoundException {
    Device device = null;

    try {
      // Ask the server for the device
      Collection<TransferDevice> serverResponse = requestHandler.getDevices(new HashSet<>(Collections.singletonList(info)));

      // Convert the response object into a client device
      for (TransferDevice transferDevice : serverResponse) {
        if (transferDevice.getDeviceClassName().equals(info.getClassName()) && transferDevice.getName().equals(info.getDeviceName())) {
          device = createClientDevice(transferDevice, info.getClassName());
            // Add the device to the cache
            deviceCache.add(device);
        }
      }

      // Retrieve and set all commands of the device at once
      getDeviceCommands(Collections.singletonList(device), serverResponse);

    } catch (JMSException e) {
      log.error("subscribeDevices() - JMS connection lost -> Could not retrieve devices from the C2MON server.", e);
    }

    // If the device was not found, throw an exception
    if (device == null) {
      throw new DeviceNotFoundException("Unknown device (class: " + info.getClassName() + " name: " + info.getDeviceName() + " requested.");
    }

    return device;
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

        // Retrieve and set commands of all devices at once
        getDeviceCommands(devices, serverResponse);

      } catch (JMSException e) {
        log.error("getAllDevices() - JMS connection lost -> Could not retrieve devices from the C2MON server.", e);
      }
    }

    // Add the devices to the cache
    // TODO: purge the device cache after a certain amount of time?
    for (Device device : devices) {
      deviceCache.add(device);
    }

    return devices;
  }

  /**
   * Private helper method to bulk retrieve all commands of a set of transfer devices in one
   * go, in order to reduce the number of server calls. The commands, once retrieved, will be
   * inserted in-place into their respective devices.
   *
   * @param devices the list of devices whose commands have not yet been retrieved
   * @param transferDevices the list of transfer devices containing the command information
   */
  private void getDeviceCommands(Collection<Device> devices, Collection<TransferDevice> transferDevices) {
    Set<Long> commandTagIds = transferDevices.stream()
            .flatMap(transferDevice -> transferDevice.getDeviceCommands().stream())
            .map(DeviceManager::parseToCommandId)
            .collect(Collectors.toSet());

    if (!commandTagIds.isEmpty()) {
      Set<CommandTag<Object>> commandTags = commandService.getCommandTags(commandTagIds);
      for (CommandTag<Object> commandTag : commandTags) {

        // Find the device command to which this command tag belongs
        for (TransferDevice transferDevice : transferDevices) {
          for (DeviceCommand deviceCommand : transferDevice.getDeviceCommands()) {
            if (commandTag.getId().equals(parseToCommandId(deviceCommand))) {
              devices.stream()
                      .filter(device -> device.getName().equals(transferDevice.getName()))
                      .forEach(device -> device.addCommand(deviceCommand.getName(), commandTag));
            }
          }
        }
      }
    }
  }

  @Override
  public void subscribeDevice(Device device, final DeviceUpdateListener listener) {
    subscribeDevices(new HashSet<>(Collections.singletonList(device)), listener);
  }

  @Override
  public void subscribeDevices(Set<Device> devices, final DeviceUpdateListener listener) {
    Set<Long> dataTagIds = new HashSet<>();

    for (Device device : devices) {
      DeviceImpl deviceImpl = (DeviceImpl) device;

      // Here, just get the tag IDs to avoid calling the server
      dataTagIds.addAll(deviceImpl.getPropertyDataTagIds());

      // Add the devices to the cache
      deviceCache.add(device);

      // If the device contains properties that are client rules, also subscribe
      // to the tags contained within those rules
      for (ClientRuleTag<?> ruleTag : deviceImpl.getRuleTags()) {
        tagService.subscribe(ruleTag.getRuleExpression().getInputTagIds(), ruleTag);
      }
    }

    // Register the listener
    deviceUpdateListeners.add(new ListenerWrapper(listener, new HashSet<>(devices)));

    // Use TagManager to subscribe to all properties of the device
    tagService.subscribe(dataTagIds, this);
  }

  @Override
  public void subscribeDevice(DeviceInfo deviceInfo, DeviceInfoUpdateListener listener) {
    subscribeDevices(Collections.singleton(deviceInfo), listener);
  }

  @Override
  public void subscribeDevices(Set<DeviceInfo> deviceInfoList, DeviceInfoUpdateListener listener) {

    if (deviceInfoList.isEmpty()) {
      return;
    }

    // Copy into a HashSet to make sure it's serialisable and mutable
    HashSet<DeviceInfo> devicesToRetrieve = new HashSet<>(deviceInfoList);
    Set<Device> devices = readOutCachedDevices(devicesToRetrieve);

    try {
      // Ask the server for the devices
      Collection<TransferDevice> serverResponse = requestHandler.getDevices(devicesToRetrieve);

      List<DeviceInfo> unknownDevices = new ArrayList<>();

      // Convert the response objects into client devices. If any were not
      // returned, add them to the list of unknown devices.
      for (DeviceInfo deviceInfo : devicesToRetrieve) {
        Device device = null;

        for (TransferDevice transferDevice : serverResponse) {
          if (transferDevice.getDeviceClassName().equals(deviceInfo.getClassName()) && transferDevice.getName().equals(deviceInfo.getDeviceName())) {
            device = createClientDevice(transferDevice, deviceInfo.getClassName());
            devices.add(device);
          }
        }

        // Retrieve and set commands of all devices at once
        getDeviceCommands(devices, serverResponse);

        if (device == null) {
          log.info("Unknown device (class: " + deviceInfo.getClassName() + " name: " + deviceInfo.getDeviceName() + " requested.");
          unknownDevices.add(deviceInfo);
        }
      }

      // If there were unknown devices in the request list, we need to notify
      // the client by invoking the onDeviceNotFound() method of the listener.
      if (!unknownDevices.isEmpty()) {
        listener.onDevicesNotFound(unknownDevices);
      }

      // TODO: Caching of devices
      // TODO: Device reconfiguration should somehow trigger client update
      for (Device device : devices) {
        deviceCache.add(device);
      }

      if (!devices.isEmpty()) {
        // Make the subscription
        subscribeDevices(devices, listener);
      }

    } catch (JMSException e) {
      log.error("subscribeDevices() - JMS connection lost -> Could not retrieve devices from the C2MON server.", e);
    }
  }

  @Override
  public void onInitialUpdate(Collection<Tag> initialValues) {
    List<Device> devices = deviceCache.getAllDevices();
    Set<Device> updatedDevices = new HashSet<>();

    for (Tag tag : initialValues) {
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
    for (ListenerWrapper wrapper : deviceUpdateListeners) {
      if (updatedDevices.equals(wrapper.getDevices()) && wrapper.isInitialUpdateRequired()) {
        wrapper.getListener().onInitialUpdate(new ArrayList<>(updatedDevices));
        wrapper.setInitialUpdateRequired(false);
      }
    }
  }

  @Override
  public void onUpdate(Tag tag) {
    List<Device> devices = deviceCache.getAllDevices();

    // Find the device(s) that use this tag
    for (Device device : devices) {
      DeviceImpl deviceImpl = (DeviceImpl) device;

      if (deviceImpl.getPropertyDataTagIds().contains(tag.getId())) {
        PropertyInfo propertyInfo = deviceImpl.updateProperty(tag);

        // Invoke the listeners that are interested in these tags/devices
        for (ListenerWrapper wrapper : deviceUpdateListeners) {
          if (wrapper.getDevices().contains(device)) {
            wrapper.getListener().onUpdate(device, propertyInfo);
          }
        }
      }
    }
  }

  @Override
  public void unsubscribeDevice(Device device, DeviceUpdateListener listener) {
    unsubscribeDevices(new HashSet<>(Collections.singletonList(device)), listener);
  }

  @Override
  public void unsubscribeDevices(Set<Device> devices, final DeviceUpdateListener listener) {
    Set<Long> dataTagIds = new HashSet<>();

    for (Device device : devices) {
      DeviceImpl deviceImpl = (DeviceImpl) device;

      // Remove the device from the listener
      for (ListenerWrapper wrapper : deviceUpdateListeners) {
        if (wrapper.getListener().equals(listener)) {
          wrapper.getDevices().remove(device);
          dataTagIds.addAll(deviceImpl.getPropertyDataTagIds());
        }
      }

      // Remove the device from the cache if nobody is listening for updates
      if (!isSubscribed(device)) {
        deviceCache.remove(device);
      }
    }

    // If no devices are left in the set, remove the listener itself
    List<ListenerWrapper> listenersToRemove = new ArrayList<>();
    for (ListenerWrapper wrapper : deviceUpdateListeners) {
      if (wrapper.getDevices().isEmpty()) {
        listenersToRemove.add(wrapper);
      }
    }
    deviceUpdateListeners.removeAll(listenersToRemove);

    // Use TagManager to unsubscribe from all properties of the device
    tagService.unsubscribe(dataTagIds, this);
  }

  @Override
  public void unsubscribeAllDevices(final DeviceUpdateListener listener) {
    Set<Device> allDevices = new HashSet<>(deviceCache.getAllDevices());
    unsubscribeDevices(allDevices, listener);
  }

  @Override
  public Collection<Device> getAllSubscribedDevices(DeviceUpdateListener listener) {
    Collection<Device> devices = new ArrayList<>();

    for (ListenerWrapper wrapper : deviceUpdateListeners) {
      if (wrapper.getListener().equals(listener)) {
        devices = wrapper.getDevices();
      }
    }

    return devices;
  }

  private static Long parseToCommandId(DeviceCommand deviceCommand) {
    return Long.valueOf(deviceCommand.getValue().replace("\"", ""));
  }

  /**
   * Private helper method to retrieve the Device corresponding to the entries of the deviceInfos from the cache where
   * possible.
   * @param deviceInfos The {@link DeviceInfo}s for which to retrieve the corresponding {@link Device} elements from the
   *                    cache. The elements for which this is possible will be removed from the set.
   * @return The set of {@link Device} elements which could be retrieved from the cache.
   */
  private Set<Device> readOutCachedDevices(HashSet<DeviceInfo> deviceInfos) {

    Set<Device> devices = new HashSet<>();
    for (DeviceInfo info : deviceInfos) {
      Device device = deviceCache.get(info.getDeviceName());
      if (device != null) {
        deviceInfos.remove(info);
        devices.add(device);
      }
    }
    return devices;
  }

  /**
   * Check if the given device is subscribed to by any listener.
   *
   * @param device the device to be checked
   * @return true if the device is subscribed to, false otherwise
   */
  public boolean isSubscribed(Device device) {
    for (ListenerWrapper wrapper : deviceUpdateListeners) {
      if (wrapper.getDevices().contains(device)) {
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
    Set<DeviceUpdateListener> listeners = new HashSet<>();

    for (ListenerWrapper wrapper : deviceUpdateListeners) {
      listeners.add(wrapper.getListener());
    }

    return listeners;
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
    DeviceImpl device = new DeviceImpl(transferDevice.getId(), transferDevice.getName(), transferDevice.getDeviceClassId(), deviceClassName);

    try {
      // Set the properties
      device.setDeviceProperties(transferDevice.getDeviceProperties());
    } catch (ImproperDeviceException e) {
      log.error("Received property containing incorrect result type from the server. The device {} with with id {} could not be added to the list.", device.getName(), device.getId(), e);
    }

    return device;
  }
}
