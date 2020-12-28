package cern.c2mon.client.core.device;
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


import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cern.c2mon.client.core.C2monServiceGateway;
import cern.c2mon.client.core.device.listener.DeviceInfoUpdateListener;
import cern.c2mon.client.core.device.property.Category;
import cern.c2mon.client.core.device.property.Field;
import cern.c2mon.client.core.device.property.Property;
import cern.c2mon.client.core.device.property.PropertyInfo;
import cern.c2mon.client.core.service.DeviceService;
import cern.c2mon.shared.client.device.DeviceInfo;

/**
 * Integration test which takes all configured Devices of all classes and
 * subscribes to their properties.
 *
 * Pass -Dc2mon.client.conf.url=http://timweb/test/conf/c2mon-client.properties
 * to the VM to run.
 *
 * @author Justin Lewis Salmon
 */
public class DeviceIntegrationTest {

  static Logger log = LoggerFactory.getLogger(DeviceIntegrationTest.class);

  static CountDownLatch latch;

  public static void main(String[] args) throws InterruptedException {
    DeviceService manager = C2monServiceGateway.getDeviceService();
    List<Device> allDevices = new ArrayList<>();

    List<String> deviceClassNames = manager.getAllDeviceClassNames();
    log.info("Retrieved the following DeviceClass names:");

    for (String name : deviceClassNames) {
      log.info("\t" + name);
    }

    for (String name : deviceClassNames) {
      List<Device> devices = manager.getAllDevices(name);
      allDevices.addAll(devices);

      log.info("Retrieved the following devices for device class " + name + ":");
      for (Device device : devices) {
        log.info("\t" + device.getName() + " (id: " + device.getId() + ")");
      }

      for (Device device : devices) {
        List<Property> properties = device.getProperties();

        log.info("Found the following properties for device " + device.getName() + ":");
        for (Property property : properties) {

          if (property.getCategory() == Category.MAPPED_PROPERTY) {
            log.info("\t" + property.getName() + ": (mapped property)");

            log.info("\tFound the following fields for mapped property " + property.getName() + ":");
            for (Field field : property.getFields()) {
              log.info("\t\t" + field.getName() + ": " + field.getTag().getName());
            }

          } else {
            log.info("\t" + property.getName() + ": " + property.getTag().getName());
          }
        }

        log.info("Subscribing to device " + device.getName() + "...");
        manager.subscribeDevice(device, listener);
      }

      log.info("Unsubscribing from all devices");
      manager.unsubscribeDevices(new HashSet<Device>(devices), listener);
    }

    log.info("Attempting subscription to an unknown device");
    DeviceInfo info = new DeviceInfo("unknown", "unknown");
    manager.subscribeDevices(new HashSet<>(Arrays.asList(info)), listener);

    log.info("Subscribing to two different devices with the same listener (should get two callbacks)");
    latch = new CountDownLatch(2);
    manager.subscribeDevice(allDevices.get(0), listener2);
    manager.subscribeDevice(allDevices.get(1), listener2);

    latch.await(1000, TimeUnit.MILLISECONDS);
    Assert.assertTrue(latch.getCount() == 0);

    manager.unsubscribeAllDevices(listener2);
  }

  static DeviceInfoUpdateListener listener = new DeviceInfoUpdateListener() {
    @Override
    public void onInitialUpdate(List<Device> devices) {
      for (Device device : devices) {
        log.info("Subscribed and got the following properties for device " + device.getName() + ":");
        for (Property property : device.getProperties()) {
          if (property.getCategory() == Category.MAPPED_PROPERTY) {
            log.info("\t" + property.getName() + ": (mapped property)");

            log.info("\tSubscribed and got the following fields for mapped property " + property.getName() + ":");
            for (Field field : property.getFields()) {
              log.info("\t\t" + field.getName() + ": " + field.getTag().getName() + " (" + field.getTag().getValue() + ")");
            }

          } else {
            log.info("\t" + property.getName() + ": " + property.getTag().getName() + " (" + property.getTag().getValue() + ")");
          }
        }
      }
    }

    @Override
    public void onUpdate(Device device, PropertyInfo propertyInfo) {
      log.info("onUpdate(): device=" + device.getName() + " property=" + propertyInfo.getPropertyName() + " value="
          + device.getProperty(propertyInfo.getPropertyName()).getTag().getValue());
    }

    @Override
    public void onDevicesNotFound(List<DeviceInfo> unknownDevices) {
      log.info("onDevicesNotFound(): The following devices were requested but not found on the server:");
      for (DeviceInfo info : unknownDevices) {
        log.info("\tclass=" + info.getClassName() + " device=" + info.getDeviceName());
      }
    }
  };

  static DeviceInfoUpdateListener listener2 = new DeviceInfoUpdateListener() {

    @Override
    public void onUpdate(Device device, PropertyInfo propertyInfo) {
      log.info("onUpdate()");
    }

    @Override
    public void onInitialUpdate(List<Device> devices) {
      log.info("onInitialUpdate()");
      latch.countDown();
    }

    @Override
    public void onDevicesNotFound(List<DeviceInfo> unknownDevices) {
      log.info("onDevicesNotFound()");
    }
  };
}
