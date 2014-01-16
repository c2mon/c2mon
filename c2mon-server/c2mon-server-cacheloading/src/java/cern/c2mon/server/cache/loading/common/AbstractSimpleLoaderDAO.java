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
