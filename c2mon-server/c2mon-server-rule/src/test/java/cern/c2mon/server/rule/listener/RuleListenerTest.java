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
import cern.c2mon.cache.config.CacheConfigModuleRef;
import cern.c2mon.cache.impl.configuration.C2monIgniteConfiguration;
import cern.c2mon.server.cache.dbaccess.config.CacheDbAccessModule;
import cern.c2mon.server.cache.loading.config.CacheLoadingModuleRef;
import cern.c2mon.server.common.config.CommonModule;
import cern.c2mon.server.common.datatag.DataTag;
import cern.c2mon.server.common.datatag.DataTagCacheObject;
import cern.c2mon.server.common.rule.RuleTag;
import cern.c2mon.server.common.rule.RuleTagCacheObject;
import cern.c2mon.server.rule.config.RuleModule;
import cern.c2mon.server.test.CacheObjectCreation;
import cern.c2mon.shared.common.CacheEvent;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.concurrent.CountDownLatch;

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

  @Before
  public void populateCaches() {
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
    assertEquals(dataTagCache.get(dataTag1.getId()).getValue(), Boolean.TRUE);
    assertEquals(dataTagCache.get(dataTag2.getId()).getValue(), Boolean.TRUE);
    assertEquals(ruleTagCache.get(ruleTag.getId()).getValue(), 1000);
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
    latch.await();
    assertEquals(3, ruleTagCache.get(ruleTag.getId()).getValue());
  }

  /**
   * Tests that a update to a data tag in the cache results in
   * an update to a rule depending on it.
   *
   * Uses the test data created in the cache modules.
   */
  @Test
  public void testRuleTrue() throws InterruptedException {
    final CountDownLatch latch = new CountDownLatch(1);

    ruleTagCache.getCacheListenerManager().registerListener(cacheable -> {
      latch.countDown();
    }, CacheEvent.UPDATE_ACCEPTED);

    //(2) second test of rule update
    //update dataTag to TRUE, notify listeners and check rule was again updated
    dataTagService.getCache().compute(dataTag1.getId(), dataTag -> {
      TagController.setValue(dataTag, Boolean.TRUE, "now true");
      TagController.validate(dataTag);
    });
    assertEquals(Boolean.TRUE, dataTagCache.get(dataTag1.getId()).getValue());
    latch.await();
    assertEquals(2, ruleTagCache.get(ruleTag.getId()).getValue());
  }
}
