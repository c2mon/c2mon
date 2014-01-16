package cern.c2mon.server.cache.listener;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import org.junit.Before;
import org.junit.Test;

import cern.c2mon.server.cache.C2monCacheListener;
import cern.c2mon.shared.common.Cacheable;

/**
 * Unit test testing the CacheListener class functionality (asynchronous listener).
 * @author mbrightw
 *
 */
public class CacheListenerTest {

  /**
   * Class to test.
   */
  private CacheListener cacheListener;
  
  /**
   * Mocked listener waiting to be notified of updates.
   */
  private C2monCacheListener mockTimCacheListener;
  
  /**
   * Before each test method.
   */
  @Before
  public void setUp() {
    mockTimCacheListener = createMock(C2monCacheListener.class);
    cacheListener = new CacheListener(mockTimCacheListener);
    cacheListener.start();
  }
  
  /**
   * Tests the mock listener is notified of an update.
   * @throws CloneNotSupportedException should not normally be thrown
   * @throws InterruptedException if exception while waiting (need to wait for second thread to process update)
   */
  @Test
  public void testNotifyElementUpdated() throws CloneNotSupportedException, InterruptedException {
    final Cacheable mockCacheable = createMock(Cacheable.class);
      
    //expect clone to be called: just return the same object for the testing - cloning no longer done for each listener!
//    expect(mockCacheable.clone()).andAnswer(new IAnswer<Cacheable>() {
//      public Cacheable answer() throws Throwable { 
//        return mockCacheable;
//      }
//    });
    //expect the C2monCacheListener to be notified of the update by the ThreadHandler in the
    //  asynchronous listener
    mockTimCacheListener.notifyElementUpdated(mockCacheable);
    
    //replay the scenario, notifying of the update
    replay(mockCacheable);        
    replay(mockTimCacheListener);
    cacheListener.notifyElementUpdated(mockCacheable);
    Thread.sleep(5000); //otherwise verify before processed by other thread
    verify(mockTimCacheListener);
    verify(mockCacheable);
  }
  
}
