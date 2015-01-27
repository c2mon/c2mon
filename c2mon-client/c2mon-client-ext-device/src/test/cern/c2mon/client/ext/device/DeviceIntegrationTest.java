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

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.apache.log4j.Logger;

import cern.c2mon.client.ext.device.property.Category;
import cern.c2mon.client.ext.device.property.Field;
import cern.c2mon.client.ext.device.property.Property;
import cern.c2mon.client.ext.device.property.PropertyInfo;
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

  static Logger log = Logger.getLogger(DeviceIntegrationTest.class);

  public static void main(String[] args) {
    C2monDeviceManager manager = C2monDeviceGateway.getDeviceManager();

    List<String> deviceClassNames = manager.getAllDeviceClassNames();
    log.info("Retrieved the following DeviceClass names:");

    for (String name : deviceClassNames) {
      log.info("\t" + name);
    }

    for (String name : deviceClassNames) {
      List<Device> devices = manager.getAllDevices(name);

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

      log.info("Unsubscribing and re-subscribing to all devices");
      manager.unsubscribeDevices(new HashSet<Device>(devices), listener);
      manager.subscribeDevices(new HashSet<Device>(devices), listener);

      log.info("Attempting subscription to an unknown device");
      DeviceInfo info = new DeviceInfo("unknown", "unknown");
      manager.subscribeDevices(new HashSet<>(Arrays.asList(info)), listener);
    }
  }

  static DeviceUpdateListener listener = new DeviceUpdateListener() {
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
}
