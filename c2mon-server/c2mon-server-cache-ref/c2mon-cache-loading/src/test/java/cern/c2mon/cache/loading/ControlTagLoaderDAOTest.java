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
package cern.c2mon.cache.loading;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import cern.c2mon.cache.loading.config.CacheLoadingModule;
import cern.c2mon.server.cache.dbaccess.config.CacheDbAccessModule;
import cern.c2mon.server.common.config.CommonModule;
import cern.c2mon.server.common.control.ControlTag;
import cern.c2mon.server.test.DatabasePopulationRule;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
        CommonModule.class,
        CacheDbAccessModule.class,
        CacheLoadingModule.class,
        DatabasePopulationRule.class
})
public class ControlTagLoaderDAOTest {

  @Rule
  @Autowired
  public DatabasePopulationRule databasePopulationRule;

  @Autowired
  private ControlTagLoaderDAO controlTagLoaderDAO;

  @Test
  public void testGetItem() {
    assertNotNull(controlTagLoaderDAO.getItem(1260L));
  }

  /**
   * Check the default property is picked up (should override that set in the
   * cache object itself).
   */
  @Test
  public void testGetItemDoPostDbLoading() {
    ControlTag tag = controlTagLoaderDAO.getItem(1261L);
    assertNotNull(tag);
  }

  @Test
  public void testGetAll() {
    assertNotNull(controlTagLoaderDAO.getAllAsMap());
    assertTrue(controlTagLoaderDAO.getAllAsMap().size() > 10);
  }
}
