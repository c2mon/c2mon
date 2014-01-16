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
package cern.c2mon.server.cache.equipment;

import javax.annotation.PostConstruct;

import net.sf.ehcache.Ehcache;
import net.sf.ehcache.loader.CacheLoader;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Service;

import cern.c2mon.server.cache.ClusterCache;
import cern.c2mon.server.cache.EquipmentCache;
import cern.c2mon.server.cache.common.AbstractCache;
import cern.c2mon.server.cache.common.C2monCacheLoader;
import cern.c2mon.server.cache.loading.SimpleCacheLoaderDAO;
import cern.c2mon.server.common.config.C2monCacheName;
import cern.c2mon.server.common.equipment.Equipment;

/**
 * Implementation of the Equipment cache. 
 * 
 * <p>Contains initialization logic.
 * 
 * @author Mark Brightwell
 *
 */
@Service("equipmentCache")
@ManagedResource(objectName="cern.c2mon:type=cache,name=equipmentCache")
public class EquipmentCacheImpl extends AbstractCache<Long, Equipment> implements EquipmentCache {

  /**
   * Static class logger.
   */
  private static final Logger LOGGER = Logger.getLogger(EquipmentCacheImpl.class);

  @Autowired
  public EquipmentCacheImpl(final ClusterCache clusterCache, 
                          @Qualifier("equipmentEhcache") final Ehcache ehcache,
                          @Qualifier("equipmentEhcacheLoader") final CacheLoader cacheLoader, 
                          @Qualifier("equipmentCacheLoader") final C2monCacheLoader c2monCacheLoader,
                          @Qualifier("equipmentDAO") final SimpleCacheLoaderDAO<Equipment> cacheLoaderDAO) {
    super(clusterCache, ehcache, cacheLoader, c2monCacheLoader, cacheLoaderDAO);    
  }

  /**
   * Init method called on bean creation.
   */
  @PostConstruct
  public void init() {
    LOGGER.info("Initializing Equipment cache...");
    
    commonInit();
      
    try {
      getCache().setNodeBulkLoadEnabled(false);
    } catch (UnsupportedOperationException ex) {
      LOGGER.warn("setNodeBulkLoadEnabled() method threw an exception when "
          + "loading the cache (UnsupportedOperationException) - this is "
          + "normal behaviour in a single-server mode and can be ignored");
    }        
   
    try {
      getCache().setNodeCoherent(true);
    } catch (UnsupportedOperationException ex) {
      LOGGER.warn("setNodeCoherent() method threw an exception when "
          + "loading the cache (UnsupportedOperationException) - this is "
          + "normal behaviour in a single-server mode and can be ignored");      
    }
        
    LOGGER.info("Equipment cache initialization complete.");
  }

  @Override
  protected void doPostDbLoading(Equipment cacheObject) {
    //do nothing
  }

  @Override
  protected C2monCacheName getCacheName() {
    return C2monCacheName.EQUIPMENT;
  }
  
  @Override
  protected String getCacheInitializedKey() {
    return cacheInitializedKey;
  }

}
