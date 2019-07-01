package cern.c2mon.cache.api;

import cern.c2mon.cache.api.listener.BufferedCacheListener;
import cern.c2mon.cache.api.listener.CacheListener;
import cern.c2mon.cache.api.listener.Listener;
import cern.c2mon.server.common.component.Lifecycle;
import cern.c2mon.shared.common.Cacheable;

/**
 * This interface is a partially static convenience reference to {@link C2monCacheBase}&lt;Long,*&gt;
 * <p>
 * Since any {@link Cacheable} has a {@code Long} key by default it is only for rare and exceptional
 * use cases that you will want to implement {@code C2monCache} instead of this
 * <p>
 * Do NOT add any methods to this interface. It is just a static type reference and the code
 * (e.g unchecked casts) operates on this assumption
 *
 * @param <V> the type of {@code Cacheable} in the cache
 */
public interface C2monCache<V extends Cacheable> extends C2monCacheBase<Long, V>, Listener<V> {


  // TODO Document this to show it should be default behavior
  default void putAndNotify(Long key, V value) {
    put(key, value);
    notifyListenersOfUpdate(value);
  }

  default void putQuiet(Long key, V value) {
    put(key, value);
  }

  Listener<V> getListenerService();

  @Override
  default void notifyListenersOfUpdate(V cacheable) {
    this.getListenerService().notifyListenersOfUpdate(cacheable);
  }

  @Override
  default void notifyListenerStatusConfirmation(V cacheable, long timestamp) {
    this.getListenerService().notifyListenerStatusConfirmation(cacheable, timestamp);
  }

  @Override
  default void registerSynchronousListener(CacheListener cacheListener) {
    this.getListenerService().registerSynchronousListener(cacheListener);
  }

  @Override
  default Lifecycle registerListener(CacheListener cacheListener) {
    return this.getListenerService().registerListener(cacheListener);
  }

  @Override
  default Lifecycle registerThreadedListener(CacheListener cacheListener, int queueCapacity, int threadPoolSize) {
    return this.getListenerService().registerThreadedListener(cacheListener, queueCapacity, threadPoolSize);
  }

  @Override
  default Lifecycle registerBufferedListener(BufferedCacheListener bufferedCacheListener, int frequency) {
    return this.getListenerService().registerBufferedListener(bufferedCacheListener, frequency);
  }

  @Override
  default Lifecycle registerKeyBufferedListener(BufferedCacheListener bufferedCacheListener, int frequency) {
    return this.getListenerService().registerKeyBufferedListener(bufferedCacheListener, frequency);
  }
}
