package cern.c2mon.server.cache.dbaccess;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
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

import cern.c2mon.server.cache.dbaccess.test.TestDataHelper;
import cern.c2mon.server.common.process.ProcessCacheObject;
import cern.c2mon.server.common.process.ProcessCacheObject.LocalConfig;
import cern.c2mon.server.test.CacheObjectComparison;
import cern.c2mon.shared.common.Cacheable;
import cern.c2mon.shared.common.supervision.SupervisionConstants.SupervisionStatus;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:cern/c2mon/server/cache/dbaccess/config/server-cachedbaccess-test.xml"})
@TransactionConfiguration(transactionManager="cacheTransactionManager", defaultRollback=true)
@Transactional
public class ProcessMapperTest {

  /**
   * Class to test
   */
  @Autowired
  private ProcessMapper processMapper;
  
  @Autowired
  private TestDataHelper testDataHelper;
  
  private ProcessCacheObject originalProcess;
  
  
  @SuppressWarnings("deprecation")
  @Before
  public void insertTestProcess() {
    testDataHelper.createTestData();
    testDataHelper.insertTestDataIntoDB();
    originalProcess = testDataHelper.getProcess();
  }
  
  @After
  public void deleteTestProcess() {
    //processMapper.deleteProcess(Long.valueOf(100000));
    testDataHelper.removeTestData();
  }
  
  /**
   * Test does not insert associated equipment yet and test equipment retrieval 
   *  - add later TODO
   */
  @Test
  public void testInsertAndRetrieve() {
//    ProcessCacheObject originalProcess = createTestProcess1();
//    processMapper.insertProcess(originalProcess);
    
    ProcessCacheObject retrievedProcess = (ProcessCacheObject) processMapper.getItem(originalProcess.getId());
    
    assertNotNull(retrievedProcess);
    
    assertEquals(originalProcess.getId(), retrievedProcess.getId());
    assertEquals(originalProcess.getName(), retrievedProcess.getName());
    assertEquals(originalProcess.getDescription(), retrievedProcess.getDescription());
    assertEquals(originalProcess.getMaxMessageDelay(), retrievedProcess.getMaxMessageDelay());
    assertEquals(originalProcess.getMaxMessageSize(), retrievedProcess.getMaxMessageSize());
    assertEquals(originalProcess.getStateTagId(), retrievedProcess.getStateTagId());
    assertEquals(originalProcess.getAliveInterval(), retrievedProcess.getAliveInterval());
    assertEquals(originalProcess.getAliveTagId(), retrievedProcess.getAliveTagId());
//    assertEquals(originalProcess.getSupervisionStatus(), retrievedProcess.getSupervisionStatus()); no longer persisted to DB; set to UNCERTAIN when server starts
    assertEquals(originalProcess.getStartupTime(), retrievedProcess.getStartupTime());
    assertEquals(originalProcess.getCurrentHost(), retrievedProcess.getCurrentHost());
    assertEquals(originalProcess.getEquipmentIds(), retrievedProcess.getEquipmentIds());
    assertEquals(originalProcess.getRequiresReboot(), retrievedProcess.getRequiresReboot());
    assertEquals(originalProcess.getStatusTime(), retrievedProcess.getStatusTime());
    assertEquals(originalProcess.getStatusDescription(), retrievedProcess.getStatusDescription());    
    assertEquals(originalProcess.getProcessPIK(), retrievedProcess.getProcessPIK());   
    assertEquals(originalProcess.getLocalConfig(), retrievedProcess.getLocalConfig());   
    
  }
  
  /**
   * Tests the result set is none empty.
   */
  @Test
  public void testGetAll() {
    List<Cacheable> returnList = processMapper.getAll();
    assertTrue(returnList.size() > 0);
  }
  
  /**
   * Tests the cache persistence method.
   */
  @Test
  public void testUpdate() {
    assertFalse(originalProcess.getSupervisionStatus().equals(SupervisionStatus.RUNNING));
    originalProcess.setSupervisionStatus(SupervisionStatus.RUNNING);
    Timestamp ts = new Timestamp(System.currentTimeMillis() + 1000);
    originalProcess.setStartupTime(ts);
    originalProcess.setRequiresReboot(true);
    originalProcess.setStatusDescription("New status description.");
    originalProcess.setStatusTime(ts);
    originalProcess.setProcessPIK(67890L);
    originalProcess.setLocalConfig(LocalConfig.N);
    
    processMapper.updateCacheable(originalProcess);
    
    ProcessCacheObject retrievedProcess = (ProcessCacheObject) processMapper.getItem(originalProcess.getId());
    assertEquals(SupervisionStatus.RUNNING, retrievedProcess.getSupervisionStatus());
    assertEquals(ts, retrievedProcess.getStartupTime());
    assertEquals(originalProcess.getRequiresReboot(), retrievedProcess.getRequiresReboot());
    assertEquals(originalProcess.getStatusDescription(), retrievedProcess.getStatusDescription());
    assertEquals(originalProcess.getStatusTime(), retrievedProcess.getStatusTime());
    assertEquals(originalProcess.getProcessPIK(), retrievedProcess.getProcessPIK());   
    assertEquals(originalProcess.getLocalConfig(), retrievedProcess.getLocalConfig()); 
  }
  
  @Test
  public void testIsInDB() {
    assertTrue(processMapper.isInDb(50L));
  }
  
  @Test
  public void testNotInDB() {
    assertFalse(processMapper.isInDb(150L));
  }
  
}
