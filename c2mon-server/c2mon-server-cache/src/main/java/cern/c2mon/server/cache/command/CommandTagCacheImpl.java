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

import cern.c2mon.server.cache.ClusterCache;
import cern.c2mon.server.cache.CommandTagCache;
import cern.c2mon.server.cache.command.query.CommandTagQuery;
import cern.c2mon.server.cache.common.AbstractCache;
import cern.c2mon.server.cache.config.CacheProperties;
import cern.c2mon.server.cache.loading.SimpleCacheLoaderDAO;
import cern.c2mon.server.cache.loading.common.C2monCacheLoader;
import cern.c2mon.server.common.config.C2monCacheName;
import cern.c2mon.server.ehcache.Ehcache;
import cern.c2mon.server.ehcache.loader.CacheLoader;
import cern.c2mon.shared.common.command.CommandTag;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * Implementation of the CommandTag cache.
 *
 * @author Mark Brightwell
 *
 */
@Slf4j
@Service
public class CommandTagCacheImpl extends AbstractCache<Long, CommandTag> implements CommandTagCache {

  private CommandTagQuery commandTagQuery;

  @Autowired
  public CommandTagCacheImpl(final ClusterCache clusterCache,
                             @Qualifier("commandTagEhcache") final Ehcache ehcache,
                             @Qualifier("commandTagEhcacheLoader") final CacheLoader cacheLoader,
                             @Qualifier("commandTagCacheLoader") final C2monCacheLoader c2monCacheLoader,
                             @Qualifier("commandTagDAO") final SimpleCacheLoaderDAO<CommandTag> cacheLoaderDAO,
                             final CacheProperties properties,
                             @Qualifier("commandTagQuery") final CommandTagQuery commandTagQuery) {
    super(clusterCache, ehcache, cacheLoader, c2monCacheLoader, cacheLoaderDAO, properties);
    this.commandTagQuery = commandTagQuery;
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

    if (name == null || name.equalsIgnoreCase("")) {
      throw new IllegalArgumentException("Attempting to retrieve a CommandTag from the cache with a NULL or empty name parameter.");
    }

    return commandTagQuery.findCommandTagIdByName(name);
  }

  @Override
  protected String getCacheInitializedKey() {
    return cacheInitializedKey;
  }


}
