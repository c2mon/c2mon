package cern.c2mon.cache.api;

import cern.c2mon.cache.api.listener.C2monBufferedCacheListener;
import cern.c2mon.cache.api.listener.C2monCacheListener;
import cern.c2mon.server.common.component.Lifecycle;
import cern.c2mon.shared.common.Cacheable;

/**
 * @author Szymon Halastra
 */
public interface C2monListener<V extends Cacheable> {

  /**
   * Creates a new cache element and calls the Ehcache notifyElementUpdated
   * method.
   * <p>
   * Notifies the listeners that an update occurred for this DataTag. Should
   * be called *within a lock on the cache object* so the object is not modified
   * before being passed to the listeners (using a clone).
   *
   * @param cacheable the cache object that has been updated
   */
  void notifyListenersOfUpdate(V cacheable);

  void notifyListenerStatusConfirmation(V cacheable, long timestamp);

  void registerSynchronousListener(C2monCacheListener<? super V> cacheListener);

  Lifecycle registerListener(C2monCacheListener<? super V> cacheListener);

  Lifecycle registerThreadedListener(C2monCacheListener<? super V> cacheListener, int queueCapacity, int threadPoolSize);

  Lifecycle registerBufferedListener(C2monBufferedCacheListener<Cacheable> c2monBufferedCacheListener, int frequency);

  Lifecycle registerKeyBufferedListener(C2monBufferedCacheListener<Long> bufferedCacheListener, int frequency);
}
