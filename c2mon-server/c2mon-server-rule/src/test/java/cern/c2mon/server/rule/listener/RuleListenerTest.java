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

import java.sql.Timestamp;

import cern.c2mon.server.cache.DataTagCache;
import cern.c2mon.server.cache.DataTagFacade;
import cern.c2mon.server.cache.RuleTagCache;
import cern.c2mon.server.cache.config.CacheModule;
import cern.c2mon.server.cache.dbaccess.config.CacheDbAccessModule;
import cern.c2mon.server.cache.loading.config.CacheLoadingModule;
import cern.c2mon.server.common.config.CommonModule;
import cern.c2mon.server.common.datatag.DataTag;
import cern.c2mon.server.common.rule.RuleTag;
import cern.c2mon.server.rule.config.RuleModule;
import cern.c2mon.server.rule.junit.RuleCachePopulationRule;
import cern.c2mon.server.test.CacheObjectCreation;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

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
    CacheModule.class,
    CacheDbAccessModule.class,
    CacheLoadingModule.class,
    RuleModule.class
})
public class RuleListenerTest {

  @Rule
  @Autowired
  public RuleCachePopulationRule ruleCachePopulationRule;

  /**
   * The time the main thread sleeps to allow listeners to act on
   * notifications.
   */
  private static final int SLEEP_TIME = 2000;

  @Autowired
  private DataTagFacade dataTagFacade;

  @Autowired
  private DataTagCache dataTagCache;

  @Autowired
  private RuleTagCache ruleTagCache;

  /**
   * Tests that a update to a data tag in the cache results in
   * an update to a rule depending on it.
   *
   * Uses the test data created in the cache modules.
   * @throws InterruptedException
   */
  @Test
  public void testRuleEvaluation() throws InterruptedException {
    DataTag dataTag1 = CacheObjectCreation.createTestDataTag();
    DataTag dataTag2 = CacheObjectCreation.createTestDataTag2();
    RuleTag ruleTag = CacheObjectCreation.createTestRuleTag();

    dataTagCache.putQuiet(dataTag1);
    dataTagCache.putQuiet(dataTag2);
    ruleTagCache.putQuiet(ruleTag);

    //check still set as expected in test class
    assertEquals(dataTag1.getValue(), Boolean.TRUE);
    assertEquals(dataTag2.getValue(), Boolean.TRUE);
    assertEquals(ruleTag.getValue(), Integer.valueOf(1000)); //set manually in cache

    //check are all in cache
    assertTrue(dataTagCache.hasKey(dataTag1.getId()));
    assertTrue(dataTagCache.hasKey(dataTag2.getId()));
    assertTrue(ruleTagCache.hasKey(ruleTag.getId()));
    //and have correct values
    assertEquals(dataTagCache.get(dataTag1.getId()).getValue(), Boolean.TRUE);
    assertEquals(dataTagCache.get(dataTag2.getId()).getValue(), Boolean.TRUE);
    assertEquals(ruleTagCache.get(ruleTag.getId()).getValue(), Integer.valueOf(1000));

    //recall rule is (#1000000 = true)|(#110 = true)[2],true[3]

    //(1) first test of rule update
    //update dataTag and check rule was updated after short wait (to account for buffer mainly, and passing to new thread)
    dataTagFacade.updateAndValidate(dataTag1.getId(), Boolean.FALSE, "now false", new Timestamp(System.currentTimeMillis()));
    //NOTIFY OF UPDATE! - can remove as moved notification into facade objects
    //dataTagCache.notifyListenersOfUpdate(dataTag1);

    //check update was made
    assertEquals(dataTagCache.get(dataTag1.getId()).getValue(), Boolean.FALSE);
    try {
      Thread.sleep(SLEEP_TIME);
    } catch (InterruptedException e) {
      throw e;
    }
    assertEquals(Integer.valueOf(3), ruleTag.getValue());

    //(2) second test of rule update
    //update dataTag to TRUE, notify listeners and check rule was again updated
    dataTagFacade.updateAndValidate(dataTag1.getId(), Boolean.TRUE, "now true", new Timestamp(System.currentTimeMillis()));
    //dataTagCache.notifyListenersOfUpdate(dataTag1);
    assertEquals(dataTagCache.get(dataTag1.getId()).getValue(), Boolean.TRUE);
    try {
      Thread.sleep(SLEEP_TIME);
    } catch (InterruptedException e) {
      throw e;
    }
    assertEquals(Integer.valueOf(2), ruleTag.getValue());
  }

}
