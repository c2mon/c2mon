package cern.c2mon.server.cache.dbaccess;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.sql.Timestamp;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import cern.c2mon.server.cache.dbaccess.structure.DBBatch;
import cern.c2mon.server.cache.dbaccess.test.TestDataHelper;
import cern.c2mon.server.common.datatag.DataTag;
import cern.c2mon.server.common.datatag.DataTagCacheObject;
import cern.c2mon.shared.common.datatag.DataTagAddress;
import cern.c2mon.shared.common.datatag.DataTagConstants;
import cern.c2mon.shared.common.datatag.DataTagQuality;
import cern.c2mon.shared.common.datatag.DataTagQualityImpl;
import cern.c2mon.shared.common.datatag.DataTagValueDictionary;
import cern.c2mon.shared.common.datatag.TagQualityStatus;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:cern/c2mon/server/cache/dbaccess/config/server-cachedbaccess-test.xml"})
@TransactionConfiguration(transactionManager="cacheTransactionManager", defaultRollback=true)
@Transactional
public class DataTagMapperTest {
  
  @Autowired
  private DataTagMapper dataTagMapper; 
  
  @Autowired
  private TestDataHelper testDataHelper;
  
  private DataTagCacheObject dataTag;
  
  
//  @BeforeClass
//  public static void setUp() {
//    //start Spring context
//    String contextFile = "cern/c2mon/server/cache/dbaccess/config/server-cachedbaccess-test.xml";
//    ApplicationContext applicationContext = new ClassPathXmlApplicationContext(contextFile);
//    dataTagMapper = (DataTagMapper) applicationContext.getBean("dataTagMapper");
//    testDataHelper = (TestDataHelper) applicationContext.getBean("testDataHelper");
//  }
  
  @Before
  public void setUp() {
    testDataHelper.removeTestData();
  }
  
  @After
  public void cleanDb() {
    testDataHelper.removeTestData();
  }
  
  @Test
  public void testAlarmCollectionCorrect() {
    testDataHelper.createTestData();
    testDataHelper.insertTestDataIntoDB();
    DataTagCacheObject dataTagFromDB = (DataTagCacheObject) dataTagMapper.getItem(testDataHelper.getDataTag().getId());
    Long alarmId1 = testDataHelper.getAlarm1().getId();
    Long alarmId2 = testDataHelper.getAlarm2().getId();
    Long alarmId3 = testDataHelper.getAlarm3().getId();
    assertEquals(3, dataTagFromDB.getAlarmIds().size());
    assertTrue(dataTagFromDB.getAlarmIds().contains(alarmId1));
    assertTrue(dataTagFromDB.getAlarmIds().contains(alarmId2));
    assertTrue(dataTagFromDB.getAlarmIds().contains(alarmId3));
  }
  
  @Test
  public void testGetNumberItems() {
    assertTrue(dataTagMapper.getNumberItems() > 5);   
  }
  
  @Test
  public void testGetRowBatch() {
    DBBatch dbBatch = new DBBatch(200000L, 200002L);
    List<DataTag> datatags = dataTagMapper.getRowBatch(dbBatch);
    assertNotNull(datatags);
    assertTrue(datatags.size() == 3);
    
    DBBatch dbBatch2 = new DBBatch(200002L, 200006L);
    List<DataTag> datatags2 = dataTagMapper.getRowBatch(dbBatch2);
    assertNotNull(datatags2);
    assertTrue(datatags2.size() == 4); //since 200006 is not in DB 
  }
  
  @Test
  public void testGetAllDataTags() {
    dataTagMapper.getAll();
  }
  
  @Test
  public void testGetDataTag() {
    //construct fake DataTagCacheObject, setting all fields
    DataTagCacheObject cacheObject = new DataTagCacheObject();
    cacheObject.setId(new Long(150000));  //must be non null in DB
    cacheObject.setName("Junit_test_tag"); //non null
    cacheObject.setDescription("test description");
    cacheObject.setMode(DataTagConstants.MODE_TEST); //non null
    cacheObject.setDataType("Boolean"); // non null
    cacheObject.setTopic("c2mon.tag." + 50);
    cacheObject.setLogged(false); //null allowed
    cacheObject.setUnit("test unit m/sec");
    cacheObject.setDipAddress("testDIPaddress");
    cacheObject.setJapcAddress("testJAPCaddress");
    cacheObject.setValue(Boolean.TRUE);
    cacheObject.setValueDescription("test value description");
    cacheObject.setSimulated(false); //null allowed
    cacheObject.setEquipmentId(new Long(150)); //need test equipment inserted - use EquipmentMapperTest
    cacheObject.setMinValue(new Float(23.3));
    cacheObject.setMaxValue(new Float(12.2));
    cacheObject.setValueDictionary(new DataTagValueDictionary());
    cacheObject.setAddress(new DataTagAddress());
    cacheObject.setDataTagQuality(new DataTagQualityImpl(TagQualityStatus.EQUIPMENT_DOWN));
    cacheObject.setCacheTimestamp(new Timestamp(System.currentTimeMillis()));
    cacheObject.setSourceTimestamp(new Timestamp(System.currentTimeMillis()));
    cacheObject.setRuleIdsString("1234");
    cacheObject.setProcessId(50L); //need test process also (P_JAPC01)
    
    
    //put in database
    dataTagMapper.testInsertDataTag(cacheObject);
    
    //retrieve from database
    DataTagCacheObject retrievedObject = (DataTagCacheObject) dataTagMapper.getItem(new Long(150000));
    
    assertEquals(cacheObject.getId(), retrievedObject.getId());
    assertEquals(cacheObject.getName(), retrievedObject.getName());
    assertEquals(cacheObject.getDescription(), retrievedObject.getDescription());
    assertEquals(cacheObject.getMode(), retrievedObject.getMode());
    assertEquals(cacheObject.getDataType(), retrievedObject.getDataType());
    assertEquals("c2mon.tag.default.publication", retrievedObject.getTopic()); //at loading, should be set to default
    assertEquals(cacheObject.isLogged(), retrievedObject.isLogged());
    assertEquals(cacheObject.getUnit(), retrievedObject.getUnit());
    assertEquals(cacheObject.getDipAddress(), retrievedObject.getDipAddress());
    assertEquals(cacheObject.getJapcAddress(), retrievedObject.getJapcAddress());
    assertEquals(cacheObject.getValue(), retrievedObject.getValue());
    assertEquals(cacheObject.getValueDescription(), retrievedObject.getValueDescription());
    assertEquals(cacheObject.isSimulated(), retrievedObject.isSimulated());
    assertEquals(cacheObject.getEquipmentId(), retrievedObject.getEquipmentId());
    assertEquals(cacheObject.getProcessId(), retrievedObject.getProcessId());
    assertEquals(cacheObject.getMinValue(), retrievedObject.getMinValue());
    assertEquals(cacheObject.getMaxValue(), retrievedObject.getMaxValue());
    assertEquals(cacheObject.getValueDictionary().toXML(), retrievedObject.getValueDictionary().toXML()); //compare XML of value dictionary
    assertEquals(cacheObject.getAddress(), retrievedObject.getAddress());
    assertEquals(cacheObject.getDataTagQuality(), retrievedObject.getDataTagQuality());   
    assertEquals(cacheObject.getTimestamp(), retrievedObject.getTimestamp());
    assertEquals(cacheObject.getSourceTimestamp(), retrievedObject.getSourceTimestamp());
    assertEquals(cacheObject.getRuleIdsString(), retrievedObject.getRuleIdsString());
    
    dataTagMapper.deleteDataTag(cacheObject.getId());
  }
  
  @Test
  public void testUpdateDataTag() {
    //construct fake DataTagCacheObject
    DataTagCacheObject cacheObject = new DataTagCacheObject();
    cacheObject.setId(new Long(150000));  //must be non null in DB
    cacheObject.setName("Junit_test_tag"); //non null    
    cacheObject.setMode(DataTagConstants.MODE_TEST); //non null
    cacheObject.setDataType("Boolean"); // non null
    cacheObject.setEquipmentId(new Long(150)); //need test equipment inserted
    
    dataTagMapper.testInsertDataTag(cacheObject);
     
    cacheObject.setValue(Boolean.TRUE);
    cacheObject.setValueDescription("test value description");
    cacheObject.setSimulated(false); //null allowed    
    cacheObject.setDataTagQuality(new DataTagQualityImpl(TagQualityStatus.UNDEFINED_VALUE,"undefined value"));
    cacheObject.setCacheTimestamp(new Timestamp(System.currentTimeMillis()));
    cacheObject.setSourceTimestamp(new Timestamp(System.currentTimeMillis()));

    dataTagMapper.updateCacheable(cacheObject);
    
    DataTagCacheObject retrievedObject = (DataTagCacheObject) dataTagMapper.getItem(new Long(150000));
    
    //updated values are changed
    assertEquals(cacheObject.getValue(), retrievedObject.getValue());
    assertEquals(cacheObject.getValueDescription(), retrievedObject.getValueDescription());
    assertEquals(cacheObject.isSimulated(), retrievedObject.isSimulated());
    assertEquals(cacheObject.getDataTagQuality(), retrievedObject.getDataTagQuality());
    assertEquals(cacheObject.getTimestamp(), retrievedObject.getTimestamp());
    assertEquals(cacheObject.getSourceTimestamp(), retrievedObject.getSourceTimestamp());
    
    //other values should be the same or ...
    assertEquals(cacheObject.getId(), retrievedObject.getId());
    assertEquals(cacheObject.getName(), retrievedObject.getName());
    assertEquals(cacheObject.getMode(), retrievedObject.getMode());
    assertEquals(cacheObject.getDataType(), retrievedObject.getDataType());
    
    //... null/default
    assertNull(retrievedObject.getDescription());
    assertEquals(false, retrievedObject.isLogged()); //default boolean
    assertNull(retrievedObject.getUnit());
    assertNull(retrievedObject.getDipAddress());
    assertNull(retrievedObject.getJapcAddress());
    assertNull(retrievedObject.getMinValue());
    assertNull(retrievedObject.getMaxValue());
    assertEquals(cacheObject.getValueDictionary().toXML(), retrievedObject.getValueDictionary().toXML());
    assertNull(retrievedObject.getAddress());
    
    dataTagMapper.deleteDataTag(cacheObject.getId());
        
  }
  
  @Test
  public void testIsInDB() {
    assertTrue(dataTagMapper.isInDb(200000L));
  }
  
  @Test
  public void testNotInDB() {
    assertFalse(dataTagMapper.isInDb(60010L));
  }
  
  /**
   * Make sure the test DataTag is deleted after each method.
   */
//  @After
//  public void deleteTestDataTag() {
//    dataTagMapper.deleteDataTag(dataTag.getId());
//  }
 
}
