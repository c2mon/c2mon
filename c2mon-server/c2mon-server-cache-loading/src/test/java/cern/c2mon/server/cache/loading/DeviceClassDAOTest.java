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
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;

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

  @Inject
  private DeviceDAO deviceDAO;

  @Test
  public void testGetItem() {
    DeviceClassCacheObject device1 = (DeviceClassCacheObject) deviceClassDAO.getItem(400L);
    assertNotNull(device1);

    DeviceClass device2 = deviceClassDAO.getItem(401L);
    assertNotNull(device2);
  }

  @Test
  public void deleteWithRemovingDevicesNoFields() {
    deviceDAO.deleteItem(302L);
    deviceDAO.deleteItem(303L);
    deviceClassDAO.deleteItem(401L);
  }

  @Test
  public void deleteWithRemovingDevicesHasFields() {
    deviceDAO.deleteItem(300L);
    deviceDAO.deleteItem(301L);
    deviceClassDAO.deleteItem(400L);
  }

  @Test(expected = DataIntegrityViolationException.class)
  public void deleteWithoutRemovingDevices() {
    deviceClassDAO.deleteItem(400L);
  }

}
