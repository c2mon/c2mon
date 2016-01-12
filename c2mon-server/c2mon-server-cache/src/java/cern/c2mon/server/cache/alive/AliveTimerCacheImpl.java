/******************************************************************************
 * Copyright (C) 2010-2016 CERN. All rights not expressly granted are reserved.
 * 
 * This file is part of the CERN Control and Monitoring Platform 'C2MON'.
 * C2MON is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the license.
 * 
 * C2MON is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
 * more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with C2MON. If not, see <http://www.gnu.org/licenses/>.
 *****************************************************************************/
package cern.c2mon.server.cache.alive;

import javax.annotation.PostConstruct;

import net.sf.ehcache.Ehcache;
import net.sf.ehcache.loader.CacheLoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final Logger LOGGER = LoggerFactory.getLogger(AliveTimerCacheImpl.class);


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
