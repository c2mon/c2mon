package cern.c2mon.server.cache.rule;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import cern.c2mon.server.cache.dbaccess.RuleTagMapper;
import cern.c2mon.server.common.rule.RuleTag;
import cern.c2mon.server.common.rule.RuleTagCacheObject;
import cern.c2mon.server.test.CacheObjectComparison;

/**
 * Integration test of the RuleTagCache with the loading
 * and DB access modules.
 * 
 * @author Mark Brightwell
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext
@ContextConfiguration({"classpath:cern/c2mon/server/cache/config/server-cache-rule-test.xml"})
public class RuleTagCacheTest {

  @Autowired
  private RuleTagMapper ruleTagMapper;
  
  @Autowired
  private RuleTagCacheImpl ruleTagCache;
  
  @Test
  @DirtiesContext
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
  @DirtiesContext
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
}
