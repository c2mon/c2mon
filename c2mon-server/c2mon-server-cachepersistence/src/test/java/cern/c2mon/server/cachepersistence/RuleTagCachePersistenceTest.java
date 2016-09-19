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

import cern.c2mon.server.cache.dbaccess.RuleTagMapper;
import cern.c2mon.server.cache.rule.RuleTagCacheImpl;
import cern.c2mon.server.cachepersistence.common.BatchPersistenceManagerImpl;
import cern.c2mon.server.cachepersistence.listener.PersistenceSynchroListener;
import cern.c2mon.server.common.rule.RuleTag;
import cern.c2mon.server.common.rule.RuleTagCacheObject;
import cern.c2mon.server.test.CacheObjectCreation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Integration test of the cache-persistence and cache
 * modules. Test the correct persistence of updates to
 * the RuleTag cache.
 *
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
public class RuleTagCachePersistenceTest {

  @Rule
  @Autowired
  public DatabasePopulationRule databasePopulationRule;

  @Autowired
  private RuleTagMapper ruleTagMapper;

  @Autowired
  private RuleTagCacheImpl ruleTagCache;

  @Autowired
  private BatchPersistenceManagerImpl ruleTagPersistenceManager;

  @Autowired
  private PersistenceSynchroListener ruleTagPersistenceSynchroListener;

  private RuleTag originalObject;

  @Before
  public void insertTestTag() throws IOException {
    originalObject = CacheObjectCreation.createTestRuleTag();
    ruleTagMapper.insertRuleTag((RuleTagCacheObject) originalObject);
  ruleTagPersistenceSynchroListener.start();
  }

  /**
   * Tests the functionality: put value in cache -> persist to DB.
   */
  @Test
  public void testRuleTagPersistence() throws InterruptedException {

    ruleTagCache.putQuiet(originalObject);

    //check it is in cache (only values so far...)

    RuleTagCacheObject cacheObject = (RuleTagCacheObject) ruleTagCache.get(originalObject.getId());
    assertEquals(ruleTagCache.get(originalObject.getId()).getValue(), originalObject.getValue());
    //check it is in database (only values so far...)
    RuleTagCacheObject objectInDB = (RuleTagCacheObject) ruleTagMapper.getItem(originalObject.getId());
    assertNotNull(objectInDB);
    assertEquals(objectInDB.getValue(), originalObject.getValue());
    assertEquals(new Integer(1000), objectInDB.getValue()); //value is 1000 for test rule tag

    //now update the cache object to new value
    cacheObject.setValue(new Integer(2000));
    //notify the listeners
    ruleTagCache.notifyListenersOfUpdate(cacheObject);

    // trigger the persist
    ruleTagPersistenceManager.persistAllCacheToDatabase();

    objectInDB = (RuleTagCacheObject) ruleTagMapper.getItem(originalObject.getId());
    assertNotNull(objectInDB);
    assertEquals(Integer.valueOf(2000), objectInDB.getValue());

    //clean up...
    //remove from cache
    ruleTagCache.remove(originalObject.getId());

  }
}
