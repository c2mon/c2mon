package cern.c2mon.server.cache.dbaccess;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

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

import cern.c2mon.server.cache.dbaccess.test.TestDataHelper;
import cern.c2mon.server.common.alive.AliveTimer;
import cern.c2mon.server.common.alive.AliveTimerCacheObject;
import cern.c2mon.shared.common.Cacheable;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:cern/c2mon/server/cache/dbaccess/config/server-cachedbaccess-test.xml"})
@TransactionConfiguration(transactionManager="cacheTransactionManager", defaultRollback=true)
@Transactional
public class AliveTimerMapperTest {
  
  private static final Long TEST_ALIVE_ID = new Long(500000);
  
  /**
   * Class to test.
   */
  @Autowired
  private AliveTimerMapper aliveTimerMapper;
  
  /**
   * For creating test data.
   */
  @Autowired
  private TestDataHelper testDataHelper;
  
  @Before
  public void insertTestData() {
    testDataHelper.createTestData();
    testDataHelper.insertTestDataIntoDB();
  }
  
  @After
  public void cleanDataBase() {
    testDataHelper.removeTestData(); 
  }
  
  //need tests inserting process, equipment and check appear in retrieved view
  @Test
  public void testRetrieveProcessAlive() {
  //id in control tag cache is the same as in alivetimer cache
    AliveTimerCacheObject retrievedCacheObject = (AliveTimerCacheObject) aliveTimerMapper.getItem(testDataHelper.getProcessAliveTag().getId());
    assertEquals(testDataHelper.getProcessAliveTag().getId(), retrievedCacheObject.getId());
    assertEquals(testDataHelper.getProcess().getAliveInterval(), retrievedCacheObject.getAliveInterval());
    assertEquals("PROC", retrievedCacheObject.getAliveType());
    assertEquals(testDataHelper.getProcess().getId(), retrievedCacheObject.getRelatedId());
    assertEquals(testDataHelper.getProcess().getName(), retrievedCacheObject.getRelatedName());
    assertEquals(testDataHelper.getProcess().getStateTagId(), retrievedCacheObject.getRelatedStateTagId());    
    assertTrue(retrievedCacheObject.getDependentAliveTimerIds().size() == 1); //2 dependent alive timers (eq and subeq)
    assertTrue(retrievedCacheObject.getDependentAliveTimerIds().contains(testDataHelper.getEquipment().getAliveTagId()));
    //assertTrue(retrievedCacheObject.getDependentAliveTimerIds().contains(testDataHelper.getSubEquipment().getAliveTagId())); only contains equipment alives!
  }
  
  /**
   * So far, only tests retrieved list of values is not empty.
   */
  @Test
  public void testGetAll() {
    List<AliveTimer> returnList = aliveTimerMapper.getAll();
    assertTrue(returnList.size() > 0);
  }
  
  @Test
  public void testGetOne() {
    Cacheable item = aliveTimerMapper.getItem(1221);  
    assertNotNull(item);
  }
  
  private AliveTimerCacheObject createTestAliveTimerOld() {
    AliveTimerCacheObject aliveTimer = new AliveTimerCacheObject(TEST_ALIVE_ID);
//    aliveTimer.setAliveTagId(TEST_ALIVE_ID);
    aliveTimer.setAliveType(AliveTimer.ALIVE_TYPE_PROCESS);
    aliveTimer.setAliveInterval(60000);
    aliveTimer.setRelatedId(Long.valueOf(1000000));
    aliveTimer.setRelatedName("test related name");
    aliveTimer.setRelatedStateTagId(Long.valueOf(400000));
    return aliveTimer;
  }
  
  @Test
  public void testIsInDB() {
    assertTrue(aliveTimerMapper.isInDb(1224L));
  }
  
  @Test
  public void testNotInDB() {
    assertFalse(aliveTimerMapper.isInDb(1263L));
  }
  
  
}
