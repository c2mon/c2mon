package cern.c2mon.cache.api;

import cern.c2mon.cache.api.listener.C2monBufferedCacheListener;
import cern.c2mon.cache.api.listener.C2monCacheListener;
import cern.c2mon.server.common.component.Lifecycle;
import cern.c2mon.shared.common.Cacheable;

/**
 * @author Szymon Halastra
 */
public interface C2monListener<V extends Cacheable> {
  void notifyListenersOfUpdate(V cacheable);

  void notifyListenerStatusConfirmation(V cacheable, long timestamp);

  void registerSynchronousListener(C2monCacheListener<? super V> cacheListener);

  Lifecycle registerListener(C2monCacheListener<? super V> cacheListener);

  Lifecycle registerThreadedListener(C2monCacheListener<? super V> cacheListener, int queueCapacity, int threadPoolSize);

  Lifecycle registerBufferedListener(C2monBufferedCacheListener<Cacheable> c2monBufferedCacheListener, int frequency);

  Lifecycle registerKeyBufferedListener(C2monBufferedCacheListener<Long> bufferedCacheListener, int frequency);
}
