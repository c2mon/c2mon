package cern.c2mon.server.cache.dbaccess;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.sql.Timestamp;
import java.util.List;

import javax.annotation.Resource;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import cern.c2mon.server.cache.dbaccess.structure.DBBatch;
import cern.c2mon.server.cache.dbaccess.test.TestDataHelper;
import cern.c2mon.server.common.rule.RuleTag;
import cern.c2mon.server.common.rule.RuleTagCacheObject;
import cern.c2mon.server.test.CacheObjectCreation;
import cern.c2mon.shared.common.datatag.DataTagQualityImpl;
import cern.c2mon.shared.common.datatag.TagQualityStatus;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:cern/c2mon/server/cache/dbaccess/config/server-cachedbaccess-test.xml"})
@TransactionConfiguration(transactionManager="cacheTransactionManager", defaultRollback=true)
@Transactional
public class RuleTagMapperTest {

  @Resource
  private RuleTagMapper ruleTagMapper;
  
  @Resource
  private TestDataHelper testDataHelper;
  
  //private RuleTag ruleTag;
  
//  @Before
//  public void insertTestTag() {
//    testDataHelper.insertTestData();
//    ruleTag = testDataHelper.getProcess();
//  }
  
  @Test
  public void testInsertCompletes() {
    RuleTagCacheObject ruleTag = CacheObjectCreation.createTestRuleTag();
    ruleTagMapper.insertRuleTag(ruleTag);
    ruleTagMapper.deleteRuleTag(ruleTag.getId());
  }
  
  
  @Test
  public void testGetAllControlTags() {
    assertNotNull(ruleTagMapper);
    List<RuleTag> allList = ruleTagMapper.getAll();
    assertTrue(allList.size() != 0);
  }
  
  @Test
  public void testGetNumberItems() {
    assertTrue(ruleTagMapper.getNumberItems() > 5);  
  }
  
  @Test
  /**
   * Need 25 rules in the DATATAG table.
   */
  public void testGetRowBatch() {
    DBBatch dbBatch = new DBBatch(60000L, 60006L);
    List<RuleTag> ruletags = ruleTagMapper.getRowBatch(dbBatch);
    assertNotNull(ruletags);
    assertEquals(7, ruletags.size());
    
    DBBatch dbBatch2 = new DBBatch(60000L, 60000L);
    List<RuleTag> ruletags2 = ruleTagMapper.getRowBatch(dbBatch2);
    assertNotNull(ruletags2);
    assertEquals(1, ruletags2.size()); 
  }
  
  @Test
  public void testInsertAndRetrieve() {
    testDataHelper.createTestData();
    testDataHelper.insertTestDataIntoDB();
    RuleTagCacheObject ruleTag = testDataHelper.getRuleTag();
    //topic is set when process ids are set so is set to default here
    ruleTag.setTopic("c2mon.tag.default.publication");
    RuleTagCacheObject retrievedObject = (RuleTagCacheObject) ruleTagMapper.getItem(ruleTag.getId());
    
    assertNotNull(retrievedObject);
    
    //compare        
    assertEquals(ruleTag.getId(), retrievedObject.getId());
    assertEquals(ruleTag.getName(), retrievedObject.getName());
    assertEquals(ruleTag.getDescription(), retrievedObject.getDescription());
    assertEquals(ruleTag.getMode(), retrievedObject.getMode());
    assertEquals(ruleTag.getDataType(), retrievedObject.getDataType());
    assertEquals(ruleTag.isLogged(), retrievedObject.isLogged());
    assertEquals(ruleTag.getUnit(), retrievedObject.getUnit());
    assertEquals(ruleTag.getDipAddress(), retrievedObject.getDipAddress());
    assertEquals(ruleTag.getJapcAddress(), retrievedObject.getJapcAddress());
    assertEquals(ruleTag.getValue(), retrievedObject.getValue());
    assertEquals(ruleTag.getTopic(), retrievedObject.getTopic());
    assertEquals(ruleTag.getValueDescription(), retrievedObject.getValueDescription());
    assertEquals(ruleTag.isSimulated(), retrievedObject.isSimulated());    
    assertEquals(ruleTag.getValueDictionary().toXML(), retrievedObject.getValueDictionary().toXML()); //compare XML of value dictionary  
    assertEquals(ruleTag.getDataTagQuality(), retrievedObject.getDataTagQuality()); 
    assertEquals(ruleTag.getTimestamp(), retrievedObject.getTimestamp());
    assertEquals(ruleTag.getRuleIdsString(), retrievedObject.getRuleIdsString());
    
    //rule specific
    assertEquals(ruleTag.getRuleIdsString(), retrievedObject.getRuleIdsString());
  }
  
  @Test
  public void testUpdateRuleTag() { 
    testDataHelper.createTestData();
    testDataHelper.insertTestDataIntoDB();
    RuleTagCacheObject ruleTag = testDataHelper.getRuleTag();
    
    ruleTag.setValue(new Integer(2000));    
    ruleTag.setCacheTimestamp(new Timestamp(System.currentTimeMillis()));
    ruleTag.setValueDescription("new control value");
    ruleTag.setDataTagQuality(new DataTagQualityImpl(TagQualityStatus.UNKNOWN_REASON, "test quality unknown reason"));
    ruleTag.setSimulated(false);
    
    ruleTagMapper.updateCacheable(ruleTag);
    
    RuleTagCacheObject retrievedObject = (RuleTagCacheObject) ruleTagMapper.getItem(ruleTag.getId());
    
    assertEquals(ruleTag.getValue(), retrievedObject.getValue());
    assertEquals(ruleTag.getValueDescription(), retrievedObject.getValueDescription());    
    assertEquals(ruleTag.getDataTagQuality(), retrievedObject.getDataTagQuality()); 
    assertEquals(ruleTag.getTimestamp(), retrievedObject.getTimestamp());    
    assertEquals(ruleTag.isSimulated(), retrievedObject.isSimulated());
  }
  
  @Test
  public void testIsInDB() {
    assertTrue(ruleTagMapper.isInDb(60001L));
  }
  
  @Test
  public void testNotInDB() {
    assertFalse(ruleTagMapper.isInDb(200000L));
  }
  
  @After
  public void cleanDB() {
    testDataHelper.removeTestData();
  }
  
}
