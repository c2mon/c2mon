package cern.c2mon.server.cache.dbaccess;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.sql.Timestamp;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import cern.c2mon.server.cache.dbaccess.test.TestDataHelper;
import cern.c2mon.server.common.subequipment.SubEquipment;
import cern.c2mon.server.common.subequipment.SubEquipmentCacheObject;
import cern.c2mon.shared.common.supervision.SupervisionConstants.SupervisionStatus;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:cern/c2mon/server/cache/dbaccess/config/server-cachedbaccess-test.xml"})
@TransactionConfiguration(transactionManager="cacheTransactionManager", defaultRollback=true)
@Transactional
public class SubEquipmentMapperTest {
  /**
   * Class to test
   */
  @Autowired
  private SubEquipmentMapper subEquipmentMapper;
  
  @Autowired
  private EquipmentMapper equipmentMapper;
  
  @Autowired
  private TestDataHelper testDataHelper;
  
  private SubEquipmentCacheObject subEquipmentCacheObject;
  
  @Before
  public void loadTestData() {
    testDataHelper.createTestData();
    testDataHelper.insertTestDataIntoDB();
    subEquipmentCacheObject = testDataHelper.getSubEquipment();
  }
  
  /**
   * Tests the result set is none empty.
   */
  @Test
  public void testGetAll() {
    List<SubEquipment> returnList = subEquipmentMapper.getAll();
    assertTrue(returnList.size() > 0);
  }
  
  @Test
  public void testRetrieve() {
//    EquipmentCacheObject equipmentCacheObject = EquipmentMapperTest.createTestEquipment();
//    equipmentMapper.insertEquipment(equipmentCacheObject);
//    SubEquipmentCacheObject subEquipmentCacheObject = EquipmentMapperTest.createTestSubEquipment();
//    subEquipmentMapper.insertSubEquipment(subEquipmentCacheObject);
    SubEquipmentCacheObject retrievedObject = (SubEquipmentCacheObject) subEquipmentMapper.getItem(subEquipmentCacheObject.getId());
    assertEquals(subEquipmentCacheObject.getId(), retrievedObject.getId());
    assertEquals(subEquipmentCacheObject.getName(), retrievedObject.getName());    
    assertEquals(subEquipmentCacheObject.getAliveInterval(), retrievedObject.getAliveInterval());
    assertEquals(subEquipmentCacheObject.getAliveTagId(), retrievedObject.getAliveTagId());
    assertEquals(subEquipmentCacheObject.getCommFaultTagId(), retrievedObject.getCommFaultTagId());    
    assertEquals(subEquipmentCacheObject.getStateTagId(), retrievedObject.getStateTagId());
    assertEquals(subEquipmentCacheObject.getHandlerClassName(), retrievedObject.getHandlerClassName());
    assertEquals(subEquipmentCacheObject.getDescription(), retrievedObject.getDescription()); 
    assertEquals(subEquipmentCacheObject.getParentId(), retrievedObject.getParentId());
    assertEquals(subEquipmentCacheObject.getStatusDescription(), retrievedObject.getStatusDescription());
    assertEquals(subEquipmentCacheObject.getStatusTime(), retrievedObject.getStatusTime());
    assertEquals(subEquipmentCacheObject.getSupervisionStatus(), retrievedObject.getSupervisionStatus());
  }
  
  
  @Test
  public void testSelectSubEquipmentsByEquipment() {
    List<SubEquipment> subEquipmentList = subEquipmentMapper.selectSubEquipmentsByEquipment(subEquipmentCacheObject.getParentId());
    assertEquals(2, subEquipmentList.size());
  }
  
  @Test
  public void testUpdateConfig() {
    assertEquals(new Long(5000300), subEquipmentCacheObject.getAliveTagId());
    subEquipmentCacheObject.setAliveTagId(1251L);
    subEquipmentCacheObject.setCommFaultTagId(1252L);
    subEquipmentCacheObject.setStateTagId(1250L);
    subEquipmentMapper.updateSubEquipmentConfig(subEquipmentCacheObject);
    SubEquipment updatedEquipment = subEquipmentMapper.getItem(subEquipmentCacheObject.getId());
    assertEquals(new Long(1251), updatedEquipment.getAliveTagId());
    assertEquals(new Long(1252), updatedEquipment.getCommFaultTagId());
    assertEquals(new Long(1250), updatedEquipment.getStateTagId());      
  }
  
  /**
   * Tests the cache persistence method.
   */
  @Test
  @DirtiesContext
  public void testUpdate() {
    assertFalse(subEquipmentCacheObject.getSupervisionStatus().equals(SupervisionStatus.RUNNING));
    subEquipmentCacheObject.setSupervisionStatus(SupervisionStatus.RUNNING); 
    Timestamp ts = new Timestamp(System.currentTimeMillis() + 1000);
    subEquipmentCacheObject.setStatusDescription("New status description.");
    subEquipmentCacheObject.setStatusTime(ts);
    subEquipmentMapper.updateCacheable(subEquipmentCacheObject);
    
    SubEquipmentCacheObject retrievedEquipment = (SubEquipmentCacheObject) subEquipmentMapper.getItem(subEquipmentCacheObject.getId());
    assertEquals(SupervisionStatus.RUNNING, retrievedEquipment.getSupervisionStatus());
    assertEquals(ts, retrievedEquipment.getStatusTime());
    assertEquals("New status description.", retrievedEquipment.getStatusDescription());
  }
  
  @Test
  public void testIsInDB() {
    assertTrue(subEquipmentMapper.isInDb(250L));
  }
  
  @Test
  public void testNotInDB() {
    assertFalse(subEquipmentMapper.isInDb(150L));
  }
    
  @After
  public void cleanDatabase() {
    testDataHelper.removeTestData();
  }
  
}
