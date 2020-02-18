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
package cern.c2mon.server.rule.listener;

import cern.c2mon.cache.actions.CacheActionsModuleRef;
import cern.c2mon.cache.actions.datatag.DataTagService;
import cern.c2mon.cache.actions.tag.TagController;
import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.api.listener.CacheListenerManagerImpl;
import cern.c2mon.cache.config.CacheConfigModuleRef;
import cern.c2mon.cache.impl.configuration.C2monIgniteConfiguration;
import cern.c2mon.server.cache.dbaccess.config.CacheDbAccessModule;
import cern.c2mon.server.cache.loading.config.CacheLoadingModuleRef;
import cern.c2mon.server.cache.test.CacheObjectCreation;
import cern.c2mon.server.common.config.CommonModule;
import cern.c2mon.server.common.datatag.DataTag;
import cern.c2mon.server.common.datatag.DataTagCacheObject;
import cern.c2mon.server.common.rule.RuleTag;
import cern.c2mon.server.common.rule.RuleTagCacheObject;
import cern.c2mon.server.rule.RuleTagService;
import cern.c2mon.server.rule.config.RuleModule;
import cern.c2mon.server.rule.evaluation.RuleEvaluatorImpl;
import cern.c2mon.shared.common.CacheEvent;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


/**
 * Integration testing of the rule module.
 *
 * To run these also need to load the cache modules and the server-common module.
 *
 * @author mbrightw
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
    CommonModule.class,
    CacheActionsModuleRef.class,
    CacheConfigModuleRef.class,
    CacheDbAccessModule.class,
    C2monIgniteConfiguration.class,
    CacheLoadingModuleRef.class,
    RuleModule.class
})
public class RuleListenerTest {

  @Autowired
  private DataTagService dataTagService;

  @Autowired
  private C2monCache<DataTag> dataTagCache;

  @Autowired
  private C2monCache<RuleTag> ruleTagCache;
  private DataTagCacheObject dataTag1;
  private RuleTagCacheObject ruleTag;

  @Autowired RuleEvaluatorImpl ruleEvaluator;
  @Autowired RuleTagService ruleTagService;

  @Before
  public void populateCaches() {
    // Remove existing listener managers during data insertion
    dataTagCache.setCacheListenerManager(new CacheListenerManagerImpl<>());
    ruleTagCache.setCacheListenerManager(new CacheListenerManagerImpl<>());

    dataTag1 = CacheObjectCreation.createTestDataTag();
    DataTag dataTag2 = CacheObjectCreation.createTestDataTag2();
    ruleTag = CacheObjectCreation.createTestRuleTag();

    ruleTagCache.put(ruleTag.getId(), ruleTag);
    dataTagCache.put(dataTag1.getId(), dataTag1);
    dataTagCache.put(dataTag2.getId(), dataTag2);

    //check still set as expected in test class
    assertEquals(dataTag1.getValue(), Boolean.TRUE);
    assertEquals(dataTag2.getValue(), Boolean.TRUE);
    assertEquals(ruleTag.getValue(), 1000); //set manually in cache

    //check are all in cache
    assertTrue(dataTagCache.containsKey(dataTag1.getId()));
    assertTrue(dataTagCache.containsKey(dataTag2.getId()));
    assertTrue(ruleTagCache.containsKey(ruleTag.getId()));
    //and have correct values
    assertTrue((Boolean) dataTagCache.get(dataTag1.getId()).getValue());
    assertTrue((Boolean) dataTagCache.get(dataTag2.getId()).getValue());
    assertEquals(1000, ruleTagCache.get(ruleTag.getId()).getValue());

    // Reinsert the useful listeners
    ruleEvaluator.init();
//    ruleTagService.init();
  }

  /**
   * Tests that a update to a data tag in the cache results in
   * an update to a rule depending on it.
   *
   * Uses the test data created in the cache modules.
   * @throws InterruptedException
   */
  @Test
  public void testRuleEvaluation() throws InterruptedException {
    final CountDownLatch latch = new CountDownLatch(1);

    ruleTagCache.getCacheListenerManager().registerListener(cacheable -> {
      System.out.println("Listener 1: " + cacheable);
      latch.countDown();
    }, CacheEvent.UPDATE_ACCEPTED);

    //(1) first test of rule update
    //update dataTag and check rule was updated after short wait (to account for buffer mainly, and passing to new thread)
    dataTagService.getCache().compute(dataTag1.getId(), dataTag -> {
      TagController.setValue(dataTag, Boolean.FALSE, "now false");
      TagController.validate(dataTag);
    });

    //check update was made
    assertEquals(Boolean.FALSE, dataTagCache.get(dataTag1.getId()).getValue());
    assertTrue("Event should be received", latch.await(1, TimeUnit.SECONDS));
    assertEquals(3, ruleTagCache.get(ruleTag.getId()).getValue());
  }
}
