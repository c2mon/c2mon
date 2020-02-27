package cern.c2mon.cache.api.impl;

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
public class SimpleCache<V extends Cacheable> extends AbstractCache<V> {

  @Getter
  private final MapBasedCache<V> cache;

  public SimpleCache(String cacheName) {
    super(cacheName);
    cache = new MapBasedCache<>();
  }

  @Override
  public void init() {
    // No-op
  }

  @Override
  public Set<Long> getKeys() {
    return cache.getMap().keySet();
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
      if (!cache.isClosed()) {
        cache.close();
      }
    }
  }
}
