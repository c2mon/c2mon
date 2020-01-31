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
import cern.c2mon.cache.impl.configuration.C2monIgniteConfiguration;
import cern.c2mon.server.cache.dbaccess.RuleTagMapper;
import cern.c2mon.server.cache.dbaccess.config.CacheDbAccessModule;
import cern.c2mon.server.cache.loading.config.CacheLoadingModuleRef;
import cern.c2mon.server.cache.test.CacheObjectCreation;
import cern.c2mon.server.cachepersistence.config.CachePersistenceModule;
import cern.c2mon.server.cachepersistence.config.RuleTagPersistenceConfig;
import cern.c2mon.server.common.config.CommonModule;
import cern.c2mon.server.common.rule.RuleTag;
import cern.c2mon.server.common.rule.RuleTagCacheObject;
import cern.c2mon.server.test.DatabasePopulationRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Integration test of the cache-persistence and cache
 * modules. Test the correct persistence of updates to
 * the RuleTag cache.
 *
 * @author Mark Brightwell
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
  CommonModule.class,
  CacheConfigModuleRef.class,
  CacheDbAccessModule.class,
  C2monIgniteConfiguration.class,
  DatabasePopulationRule.class,
  CacheLoadingModuleRef.class,
  CachePersistenceModule.class
})
public class RuleTagCachePersistenceTest {

  @Rule
  @Inject
  public DatabasePopulationRule databasePopulationRule;

  @Inject
  private RuleTagMapper ruleTagMapper;

  @Inject
  private C2monCache<RuleTag> ruleTagCache;

  @Inject
  private RuleTagPersistenceConfig ruleTagPersistenceConfig;

  private RuleTag originalObject;

  @Before
  public void insertTestTag() throws IOException {
    originalObject = CacheObjectCreation.createTestRuleTag();
    ruleTagMapper.insertRuleTag((RuleTagCacheObject) originalObject);
  }

  /**
   * Tests the functionality: put value in cache -> persist to DB.
   */
  @Test
  public void testRuleTagPersistence() throws InterruptedException {

    ruleTagCache.put(originalObject.getId(), originalObject);

    //check it is in cache (only values so far...)

    RuleTagCacheObject cacheObject = (RuleTagCacheObject) ruleTagCache.get(originalObject.getId());
    assertEquals(ruleTagCache.get(originalObject.getId()).getValue(), originalObject.getValue());
    //check it is in database (only values so far...)
    RuleTagCacheObject objectInDB = (RuleTagCacheObject) ruleTagMapper.getItem(originalObject.getId());
    assertNotNull(objectInDB);
    assertEquals(objectInDB.getValue(), originalObject.getValue());
    assertEquals(1000, objectInDB.getValue()); //value is 1000 for test rule tag

    //now update the cache object to new value
    cacheObject.setValue(2000);
    ruleTagCache.put(cacheObject.getId(), cacheObject);

    // trigger the persist
    ruleTagPersistenceConfig.getBatchPersistenceManager().persistAllCacheToDatabase();

    objectInDB = (RuleTagCacheObject) ruleTagMapper.getItem(originalObject.getId());
    assertNotNull(objectInDB);
    assertEquals(2000, objectInDB.getValue());
  }
}
