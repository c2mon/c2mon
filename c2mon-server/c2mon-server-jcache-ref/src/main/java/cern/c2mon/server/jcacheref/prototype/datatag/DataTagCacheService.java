package cern.c2mon.server.jcacheref.prototype.datatag;

import javax.cache.Cache;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cern.c2mon.server.common.datatag.DataTag;
import cern.c2mon.server.common.thread.Event;
import cern.c2mon.shared.common.datatag.SourceDataTagValue;

/**
 * @author Szymon Halastra
 */

@Service
public class DataTagCacheService<T extends DataTag> {

  private Cache<Long, T> dataTagCache;

  @Autowired
  public DataTagCacheService(Cache<Long, T> dataTagCache) {
    this.dataTagCache = dataTagCache;
  }

  public Cache<Long, T> getDataTagCache() {
    return dataTagCache;
  }

  /**
   * Updates the DataTag in the cache from the passed SourceDataTagValue. The method notifies
   * any cache listeners if an update is made.
   * <p>
   * <p>The cache timestamp is set to the current time. The DAQ and source timestamps are
   * set to the values received in the SourceDataTagValue.
   *
   * @param dataTagId          id of DataTag
   * @param sourceDataTagValue the value received from the data acquisition layer
   *
   * @return true if the tag was indeed updated (that is, the cache was modified, i.e. the update was not
   * filtered out for some reason), together with the cache timestamp of this update
   * @throws CacheElementNotFoundException if the Tag cannot be found in the cache
   */
  public final Event<Boolean> updateFromSource(final Long dataTagId, final SourceDataTagValue sourceDataTagValue) {
    T dataTag = dataTagCache.get(dataTagId);
    Event<Boolean> returnEvent = updateFromSource(dataTag, sourceDataTagValue);

    return returnEvent;
  }

  /**
   * Updates the tag object if the value is not filtered out. Contains the logic on when a
   * DataTagCacheObject should be updated with new values and when not (in particular
   * timestamp restrictions).
   * <p>
   * <p>Also notifies the listeners if an update was performed.
   * <p>
   * <p>Notice the tag is not put back in the cache here.
   *
   * @param dataTag            is modified by the method
   * @param sourceDataTagValue the source value received from the DAQ
   *
   * @return true if an update was performed (i.e. the value was not filtered out)
   */
  protected final Event<Boolean> updateFromSource(final T dataTag, final SourceDataTagValue sourceDataTagValue) {
    long eventTime = 0L;
    Boolean updated = Boolean.FALSE;

    eventTime = dataTag.getCacheTimestamp().getTime();

    return new Event<Boolean>(eventTime, updated);
  }
}