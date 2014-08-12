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
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.jms.JMSException;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cern.c2mon.client.core.C2monTagManager;
import cern.c2mon.client.core.cache.BasicCacheHandler;
import cern.c2mon.client.ext.device.cache.DeviceCache;
import cern.c2mon.client.ext.device.request.DeviceRequestHandler;

/**
 * This class implements the {@link C2monDeviceManager} interface and provides a
 * concrete class for interacting with the C2MON server following the
 * class/device/property model.
 *
 * @author Justin Lewis Salmon
 */
@Service
public class DeviceManager implements C2monDeviceManager {

  /** Log4j logger for this class */
  private static final Logger LOG = Logger.getLogger(DeviceManager.class);

  /** Reference to the <code>TagManager</code> singleton */
  private final C2monTagManager tagManager;

  /** Reference to the <code>BasicCacheHandler</code> singleton */
  private final BasicCacheHandler dataTagCache;

  /** Reference to the <code>DeviceCache</code> singleton */
  private final DeviceCache deviceCache;

  /** Provides methods for requesting information from the C2MON server */
  private final DeviceRequestHandler requestHandler;

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
  protected DeviceManager(final C2monTagManager pTagManager, final BasicCacheHandler pDataTagCache, final DeviceCache pDeviceCache,
      final DeviceRequestHandler pRequestHandler) {
    this.tagManager = pTagManager;
    this.dataTagCache = pDataTagCache;
    this.deviceCache = pDeviceCache;
    this.requestHandler = pRequestHandler;
  }

  @Override
  public List<String> getAllDeviceClassNames() {
    List<String> deviceClassNames = new ArrayList<>();

    // Ask the server for all class names via RequestHandler
    try {
      Collection<String> serverResponse = requestHandler.getAllDeviceClassNames();
      deviceClassNames.addAll(serverResponse);
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

      // TODO: Didn't find any devices of that class in the cache, so call the
      // server
    }

    return devices;
  }

  @Override
  public void subscribeDevices(Set<Device> devices, final DeviceUpdateListener listener) {

    for (Device deviceInterface : devices) {
      DeviceImpl device = (DeviceImpl) deviceInterface;

      // Here, just get the tag IDs to avoid calling the server
      Set<Long> dataTagIds = device.getPropertyValueIds();

      // Use TagManager to subscribe to all properties of the device
      device.addDeviceUpdateListener(listener);
      tagManager.subscribeDataTags(dataTagIds, device);

      // The properties should now be in the cache
      // Map<Long, ClientDataTag> dataTags = dataTagCache.get(dataTagIds);
      //
      // Map<String, ClientDataTagValue> propertyValues = new HashMap<String,
      // ClientDataTagValue>();
      // for (ClientDataTagValue tag : dataTags.values()) {
      // propertyValues.put(tag.getName(), tag);
      // }
      //
      // device.setPropertyValues(propertyValues);

      // Give the initial values to the device listener
      // listener.onUpdate(device, null);
    }
  }

  @Override
  public void unsubscribeDevices(Set<Device> devices, final DeviceUpdateListener listener) {

    for (Device deviceInterface : devices) {
      DeviceImpl device = (DeviceImpl) deviceInterface;
      Set<Long> dataTagIds = device.getPropertyValueIds();

      // Use TagManager to unsubscribe from all properties of the device
      tagManager.unsubscribeDataTags(dataTagIds, device);

      // Remove the listener
      device.removeDeviceUpdateListener(listener);

      // Remove the device from the cache if nobody is listening for updates
      if (!device.hasUpdateListeners()) {
        deviceCache.remove(device);
      }
    }
  }

  @Override
  public void unsubscribeAllDevices(final DeviceUpdateListener listener) {
    Set<Device> allDevices = new HashSet<Device>(deviceCache.getAllDevices());
    unsubscribeDevices(allDevices, listener);
  }

}
