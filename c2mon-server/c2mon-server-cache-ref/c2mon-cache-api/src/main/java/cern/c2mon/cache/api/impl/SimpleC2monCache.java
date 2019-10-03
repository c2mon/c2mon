package cern.c2mon.cache.api.impl;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.api.listener.CacheListenerManager;
import cern.c2mon.cache.api.listener.CacheListenerManagerImpl;
import cern.c2mon.cache.api.loader.CacheLoader;
import cern.c2mon.cache.api.spi.CacheQuery;
import cern.c2mon.cache.api.transactions.TransactionalCallable;
import cern.c2mon.shared.common.Cacheable;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import javax.cache.Cache;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

/**
 * Class used only for testing, as a simple implementation of C2monCache
 *
 * @author Szymon Halastra
 * @author Alexandros Papageorgiou Koufidis
 */
public class SimpleC2monCache<V extends Cacheable> implements C2monCache<V> {

  @Getter
  private final MapBasedCache<V> cache = new MapBasedCache<>();
  @Getter
  @Setter
  protected CacheLoader<V> cacheLoader;
  @Getter
  private CacheListenerManager<V> cacheListenerManager = new CacheListenerManagerImpl<>();
  @Getter
  private String cacheName;

  public SimpleC2monCache(String cacheName) {
    this.cacheName = cacheName;
  }

  @Override
  public void init() {

  }

  @Override
  public Set<Long> getKeys() {
    return cache.getMap().keySet();
  }

  @Override
  public Cache<Long, V> getCache() {
    return cache;
  }

  @Override
  public <S> Optional<S> executeTransaction(TransactionalCallable<S> callable) {
    S returnValue = callable.call();

    if (returnValue == null) {
      return Optional.empty();
    }
    return Optional.of(returnValue);
  }


  @Override
  public Collection<V> query(@NonNull Function<V, Boolean> filter) {
    return null;
  }

  @Override
  public Collection<V> query(@NonNull CacheQuery<V> providedQuery) {
    return null;
  }
}
