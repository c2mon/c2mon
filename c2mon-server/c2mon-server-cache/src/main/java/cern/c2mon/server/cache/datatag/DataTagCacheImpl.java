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
import cern.c2mon.server.cache.common.C2monCacheLoader;
import cern.c2mon.server.cache.exception.CacheElementNotFoundException;
import cern.c2mon.server.cache.loading.SimpleCacheLoaderDAO;
import cern.c2mon.server.cache.tag.AbstractTagCache;
import cern.c2mon.server.common.config.C2monCacheName;
import cern.c2mon.server.common.datatag.DataTag;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.loader.CacheLoader;
import net.sf.ehcache.search.Attribute;
import net.sf.ehcache.search.Query;
import net.sf.ehcache.search.Results;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.LinkedList;
import java.util.List;

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
  private static final Logger LOGGER = LoggerFactory.getLogger(DataTagCacheImpl.class);

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
  public List<Long> getDataTagIdsByEquipmentId(Long equipmentId) {
    List<Long> tagIds = new LinkedList<>();
    Results results = null;

    if (equipmentId == null) {
      throw new IllegalArgumentException("Attempting to retrieve a List od DataTag ids from the cache with a NULL " +
          "parameter.");
    }

    try {
      Attribute<Long> cacheEquipmentId = getCache().getSearchAttribute("equipmentId");
      Query query = getCache().createQuery();
      results = query.includeKeys().addCriteria(cacheEquipmentId.eq(equipmentId)).execute();

      if (results == null) {
        throw new CacheElementNotFoundException("Failed to execute query with equipmentId " + equipmentId + " : " +
            "Result is null.");
      }
      results.all().forEach(r -> tagIds.add((Long) r.getKey()));

    } finally {
      if (results != null) {
        // Discard the results when done to free up cache resources.
        results.discard();
      }
    }
    return tagIds;
  }

  @Override
  public List<Long> getDataTagIdsBySubEquipmentId(Long subEquipmentId) {
    List<Long> tagIds = new LinkedList<>();
    Results results = null;

    if (subEquipmentId == null) {
      throw new IllegalArgumentException("Attempting to retrieve a List od DataTag ids from the cache with a NULL " +
          "parameter.");
    }

    try {
      Attribute<Long> cacheEquipmentId = getCache().getSearchAttribute("subEquipmentId");
      results = getCache().createQuery().includeKeys().addCriteria(cacheEquipmentId.eq(subEquipmentId)).execute();

      if (results == null) {
        throw new CacheElementNotFoundException("Failed to execute query with subEquipmentId " + subEquipmentId + " : " +
            "Result is null.");
      }
      results.all().forEach(r -> tagIds.add((Long) r.getKey()));

    } finally {
      if (results != null) {
        // Discard the results when done to free up cache resources.
        results.discard();
      }
    }
    return tagIds;
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
