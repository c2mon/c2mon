package cern.c2mon.cache.impl;

import cern.c2mon.server.cache.C2monCacheFactory;
import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.shared.common.Cacheable;
import lombok.extern.slf4j.Slf4j;
import org.apache.ignite.cache.CacheMode;
import org.apache.ignite.configuration.CacheConfiguration;

import cern.c2mon.cache.api.AbstractCache;
import cern.c2mon.cache.api.factory.AbstractCacheFactory;
import org.springframework.stereotype.Service;

/**
 * @author Szymon Halastra
 */

@Slf4j
@Service
public class IgniteFactory extends C2monCacheFactory {

  @Override
  public <K, V extends Cacheable> AbstractCache<Long,V> createCache(String name, Class<K> keyType, Class<V> valueType) {
    CacheConfiguration<Long,V> cacheConfiguration = new DefaultIgniteC2monCacheConfiguration<>(name);

    cacheConfiguration.setIndexedTypes(keyType, valueType);

    return new IgniteC2monCache<>(name, cacheConfiguration);
  }

  @Override
  public <V extends Cacheable> C2monCache<V> createCache(String name, Class<V> valueType) {
    return (IgniteC2monCache<V>) createCache(name, Long.class, valueType);
  }

  @Override
  public AbstractCacheFactory getCachingFactory() {
    return this;
  }
}
