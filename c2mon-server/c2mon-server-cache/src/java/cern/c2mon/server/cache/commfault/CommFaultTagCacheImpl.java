package cern.c2mon.server.cache.commfault;

import javax.annotation.PostConstruct;

import net.sf.ehcache.Ehcache;
import net.sf.ehcache.loader.CacheLoader;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import cern.c2mon.server.cache.ClusterCache;
import cern.c2mon.server.cache.CommFaultTagCache;
import cern.c2mon.server.cache.common.AbstractCache;
import cern.c2mon.server.cache.common.C2monCacheLoader;
import cern.c2mon.server.cache.loading.SimpleCacheLoaderDAO;
import cern.c2mon.server.common.commfault.CommFaultTag;
import cern.c2mon.server.common.config.C2monCacheName;

/**
 * Implementation of CommFaultTag cache.
 * 
 * @author Mark Brightwell
 *
 */
@Service("commFaultTagCache")
public class CommFaultTagCacheImpl extends AbstractCache<Long, CommFaultTag> implements CommFaultTagCache {

  /**
   * Private class logger.
   */
  private static final Logger LOGGER = Logger.getLogger(CommFaultTagCacheImpl.class);

  @Autowired
  public CommFaultTagCacheImpl(final ClusterCache clusterCache, 
                          @Qualifier("commFaultTagEhcache") final Ehcache ehcache,
                          @Qualifier("commFaultTagEhcacheLoader") final CacheLoader cacheLoader, 
                          @Qualifier("commFaultTagCacheLoader") final C2monCacheLoader c2monCacheLoader,
                          @Qualifier("commFaultTagDAO") final SimpleCacheLoaderDAO<CommFaultTag> cacheLoaderDAO) {
    super(clusterCache, ehcache, cacheLoader, c2monCacheLoader, cacheLoaderDAO);    
  }
  
  @PostConstruct
  public void init() {
    LOGGER.info("Initializing the CommFaultTag cache...");
    commonInit();
    LOGGER.info("... CommFaultTag cache initialization complete."); 
  }

  @Override
  protected void doPostDbLoading(CommFaultTag cacheObject) {
    //do nothing
  }

  @Override
  protected C2monCacheName getCacheName() {    
    return C2monCacheName.COMMFAULT;
  }
  
  @Override
  protected String getCacheInitializedKey() {
    return cacheInitializedKey;
  }
  
}
