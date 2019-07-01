package cern.c2mon.cache.api;

import cern.c2mon.cache.api.listener.BufferedCacheListener;
import cern.c2mon.cache.api.listener.CacheListener;
import cern.c2mon.cache.api.listener.Listener;
import cern.c2mon.cache.api.loader.CacheLoader;
import cern.c2mon.cache.api.transactions.TransactionalCallable;
import cern.c2mon.server.common.component.Lifecycle;
import cern.c2mon.shared.common.Cacheable;

import javax.cache.Cache;
import java.io.Serializable;
import java.util.Optional;
import java.util.Set;


/**
 * @param <K> cache key type
 * @param <V> cache element type
 * @author Szymon Halastra
 */
public interface C2monCacheBase<K, V extends Cacheable> extends Cache<K, V>, Listener<V>, Serializable {

  CacheLoader<K,V> getCacheLoader();

  void setCacheLoader(CacheLoader<K,V> cacheLoader);

  // TODO Do we want this?
  Set<K> getKeys();

  void init();

  <S> Optional<S> executeTransaction(TransactionalCallable<S> callable);

  // TODO Document this to show it should be default behavior
  default void putAndNotify(K key, V value) {
    put(key, value);
    notifyListenersOfUpdate(value);
  }

  default void putQuiet(K key, V value) {
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