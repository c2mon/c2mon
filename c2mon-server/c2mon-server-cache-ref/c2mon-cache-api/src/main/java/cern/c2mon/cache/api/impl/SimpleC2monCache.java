package cern.c2mon.cache.api.impl;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.api.exception.CacheElementNotFoundException;
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
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

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
  public V get(@NonNull Long key) throws NullPointerException, CacheElementNotFoundException {
    return (V) C2monCache.super.get(key).clone();
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
  public <S> S executeTransaction(TransactionalCallable<S> callable) {
    return callable.call();
  }


  @Override
  public Collection<V> query(Function<V, Boolean> filter) {
    return getKeys().stream().map(this::get).filter(filter::apply).collect(Collectors.toList());
  }

  @Override
  public Collection<V> query(CacheQuery<V> providedQuery) {
    return query(providedQuery.filter());
  }
}
