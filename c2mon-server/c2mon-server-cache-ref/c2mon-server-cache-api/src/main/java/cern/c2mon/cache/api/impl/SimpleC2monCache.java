package cern.c2mon.cache.api.impl;

import cern.c2mon.cache.api.C2monCacheimpl;
import cern.c2mon.cache.api.spi.CacheQuery;
import cern.c2mon.shared.common.Cacheable;
import lombok.Getter;

import java.util.Collection;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Class used only for testing, as a simple implementation of C2monCache
 *
 * @author Szymon Halastra
 * @author Alexandros Papageorgiou Koufidis
 */
public class SimpleC2monCache<V extends Cacheable> extends C2monCacheimpl<V> {

  @Getter
  private final MapBasedCache<V> mapBasedCache;

  public SimpleC2monCache(String cacheName) {
    super(cacheName, new MapBasedCache<>());
    mapBasedCache = (MapBasedCache<V>) cache;
  }

  @Override
  public void init() {
    // No-op
  }

  @Override
  public Set<Long> getKeys() {
    return mapBasedCache.getMap().keySet();
  }

  @Override
  public <S> S executeTransaction(Supplier<S> callable) {
    return callable.get();
  }

  @Override
  public Collection<V> query(Function<V, Boolean> filter) {
    return getKeys().stream().map(this::get).filter(filter::apply).collect(Collectors.toList());
  }

  @Override
  public Collection<V> query(CacheQuery<V> providedQuery) {
    return query(providedQuery.filter());
  }

  @Override
  public void close() {
    // It's quite possible that the cache is already closed, so check first
    synchronized (this) {
      if (!mapBasedCache.isClosed())
        mapBasedCache.close();
      // TODO (Alex) Close listener manager?
    }
  }
}
