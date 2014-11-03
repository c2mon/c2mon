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

import static cern.c2mon.server.test.device.ObjectComparison.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Iterator;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import cern.c2mon.server.cache.dbaccess.DeviceMapper;
import cern.c2mon.server.common.device.Device;
import cern.c2mon.shared.client.device.DeviceProperty;

/**
 * Integration test of the DeviceCache implementation with the cache loading and
 * cache DB access modules.
 *
 * @author Justin Lewis Salmon
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext
@ContextConfiguration({ "classpath:cern/c2mon/server/cache/config/server-cache-device-test.xml" })
public class DeviceCacheTest {

  @Autowired
  private DeviceMapper deviceMapper;

  @Autowired
  private DeviceCacheImpl deviceCache;

  @Test
  public void testCacheLoading() throws ClassNotFoundException {
    assertNotNull(deviceCache);

    List<Device> deviceList = deviceMapper.getAll();

    // Test the cache is the same size as in DB
    assertEquals(deviceList.size(), deviceCache.getCache().getKeys().size());
    // Compare all the objects from the cache and buffer
    for (Device device : deviceList) {
      Device fromCache = deviceCache.getCopy(device.getId());

      assertEquals(device.getName(), fromCache.getName());

      // Compare properties
      for (DeviceProperty property : device.getDeviceProperties()) {
        assertDevicePropertyListContains(fromCache.getDeviceProperties(), property);
      }
    }

    Iterator<Device> it = deviceList.iterator();
    while (it.hasNext()) {
      Device currentDevice = it.next();
      // Equality of DataTagCacheObjects => currently only compares names
      assertEquals(currentDevice.getName(), (deviceCache.getCopy(currentDevice.getId()).getName()));
    }
  }
}
