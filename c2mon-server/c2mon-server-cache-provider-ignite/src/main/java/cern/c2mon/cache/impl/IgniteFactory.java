package cern.c2mon.cache.impl;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.api.factory.AbstractCacheFactory;
import cern.c2mon.shared.common.Cacheable;
import lombok.extern.slf4j.Slf4j;
import org.apache.ignite.cache.CacheAtomicityMode;
import org.apache.ignite.cache.CacheMode;
import org.apache.ignite.configuration.CacheConfiguration;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

/**
 * Creates Ignite caches (or uses existing ones)
 *
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
    CacheConfiguration<Long, V> cacheConfiguration = defaultIgniteCacheConfiguration(name);

    cacheConfiguration.setIndexedTypes(Long.class, valueType);

    return new IgniteC2monCache<>(name, cacheConfiguration, igniteInstance);
  }

  /**
   * Default configuration for a single Ignite cache
   */
  public static <V extends Cacheable> CacheConfiguration<Long, V> defaultIgniteCacheConfiguration(String name) {
    CacheConfiguration<Long, V> cacheConfiguration = new CacheConfiguration<>(name);
    cacheConfiguration.setCacheMode(CacheMode.REPLICATED);
    cacheConfiguration.setAtomicityMode(CacheAtomicityMode.TRANSACTIONAL);
    return cacheConfiguration;
  }

  @Override
  public AbstractCacheFactory getCachingFactory() {
    return this;
  }
}
