package cern.c2mon.cache.api;

import cern.c2mon.cache.api.listener.BufferedCacheListener;
import cern.c2mon.cache.api.listener.CacheListener;
import cern.c2mon.cache.api.listener.Listener;
import cern.c2mon.cache.api.loader.CacheLoader;
import cern.c2mon.cache.api.transactions.TransactionalCallable;
import cern.c2mon.server.common.component.Lifecycle;
import cern.c2mon.shared.common.Cacheable;

import javax.annotation.PostConstruct;
import javax.cache.Cache;
import java.io.Serializable;
import java.util.Optional;
import java.util.Set;


/**
 * @param <V> cache element type
 * @author Szymon Halastra
 * @author Alexandros Papageorgiou Koufidis
 * @author Brice Copy
 */
public interface C2monCache<V extends Cacheable> extends Cache<Long, V>, Serializable, Listener<V> {

  CacheLoader getCacheLoader();

  void setCacheLoader(CacheLoader<V> cacheLoader);

  // TODO Do we want this?
  Set<Long> getKeys();

  @PostConstruct
  void init();

  <T> Optional<T> executeTransaction(TransactionalCallable<T> callable);

  void executeTransaction(Runnable callable);

  default void putQuiet(Long key, V value) {
    put(key, value);
  }

  Listener<V> getListenerService();


  // TODO Document this to show it should be default behavior
  default void putAndNotify(Long key, V value) {
    put(key, value);
    notifyListenersOfUpdate(value);
  }

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