package cern.c2mon.server.cache.alive;

import javax.annotation.PostConstruct;

import net.sf.ehcache.Ehcache;
import net.sf.ehcache.loader.CacheLoader;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import cern.c2mon.server.cache.AliveTimerCache;
import cern.c2mon.server.cache.ClusterCache;
import cern.c2mon.server.cache.common.AbstractCache;
import cern.c2mon.server.cache.common.C2monCacheLoader;
import cern.c2mon.server.cache.loading.SimpleCacheLoaderDAO;
import cern.c2mon.server.common.alive.AliveTimer;
import cern.c2mon.server.common.config.C2monCacheName;

/**
 * Implementation of the AliveTimer cache.
 * 
 * @author Mark Brightwell
 */
@Service("aliveTimerCache")
public class AliveTimerCacheImpl extends AbstractCache<Long, AliveTimer> implements AliveTimerCache {

    /**
     * Private class logger.
     */
    private static final Logger LOGGER = Logger.getLogger(AliveTimerCacheImpl.class);


    @Autowired
    public AliveTimerCacheImpl(final ClusterCache clusterCache,
            @Qualifier("aliveTimerEhcache") final Ehcache ehcache,
            @Qualifier("aliveTimerEhcacheLoader") final CacheLoader cacheLoader,
            @Qualifier("aliveTimerCacheLoader") final C2monCacheLoader c2monCacheLoader,
            @Qualifier("aliveTimerDAO") final SimpleCacheLoaderDAO<AliveTimer> cacheLoaderDAO) {
        super(clusterCache, ehcache, cacheLoader, c2monCacheLoader, cacheLoaderDAO);
    }

    /**
     * Init method called on bean creation. Calls the cache loading procedure (loading from DB).
     */
    @PostConstruct
    public void init() {
        LOGGER.debug("Initializing AliveTimer cache...");

        commonInit();

        LOGGER.info("... AliveTimer cache initialization complete.");
    }

    @Override
    protected void doPostDbLoading(AliveTimer cacheObject) {
        // do nothing
    }

    @Override
    protected C2monCacheName getCacheName() {
        return C2monCacheName.ALIVETIMER;
    }
    
    @Override
    protected String getCacheInitializedKey() {
      return cacheInitializedKey;
    }

}
