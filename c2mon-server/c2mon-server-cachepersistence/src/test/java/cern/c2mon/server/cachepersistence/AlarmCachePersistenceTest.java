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

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.config.CacheConfigModuleRef;
import cern.c2mon.server.cache.dbaccess.AlarmMapper;
import cern.c2mon.server.cache.dbaccess.config.CacheDbAccessModule;
import cern.c2mon.server.cachepersistence.common.BatchPersistenceManagerImpl;
import cern.c2mon.server.cachepersistence.config.CachePersistenceModule;
import cern.c2mon.server.common.alarm.Alarm;
import cern.c2mon.server.common.alarm.AlarmCacheObject;
import cern.c2mon.server.common.config.CommonModule;
import cern.c2mon.server.test.DatabasePopulationRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Tests of persistence mechanisms to the Alarm cache.
 * Integration test with the cache module (including loading module).
 *
 * @author Mark Brightwell
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
  CommonModule.class,
  CacheConfigModuleRef.class,
  CacheDbAccessModule.class,
  CachePersistenceModule.class,
  DatabasePopulationRule.class
})
public class AlarmCachePersistenceTest {

  @Rule
  @Inject
  public DatabasePopulationRule databasePopulationRule;

  @Inject
  private C2monCache<Alarm> alarmCache;

  @Inject
  private AlarmMapper alarmMapper;

  @Inject
  private BatchPersistenceManagerImpl alarmPersistenceManager;

  private Alarm originalObject;

  @Before
  public void before() {
    originalObject = alarmMapper.getItem(350000L);
//    alarmPersistenceSynchroListener.start();
  }

  /**
   * Tests the functionality: put value in cache -> persist to DB.
   */
  @Test
  public void testAlarmPersistence() {

    alarmCache.put(originalObject.getId(), originalObject);

    //check state is as expected
    assertEquals(false, originalObject.isActive());

    //check it is in cache (only compares states so far)
    AlarmCacheObject cacheObject = (AlarmCacheObject) alarmCache.get(originalObject.getId());
    assertEquals(alarmCache.get(originalObject.getId()).isActive(), originalObject.isActive());
    //check it is in database (only values so far...)
    AlarmCacheObject objectInDB = (AlarmCacheObject) alarmMapper.getItem(originalObject.getId());
    assertNotNull(objectInDB);
    assertEquals(objectInDB.isActive(), originalObject.isActive());
    assertEquals(false, objectInDB.isActive()); //state is TERMINATE in test alarm 1

    //now update the cache object to new value
    cacheObject.setActive(true);

    // trigger the persist
    alarmPersistenceManager.persistAllCacheToDatabase();

    objectInDB = (AlarmCacheObject) alarmMapper.getItem(originalObject.getId());
    assertNotNull(objectInDB);
    assertEquals(true, objectInDB.isActive());
    assertEquals(objectInDB.isActive(), objectInDB.isInternalActive());

    //clean up...
    //remove from cache
    alarmCache.remove(originalObject.getId());
  }
}
