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

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import cern.c2mon.server.cache.dbaccess.LoaderMapper;
import cern.c2mon.server.cache.loading.CacheLoaderDAO;
import cern.c2mon.shared.common.Cacheable;

/**
 * Abstract class providing common cache loading functionality. Since all TIM
 * caches are loaded from the database, all caches are provided with such a 
 * DAO object.
 * 
 * @author Mark Brightwell
 * @param <T> type of cache object
 *
 */
public abstract class AbstractDefaultLoaderDAO<T extends Cacheable> extends AbstractSimpleLoaderDAO<T> implements CacheLoaderDAO<T> {

  private static Logger LOGGER = Logger.getLogger(AbstractDefaultLoaderDAO.class);
  
  /**
   * The initial size of the Map used to store the cache object loaded from the DB.
   */
  private int initialBufferSize;
  
  /**
   * Loader mapper for single threaded loading.
   */
  private LoaderMapper<T> loaderMapper;
  
  /**
   * Constructor.
   * @param initialBufferSize size of buffer for storing the objects
   * @param loaderMapper required mapper for loading from the DB
   */
  public AbstractDefaultLoaderDAO(final int initialBufferSize, final LoaderMapper<T> loaderMapper) {
    super(loaderMapper);
    this.initialBufferSize = initialBufferSize;
    this.loaderMapper = loaderMapper;
  }
  
  /**
   * Returns a map {id -> cache object} of all DataTag objects in the DB.
   * Also does postDbLoading changes!
   */
  @Override
  public Map<Long, T> getAllAsMap() {
    List<T> cacheableList = loaderMapper.getAll();
    ConcurrentHashMap<Long, T> returnMap = new ConcurrentHashMap<Long, T>(initialBufferSize);
    Iterator<T> it = cacheableList.iterator();
    T current;
    while (it.hasNext()) {
      current = it.next();
      if (current != null) {
        returnMap.put(current.getId(), doPostDbLoading(current));
      } else {
        LOGGER.warn("Null value retrieved from DB by Mapper " + loaderMapper.getClass().getSimpleName());
      }
    }
    return returnMap;
  }
  
}
