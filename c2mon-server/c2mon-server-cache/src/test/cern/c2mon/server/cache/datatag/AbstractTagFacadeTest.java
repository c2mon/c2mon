package cern.c2mon.server.cache.datatag;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.sql.Timestamp;

import org.junit.Before;
import org.junit.Test;

import cern.c2mon.server.cache.rule.RuleTagFacadeImpl;
import cern.c2mon.server.common.rule.RuleTagCacheObject;
import cern.c2mon.shared.common.datatag.TagQualityStatus;

/**
 * JUnit test of AbstractTagFacade class.
 * @author Mark Brightwell
 *
 */
public class AbstractTagFacadeTest {

  /**
   * Testing AbstractTagFacade.
   */
  private RuleTagFacadeImpl ruleTagFacade; 
  
  @Before
  public void init() {   
    ruleTagFacade = new RuleTagFacadeImpl(null, null, null, null, null);
  }
  
  /**
   * Testing filterout() method.
   */
  @Test
  public void testInvalidUpdateFilteredOut() {
    RuleTagCacheObject rule = new RuleTagCacheObject(10L);
    rule.setValue(20L);
    rule.setValueDescription("value description");
    rule.getDataTagQuality().addInvalidStatus(TagQualityStatus.VALUE_EXPIRED, "quality description");
    
    //should be filterout out
    assertTrue(ruleTagFacade.filterout(rule, 20L, "value description", TagQualityStatus.VALUE_EXPIRED, "quality description", new Timestamp(System.currentTimeMillis())));
  }
  
  @Test
  public void testValidUpdateNotFilteredOut() {
    RuleTagCacheObject rule = new RuleTagCacheObject(10L);
    rule.setValue(21L);
    rule.setValueDescription("value description");    
    
    //should be filterout out
    assertFalse(ruleTagFacade.filterout(rule, 20L, "value description", null, null, new Timestamp(System.currentTimeMillis())));
  }
  
  @Test
  public void testNewValueDescriptionNotFilteredOut() {
    RuleTagCacheObject rule = new RuleTagCacheObject(10L);
    rule.setValue(20L);
    rule.setValueDescription("value description");   
    rule.getDataTagQuality().addInvalidStatus(TagQualityStatus.VALUE_EXPIRED, "quality description");
    
    //should be filterout out
    assertFalse(ruleTagFacade.filterout(rule, 20L, "new value description", TagQualityStatus.VALUE_EXPIRED, "quality description", new Timestamp(System.currentTimeMillis())));
  }
  
  @Test(expected=IllegalArgumentException.class)
  public void testFailWithOnlyQualityDescription() {
    RuleTagCacheObject rule = new RuleTagCacheObject(10L);
    rule.setValue(21L);
    rule.setValueDescription("value description");   
    
    //should be filterout out
    assertTrue(ruleTagFacade.filterout(rule, 20L, "new value description", null, "quality description", new Timestamp(System.currentTimeMillis())));
  }
  
  /**
   * As above but with null description and quality description.
   */
  @Test
  public void testInvalidUpdateFilteredOut2() {
    RuleTagCacheObject rule = new RuleTagCacheObject(10L);
    rule.setValue(20L);
    rule.getDataTagQuality().addInvalidStatus(TagQualityStatus.VALUE_EXPIRED, null);
    
    //should be filterout out
    assertTrue(ruleTagFacade.filterout(rule, 20L, null, TagQualityStatus.VALUE_EXPIRED, null, new Timestamp(System.currentTimeMillis())));
  }
  
  @Test 
  public void testFilterOutRepeatedNull() {
    RuleTagCacheObject rule = new RuleTagCacheObject(10L);
    rule.setValue(null);
    rule.getDataTagQuality().addInvalidStatus(TagQualityStatus.VALUE_EXPIRED, null);
    
    //should be filterout out
    assertTrue(ruleTagFacade.filterout(rule, null, null, TagQualityStatus.VALUE_EXPIRED, null, new Timestamp(System.currentTimeMillis())));
  }
  
  @Test(expected=NullPointerException.class)
  public void testFilteroutWithNullTag() {
    ruleTagFacade.filterout(null, null, null, null, null, new Timestamp(System.currentTimeMillis()));
  }
  
  @Test
  public void testFilterOutInvalidation() {
    RuleTagCacheObject rule = new RuleTagCacheObject(10L);
    rule.setValue(20L);
    rule.setValueDescription("value description");   
    rule.getDataTagQuality().addInvalidStatus(TagQualityStatus.VALUE_EXPIRED, "quality description");
    
    //should be filterout out
    assertTrue(ruleTagFacade.filteroutInvalidation(rule, TagQualityStatus.VALUE_EXPIRED, "quality description", new Timestamp(System.currentTimeMillis())));
  }
  
}
