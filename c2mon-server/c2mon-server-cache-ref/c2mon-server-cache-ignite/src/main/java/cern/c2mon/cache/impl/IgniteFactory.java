package cern.c2mon.cache.impl;

import cern.c2mon.server.cache.C2monCacheFactory;
import cern.c2mon.server.cache.C2monCacheTyped;
import cern.c2mon.shared.common.Cacheable;
import lombok.extern.slf4j.Slf4j;
import org.apache.ignite.cache.CacheMode;
import org.apache.ignite.configuration.CacheConfiguration;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.api.factory.AbstractCacheFactory;
import org.springframework.stereotype.Service;

/**
 * @author Szymon Halastra
 */

@Slf4j
@Service
public class IgniteFactory extends C2monCacheFactory {

  @Override
  public <K, V extends Cacheable> C2monCache<Long,V> createCache(String name, Class<K> keyType, Class<V> valueType) {
    CacheConfiguration<Long,V> cacheConfiguration = new CacheConfiguration<>();

    cacheConfiguration.setName(name);
    cacheConfiguration.setIndexedTypes(keyType, valueType);
    cacheConfiguration.setCacheMode(CacheMode.REPLICATED);

    return new IgniteC2monCache<>(name, cacheConfiguration);
  }

  @Override
  public <V extends Cacheable> C2monCacheTyped<V> createCacheTyped(String name, Class<V> valueType) {
    return (IgniteC2monCache<V>) createCache(name, Long.class, valueType);
  }

  @Override
  public AbstractCacheFactory getCachingFactory() {
    return this;
  }
}
