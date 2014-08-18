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
package cern.c2mon.server.cache.device;


import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import cern.c2mon.server.cache.DeviceClassCache;
import cern.c2mon.server.common.device.DeviceClass;
import cern.c2mon.server.common.device.DeviceClassCacheObject;

/**
 * This test class does not do any proper cache loading, it simply loads in data
 * manually to an Ehcache instance, and all the other stuff is mocked. Does not
 * rely on test data in DB.
 *
 * @author Justin Lewis Salmon
 */
@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext
@ContextConfiguration({ "classpath:cern/c2mon/server/cache/config/server-cache-deviceclass-isolated-test.xml" })
public class DeviceClassCacheIsolatedTest {

  /** Component to test */
  @Autowired
  DeviceClassCache deviceClassCache;

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
  }
}
