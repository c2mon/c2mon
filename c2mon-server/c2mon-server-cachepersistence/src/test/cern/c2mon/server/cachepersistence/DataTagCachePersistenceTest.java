/******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2005-2011 CERN.
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * 
 * Author: TIM team, tim.support@cern.ch
 *****************************************************************************/
package cern.c2mon.server.cachepersistence;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.sql.Timestamp;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import cern.c2mon.server.cache.datatag.DataTagCacheImpl;
import cern.c2mon.server.cache.dbaccess.DataTagMapper;
import cern.c2mon.server.cache.dbaccess.test.TestDataHelper;
import cern.c2mon.server.common.datatag.DataTag;
import cern.c2mon.server.common.datatag.DataTagCacheObject;
import cern.c2mon.shared.common.datatag.DataTagConstants;

/**
 * Integration test of the cache-persistence and cache modules.
 * @author Mark Brightwell
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext
@ContextConfiguration({"classpath:cern/c2mon/server/cachepersistence/config/server-cachepersistence-datatag-test.xml" })
//@TransactionConfiguration(transactionManager="cacheTransactionManager", defaultRollback=true)
//@Transactional
public class DataTagCachePersistenceTest implements ApplicationContextAware {

  /**
   * Need context to explicitly start it (cache listeners
   * need explicit starting).
   */
  private ApplicationContext context;
  
  @Autowired
  private DataTagCacheImpl dataTagCache;
  
  @Autowired
  private DataTagMapper dataTagMapper;
  
  @Autowired
  private TestDataHelper testDataHelper;
  
  private DataTagCacheObject originalObject;
  
  //@BeforeTransaction
  public void cleanDB() {
    testDataHelper.removeTestData();
    dataTagMapper.deleteDataTag(Long.valueOf(1000100));
  }
  
  @Before
  public void setUpData() { 
    cleanDB();
    testDataHelper.createTestData();
    testDataHelper.insertTestDataIntoDB();
    originalObject = testDataHelper.getDataTag();
  }
  
  @Before
  public void startContext() {
    ((AbstractApplicationContext) context).start();
  }
   
  @After
  public void deleteTestTag() {
    testDataHelper.removeTestData();
    dataTagMapper.deleteDataTag(Long.valueOf(1000100));
    //dataTagMapper.deleteDataTag(originalObject.getId()); //from DB
  }
  
  /**
   * Tests the functionality: put value in cache -> persist to DB.
   */
  @Test
  public void testTagPersistence() {
    
    //create a test cache object, put in DB and cache
//    DataTagCacheObject originalObject = (DataTagCacheObject) DataTagMapperTest.createTestDataTag();
//    dataTagMapper.testInsertDataTag((DataTagCacheObject) originalObject);
    dataTagCache.put(originalObject.getId(), originalObject);
    
    //check it is in cache (only values so far...)
    
    DataTagCacheObject cacheObject = (DataTagCacheObject) dataTagCache.get(originalObject.getId());
    assertEquals(((DataTag) dataTagCache.get(originalObject.getId())).getValue(), originalObject.getValue());    
    //check it is in database (only values so far...)
    DataTagCacheObject objectInDB = (DataTagCacheObject) dataTagMapper.getItem(originalObject.getId());
    assertNotNull(objectInDB);
    assertEquals(objectInDB.getValue(), originalObject.getValue());
    assertEquals(Boolean.TRUE, objectInDB.getValue());
    
    //now update the cache object to false
    cacheObject.setValue(Boolean.FALSE);
    //notify the listeners
    dataTagCache.notifyListenersOfUpdate(cacheObject);
    
    //...and check the DB was updated after the buffer has time to fire
    try {
      Thread.sleep(20000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    objectInDB = (DataTagCacheObject) dataTagMapper.getItem(originalObject.getId());
    assertNotNull(objectInDB);    
    assertEquals(Boolean.FALSE, objectInDB.getValue());
    
    //clean up...
    //remove from cache
    dataTagCache.remove(originalObject.getId());
    
    //remove from database
    //dataTagMapper.deleteDataTag();
  
}
  
  /**
   * Tests that if 2 updates for the same tag are written to the cache at roughly
   * the same time, that only the most recent one ends up in the DB. This is testing
   * the way the synchrobuffer is integrated into the design.
   * 
   * We assume that incoming updates for the same tag have different timestamps.
   * 
   * Is only testing the one-server configuration (each member of the cluster will have a separate buffer).
   */
  //@Test
  public void testLatestUpdatePersistedToDB() {
    //load initial test tag into cache and DB    
    DataTagCacheObject floatTag = new DataTagCacheObject();
    floatTag.setId(new Long(1000100));  //must be non null in DB
    floatTag.setName("Test float tag"); //non null    
    floatTag.setMode(DataTagConstants.MODE_TEST); //non null
    floatTag.setDataType("Float"); // non null
    //floatTag.setEquipmentId(new Long(300000)); //need test equipment inserted - using test equipment
    floatTag.setValue(new Float(10));
    floatTag.setCacheTimestamp(new Timestamp(System.currentTimeMillis() - 10)); //before both updates
    
    dataTagMapper.testInsertDataTag(floatTag);
    assertNotNull(dataTagMapper.getItem(floatTag.getId()));
    dataTagCache.put(floatTag.getId(), floatTag);
    
    //update the cache with the first value
    DataTagCacheObject cacheObject = (DataTagCacheObject) dataTagCache.get(floatTag.getId());
    cacheObject.setValue(new Float(20));
    cacheObject.setCacheTimestamp(new Timestamp(System.currentTimeMillis() - 1)); // to make sure it is before the second update (and not filtered out at cache level)
    dataTagCache.notifyListenersOfUpdate(cacheObject);
    
    //update with the second
    cacheObject.setValue(new Float(30));
    cacheObject.setCacheTimestamp(new Timestamp(System.currentTimeMillis()));
    dataTagCache.notifyListenersOfUpdate(cacheObject);

    //check the second is always the one in the cache
    assertEquals(new Float(30), cacheObject.getValue());
    
    //wait and check that the final one is the one persisted to DB
    try {
      Thread.sleep(30000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    DataTag objectInDB = (DataTag) dataTagMapper.getItem(floatTag.getId());
    assertNotNull(objectInDB);
    assertEquals(new Float(30), objectInDB.getValue());
    
    //remove from cache
    dataTagCache.remove(floatTag.getId());
    //remove from DB (in After)
    
  }
  
  /**
   * Set the application context. Used for explicit start.
   */
  @Override
  public void setApplicationContext(ApplicationContext arg0) throws BeansException {
    context = arg0;
  }

  
}
