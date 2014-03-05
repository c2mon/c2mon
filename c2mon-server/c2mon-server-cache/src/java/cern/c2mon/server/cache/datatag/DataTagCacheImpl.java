/******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2005-2010 CERN.
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * 
 * Author: TIM team, tim.support@cern.ch
 *****************************************************************************/
package cern.c2mon.server.cache.datatag;

import javax.annotation.PostConstruct;

import net.sf.ehcache.Ehcache;
import net.sf.ehcache.loader.CacheLoader;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Service;

import cern.c2mon.server.cache.ClusterCache;
import cern.c2mon.server.cache.DataTagCache;
import cern.c2mon.server.cache.common.C2monCacheLoader;
import cern.c2mon.server.cache.loading.SimpleCacheLoaderDAO;
import cern.c2mon.server.cache.tag.AbstractTagCache;
import cern.c2mon.server.common.config.C2monCacheName;
import cern.c2mon.server.common.datatag.DataTag;

/**
 * Implementation of the DataTag cache.
 * 
 * @author Mark Brightwell
 *
 */
@Service("dataTagCache")
@ManagedResource(objectName="cern.c2mon:type=cache,name=dataTagCache")
public class DataTagCacheImpl extends AbstractTagCache<DataTag> implements DataTagCache {
  
  /**
   * Class logger.
   */
  private static final Logger LOGGER = Logger.getLogger(DataTagCacheImpl.class);
  
  @Autowired
  public DataTagCacheImpl(@Qualifier("clusterCache") final ClusterCache clusterCache, 
                          @Qualifier("dataTagEhcache") final Ehcache ehcache,
                          @Qualifier("dataTagEhcacheLoader") final CacheLoader cacheLoader, 
                          @Qualifier("dataTagCacheLoader") final C2monCacheLoader c2monCacheLoader,
                          @Qualifier("dataTagLoaderDAO") final SimpleCacheLoaderDAO<DataTag> cacheLoaderDAO) {
    super(clusterCache, ehcache, cacheLoader, c2monCacheLoader, cacheLoaderDAO);    
  }
  
  /**
   * Run on bean initialization. Sets the Ehcache field to the appropriate cache and calls
   * the preload routine if necessary.
   */
  @PostConstruct
  public void init() {        
    LOGGER.info("Initializing the DataTag cache...");    

    try {
      getCache().setNodeBulkLoadEnabled(true);
    } catch (UnsupportedOperationException ex) {
      LOGGER.warn("setNodeBulkLoadEnabled() method threw an exception when "
          + "loading the cache (UnsupportedOperationException) - this is "
          + "normal behaviour in a single-server mode and can be ignored");
    }
    
    //common initialization (other than preload, which needs synch below)
    commonInit();
    
    try {
      getCache().setNodeBulkLoadEnabled(false);
    } catch (UnsupportedOperationException ex) {
      LOGGER.warn("setNodeBulkLoadEnabled() method threw an exception when "
          + "loading the cache (UnsupportedOperationException) - this is "
          + "normal behaviour in a single-server mode and can be ignored");      
    }
    
    LOGGER.info("... DataTag cache initialization complete.");
  }

  @Override
  protected void doPostDbLoading(DataTag cacheObject) {
    //do nothing
  }

  @Override
  protected C2monCacheName getCacheName() {    
    return C2monCacheName.DATATAG;
  }

  @Override
  protected String getCacheInitializedKey() {
    return cacheInitializedKey;
  }

}
