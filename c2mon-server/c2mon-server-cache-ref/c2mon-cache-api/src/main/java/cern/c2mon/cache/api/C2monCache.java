package cern.c2mon.cache.api;

import cern.c2mon.cache.api.listener.BufferedCacheListener;
import cern.c2mon.cache.api.listener.CacheListener;
import cern.c2mon.cache.api.listener.CacheSupervisionListener;
import cern.c2mon.cache.api.listener.Listener;
import cern.c2mon.cache.api.loader.CacheLoader;
import cern.c2mon.cache.api.transactions.TransactionalCallable;
import cern.c2mon.server.common.component.Lifecycle;
import cern.c2mon.shared.common.Cacheable;
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

  // TODO Do we want this?
  Set<Long> getKeys();

  @PostConstruct
  void init();

  <T> Optional<T> executeTransaction(TransactionalCallable<T> callable);

  void executeTransaction(Runnable callable);

  default void putQuiet(Long key, V value) {
    put(key, value);
  }

  @Delegate(types = Listener.class)
  Listener<V> getListenerService();

  // TODO Document this to show it should be default behavior
  default void putAndNotify(Long key, V value) {
    put(key, value);
    notifyListenersOfUpdate(value);
  }
}