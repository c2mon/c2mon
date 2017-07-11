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
package cern.c2mon.server.cache.rule;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import cern.c2mon.server.cache.AbstractCacheIntegrationTest;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import cern.c2mon.server.cache.dbaccess.RuleTagMapper;
import cern.c2mon.server.common.rule.RuleTag;
import cern.c2mon.server.common.rule.RuleTagCacheObject;
import cern.c2mon.server.common.tag.Tag;
import cern.c2mon.server.test.CacheObjectComparison;

/**
 * Integration test of the RuleTagCache with the loading
 * and DB access modules.
 *
 * @author Mark Brightwell
 *
 */
public class RuleTagCacheTest extends AbstractCacheIntegrationTest {

  @Autowired
  private RuleTagMapper ruleTagMapper;

  @Autowired
  private RuleTagCacheImpl ruleTagCache;

  @Test
  public void testCacheLoading() {
    assertNotNull(ruleTagCache);

    List<RuleTag> ruleList = ruleTagMapper.getAll(); //IN FACT: GIVES TIME FOR CACHE TO FINISH LOADING ASYNCH BEFORE COMPARISON BELOW...

    //test the cache is the same size as in DB
    assertEquals(ruleList.size(), ruleTagCache.getCache().getKeys().size());
    //compare all the objects from the cache and buffer
    Iterator<RuleTag> it = ruleList.iterator();
    while (it.hasNext()) {
      RuleTagCacheObject currentRule = (RuleTagCacheObject) it.next();
      //only compares one field so far (name, which does not change when server is running!)
      assertEquals(currentRule.getName(), (((RuleTag) ruleTagCache.getCopy(currentRule.getId())).getName()));
    }
  }

  /**
   * Tests the getCopy method retrieves an existing Rule correctly.
   */
  @Test
  public void testGetCopy() {
    RuleTagCacheObject cacheObject = (RuleTagCacheObject) ruleTagCache.getCopy(60002L);
    RuleTagCacheObject objectInDb = (RuleTagCacheObject) ruleTagMapper.getItem(60002L);
    //Servertimestamp is always set when creating the object, so the second call above should have a t.s. that is later (first
    // is set when loading cache from DB)
    assertTrue(objectInDb.getTimestamp().after(cacheObject.getTimestamp()));
    //reset t.s. for comparison method to succeed
    objectInDb.setCacheTimestamp(cacheObject.getCacheTimestamp());
    CacheObjectComparison.equalsTag(cacheObject, objectInDb);
  }

  /**
   * Tests the parent ids are loaded correctly from the DB on a simple get() call.
   */
  @Test
  public void testParentIdLoading() {
   // first remove the object from the cache
   ruleTagCache.remove(60011L);

   //load from DB & check parent ids
   RuleTag rule = ruleTagCache.loadFromDb(60011L);
   assertNotNull(rule);
   assertEquals(2, rule.getProcessIds().size());
   assertEquals(2, rule.getEquipmentIds().size());
   assertTrue(rule.getProcessIds().contains(50L));
   assertTrue(rule.getProcessIds().contains(51L));
   assertTrue(rule.getEquipmentIds().contains(150L));
   assertTrue(rule.getEquipmentIds().contains(170L));
  }

  /**
   * New test with differently configured rule for detecting problems in recursive rule loading.
   */
  @Test
  public void testInitialParentIdLoading() {
    // first remove the object from the cache
    ruleTagCache.remove(59999L);

    // load from DB & check parent ids
    RuleTag rule = ruleTagCache.loadFromDb(59999L);

    assertNotNull(rule);
    assertEquals(2, rule.getProcessIds().size());
    assertEquals(2, rule.getEquipmentIds().size());
    assertTrue(rule.getProcessIds().contains(50L));
    assertTrue(rule.getProcessIds().contains(51L));
    assertTrue(rule.getEquipmentIds().contains(150L));
    assertTrue(rule.getEquipmentIds().contains(170L));
  }
  
  @Test
  public void testGetTagByName() {
    Assert.assertNull(ruleTagCache.get("does not exist"));
    
    Tag tag = ruleTagCache.get("DIAMON_clic_CS-CCR-DEV3");
    Assert.assertNotNull(tag);
    Assert.assertEquals(Long.valueOf(60000L), tag.getId());
    Assert.assertEquals("Integer", tag.getDataType());
    
    tag = ruleTagCache.get("RULE_WITH_MuLtIpLe_PARENTS");
    Assert.assertNotNull(tag);
    Assert.assertEquals(Long.valueOf(60011L), tag.getId());
    Assert.assertEquals("Integer", tag.getDataType()); 
  }
  
  /**
   * Tag 60001 is referenced by rules 60008, 60009, 60010, 60011.
   */
  @Test
  public void testSearchByRuleInputTagId() {
    Collection<RuleTag> resultList = ruleTagCache.findByRuleInputTagId(60001L);
    Assert.assertNotNull(resultList);
    Assert.assertEquals(4, resultList.size());

    Assert.assertTrue(resultList.stream().anyMatch(tag -> tag.getId() == 60008L));
    Assert.assertTrue(resultList.stream().anyMatch(tag -> tag.getId() == 60009L));
    Assert.assertTrue(resultList.stream().anyMatch(tag -> tag.getId() == 60010L));
    Assert.assertTrue(resultList.stream().anyMatch(tag -> tag.getId() == 60011L));
  }

  @Test
  public void testSearchWithNameWildcard() {
    Collection<RuleTag> resultList = ruleTagCache.findByNameWildcard("does not exist");
    Assert.assertNotNull(resultList);
    Assert.assertEquals(0, resultList.size());
    
    resultList = ruleTagCache.findByNameWildcard("DIAMON_clic_CS-CCR-DEV3");
    Assert.assertNotNull(resultList);
    Assert.assertEquals(1, resultList.size());
    RuleTag tag = resultList.iterator().next();
    Assert.assertEquals(Long.valueOf(60000L), tag.getId());
    Assert.assertEquals("Integer", tag.getDataType());
    
    String regex = "DIAMON_clic_CS-CCR-*";
    resultList = ruleTagCache.findByNameWildcard(regex);
    Assert.assertNotNull(resultList);
    Assert.assertEquals(11, resultList.size());
    for (RuleTag ruleTag : resultList) {
      Assert.assertTrue(ruleTag.getName().toLowerCase().startsWith(regex.substring(0, regex.lastIndexOf('*')).toLowerCase()));
    }
    
    
    String regex2 = "DIAMON_*_CS-CCR-*";
    resultList = ruleTagCache.findByNameWildcard(regex2);
    Assert.assertNotNull(resultList);
    Assert.assertEquals(11, resultList.size());
    for (RuleTag ruleTag : resultList) {
      Assert.assertTrue(ruleTag.getName().toLowerCase().startsWith(regex2.substring(0, regex2.indexOf('*')).toLowerCase()));
    }
    
    
    String regex3 = "*_PARENTS";
    resultList = ruleTagCache.findByNameWildcard(regex3);
    Assert.assertNotNull(resultList);
    Assert.assertEquals(1, resultList.size());
    for (RuleTag ruleTag : resultList) {
      Assert.assertTrue(ruleTag.getName().toLowerCase().endsWith(regex3.substring(regex3.lastIndexOf('*') + 1).toLowerCase()));
    }
  }
}
