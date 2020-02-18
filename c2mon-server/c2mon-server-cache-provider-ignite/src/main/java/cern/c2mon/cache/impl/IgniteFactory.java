package cern.c2mon.cache.impl;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.api.factory.AbstractCacheFactory;
import cern.c2mon.shared.common.Cacheable;
import lombok.extern.slf4j.Slf4j;
import org.apache.ignite.configuration.CacheConfiguration;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

/**
 * @author Szymon Halastra, Alexander Papageorgiou
 */
@Slf4j
@Named
@Singleton
public class IgniteFactory extends AbstractCacheFactory {

  // This seems to be thread safe, but keep an eye out for strange behavior
  private final IgniteC2monBean igniteInstance;

  @Inject
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
