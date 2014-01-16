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
package cern.c2mon.server.cache.process;

import javax.annotation.PostConstruct;

import net.sf.ehcache.Ehcache;
import net.sf.ehcache.loader.CacheLoader;
import net.sf.ehcache.search.Attribute;
import net.sf.ehcache.search.Query;
import net.sf.ehcache.search.Results;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Service;

import cern.c2mon.server.cache.ClusterCache;
import cern.c2mon.server.cache.ProcessCache;
import cern.c2mon.server.cache.common.AbstractCache;
import cern.c2mon.server.cache.common.C2monCacheLoader;
import cern.c2mon.server.cache.exception.CacheElementNotFoundException;
import cern.c2mon.server.cache.loading.SimpleCacheLoaderDAO;
import cern.c2mon.server.common.config.C2monCacheName;
import cern.c2mon.server.common.process.Process;

/**
 * Implementation of the Process cache.
 * 
 * @author Mark Brightwell
 */
@Service("processCache")
@ManagedResource(objectName="cern.c2mon:type=cache,name=processTagCache")
public class ProcessCacheImpl extends AbstractCache<Long, Process> implements ProcessCache {
   
  /**
   * Private class logger.
   */
  private static final Logger LOGGER = Logger.getLogger(ProcessCacheImpl.class);

  
  @Autowired
  public ProcessCacheImpl(final ClusterCache clusterCache, 
                          @Qualifier("processEhcache") final Ehcache ehcache,
                          @Qualifier("processEhcacheLoader") final CacheLoader cacheLoader, 
                          @Qualifier("processCacheLoader") final C2monCacheLoader c2monCacheLoader,
                          @Qualifier("processDAO") final SimpleCacheLoaderDAO<Process> cacheLoaderDAO) {
    super(clusterCache, ehcache, cacheLoader, c2monCacheLoader, cacheLoaderDAO);
  }
  
  /**
   * Run on bean initialization. Sets the Ehcache field to the appropriate cache and calls
   * the preload routine if necessary.
   */
  @PostConstruct
  public void init() {    
    LOGGER.info("Initializing Process cache...");
      
    commonInit();
    
    LOGGER.info("... Process cache initialization complete.");    
  }


  @Override
  public Process getCopy(final String name) {
    Long key = getProcessId(name);
    return getCopy(key);    
  }

  @Override
  protected void doPostDbLoading(final Process cacheObject) {
    //do nothing 
  }

  @Override
  protected C2monCacheName getCacheName() {   
    return C2monCacheName.PROCESS;
  }
  
  @Override
  protected String getCacheInitializedKey() {
    return cacheInitializedKey;
  }

  @Override
  public Long getProcessId(final String name) {
    Long processKey = null;
    Results results = null;
    
    if (name == null || name.equalsIgnoreCase("")) {
      throw new IllegalArgumentException("Attempting to retrieve a Process from the cache with a NULL or empty name parameter.");
    }
    
    try {
      Attribute<String> processName = getCache().getSearchAttribute("processName");
      // By limiting the query result list to 1 it is up to the administrator to make
      // sure that the process name is unique. Otherwise this will result in an unpredictable behaviour.
      Query query = getCache().createQuery();
      results = query.includeKeys().addCriteria(processName.eq(name)).maxResults(1).execute();
      
      // Find the number of results -- the number of hits.
      int size = results.size();
      if (size == 0) {
        throw new CacheElementNotFoundException("Failed to find a process with name " + name + " in the cache.");
      }
      
      processKey = (Long) results.all().get(0).getKey();
    }
    finally {
      if (results != null) {
        // Discard the results when done to free up cache resources.
        results.discard();
      }
    }

    return processKey;
  }
  
}
