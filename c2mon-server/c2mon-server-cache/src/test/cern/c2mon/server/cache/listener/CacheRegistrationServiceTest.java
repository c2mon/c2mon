package cern.c2mon.server.cache.listener;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.sql.Timestamp;

import javax.annotation.Resource;

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

import cern.c2mon.server.cache.C2monCacheListener;
import cern.c2mon.server.cache.CacheRegistrationService;
import cern.c2mon.server.cache.ControlTagCache;
import cern.c2mon.server.cache.ControlTagFacade;
import cern.c2mon.server.cache.DataTagCache;
import cern.c2mon.server.cache.DataTagFacade;
import cern.c2mon.server.cache.dbaccess.test.TestDataHelper;
import cern.c2mon.server.common.component.Lifecycle;
import cern.c2mon.server.common.control.ControlTag;
import cern.c2mon.server.common.datatag.DataTag;
import cern.c2mon.server.common.tag.Tag;

/**
 * Integration test that checks that registered listeners
 * are indeed notified of the cache updates (the cache module
 * is started without mocks).
 * 
 * <p>Does the check both for single and multi-threaded
 * registrations.
 * 
 * <p>Currently only check for Tag caches (the only ones updated
 * in the server so far).
 * 
 * @author Mark Brightwell
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:cern/c2mon/server/cache/config/server-cache-registration-test.xml" })
@DirtiesContext
public class CacheRegistrationServiceTest implements ApplicationContextAware {
  
  /**
   * Spring context, that needs starting manually.
   */
  private ApplicationContext context;
  
  @Resource
  private CacheRegistrationService cacheRegistrationService;
  
  @Resource
  private DataTagCache dataTagCache;
  
  @Resource
  private ControlTagCache controlTagCache;
  
  @Resource
  private ControlTagFacade controlTagFacade;
  
  @Resource
  private DataTagFacade dataTagFacade;
  
  /**
   * For DB insertion of tag used.
   */
  @Autowired
  private TestDataHelper testDataHelper;
  
  private DataTag dataTag;
  
  private ControlTag controlTag;
  
  @Before
  public void insertTestTag() {
    testDataHelper.createTestData();
    testDataHelper.insertTestDataIntoDB();
    dataTag = testDataHelper.getDataTag();
    controlTag = testDataHelper.getProcessAliveTag();
    
    //for this test put in cache
    dataTagCache.put(dataTag.getId(), dataTag);
    controlTagCache.put(controlTag.getId(), controlTag);    
  }
  
  @Before
  public void startContext() {
    ((AbstractApplicationContext) context).start();
  }
  
  @After
  public void deleteTestTag() {
    testDataHelper.removeTestData();
    controlTagCache.remove(testDataHelper.getProcessAliveTag().getId());
  }
  
  /**
   * Tests that an update in the Datatag, Controltag or RuleTag cache (TODO ruletag cache)
   * is picked up by a registered listener (asynchronous listener using ThreadHandler class).
   * This tag is present in the cache for this test.
   * TODO not working correctly at the moment: assertion fails but says tautology is the reason!
   * @throws SecurityException
   * @throws NoSuchMethodException
   * @throws CloneNotSupportedException
   */
  //@Test //remove for now - using test below instead
  public void testRegisterToAllTags() throws SecurityException, NoSuchMethodException, CloneNotSupportedException {
        
    //check equals() method is working as expected on the datatag
    //(since a cloned datatag is passed to the listener module)
    assertTrue(dataTag.clone().equals(dataTag));
    
    //set up mock cache listener    
    //change this on refactoring with generics... check works ok (abstract to interface)
    C2monCacheListener<Tag> mockCacheListener = createMock(C2monCacheListener.class, C2monCacheListener.class.getMethod("notifyElementUpdated", new Class<?>[] {Object.class}));        
    Lifecycle listenerContainer = cacheRegistrationService.registerToAllTags(mockCacheListener, 1);
    listenerContainer.start(); //does nothing
    assertNotNull(listenerContainer);
           
    //record expected call on the listener when an update is made
    mockCacheListener.notifyElementUpdated(dataTag); //equality of DataTagCacheObject is defined as equality of id's, cloned values are equal...
    
    //check behaviour is as expected
    replay(mockCacheListener);
    dataTagFacade.updateAndValidate(dataTag, Boolean.FALSE, "listener test value", new Timestamp(System.currentTimeMillis()));
    dataTagCache.notifyListenersOfUpdate(dataTag);
    listenerContainer.stop();
    verify(mockCacheListener);
    
  }
  
  /**
   * New test of listener registration without using mocks (above test fails for some reason).
   * 
   * <p>Registers on single thread.
   * 
   * Removed as fails if run together with next test (reason unknown...).
   * 
   * @throws InterruptedException if wait is interrupted
   */
  @Test
  @DirtiesContext
  public void testRegisterToAllTagsOutput() throws InterruptedException {
    commonRegisterTest(1);
  }
  
  /**
   * Same as testRegisterToAllTagsOutput() but on multiple threads (3).
   * 
   * @throws InterruptedException
   */
  @Test
  @DirtiesContext
  public void testRegisterOnMultipleThreads() throws InterruptedException {
    commonRegisterTest(3);
  }
  
  private void commonRegisterTest(int threads) throws InterruptedException {
  //set up listener
    OutputTestListener testListener = new OutputTestListener();
    Lifecycle listenerContainer = cacheRegistrationService.registerToAllTags(testListener, threads);
    listenerContainer.start(); //does nothing
    assertNotNull(listenerContainer);
    
    //update tag in cache and notify listeners
    dataTagFacade.updateAndValidate(dataTag, Boolean.FALSE, "listener test value", new Timestamp(System.currentTimeMillis()));
    
    //sleep to allow separate thread to process
   
    Thread.sleep(200);
   
    //check listener received the value
    assertNotNull(testListener.receivedValue);
    assertNotNull(testListener.receivedId);
    assertEquals(dataTag.getValue(), testListener.receivedValue);
    assertEquals(dataTag.getValue(), Boolean.FALSE); //check is indeed false
    assertEquals(dataTag.getId(), testListener.receivedId);
    
    //do the same for control tags
    dataTagFacade.updateAndValidate(controlTag, Long.valueOf(100), "listener test control tag value", new Timestamp(System.currentTimeMillis()));
        
    //sleep to allow separate thread to process
    
    Thread.sleep(100);    
    
    //check listener notified
    assertEquals(controlTag.getValue(), testListener.receivedValue);
    assertEquals(controlTag.getValue(), Long.valueOf(100)); //check is indeed 100
    assertEquals(controlTag.getId(), testListener.receivedId);
    listenerContainer.stop();
    
  }
  
  public class OutputTestListener implements C2monCacheListener<Tag> {

    public Object receivedId;
    
    public Object receivedValue;
    
    @Override
    public void notifyElementUpdated(Tag tag) {
      receivedValue = tag.getValue();
      receivedId = tag.getId();
    }

    @Override
    public void confirmStatus(Tag cacheable) {
      // TODO Auto-generated method stub
      
    }
    
  }
  
  /**
   * Set the application context (used for explicit start).
   */
  @Override
  public void setApplicationContext(ApplicationContext arg0) throws BeansException {
    context = arg0;
  }
  
}
