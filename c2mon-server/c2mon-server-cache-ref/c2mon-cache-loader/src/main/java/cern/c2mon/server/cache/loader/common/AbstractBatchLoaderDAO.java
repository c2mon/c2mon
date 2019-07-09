package cern.c2mon.server.cache.loader.common;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import lombok.extern.slf4j.Slf4j;

import cern.c2mon.server.cache.dbaccess.BatchLoaderMapper;
import cern.c2mon.server.cache.dbaccess.structure.DBBatch;
import cern.c2mon.server.cache.loader.BatchCacheLoaderDAO;
import cern.c2mon.shared.common.Cacheable;

/**
 * Abstract class implementing {@link BatchCacheLoaderDAO}, providing common DAO methods
 * for loading caches on many threads.
 *
 * @param <T> the type of the cache object
 *
 * @author Mark Brightwell
 */
@Slf4j
public abstract class AbstractBatchLoaderDAO<K extends Number, T extends Cacheable> extends AbstractSimpleLoaderDAO<T> implements
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
    }
    else {
      return 0;
    }
  }

  @Override
  public Map<Long, T> getBatchAsMap(Long firstRow, Long lastRow) {
    DBBatch dbBatch = new DBBatch(firstRow, lastRow);
    List<T> cacheableList = batchLoaderMapper.getRowBatch(dbBatch);
    Map<Long, T> returnMap = new ConcurrentHashMap<>((int) (lastRow - firstRow));
    for (T element : cacheableList) {
      if (element != null) {
        returnMap.put(element.getId(), doPostDbLoading(element));
      }
      else {
        log.warn("Null value retrieved from DB by Mapper {} with firstRow:{}, lastRow:{}",
                batchLoaderMapper.getClass().getSimpleName(), firstRow, lastRow);
      }
    }
    return returnMap;
  }
}