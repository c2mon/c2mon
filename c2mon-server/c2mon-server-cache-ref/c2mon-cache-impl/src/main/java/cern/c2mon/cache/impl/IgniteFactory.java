package cern.c2mon.cache.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.ignite.cache.CacheMode;
import org.apache.ignite.configuration.CacheConfiguration;

import cern.c2mon.cache.api.Cache;
import cern.c2mon.cache.api.factory.AbstractCacheFactory;

/**
 * @author Szymon Halastra
 */

@Slf4j
public class IgniteFactory extends AbstractCacheFactory {

  @Override
  public Cache createCache(String name, Class<?> keyType, Class<?> valueType) {
    CacheConfiguration cacheConfiguration = new CacheConfiguration();

    cacheConfiguration.setName(name);
    cacheConfiguration.setIndexedTypes(keyType, valueType);
    cacheConfiguration.setCacheMode(CacheMode.REPLICATED);

    return new IgniteCache(name, cacheConfiguration);
  }

  @Override
  public AbstractCacheFactory getCachingFactory() {
    return this;
  }
}
