package cern.c2mon.cache.impl;

import cern.c2mon.shared.common.Cacheable;
import cern.c2mon.shared.common.SerializableFunction;
import org.apache.ignite.lang.IgniteBiPredicate;


public class IgniteC2monPredicateWrapper<V extends Cacheable> implements IgniteBiPredicate<Long,V> {

  private SerializableFunction<V,Boolean> c2monQuery;

  public IgniteC2monPredicateWrapper(SerializableFunction<V,Boolean> c2monQuery) {
    this.c2monQuery = c2monQuery;
  }

  @Override
  public boolean apply(Long aLong, V v) {
    return c2monQuery.apply(v);
  }
}
