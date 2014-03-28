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
package cern.c2mon.server.cachepersistence.impl;

import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.transaction.annotation.Transactional;

import cern.c2mon.server.cache.C2monCache;
import cern.c2mon.server.cache.dbaccess.PersistenceMapper;
import cern.c2mon.server.cache.exception.CacheElementNotFoundException;
import cern.c2mon.server.cachepersistence.CachePersistenceDAO;
import cern.c2mon.server.common.tag.Tag;
import cern.c2mon.shared.common.Cacheable;

/**
 * Class that can be used for caches that wish to persist updates
 * to the database. The cache object needs to have a corresponding
 * {@link PersistenceMapper}.
 * 
 * @author Mark Brightwell
 *
 * @param <T> the type of the cache object
 */
public class CachePersistenceDAOImpl<T extends Cacheable> implements CachePersistenceDAO<T> {

  /**
   * Private class logger.
   */
  private static final Logger LOGGER = Logger.getLogger(CachePersistenceDAOImpl.class);
  
  /**
   * Mapper for persisting cache updates. Needs setting in
   * the constructor.
   */
  private PersistenceMapper<T> persistenceMapper;
  
  /**
   * Reference to the cache where the cache objects can be retrieved
   * (one of the only references to the cache module from the cache
   * persistence module). Needs setting in constructor.
   */
  private C2monCache<Long, T> cache;
  
  /**
   * Constructor required cache and the persistence bean for this cache.
   * 
   * @param persistenceMapper the mapper bean for this cache
   * @param cache the cache that is being persisted
   */
  public CachePersistenceDAOImpl(final PersistenceMapper<T> persistenceMapper, final C2monCache<Long, T> cache) {
    super();
    this.persistenceMapper = persistenceMapper;
    this.cache = cache;
  }

  /**
   * Persists a single cacheable
   * setting).
   */
  @Transactional("cacheTransactionManager")
  @Override
  public void updateCacheable(final T cacheable) {
    persistenceMapper.updateCacheable(cacheable);
  }
  
  /**
   * Used to persist a batch of cache objects in a single transaction.
   * An object that is not found in the cache will not be persisted and skipped.
   * @param keyList keys of the elements that need persisting
   */
  @Transactional(value = "cacheTransactionManager")
  @Override
  public void persistBatch(final List<Long> keyList) {
    T cacheObject;
    for (Long key : keyList) {
      try {
        cacheObject = cache.getCopy(key);         
        //do not persist unconfigured tags TODO could remove as unconfigured not used
        if (cacheObject != null && (!(cacheObject instanceof Tag) || !((Tag) cacheObject).isInUnconfigured())) {                        
          persistenceMapper.updateCacheable(cacheObject);
        }
      } catch (CacheElementNotFoundException ex) {
        LOGGER.warn("Cache element with id " + key + " could not be persisted as not found in cache (may have been "
        		+ "removed in the meantime by a re-configuration). Cache is " + cache.getClass().getSimpleName(), ex);
      }
    }
  }
  
   
}
