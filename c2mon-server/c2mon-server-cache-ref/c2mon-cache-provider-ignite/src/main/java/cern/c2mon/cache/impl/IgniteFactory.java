package cern.c2mon.cache.impl;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.api.factory.AbstractCacheFactory;
import cern.c2mon.shared.common.Cacheable;
import lombok.extern.slf4j.Slf4j;
import org.apache.ignite.configuration.CacheConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Szymon Halastra
 */

@Slf4j
@Service
public class IgniteFactory extends AbstractCacheFactory {

  // Is this thread safe? We may get some mixups here with the spring autowiring if used concurrently
  private final IgniteC2monBean igniteInstance;

  @Autowired
  public IgniteFactory(final IgniteC2monBean c2monIgnite) {
    this.igniteInstance = c2monIgnite;
  }

  @Override
  public <V extends Cacheable> C2monCache<V> createCache(String name, Class<V> valueType) {
    CacheConfiguration<Long, V> cacheConfiguration = new DefaultIgniteCacheConfiguration<>(name);

    cacheConfiguration.setIndexedTypes(Long.class, valueType);

    return new IgniteC2monCache<>(name, cacheConfiguration, igniteInstance);
  }

  @Override
  public AbstractCacheFactory getCachingFactory() {
    return this;
  }
}
