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
import cern.c2mon.server.common.datatag.DataTag;
import cern.c2mon.server.test.DatabasePopulationRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.*;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
    CommonModule.class,
    CacheDbAccessModule.class,
    CacheLoadingModule.class,
    DatabasePopulationRule.class
})
public class DataTagLoaderDAOTest {

  @Rule
  @Autowired
  public DatabasePopulationRule databasePopulationRule;

  @Autowired
  private DataTagLoaderDAO dataTagLoaderDAO;

  @Test
  public void testGetItem() {
    assertNotNull(dataTagLoaderDAO.getItem(200000L));
  }

  /**
   * Check the default property is picked up (should override that set in the
   * cache object itself).
   */
  @Test
  public void testGetItemDoPostDbLoading() {
    DataTag tag = dataTagLoaderDAO.getItem(200010L);
    assertNotNull(tag);
  }

  @Test
  public void testGetBatch() {
    List<Long> expectedResult = Arrays.asList(200000L, 200001L, 200002L, 200003L, 200004L, 200005L, 200010L, 200011L, 200012L, 210000L, 210001L, 210002L, 210003L, 210008L, 210009L, 210010L);
    Map<Object, DataTag> batch1 = dataTagLoaderDAO.getBatchAsMap(1L, 100L);
    assertNotNull("Batch should not be null", batch1);
    assertEquals("Expected " + batch1.size() + " entries", expectedResult.size(), batch1.size());
    assertTrue("Mismatch in expected entries", batch1.keySet().containsAll(expectedResult));
    Map<Object, DataTag> batch2 = dataTagLoaderDAO.getBatchAsMap(1L, 10L);
    assertEquals("Expected 10 entries between rows 1 and 10", 10, batch2.size());
    assertTrue("Entries between rows 1 and 10 do not match", batch2.keySet().containsAll(expectedResult.subList(0, 9)));
    Map<Object, DataTag> batch3 = dataTagLoaderDAO.getBatchAsMap(11L, 16L);
    assertEquals("Expected 6 entries between rows 10 and 15", 6, batch3.size());
    assertTrue("Entries between rows 10 and 15 do not match", batch3.keySet().containsAll(expectedResult.subList(10, 15)));
  }
}
