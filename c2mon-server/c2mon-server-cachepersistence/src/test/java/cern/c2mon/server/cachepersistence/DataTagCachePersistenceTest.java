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
package cern.c2mon.server.cachepersistence;

import java.io.IOException;
import java.sql.Timestamp;

import cern.c2mon.server.cache.config.CacheModule;
import cern.c2mon.server.cache.dbaccess.config.CacheDbAccessModule;
import cern.c2mon.server.cachepersistence.config.CachePersistenceModule;
import cern.c2mon.server.common.config.CommonModule;
import cern.c2mon.server.test.DatabasePopulationRule;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import cern.c2mon.server.cache.datatag.DataTagCacheImpl;
import cern.c2mon.server.cache.dbaccess.DataTagMapper;
import cern.c2mon.server.cachepersistence.common.BatchPersistenceManagerImpl;
import cern.c2mon.server.cachepersistence.listener.PersistenceSynchroListener;
import cern.c2mon.server.common.datatag.DataTag;
import cern.c2mon.server.common.datatag.DataTagCacheObject;
import cern.c2mon.shared.common.datatag.DataTagConstants;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Integration test of the cache-persistence and cache modules.
 * @author Mark Brightwell
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
    CommonModule.class,
    CacheModule.class,
    CacheDbAccessModule.class,
    CachePersistenceModule.class,
    DatabasePopulationRule.class
})
public class DataTagCachePersistenceTest {

  @Rule
  @Autowired
  public DatabasePopulationRule databasePopulationRule;

  @Autowired
  private DataTagCacheImpl dataTagCache;

  @Autowired
  private DataTagMapper dataTagMapper;

  @Autowired
  private BatchPersistenceManagerImpl dataTagPersistenceManager;

  @Autowired
  private PersistenceSynchroListener dataTagPersistenceSynchroListener;

  private DataTagCacheObject originalObject;

  @Before
  public void setUpData() throws IOException {
    originalObject = (DataTagCacheObject) dataTagMapper.getItem(200000L);
    dataTagPersistenceSynchroListener.start();
  }

  /**
   * Tests the functionality: put value in cache -> persist to DB.
   */
  @Test
  public void testTagPersistence() throws InterruptedException {

    //create a test cache object, put in the cache
    dataTagCache.putQuiet(originalObject);

    //check it is in cache (only values so far...)

    DataTagCacheObject cacheObject = (DataTagCacheObject) dataTagCache.get(originalObject.getId());
    assertEquals(dataTagCache.get(originalObject.getId()).getValue(), originalObject.getValue());
    //check it is in database (only values so far...)
    DataTagCacheObject objectInDB = (DataTagCacheObject) dataTagMapper.getItem(originalObject.getId());
    assertNotNull(objectInDB);
    assertEquals(objectInDB.getValue(), originalObject.getValue());

    //now update the cache object
    cacheObject.setValue(0);

    //notify the listeners
    dataTagCache.notifyListenersOfUpdate(cacheObject);

    // trigger the batch persist
    dataTagPersistenceManager.persistAllCacheToDatabase();

    objectInDB = (DataTagCacheObject) dataTagMapper.getItem(originalObject.getId());
    assertNotNull(objectInDB);
    assertEquals(0, objectInDB.getValue());

    //clean up...
    //remove from cache
    dataTagCache.remove(originalObject.getId());
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

    dataTagMapper.insertDataTag(floatTag);
    assertNotNull(dataTagMapper.getItem(floatTag.getId()));
    dataTagCache.putQuiet(floatTag);

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
    DataTag objectInDB = dataTagMapper.getItem(floatTag.getId());
    assertNotNull(objectInDB);
    assertEquals(new Float(30), objectInDB.getValue());

    //remove from cache
    dataTagCache.remove(floatTag.getId());
    //remove from DB (in After)

  }
}
