package cern.c2mon.cache.impl;

import cern.c2mon.cache.api.spi.CacheQuery;
import cern.c2mon.shared.common.Cacheable;
import org.apache.ignite.lang.IgniteBiPredicate;

public class IgniteC2monBiPredicate<V extends Cacheable> implements IgniteBiPredicate<Long,V> {

  private CacheQuery<V> c2monQuery;

  public IgniteC2monBiPredicate(CacheQuery<V> c2monQuery) {
    this.c2monQuery = c2monQuery;
  }

  @Override
  public boolean apply(Long aLong, V v) {
    return c2monQuery.apply(v);
  }
}
