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
package cern.c2mon.server.cachepersistence.impl;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.api.exception.CacheElementNotFoundException;
import cern.c2mon.server.cache.dbaccess.PersistenceMapper;
import cern.c2mon.server.cachepersistence.CachePersistenceDAO;
import cern.c2mon.server.common.tag.Tag;
import cern.c2mon.shared.common.Cacheable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Class that can be used for caches that wish to persist updates
 * to the database. The cache object needs to have a corresponding
 * {@link PersistenceMapper}.
 *
 * Type safety has been broken because ControlTags are written as datatags in the db
 *
 * @author Mark Brightwell
 *
 * @param <T> the type of the cache object
 */
@Slf4j
public class CachePersistenceDAOImpl<T extends Cacheable> implements CachePersistenceDAO<T> {

  /**
   * Mapper for persisting cache updates. Needs setting in
   * the constructor.
   *
   * Type safety has been broken because ControlTags are written as datatags in the db
   */
  private PersistenceMapper persistenceMapper;

  /**
   * Reference to the cache where the cache objects can be retrieved
   * (one of the only references to the cache module from the cache
   * persistence module). Needs setting in constructor.
   */
  private C2monCache<T> cache;

  /**
   * Constructor required cache and the persistence bean for this cache.
   *
   * Type safety has been broken because ControlTags are written as datatags in the db
   *
   * @param persistenceMapper the mapper bean for this cache
   * @param cache the cache that is being persisted
   */
  public CachePersistenceDAOImpl(final PersistenceMapper persistenceMapper, final C2monCache<T> cache) {
    super();
    this.persistenceMapper = persistenceMapper;
    this.cache = cache;
  }

  /**
   * Used to persist a batch of cache objects in a single transaction.
   * An object that is not found in the cache will not be persisted and skipped.
   * @param keyList keys of the elements that need persisting
   */
  @Transactional(value = "databaseTransactionManager")
  @Override
  public void persistBatch(final List<Long> keyList) {
    T cacheObject;
    for (Long key : keyList) {
      try {
        cacheObject = cache.get(key);
        if (cacheObject != null && (!isUnconfiguredTag(cacheObject))) {
          persistenceMapper.updateCacheable(adaptCacheObject(cacheObject));
        }
      } catch (CacheElementNotFoundException ex) {
        log.warn("Cache element with id {} could not be persisted as not found in cache " +
            "(may have been removed in the meantime by a re-configuration). Cache is {}", key, cache.getClass().getSimpleName(), ex);
      }
    }
  }

  /**
   * Avoids persisting unconfigured tags
   *
   * Potentially deprecated? There was a note that unconfigured is unused
   */
  private boolean isUnconfiguredTag(T cacheObject) {
    return (cacheObject instanceof Tag) && ((Tag) cacheObject).isInUnconfigured();
  }

  /**
   * Allows overriding the cache object before putting in the db
   *
   * Used mainly for control tags, which need to be registered in the datatag tables
   */
  protected Cacheable adaptCacheObject(T cacheObject) {
    return cacheObject;
  }


}
