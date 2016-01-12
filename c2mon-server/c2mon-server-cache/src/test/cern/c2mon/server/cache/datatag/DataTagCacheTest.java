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
package cern.c2mon.server.cache.datatag;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import cern.c2mon.server.cache.DataTagCache;
import cern.c2mon.server.cache.dbaccess.DataTagMapper;
import cern.c2mon.server.cache.dbaccess.test.TestDataHelper;
import cern.c2mon.server.cache.exception.CacheElementNotFoundException;
import cern.c2mon.server.common.datatag.DataTag;
import cern.c2mon.server.common.datatag.DataTagCacheObject;
import cern.c2mon.server.common.tag.Tag;
import cern.c2mon.server.test.CacheObjectComparison;

/**
 * Module test. This test integrates the cache and cache loading modules and tests
 * the DataTagCache implementation. The tests include
 * 
 * - testing the cache loading mechanism (to DB)
 * - testing the robustness of the DataTagCache public interface
 * 
 * @author mbrightw
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext
@ContextConfiguration({"classpath:cern/c2mon/server/cache/config/server-cache-datatag-test.xml"})
public class DataTagCacheTest {

  @Autowired
  private DataTagCacheImpl dataTagCache;
  
  @Autowired
  private DataTagMapper dataTagMapper;
  
  @Autowired
  private TestDataHelper testDataHelper;
  
  private DataTagCacheObject originalObject;
  
  @Before
  public void insertTestTag() {
    testDataHelper.createTestData();
    testDataHelper.insertTestDataIntoDB();
    originalObject = testDataHelper.getDataTag();
  }
  
  @After
  public void deleteTestTag() {
    testDataHelper.removeTestData();    
    dataTagMapper.deleteDataTag(Long.valueOf(1000100));
    //dataTagMapper.deleteDataTag(originalObject.getId()); //from DB
  }
  
  @Test
  @DirtiesContext
  public void testCacheLoading() {    
    //remove test data for this test to work (as test data is inserted after the cache is loaded)
    deleteTestTag();
    assertNotNull(dataTagCache);
    
    List<DataTag> dataTagList = dataTagMapper.getAll();
    
    //test the cache was loaded correctly
    assertEquals(dataTagList.size(), dataTagCache.getCache().getKeys().size());
    //compare all the objects from the cache and buffer
    Iterator<DataTag> it = dataTagList.iterator();
    while (it.hasNext()) {
      DataTag currentTag = (DataTag) it.next();
      //equality of DataTagCacheObjects => currently only compares names (do not change when DAQs are running on same DB)
      assertEquals(currentTag.getName(), ((DataTagCacheObject) dataTagCache.getCopy(currentTag.getId())).getName());
    }
  }
    
    
  /**
   * If null is used as  a key, an exception should be thrown. 
   */
  @Test(expected=IllegalArgumentException.class)
  @DirtiesContext
  public void testGetWithNull() {
    //test robustness to null call
    ((DataTagCache) dataTagCache).getCopy(null);
  }
  
  /**
   * Test the return value of the get() method when the tag is not found
   * in the cache.
   */
  @Test(expected=CacheElementNotFoundException.class)
  @DirtiesContext
  public void testGetNotInCache() {
    dataTagCache.getCopy(Long.valueOf(1));
  }
  

  @Test(expected=IllegalArgumentException.class)
  @DirtiesContext
  public void testWriteLockWithNull() {
    dataTagCache.acquireWriteLockOnKey(null);    
  }
  
  @Test
  @DirtiesContext
  public void testWriteLockWithNewId() {
    dataTagCache.acquireWriteLockOnKey(2342342L);
  }
  
  @Test(expected=IllegalArgumentException.class)
  @DirtiesContext
  public void testReadLockWithNull() {
    dataTagCache.acquireReadLockOnKey(null);    
  }
  
  /**
   * Notice read and then write 
   * or write and then write are
   * not supported.
   */
  @Test
  @DirtiesContext
  public void testAcquireReadLockTwice() {
    dataTagCache.acquireReadLockOnKey(1L);
    dataTagCache.acquireReadLockOnKey(1L);    
  }
  
  /**
   * Tests the getCopy method retrieves an existing DataTag correctly. Relies on test data in DB. 
   */
  @Test
  @DirtiesContext
  public void testGetCopy() {
    DataTagCacheObject cacheObject = (DataTagCacheObject) dataTagCache.getCopy(200002L);
    DataTagCacheObject objectInDb = (DataTagCacheObject) dataTagMapper.getItem(200002L);
    //Servertimestamp is always set when creating the object, so the second call above should have a t.s. that is later (first
    // is set when loading cache from DB)
    assertTrue(objectInDb.getTimestamp().after(cacheObject.getTimestamp()));
    //reset t.s. for comparison method to succeed
    objectInDb.setCacheTimestamp(cacheObject.getCacheTimestamp());
    CacheObjectComparison.equals(cacheObject, objectInDb);
  }
  
  @Test
  @DirtiesContext
  public void testGetTagByName() {
    Assert.assertNull(dataTagCache.get("does not exist"));
    
    DataTag tag = dataTagCache.get("D_FIELD_TEST_1");
    Assert.assertNotNull(tag);
    Assert.assertEquals(Long.valueOf(210009L), tag.getId());
    Assert.assertEquals("Integer", tag.getDataType());
    
  }
  
  @Test
  @DirtiesContext
  public void testSearchWithNameWildcard() {
    Collection<DataTag> resultList = dataTagCache.findByNameWildcard("does_not_exist*");
    Assert.assertNotNull(resultList);
    Assert.assertEquals(0, resultList.size());
    
    resultList = dataTagCache.findByNameWildcard("D_FIELD_TEST_1");
    Assert.assertNotNull(resultList);
    Assert.assertEquals(1, resultList.size());
    Tag tag = resultList.iterator().next();
    Assert.assertEquals(Long.valueOf(210009L), tag.getId());
    Assert.assertEquals("Integer", tag.getDataType());
    
    String regex = "D_FIELD_TEST*";
    resultList = dataTagCache.findByNameWildcard(regex);
    Assert.assertNotNull(resultList);
    Assert.assertEquals(2, resultList.size());
    for (Tag dataTag : resultList) {
      Assert.assertTrue(dataTag.getName().toLowerCase().startsWith(regex.substring(0, regex.lastIndexOf('*')).toLowerCase()));
    }
    
    
    String regex2 = "*PROPERTY_test*";
    resultList = dataTagCache.findByNameWildcard(regex2);
    Assert.assertNotNull(resultList);
    Assert.assertEquals(5, resultList.size());
    for (Tag dataTag : resultList) {
      Assert.assertTrue(dataTag.getName().toLowerCase().contains("PROPERTY_test".toLowerCase()));
    }
  }
}
