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
package cern.c2mon.server.cache.device;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Iterator;
import java.util.List;

import cern.c2mon.server.cache.AbstractCacheIntegrationTest;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import cern.c2mon.server.cache.dbaccess.DeviceClassMapper;
import cern.c2mon.server.cache.exception.CacheElementNotFoundException;
import cern.c2mon.server.common.device.DeviceClass;
import cern.c2mon.server.common.device.DeviceClassCacheObject;

/**
 * Integration test of the DeviceClassCache implementation with the cache
 * loading and cache DB access modules.
 *
 * @author Justin Lewis Salmon
 */
public class DeviceClassCacheTest extends AbstractCacheIntegrationTest {

  @Autowired
  private DeviceClassMapper deviceClassMapper;

  @Autowired
  private DeviceClassCacheImpl deviceClassCache;

  @Test
  public void testCacheLoading() {
    assertNotNull(deviceClassCache);

    List<DeviceClass> deviceClassList = deviceClassMapper.getAll();

    // Test the cache is the same size as in DB
    assertEquals(deviceClassList.size(), deviceClassCache.getCache().getKeys().size());
    // Compare all the objects from the cache and buffer
    Iterator<DeviceClass> it = deviceClassList.iterator();
    while (it.hasNext()) {
      DeviceClass currentDeviceClass = it.next();
      // Equality of DataTagCacheObjects => currently only compares names
      assertEquals(currentDeviceClass.getName(), (deviceClassCache.getCopy(currentDeviceClass.getId()).getName()));
    }
  }

  @Test
  public void testGetDeviceClassByName() {
    DeviceClassCacheObject deviceClass1 = new DeviceClassCacheObject(1L, "test_device_class_name_1", "Test description");
    DeviceClassCacheObject deviceClass2 = new DeviceClassCacheObject(2L, "test_device_class_name_2", "Test description");
    DeviceClassCacheObject deviceClass3 = new DeviceClassCacheObject(3L, "test_device_class_name_3", "Test description");
    deviceClassCache.put(deviceClass1.getId(), deviceClass1);
    deviceClassCache.put(deviceClass2.getId(), deviceClass2);
    deviceClassCache.put(deviceClass3.getId(), deviceClass3);

    DeviceClass deviceClass = deviceClassCache.getDeviceClassByName("test_device_class_name_1");
    Assert.assertTrue(deviceClass.getId().equals(deviceClass1.getId()));
    deviceClass = deviceClassCache.getDeviceClassByName("test_device_class_name_2");
    Assert.assertTrue(deviceClass.getId().equals(deviceClass2.getId()));
    deviceClass = deviceClassCache.getDeviceClassByName("test_device_class_name_3");
    Assert.assertTrue(deviceClass.getId().equals(deviceClass3.getId()));

    // Test getting an unknown device class
    try {
      deviceClass = deviceClassCache.getDeviceClassByName("unknown_device_class");
      Assert.fail("getDeviceClassByName() did not throw exception");
    } catch (CacheElementNotFoundException e) {
    }
  }

  @Test
  public void getDeviceIds() {
    DeviceClassCacheObject deviceClass1 = new DeviceClassCacheObject(1L, "test_device_class_name_1", "Test description");
    DeviceClassCacheObject deviceClass2 = new DeviceClassCacheObject(2L, "test_device_class_name_2", "Test description");
    DeviceClassCacheObject deviceClass3 = new DeviceClassCacheObject(3L, "test_device_class_name_3", "Test description");
    deviceClassCache.put(deviceClass1.getId(), deviceClass1);
    deviceClassCache.put(deviceClass2.getId(), deviceClass2);
    deviceClassCache.put(deviceClass3.getId(), deviceClass3);

    List<Long> deviceIds = deviceClassCache.getDeviceIds();

    Assert.assertTrue(deviceIds.contains(1L));
    Assert.assertTrue(deviceIds.contains(2L));
    Assert.assertTrue(deviceIds.contains(3L));
  }
}
