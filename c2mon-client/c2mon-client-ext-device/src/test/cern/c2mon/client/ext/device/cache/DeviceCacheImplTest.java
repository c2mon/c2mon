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
package cern.c2mon.client.ext.device.cache;

import java.util.List;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import cern.c2mon.client.ext.device.Device;
import cern.c2mon.client.ext.device.DeviceImpl;

/**
 * @author Justin Lewis Salmon
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "classpath:cern/c2mon/client/ext/device/config/c2mon-devicecache-test.xml" })
public class DeviceCacheImplTest {

  /** Component to test */
  @Autowired
  private DeviceCache deviceCache;

  private static Device device1;
  private static Device device2;
  private static Device device3;

  @BeforeClass
  public static void setUp() {
    device1 = new DeviceImpl(1000L, "test_device", 1L, "test_device_class_1");
    device2 = new DeviceImpl(2000L, "test_device", 1L, "test_device_class_1");
    device3 = new DeviceImpl(3000L, "test_device", 1L, "test_device_class_2");
  }

  @Test
  public void testAddDevice() {
    deviceCache.add(device1);
    deviceCache.add(device2);
    Assert.assertTrue(deviceCache.get(device1.getId()) != null);
    Assert.assertTrue(deviceCache.get(device2.getId()) != null);
  }

  @Test
  public void testRemoveDevice() {
    deviceCache.add(device1);
    deviceCache.remove(device1);
    Assert.assertTrue(deviceCache.get(device1.getId()) == null);
  }

  @Test
  public void testGetDevice() {
    deviceCache.add(device1);
    deviceCache.add(device2);
    Assert.assertTrue(deviceCache.get(device1.getId()) == device1);
    Assert.assertTrue(deviceCache.get(device2.getId()) == device2);
  }

  @Test
  public void testGetAllDevicesOfClass() {
    deviceCache.add(device1);
    deviceCache.add(device2);
    deviceCache.add(device3);
    List<Device> devices = deviceCache.getAllDevices("test_device_class_1");
    Assert.assertTrue(devices.size() == 2);
    Assert.assertTrue(devices.contains(device1));
    Assert.assertTrue(devices.contains(device2));

    devices = deviceCache.getAllDevices("test_device_class_2");
    Assert.assertTrue(devices.size() == 1);
    Assert.assertTrue(devices.contains(device3));
  }

  @Test
  public void testGetAllDevices() {
    deviceCache.add(device1);
    deviceCache.add(device2);
    deviceCache.add(device3);
    List<Device> devices = deviceCache.getAllDevices();
    Assert.assertTrue(devices.size() == 3);
    Assert.assertTrue(devices.contains(device1));
    Assert.assertTrue(devices.contains(device2));
    Assert.assertTrue(devices.contains(device3));
  }
}
