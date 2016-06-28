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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import cern.c2mon.server.cache.DeviceCache;
import cern.c2mon.server.cache.exception.CacheElementNotFoundException;
import cern.c2mon.server.common.device.Device;
import cern.c2mon.server.common.device.DeviceCacheObject;

/**
 * This test class does not do any proper cache loading, it simply loads in data
 * manually to an Ehcache instance, and all the other stuff is mocked. Does not
 * rely on test data in DB.
 *
 * @author Justin Lewis Salmon
 */
@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext
@ContextConfiguration({ "classpath:server-cache-device-isolated-test.xml" })
public class DeviceCacheIsolatedTest {

  /** Component to test */
  @Autowired
  DeviceCache deviceCache;

  Device device;

  @Before
  public void setUp() {
    device = new DeviceCacheObject(1000L, "test_device_1", 1L);
    deviceCache.put(device.getId(), device);
  }

  @Test
  @Ignore
  public void testGet() {
    Device d = deviceCache.get(device.getId());
    Assert.assertNotNull(d);
    Assert.assertTrue(d.getId() == device.getId());
  }

  @Test(expected=CacheElementNotFoundException.class)
  @Ignore
  public void testGetNull() {
    Device d = deviceCache.get(2000L);
    Assert.assertNull(d);
  }
}
