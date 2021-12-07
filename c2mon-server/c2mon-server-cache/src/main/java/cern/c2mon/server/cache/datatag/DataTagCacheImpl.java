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
package cern.c2mon.server.cache.datatag;

import cern.c2mon.server.cache.ClusterCache;
import cern.c2mon.server.cache.DataTagCache;
import cern.c2mon.server.cache.config.CacheProperties;
import cern.c2mon.server.cache.datatag.query.DataTagQuery;
import cern.c2mon.server.cache.loading.SimpleCacheLoaderDAO;
import cern.c2mon.server.cache.loading.common.C2monCacheLoader;
import cern.c2mon.server.cache.tag.AbstractTagCache;
import cern.c2mon.server.cache.tag.query.TagQuery;
import cern.c2mon.server.common.config.C2monCacheName;
import cern.c2mon.server.common.datatag.DataTag;
import cern.c2mon.server.ehcache.Ehcache;
import cern.c2mon.server.ehcache.loader.CacheLoader;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.PostConstruct;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Service;

/**
 * Implementation of the DataTag cache.
 *
 * @author Mark Brightwell
 *
 */
@Slf4j
@Service("dataTagCache")
@ManagedResource(objectName="cern.c2mon:type=cache,name=dataTagCache")
public class DataTagCacheImpl extends AbstractTagCache<DataTag> implements DataTagCache {

  private DataTagQuery dataTagQuery;

  @Autowired
  public DataTagCacheImpl(@Qualifier("clusterCache") final ClusterCache clusterCache,
                          @Qualifier("dataTagEhcache") final Ehcache<Long, DataTag> ehcache,
                          @Qualifier("dataTagEhcacheLoader") final CacheLoader cacheLoader,
                          @Qualifier("dataTagCacheLoader") final C2monCacheLoader c2monCacheLoader,
                          @Qualifier("dataTagLoaderDAO") final SimpleCacheLoaderDAO<DataTag> cacheLoaderDAO,
                          final CacheProperties properties,
                          @Qualifier("dataTagQuery") final DataTagQuery dataTagQuery,
                          @Qualifier("abstractDataTagQuery") final TagQuery<DataTag> tagQuery) {
    super(clusterCache, ehcache, cacheLoader, c2monCacheLoader, cacheLoaderDAO, properties, tagQuery);
    this.dataTagQuery = dataTagQuery;
  }

  /**
   * Run on bean initialization. Sets the Ehcache field to the appropriate cache and calls
   * the preload routine if necessary.
   */
  @PostConstruct
  public void init() {
    log.debug("Initializing the DataTag cache...");

    //common initialization (other than preload, which needs synch below)
    commonInit();

    log.info("DataTag cache initialization complete");
  }

  @Override
  public List<Long> getDataTagIdsByEquipmentId(Long equipmentId) {
    return dataTagQuery.findDataTagIdsByEquipmentId(equipmentId);
  }

  @Override
  public List<Long> getDataTagIdsBySubEquipmentId(Long subEquipmentId) {
    return dataTagQuery.findDataTagIdsBySubEquipmentId(subEquipmentId);
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
