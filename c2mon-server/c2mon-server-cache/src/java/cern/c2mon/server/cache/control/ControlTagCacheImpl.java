/******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2005-2011 CERN.
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
package cern.c2mon.server.cache.control;

import javax.annotation.PostConstruct;

import net.sf.ehcache.Ehcache;
import net.sf.ehcache.loader.CacheLoader;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Service;

import cern.c2mon.server.cache.ClusterCache;
import cern.c2mon.server.cache.ControlTagCache;
import cern.c2mon.server.cache.common.C2monCacheLoader;
import cern.c2mon.server.cache.loading.SimpleCacheLoaderDAO;
import cern.c2mon.server.cache.tag.AbstractTagCache;
import cern.c2mon.server.common.config.C2monCacheName;
import cern.c2mon.server.common.control.ControlTag;

/**
 * Implementation of the ControlTagCache. Includes the initialization
 * steps for this cache.
 * 
 * @author Mark Brightwell
 *
 */
@Service("controlTagCache")
@ManagedResource(objectName="cern.c2mon:type=cache,name=controlTagCache")
public class ControlTagCacheImpl extends AbstractTagCache<ControlTag> implements ControlTagCache {
  
  /**
   * Private class logger.
   */
  private static final Logger LOGGER = Logger.getLogger(ControlTagCacheImpl.class);
   
  @Autowired
  public ControlTagCacheImpl(@Qualifier("clusterCache") final ClusterCache clusterCache, 
                             @Qualifier("controlTagEhcache") final Ehcache ehcache,
                             @Qualifier("controlTagEhcacheLoader") final CacheLoader cacheLoader, 
                             @Qualifier("controlTagCacheLoader") final C2monCacheLoader c2monCacheLoader,
                             @Qualifier("controlTagLoaderDAO") final SimpleCacheLoaderDAO<ControlTag> cacheLoaderDAO) {
    super(clusterCache, ehcache, cacheLoader, c2monCacheLoader, cacheLoaderDAO);    
  }
  
  /**
   * Run on bean initialization. Sets the Ehcache field to the appropriate cache and calls
   * the preload routine if necessary.
   */
  @PostConstruct
  public void init() {    
    LOGGER.info("Initializing ControlTag cache...");   
    commonInit();          
    LOGGER.info("... ControlTag cache initialization complete.");
  }

  @Override
  protected void doPostDbLoading(ControlTag cacheObject) {
    //do nothing
  }

  @Override
  protected C2monCacheName getCacheName() {    
    return C2monCacheName.CONTROLTAG;
  }
  
  @Override
  protected String getCacheInitializedKey() {
    return cacheInitializedKey;
  }

}
