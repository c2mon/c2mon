package cern.c2mon.cache.api;

import cern.c2mon.cache.api.listener.BufferedCacheListener;
import cern.c2mon.cache.api.listener.CacheListener;
import cern.c2mon.cache.api.listener.CacheSupervisionListener;
import cern.c2mon.cache.api.listener.Listener;
import cern.c2mon.cache.api.loader.CacheLoader;
import cern.c2mon.cache.api.spi.CacheQuery;
import cern.c2mon.cache.api.transactions.TransactionalCallable;
import cern.c2mon.server.common.component.Lifecycle;
import cern.c2mon.shared.common.Cacheable;
import java.util.Collection;
import lombok.experimental.Delegate;

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

  Set<Long> getKeys();

// TOOO Remove this useless annotation and test system
  @PostConstruct
  void init();

  <T> Optional<T> executeTransaction(TransactionalCallable<T> callable);

  void executeTransaction(Runnable callable);

  default void putQuiet(Long key, V value) {
    put(key, value);
  }

  // TODO Document this to show it should be default behavior
  default void putAndNotify(Long key, V value) {
    put(key, value);
    notifyListenersOfUpdate(value);
  }

  Collection<V> query(CacheQuery<V> providedQuery);

//  === Listeners ===

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