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
package cern.c2mon.server.cache.control;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import cern.c2mon.server.cache.AbstractCacheIntegrationTest;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import cern.c2mon.server.cache.dbaccess.ControlTagMapper;
import cern.c2mon.server.common.control.ControlTag;
import cern.c2mon.server.common.datatag.DataTag;
import cern.c2mon.server.common.datatag.DataTagCacheObject;
import cern.c2mon.server.common.tag.Tag;

/**
 * Integration test of the ControlTagCache implementation
 * with the cache loading and cache DB access modules.
 * 
 * @author mbrightw
 *
 */
public class ControlTagCacheTest extends AbstractCacheIntegrationTest {
    
  @Autowired
  @Qualifier("controlTagCache")
  private ControlTagCacheImpl controlTagCache;
  
  @Autowired
  private ControlTagMapper controlTagMapper;
  
  
  /**
   * Tests the cache was loaded from the DB by checking the same number of objects
   * are in each and that the names are identical.
   * Only compares the names of the cache and DB, so could be improved
   * (but to compare values need test server to be stopped!)
   */
  
  @Test
  public void testCacheLoading() {
    assertNotNull(controlTagCache);
    
    List<ControlTag> dataTagList = controlTagMapper.getAll();
    
    //test the cache was loaded correctly
    assertEquals(dataTagList.size(), controlTagCache.getCache().getKeys().size());
    //compare all the objects from the cache and buffer
    Iterator<ControlTag> it = dataTagList.iterator();
    while (it.hasNext()) {
      DataTag currentTag = (DataTag) it.next();
      //equality of DataTagCacheObjects => currently only compares names (as these don't change if the test server is running!)
      assertEquals(currentTag.getName(), ((DataTagCacheObject) controlTagCache.getCopy(currentTag.getId())).getName());
    }
  }
  
  @Test
  public void testGetTagByName() {
    Assert.assertNull(controlTagCache.get("does not exist"));
    
    DataTag tag = controlTagCache.get("P_TEST_EQUIPMENT:STATUS");
    Assert.assertNotNull(tag);
    Assert.assertEquals(Long.valueOf(1205L), tag.getId());
    Assert.assertEquals("String", tag.getDataType());
    
    tag = controlTagCache.get("P_TESTHANDLER03:ALIVE");
    Assert.assertNotNull(tag);
    Assert.assertEquals(Long.valueOf(1221L), tag.getId());
    Assert.assertEquals("Integer", tag.getDataType()); 
  }
  
  @Test
  public void testSearchWithNameWildcard() {
    Collection<ControlTag> resultList = controlTagCache.findByNameWildcard("does_not_exist*");
    Assert.assertNotNull(resultList);
    Assert.assertEquals(0, resultList.size());
    
    resultList = controlTagCache.findByNameWildcard("P_TESTHANDLER03:ALIVE");
    Assert.assertNotNull(resultList);
    Assert.assertEquals(1, resultList.size());
    Tag tag = resultList.iterator().next();
    Assert.assertEquals(Long.valueOf(1221L), tag.getId());
    Assert.assertEquals("Integer", tag.getDataType());
    
    String regex = "P_TESTHANDLER03:*";
    resultList = controlTagCache.findByNameWildcard(regex);
    Assert.assertNotNull(resultList);
    Assert.assertEquals(2, resultList.size());
    for (Tag controlTag : resultList) {
      Assert.assertTrue(controlTag.getName().toLowerCase().startsWith(regex.substring(0, regex.lastIndexOf('*')).toLowerCase()));
    }
    
    
    String regex2 = "*TESTHANDLER03*";
    resultList = controlTagCache.findByNameWildcard(regex2);
    Assert.assertNotNull(resultList);
    Assert.assertEquals(8, resultList.size());
    for (Tag controlTag : resultList) {
      Assert.assertTrue(controlTag.getName().toLowerCase().contains("TESTHANDLER03".toLowerCase()));
    }
  }
}
