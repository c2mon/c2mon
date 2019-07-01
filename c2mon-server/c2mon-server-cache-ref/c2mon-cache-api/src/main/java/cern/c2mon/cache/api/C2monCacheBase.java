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
public interface C2monCacheBase<K, V> extends Cache<K, V>, Serializable {

  CacheLoader<K,V> getCacheLoader();

  void setCacheLoader(CacheLoader<K,V> cacheLoader);

  // TODO Do we want this?
  Set<K> getKeys();

  void init();

  <S> Optional<S> executeTransaction(TransactionalCallable<S> callable);
}