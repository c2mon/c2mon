package cern.c2mon.cache.impl.query;

import cern.c2mon.cache.impl.IgniteC2monCache;
import cern.c2mon.shared.common.Cacheable;
import org.apache.ignite.cache.query.ScanQuery;
import org.apache.ignite.lang.IgniteBiPredicate;

import javax.cache.Cache;
import java.util.List;

class IgniteCacheQueryProvider {

  protected <T extends Cacheable> List<T> filter(IgniteC2monCache<T> cache, IgniteBiPredicate<Long, T> filter) {
    return cache.query(new ScanQuery<>(filter), Cache.Entry::getValue).getAll();
  }
}
