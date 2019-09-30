package cern.c2mon.cache.api.listener;

import cern.c2mon.server.common.component.Lifecycle;
import cern.c2mon.shared.common.Cacheable;

/**
 * @author Alexandros Papageorgiou Koufidis
 */
public interface ListenerDelegator<V extends Cacheable> extends Listener<V> {

  Listener<V> getListenerService();

  @Override
  default void notifyListenersOfUpdate(V cacheable) {
    getListenerService().notifyListenersOfUpdate(cacheable);
  }

  @Override
  default void notifyListenersOfSupervisionChange(V tag) {
    getListenerService().notifyListenersOfSupervisionChange(tag);
  }

  @Override
  default void notifyListenerStatusConfirmation(V cacheable, long timestamp) {
    getListenerService().notifyListenerStatusConfirmation(cacheable, timestamp);
  }

  @Override
  default void registerSynchronousListener(CacheListener<? super V> cacheListener) {
    getListenerService().registerSynchronousListener(cacheListener);
  }

  @Override
  default Lifecycle registerListener(CacheListener<? super V> cacheListener) {
    return getListenerService().registerListener(cacheListener);
  }

  @Override
  default void registerListenerWithSupervision(CacheSupervisionListener<? super V> cacheSupervisionListener) {
    getListenerService().registerListenerWithSupervision(cacheSupervisionListener);
  }

  @Override
  default Lifecycle registerThreadedListener(CacheListener<? super V> cacheListener, int queueCapacity, int threadPoolSize) {
    return getListenerService().registerThreadedListener(cacheListener, queueCapacity, threadPoolSize);
  }

  @Override
  default Lifecycle registerBufferedListener(BufferedCacheListener<Cacheable> bufferedCacheListener, int frequency) {
    return getListenerService().registerBufferedListener(bufferedCacheListener, frequency);
  }

  @Override
  default Lifecycle registerKeyBufferedListener(BufferedCacheListener<Long> bufferedCacheListener, int frequency) {
    return getListenerService().registerKeyBufferedListener(bufferedCacheListener, frequency);
  }
}
