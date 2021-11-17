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
package cern.c2mon.server.cache.command;

import javax.annotation.PostConstruct;

import cern.c2mon.server.cache.config.CacheProperties;
import lombok.extern.slf4j.Slf4j;
import cern.c2mon.server.ehcache.Ehcache;
import cern.c2mon.server.ehcache.loader.CacheLoader;

import cern.c2mon.server.ehcache.search.Attribute;
import cern.c2mon.server.ehcache.search.Query;
import cern.c2mon.server.ehcache.search.Results;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import cern.c2mon.server.cache.ClusterCache;
import cern.c2mon.server.cache.CommandTagCache;
import cern.c2mon.server.cache.common.AbstractCache;
import cern.c2mon.server.cache.loading.common.C2monCacheLoader;
import cern.c2mon.server.cache.loading.SimpleCacheLoaderDAO;
import cern.c2mon.server.common.config.C2monCacheName;
import cern.c2mon.shared.common.command.CommandTag;

/**
 * Implementation of the CommandTag cache.
 *
 * @author Mark Brightwell
 *
 */
@Slf4j
@Service
public class CommandTagCacheImpl extends AbstractCache<Long, CommandTag> implements CommandTagCache {

  @Autowired
  public CommandTagCacheImpl(final ClusterCache clusterCache,
                             @Qualifier("commandTagEhcache") final Ehcache ehcache,
                             @Qualifier("commandTagEhcacheLoader") final CacheLoader cacheLoader,
                             @Qualifier("commandTagCacheLoader") final C2monCacheLoader c2monCacheLoader,
                             @Qualifier("commandTagDAO") final SimpleCacheLoaderDAO<CommandTag> cacheLoaderDAO,
                             final CacheProperties properties) {
    super(clusterCache, ehcache, cacheLoader, c2monCacheLoader, cacheLoaderDAO, properties);
  }

  @PostConstruct
  public void init() {
    log.debug("Initializing the CommandTag cache...");
    commonInit();
    log.info("CommandTag cache initialization complete");
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
  public Long getCommandTagId(final String name) {
    Long commandTagKey = null;
    Results results = null;

    if (name == null || name.equalsIgnoreCase("")) {
      throw new IllegalArgumentException("Attempting to retrieve a CommandTag from the cache with a NULL or empty name parameter.");
    }

    try {
      Attribute<String> commandTagName = getCache().getSearchAttribute("commandTagName");
      Query query = getCache().createQuery();
      results = query.includeKeys().addCriteria(commandTagName.eq(name)).maxResults(1).execute();

      // Find the number of results -- the number of hits.
      int size = results.size();
      if (size == 0) {
        log.info("Failed to find a command tag with name " + name + " in the cache.");
      }

      commandTagKey = results.all().size() > 0 ? (Long) results.all().get(0).getKey() : null;
    }
    finally {
      if (results != null) {
        // Discard the results when done to free up cache resources.
        results.discard();
      }
    }

    return commandTagKey;
  }

  @Override
  protected String getCacheInitializedKey() {
    return cacheInitializedKey;
  }


}
