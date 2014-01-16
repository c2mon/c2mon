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
package cern.c2mon.server.cache.command;

import javax.annotation.PostConstruct;

import net.sf.ehcache.Ehcache;
import net.sf.ehcache.loader.CacheLoader;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import cern.c2mon.server.cache.ClusterCache;
import cern.c2mon.server.cache.CommandTagCache;
import cern.c2mon.server.cache.common.AbstractCache;
import cern.c2mon.server.cache.common.C2monCacheLoader;
import cern.c2mon.server.cache.loading.SimpleCacheLoaderDAO;
import cern.c2mon.server.common.config.C2monCacheName;
import cern.c2mon.shared.daq.command.CommandTag;

/**
 * Implementation of the CommandTag cache.
 * 
 * @author Mark Brightwell
 *
 */
@Service
public class CommandTagCacheImpl extends AbstractCache<Long, CommandTag> implements CommandTagCache {

  /**
   * Private class logger.
   */
  private static final Logger LOGGER = Logger.getLogger(CommandTagCacheImpl.class); 
 
  @Autowired
  public CommandTagCacheImpl(final ClusterCache clusterCache, 
                          @Qualifier("commandTagEhcache") final Ehcache ehcache,
                          @Qualifier("commandTagEhcacheLoader") final CacheLoader cacheLoader, 
                          @Qualifier("commandTagCacheLoader") final C2monCacheLoader c2monCacheLoader,
                          @Qualifier("commandTagDAO") final SimpleCacheLoaderDAO<CommandTag> cacheLoaderDAO) {
    super(clusterCache, ehcache, cacheLoader, c2monCacheLoader, cacheLoaderDAO);    
  } 
  
  @PostConstruct
  public void init() {        
    LOGGER.info("Initializing the CommandTag cache...");
    commonInit();        
    LOGGER.info("... CommandTag cache initialization complete.");
  }

  @Override
  protected void doPostDbLoading(CommandTag cacheObject) {
    //do nothing
  }

  @Override
  protected C2monCacheName getCacheName() {
    return C2monCacheName.COMMAND;
  }
  
  @Override
  protected String getCacheInitializedKey() {
    return cacheInitializedKey;
  }


}
