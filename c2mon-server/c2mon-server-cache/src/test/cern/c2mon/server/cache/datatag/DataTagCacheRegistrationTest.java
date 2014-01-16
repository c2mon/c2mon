package cern.c2mon.server.cache.datatag;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.event.RegisteredEventListeners;
import net.sf.ehcache.loader.CacheLoader;

import org.easymock.EasyMock;
import org.easymock.IMocksControl;
import org.junit.Before;

import cern.c2mon.server.cache.CacheSupervisionListener;
import cern.c2mon.server.cache.ClusterCache;
import cern.c2mon.server.cache.DataTagCache;
import cern.c2mon.server.cache.C2monCacheListener;
import cern.c2mon.server.cache.common.C2monCacheLoader;
import cern.c2mon.server.cache.loading.CacheLoaderDAO;
import cern.c2mon.server.cache.supervision.SupervisionAppender;
import cern.c2mon.server.common.datatag.DataTag;
import cern.c2mon.server.common.datatag.DataTagCacheObject;

/**
 * Unit test of registration methods in cache class (use DataTagCache implementation).
 *  
 * @author Mark Brightwell
 *
 */
public class DataTagCacheRegistrationTest {

  /**
   * Class to test.
   */
  private DataTagCache dataTagCache;
  
  /**
   * Mocks
   */
  private IMocksControl mockControl = EasyMock.createControl();
  private ClusterCache clusterCache;
  private Cache cache;
  private CacheLoader cacheLoader;
  private C2monCacheLoader c2monCacheLoader;
  private CacheLoaderDAO cacheLoaderDAO;
  private SupervisionAppender supervisionAppender;
  private RegisteredEventListeners registeredEventListeners;
  
  @Before
  public void init() {
    clusterCache = mockControl.createMock(ClusterCache.class);
    cache = mockControl.createMock(Cache.class);
    cacheLoader = mockControl.createMock(CacheLoader.class);
    c2monCacheLoader = mockControl.createMock(C2monCacheLoader.class);
    cacheLoaderDAO = mockControl.createMock(CacheLoaderDAO.class);
    supervisionAppender = mockControl.createMock(SupervisionAppender.class); 
    registeredEventListeners = new RegisteredEventListeners(cache);
    dataTagCache = new DataTagCacheImpl(clusterCache, cache, cacheLoader, c2monCacheLoader, cacheLoaderDAO);    
  }
    
  /**
   * CacheSupervisionListeners are not notified of standard Tag updates. 
   */
  //@Test - functionality not provided
  public void testRegistrationWithSupervision() {

    cache.setNodeBulkLoadEnabled(false);
    cache.setNodeBulkLoadEnabled(true);
    cache.registerCacheLoader(cacheLoader);
    EasyMock.expect(cache.getCacheEventNotificationService()).andReturn(registeredEventListeners);        
    
    DataTag tag = new DataTagCacheObject(100L);
        
    CacheSupervisionListener<DataTag> listenerWithSup = mockControl.createMock(CacheSupervisionListener.class);
    CacheSupervisionListener<DataTag> listenerWithSup2 = mockControl.createMock(CacheSupervisionListener.class);
    C2monCacheListener<DataTag> listenerStandard = mockControl.createMock(C2monCacheListener.class);
       
    listenerStandard.notifyElementUpdated(tag);
    
    mockControl.replay();
    
    ((DataTagCacheImpl) dataTagCache).init();    
    dataTagCache.registerSynchronousListener(listenerStandard);
    dataTagCache.notifyListenersOfUpdate(tag);
    
    mockControl.verify();
  }
  
  
}
