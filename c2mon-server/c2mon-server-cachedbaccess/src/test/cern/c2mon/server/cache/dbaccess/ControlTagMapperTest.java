package cern.c2mon.server.cache.dbaccess;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import java.sql.Timestamp;
import java.util.List;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import cern.c2mon.server.common.control.ControlTag;
import cern.c2mon.server.common.control.ControlTagCacheObject;
import cern.c2mon.server.test.CacheObjectCreation;
import cern.c2mon.shared.common.Cacheable;
import cern.c2mon.shared.common.datatag.DataTagQuality;
import cern.c2mon.shared.common.datatag.DataTagQualityImpl;
import cern.c2mon.shared.common.datatag.TagQualityStatus;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:cern/c2mon/server/cache/dbaccess/config/server-cachedbaccess-test.xml"})
@TransactionConfiguration(transactionManager="cacheTransactionManager", defaultRollback=true)
@Transactional
public class ControlTagMapperTest {

  /**
   * The class to test.
   */
  @Autowired
  private ControlTagMapper controlTagMapper;    
  
  @After
  public void deleteTestTag() {
    controlTagMapper.deleteControlTag(new Long(1001));
  }
  
  @Test
  public void testGetAllControlTags() {
    assertNotNull(controlTagMapper);
    List<ControlTag> allList = controlTagMapper.getAll();
    assertNotSame(0, allList.size());
  }
  
  @Test
  public void testInsertControlTagAndGetItem() {
    ControlTagCacheObject cacheObject = (ControlTagCacheObject) CacheObjectCreation.createTestControlTag(); 
    controlTagMapper.insertControlTag(cacheObject); //insert into DB
    ControlTagCacheObject retrievedObject = (ControlTagCacheObject) controlTagMapper.getItem(cacheObject.getId()); //retrieve from DB
    
    assertNotNull(retrievedObject);
    
    //check the persistence was correct
    assertEquals(cacheObject.getId(), retrievedObject.getId());
    assertEquals(cacheObject.getName(), retrievedObject.getName());
    assertEquals(cacheObject.getDescription(), retrievedObject.getDescription());
    assertEquals(cacheObject.getMode(), retrievedObject.getMode());
    assertEquals(cacheObject.getDataType(), retrievedObject.getDataType());
    assertEquals("c2mon.tag.default.publication", retrievedObject.getTopic()); //should be set to default by mapper (later set by loader DAO)
    assertEquals(cacheObject.isLogged(), retrievedObject.isLogged());
    assertEquals(cacheObject.getUnit(), retrievedObject.getUnit());
    assertEquals(cacheObject.getDipAddress(), retrievedObject.getDipAddress());
    assertEquals(cacheObject.getJapcAddress(), retrievedObject.getJapcAddress());
    assertEquals(cacheObject.getValue(), retrievedObject.getValue());
    assertEquals(cacheObject.getValueDescription(), retrievedObject.getValueDescription());
    assertEquals(cacheObject.isSimulated(), retrievedObject.isSimulated());
    assertEquals(cacheObject.getEquipmentId(), retrievedObject.getEquipmentId());
    assertEquals(cacheObject.getMinValue(), retrievedObject.getMinValue());
    assertEquals(cacheObject.getMaxValue(), retrievedObject.getMaxValue());
    assertEquals(cacheObject.getValueDictionary().toXML(), retrievedObject.getValueDictionary().toXML()); //compare XML of value dictionary
    assertEquals(cacheObject.getAddress(), retrievedObject.getAddress());
    assertEquals(cacheObject.getDataTagQuality(), retrievedObject.getDataTagQuality());//quality compare code and string    
    assertEquals(cacheObject.getTimestamp(), retrievedObject.getTimestamp());
    assertEquals(cacheObject.getSourceTimestamp(), retrievedObject.getSourceTimestamp());
    assertEquals(cacheObject.getRuleIdsString(), retrievedObject.getRuleIdsString());
    
  }
  
  @Test
  public void testUpdateControlTag() {
    ControlTagCacheObject cacheObject = CacheObjectCreation.createTestControlTag();
    controlTagMapper.insertControlTag(cacheObject);
    
    cacheObject.setValue(new Long(2000));    
    cacheObject.setCacheTimestamp(new Timestamp(System.currentTimeMillis()));
    cacheObject.setSourceTimestamp(new Timestamp(System.currentTimeMillis()));
    cacheObject.setValueDescription("new control value");
    cacheObject.setDataTagQuality(new DataTagQualityImpl(TagQualityStatus.PROCESS_DOWN, "Process down."));
    cacheObject.setSimulated(false);
    
    controlTagMapper.updateCacheable(cacheObject);
    
    ControlTagCacheObject retrievedObject = (ControlTagCacheObject) controlTagMapper.getItem(cacheObject.getId());
    
    assertEquals(cacheObject.getValue(), retrievedObject.getValue());
    assertEquals(cacheObject.getValueDescription(), retrievedObject.getValueDescription());    
    assertEquals(cacheObject.getDataTagQuality(), retrievedObject.getDataTagQuality());//quality compare code and string
    assertEquals(cacheObject.getTimestamp(), retrievedObject.getTimestamp());
    assertEquals(cacheObject.getSourceTimestamp(), retrievedObject.getSourceTimestamp());
    assertEquals(cacheObject.isSimulated(), retrievedObject.isSimulated());
    
  }
  
  @Test
  public void testIsInDB() {
    assertTrue(controlTagMapper.isInDb(1230L));
  }
  
  @Test
  public void testNotInDB() {
    assertFalse(controlTagMapper.isInDb(200004L));
  }
  
}
