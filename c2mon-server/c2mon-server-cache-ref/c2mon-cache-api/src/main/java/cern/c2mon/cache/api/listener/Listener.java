package cern.c2mon.cache.api.listener;

import cern.c2mon.server.common.component.Lifecycle;
import cern.c2mon.shared.common.Cacheable;

/**
 * @author Szymon Halastra
 */
public interface Listener<V extends Cacheable> {

  void notifyListenersOfUpdate(V cacheable);

  void notifyListenerStatusConfirmation(V cacheable, long timestamp);

  void registerSynchronousListener(CacheListener<? super V> cacheListener);

  Lifecycle registerListener(CacheListener<? super V> cacheListener);

  Lifecycle registerThreadedListener(CacheListener<? super V> cacheListener, int queueCapacity, int threadPoolSize);

  Lifecycle registerBufferedListener(BufferedCacheListener<Cacheable> bufferedCacheListener, int frequency);

  Lifecycle registerKeyBufferedListener(BufferedCacheListener<Long> bufferedCacheListener, int frequency);
}
