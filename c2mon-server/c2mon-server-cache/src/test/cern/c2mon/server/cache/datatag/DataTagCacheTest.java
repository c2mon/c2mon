package cern.c2mon.server.cache.datatag;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;
import java.util.List;

import org.junit.After;
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
}
