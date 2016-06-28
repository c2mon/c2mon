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
package cern.c2mon.server.cache.loading.common;

import cern.c2mon.server.cache.dbaccess.SimpleLoaderMapper;
import cern.c2mon.server.cache.loading.SimpleCacheLoaderDAO;
import cern.c2mon.shared.common.Cacheable;

/**
 * Abstract implementation of the SimpleCacheLoaderDAO.
 * 
 * @param <T> the cache object type
 * 
 * @author Mark Brightwell
 *
 */
public abstract class AbstractSimpleLoaderDAO<T extends Cacheable> implements SimpleCacheLoaderDAO<T> {

  /**
   * Required associated simple mapper.
   */
  private SimpleLoaderMapper<T> simpleLoaderMapper;
  
  /**
   * Constructor.
   * @param simpleLoaderMapper the mapper class
   */
  public AbstractSimpleLoaderDAO(final SimpleLoaderMapper<T> simpleLoaderMapper) {
    super();
    this.simpleLoaderMapper = simpleLoaderMapper;
  }
  
  /**
   * Finish creating an object freshly loaded from the DB.
   * 
   * <p>Particularly useful for inserting Spring property values
   * into the cache object, which may not be accessible
   * from the cache object itself.
   * 
   * <p>Is never called with null argument.
   * 
   * @param item the loaded item 
   * @return the cache item with final changes
   */
  protected abstract T doPostDbLoading(T item);

  /**
   * Method returning a DataTagCacheObject given its ID in the cache. Returns null
   * if none found.
   * 
   * @param id the DataTag id
   * @return the cache object
   */
  @Override
  public T getItem(final Object id) {
    T item = simpleLoaderMapper.getItem(id);
    if (item != null){
      return doPostDbLoading(item);
    } else
      return null;    
  }

  @Override
  public boolean isInDb(final Long id) {
    return simpleLoaderMapper.isInDb(id); 
  }
  
}
