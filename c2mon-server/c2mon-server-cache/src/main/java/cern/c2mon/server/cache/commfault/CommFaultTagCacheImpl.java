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
package cern.c2mon.server.cache.commfault;

import javax.annotation.PostConstruct;

import cern.c2mon.server.cache.config.CacheProperties;
import lombok.extern.slf4j.Slf4j;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.loader.CacheLoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import cern.c2mon.server.cache.ClusterCache;
import cern.c2mon.server.cache.CommFaultTagCache;
import cern.c2mon.server.cache.common.AbstractCache;
import cern.c2mon.server.cache.loading.common.C2monCacheLoader;
import cern.c2mon.server.cache.loading.SimpleCacheLoaderDAO;
import cern.c2mon.server.common.commfault.CommFaultTag;
import cern.c2mon.server.common.config.C2monCacheName;

/**
 * Implementation of CommFaultTag cache.
 *
 * @author Mark Brightwell
 *
 */
@Slf4j
@Service("commFaultTagCache")
public class CommFaultTagCacheImpl extends AbstractCache<Long, CommFaultTag> implements CommFaultTagCache {

  @Autowired
  public CommFaultTagCacheImpl(final ClusterCache clusterCache,
                               @Qualifier("commFaultTagEhcache") final Ehcache ehcache,
                               @Qualifier("commFaultTagEhcacheLoader") final CacheLoader cacheLoader,
                               @Qualifier("commFaultTagCacheLoader") final C2monCacheLoader c2monCacheLoader,
                               @Qualifier("commFaultTagDAO") final SimpleCacheLoaderDAO<CommFaultTag> cacheLoaderDAO,
                               final CacheProperties properties) {
    super(clusterCache, ehcache, cacheLoader, c2monCacheLoader, cacheLoaderDAO, properties);
  }

  @PostConstruct
  public void init() {
    log.info("Initializing the CommFaultTag cache...");
    commonInit();
    log.info("... CommFaultTag cache initialization complete.");
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
