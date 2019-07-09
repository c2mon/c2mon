package cern.c2mon.cache.impl;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.api.factory.AbstractCacheFactory;
import cern.c2mon.shared.common.Cacheable;
import lombok.extern.slf4j.Slf4j;
import org.apache.ignite.configuration.CacheConfiguration;
import org.springframework.stereotype.Service;

/**
 * @author Szymon Halastra
 */

@Slf4j
@Service
public class IgniteFactory extends AbstractCacheFactory {

  @Override
  public <V extends Cacheable> C2monCache<V> createCache(String name, Class<V> valueType) {
    CacheConfiguration<Long, V> cacheConfiguration = new DefaultIgniteCacheConfiguration<>(name);

    cacheConfiguration.setIndexedTypes(Long.class, valueType);

    return new IgniteC2monCacheBase<>(name, cacheConfiguration);
  }

  @Override
  public AbstractCacheFactory getCachingFactory() {
    return this;
  }
}
