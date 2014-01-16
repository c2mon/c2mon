package cern.c2mon.server.cache.listener;

import cern.c2mon.server.cache.BufferedTimCacheListener;
import cern.c2mon.shared.common.Cacheable;

/**
 * Cache listener passing collections of keys to listener modules
 * instead of the objects themselves.
 * 
 * @author Mark Brightwell
 * @param <T> the type of cache object
 *
 */
public class BufferedKeyCacheListener<T extends Cacheable> extends AbstractBufferedCacheListener<T, Long> {

  /**
   * Constructor
   * @param bufferedKeyTimCacheListener the listener to register.
   */
  public BufferedKeyCacheListener(final BufferedTimCacheListener<Long> bufferedKeyTimCacheListener) {
    super(bufferedKeyTimCacheListener);   
  }

  /**
   * Returns the key of the cache object.
   */
  @Override
  Long getDerivedObject(final T cacheable) {
    return cacheable.getId();
  }

}
