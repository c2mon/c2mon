package cern.c2mon.server.cache.listener;

import cern.c2mon.server.cache.BufferedTimCacheListener;
import cern.c2mon.shared.common.Cacheable;

/**
 * Cache listener passing collections of cache objects to listener modules
 * instead of single objects.
 * 
 * @author Mark Brightwell
 * @param <T> the type of cache object
 */
public class BufferedCacheListener<T extends Cacheable> extends AbstractBufferedCacheListener<T, T> {

  /**
   * Constructor.
   * @param bufferedTimCacheListener listener expecting collections of cache objects
   */
  public BufferedCacheListener(final BufferedTimCacheListener<T> bufferedTimCacheListener) {
    super(bufferedTimCacheListener);    
  }

  /**
   * Returns the cache object itself. 
   */
  @Override
  T getDerivedObject(final T cacheable) {
    return cacheable;
  }

}
