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

import cern.c2mon.server.cache.dbaccess.DeviceClassMapper;
import cern.c2mon.server.common.device.DeviceClass;

/**
 * Integration test of the DeviceClassCache implementation with the cache
 * loading and cache DB access modules.
 *
 * @author Justin Lewis Salmon
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext
@ContextConfiguration({ "classpath:cern/c2mon/server/cache/config/server-cache-deviceclass-test.xml" })
public class DeviceClassCacheTest {

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
}