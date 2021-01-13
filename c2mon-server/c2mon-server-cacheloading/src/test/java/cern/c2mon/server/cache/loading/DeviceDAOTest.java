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
import cern.c2mon.server.cache.loading.config.CacheLoadingModule;
import cern.c2mon.server.common.config.CommonModule;
import cern.c2mon.server.common.device.Device;
import cern.c2mon.server.common.device.DeviceCacheObject;
import cern.c2mon.server.test.DatabasePopulationRule;
import cern.c2mon.shared.client.device.DeviceProperty;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author Justin Lewis Salmon
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
    CommonModule.class,
    CacheDbAccessModule.class,
    CacheLoadingModule.class,
    DatabasePopulationRule.class
})
public class DeviceDAOTest {

  @Rule
  @Autowired
  public DatabasePopulationRule databasePopulationRule;

  /** Component to test */
  @Autowired
  DeviceDAO deviceDAO;

  @Test
  public void testGetItem() {
    Assert.assertNotNull(deviceDAO.getItem(300L));
    Assert.assertNotNull(deviceDAO.getItem(301L));
    Assert.assertTrue(deviceDAO.getItem(300L).getDeviceClassId() == 400);
    Assert.assertTrue(deviceDAO.getItem(301L).getDeviceClassId() == 400);
  }

  @Test
  public void insertDeviceWithFieldShouldInsertField() {
    // insert
    DeviceCacheObject deviceToInsert = new DeviceCacheObject();
    deviceToInsert.setId(9999L);
    deviceToInsert.setDeviceClassId(400L);
    deviceToInsert.setName("test_device_with_property");
    DeviceProperty devFieldToInsert = new DeviceProperty(1L, "cpu_load", "220000", "tagId", null);
    DeviceProperty devPropToInsert = new DeviceProperty(9L, "TEST_PROPERTY_WITH_FIELDS", null, Collections.singletonList(devFieldToInsert));
    deviceToInsert.setDeviceProperties(Collections.singletonList(devPropToInsert));
    deviceDAO.insert(deviceToInsert);

    // fetch
    Device retrievedDevice = deviceDAO.getItem(9999L);
    DeviceProperty retrievedDevProperty = retrievedDevice.getDeviceProperties().get(0);
    Map<String, DeviceProperty> retrievedDevFields = retrievedDevProperty.getFields();

    // compare
    Assert.assertEquals(1, retrievedDevFields.size());
    Assert.assertEquals(devFieldToInsert, retrievedDevFields.get("cpu_load"));
  }
}
