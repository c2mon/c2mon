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

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cern.c2mon.server.cache.dbaccess.BatchLoaderMapper;
import cern.c2mon.server.cache.dbaccess.structure.DBBatch;
import cern.c2mon.server.cache.loading.BatchCacheLoaderDAO;
import cern.c2mon.shared.common.Cacheable;

/**
 * Abstract class implementing {@link BatchCacheLoaderDAO}, providing common DAO methods
 * for loading caches on many threads.
 *
 * @param <T> the type of the cache object
 * @author Mark Brightwell
 */
@Slf4j
public abstract class AbstractBatchLoaderDAO<T extends Cacheable> extends AbstractSimpleLoaderDAO<T> implements
    BatchCacheLoaderDAO<T> {

  /**
   * Mapper required for batch loading.
   */
  private BatchLoaderMapper<T> batchLoaderMapper;

  /**
   * Constructor.
   *
   * @param batchLoaderMapper required mapper
   */
  public AbstractBatchLoaderDAO(final BatchLoaderMapper<T> batchLoaderMapper) {
    super(batchLoaderMapper);
    this.batchLoaderMapper = batchLoaderMapper;
  }


  @Override
  public Integer getMaxRow() {
    Integer maxId = batchLoaderMapper.getNumberItems();
    if (maxId != null) {
      return maxId;
    } else {
      return 0;
    }
  }

  @Override
  public Map<Object, T> getBatchAsMap(Long firstRow, Long lastRow) {
    DBBatch dbBatch = new DBBatch(firstRow, lastRow);
    List<T> cacheableList = batchLoaderMapper.getRowBatch(dbBatch);
    Map<Object, T> returnMap = new ConcurrentHashMap<>((int) (lastRow - firstRow));
    for (T element : cacheableList) {
      if (element != null) {
        returnMap.put(element.getId(), doPostDbLoading(element));
      } else {
        log.warn("Null value retrieved from DB by Mapper {} with firstRow:{}, lastRow:{}",
            batchLoaderMapper.getClass().getSimpleName(), firstRow, lastRow);
      }
    }
    return returnMap;
  }

}
