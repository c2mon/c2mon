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
package cern.c2mon.server.cache.loading;

import cern.c2mon.server.cache.dbaccess.config.CacheDbAccessModule;
import cern.c2mon.server.cache.loading.config.CacheLoadingModuleRef;
import cern.c2mon.server.common.config.CommonModule;
import cern.c2mon.server.common.device.DeviceClass;
import cern.c2mon.server.common.device.DeviceClassCacheObject;
import cern.c2mon.server.test.DatabasePopulationRule;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;
import java.util.List;

import static org.junit.Assert.assertNotNull;

/**
 * @author Justin Lewis Salmon
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
        CommonModule.class,
        CacheDbAccessModule.class,
        CacheLoadingModuleRef.class,
        DatabasePopulationRule.class
})
public class DeviceClassDAOTest {

  @Rule
  @Inject
  public DatabasePopulationRule databasePopulationRule;

  /**
   * Component to test
   */
  @Inject
  DeviceClassDAO deviceClassDAO;

  @Test
  public void testGetItem() {
    DeviceClassCacheObject device1 = (DeviceClassCacheObject) deviceClassDAO.getItem(400L);
    assertNotNull(device1);
    List<Long> deviceIds = device1.getDeviceIds();
    Assert.assertFalse(deviceIds.isEmpty());

    DeviceClass device2 = deviceClassDAO.getItem(401L);
    assertNotNull(device2);
  }
}
