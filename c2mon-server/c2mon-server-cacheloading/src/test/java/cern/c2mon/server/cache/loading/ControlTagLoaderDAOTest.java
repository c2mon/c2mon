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

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Map;

import cern.c2mon.server.cache.loading.junit.DatabasePopulationRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import cern.c2mon.server.common.control.ControlTag;
import cern.c2mon.server.common.process.Process;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({
    "classpath:config/server-cachedbaccess.xml",
    "classpath:config/server-cacheloading.xml",
    "classpath:test-config/server-test-properties.xml"
})
@TestPropertySource("classpath:c2mon-server-default.properties")
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
    assertTopicSetCorrectly(tag);    
  }
  
  @Test
  public void testGetAll() {
    assertNotNull(controlTagLoaderDAO.getAllAsMap());
    assertTrue(controlTagLoaderDAO.getAllAsMap().size() > 10);
  }
  
  @Test
  public void testGetAllDoPostDbLoading() {
    for (Map.Entry<Long, ControlTag> entry : controlTagLoaderDAO.getAllAsMap().entrySet()) {
      assertTopicSetCorrectly(entry.getValue());
    }
  }
  
  private void assertTopicSetCorrectly(ControlTag tag) {
    assertEquals("c2mon.client.controltag", tag.getTopic());
  }
  
}
