package cern.c2mon.server.cachepersistence;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import cern.c2mon.server.cache.AlarmCache;
import cern.c2mon.server.cache.dbaccess.AlarmMapper;
import cern.c2mon.server.cache.dbaccess.test.TestDataHelper;
import cern.c2mon.server.common.alarm.Alarm;
import cern.c2mon.server.common.alarm.AlarmCacheObject;
import cern.c2mon.server.common.alarm.AlarmCondition;

/**
 * Tests of persistence mechanisms to the Alarm cache.
 * Integration test with the cache module (including loading module).
 * @author Mark Brightwell
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext
@ContextConfiguration({"classpath:cern/c2mon/server/cachepersistence/config/server-cachepersistence-alarm-test.xml" })
//@TransactionConfiguration(transactionManager="cacheTransactionManager", defaultRollback=true)
//@Transactional(noRollbackFor = Exception.class)
public class AlarmCachePersistenceTest implements ApplicationContextAware {

  /**
   * Need context to explicitly start it (listeners
   * require an explicit start to the Spring context).
   */
  private ApplicationContext context;
  
  @Autowired
  private TestDataHelper testDataHelper;
  
  @Autowired
  private AlarmCache alarmCache;
  
  @Autowired
  private AlarmMapper alarmMapper;

  private Alarm originalObject;
  
  @Before
  public void before() {
    //insert test tag
    testDataHelper.removeTestData();
    testDataHelper.createTestData();
    testDataHelper.insertTestDataIntoDB();
    originalObject = testDataHelper.getAlarm1();
    
    //need *explicit* start of listeners
    ((AbstractApplicationContext) context).start();
  }
  
  @After
  public void cleanDB() {
    testDataHelper.removeTestData();
  }
  
  /**
   * Tests the functionality: put value in cache -> persist to DB.
   */
  @Test
  public void testAlarmPersistence() {
    
    alarmCache.put(originalObject.getId(), originalObject);
    
    //check state is as expected
    assertEquals(AlarmCondition.TERMINATE, originalObject.getState());
    
    //check it is in cache (only compares states so far)   
    AlarmCacheObject cacheObject = (AlarmCacheObject) alarmCache.get(originalObject.getId());
    assertEquals(((Alarm) alarmCache.get(originalObject.getId())).getState(), originalObject.getState());    
    //check it is in database (only values so far...)
    AlarmCacheObject objectInDB = (AlarmCacheObject) alarmMapper.getItem(originalObject.getId());
    assertNotNull(objectInDB);
    assertEquals(objectInDB.getState(), originalObject.getState());
    assertEquals(AlarmCondition.TERMINATE, objectInDB.getState()); //state is TERMINATE in test alarm 1
    
    //now update the cache object to new value
    cacheObject.setState(AlarmCondition.ACTIVE);
    //notify the listeners
    alarmCache.notifyListenersOfUpdate(cacheObject);
    
    //...and check the DB was updated after the buffer has time to fire
    try {
      Thread.sleep(20000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    objectInDB = (AlarmCacheObject) alarmMapper.getItem(originalObject.getId());
    assertNotNull(objectInDB);    
    assertEquals(AlarmCondition.ACTIVE, objectInDB.getState());
    
    //clean up...
    //remove from cache
    alarmCache.remove(originalObject.getId());  
  }

  /**
   * Set the application context. Used for explicit start.
   */
  @Override
  public void setApplicationContext(ApplicationContext arg0) throws BeansException {
    context = arg0;
  }
  
}
