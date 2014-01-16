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
package cern.c2mon.server.cache.alarm;

import javax.annotation.PostConstruct;

import net.sf.ehcache.Ehcache;
import net.sf.ehcache.loader.CacheLoader;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Service;

import cern.c2mon.server.cache.AlarmCache;
import cern.c2mon.server.cache.BufferedTimCacheListener;
import cern.c2mon.server.cache.ClusterCache;
import cern.c2mon.server.cache.common.AbstractCache;
import cern.c2mon.server.cache.common.C2monCacheLoader;
import cern.c2mon.server.cache.loading.AlarmLoaderDAO;
import cern.c2mon.server.common.alarm.Alarm;
import cern.c2mon.server.common.component.Lifecycle;
import cern.c2mon.server.common.config.C2monCacheName;

/**
 * Implementation of the TIM Alarm cache.
 * 
 * @author Mark Brightwell
 *
 */
@Service("alarmCache")
@ManagedResource(objectName="cern.c2mon:type=cache,name=alarmCache")
public class AlarmCacheImpl extends AbstractCache<Long, Alarm> implements AlarmCache {

  /**
   * Class logger.
   */
  private static final Logger LOGGER = Logger.getLogger(AlarmCacheImpl.class);

  @Autowired
  public AlarmCacheImpl(@Qualifier("clusterCache") final ClusterCache clusterCache, 
                        @Qualifier("alarmEhcache") final Ehcache ehcache,
                        @Qualifier("alarmEhcacheLoader") final CacheLoader cacheLoader, 
                        @Qualifier("alarmCacheLoader") final C2monCacheLoader c2monCacheLoader,
                        @Qualifier("alarmLoaderDAO") final AlarmLoaderDAO cacheLoaderDAO) {
    super(clusterCache, ehcache, cacheLoader, c2monCacheLoader, cacheLoaderDAO);    
  } 
  
  /**
   * Runs on start up.
   */
  @PostConstruct
  public void init() {    
    LOGGER.info("Initializing Alarm cache...");
    
    //common initialization (other than preload, which needs synch below)
    commonInit();
    
    LOGGER.info("... Alarm cache initialization complete.");
  }

  @Override
  protected void doPostDbLoading(Alarm cacheObject) {
    //do nothing
  }

  @Override
  protected C2monCacheName getCacheName() {    
    return C2monCacheName.ALARM;
  }

  @Override
  protected String getCacheInitializedKey() {
    return cacheInitializedKey;
  }

}
