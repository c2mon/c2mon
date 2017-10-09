package cern.c2mon.cache.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.ignite.cache.CacheMode;
import org.apache.ignite.configuration.CacheConfiguration;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.api.factory.AbstractC2monCacheFactory;

/**
 * @author Szymon Halastra
 */

@Slf4j
public class IgniteFactory extends AbstractC2monCacheFactory {

  @Override
  public C2monCache createCache(String name, Class<?> keyType, Class<?> valueType) {
    CacheConfiguration cacheConfiguration = new CacheConfiguration();

    cacheConfiguration.setName(name);
    cacheConfiguration.setIndexedTypes(keyType, valueType);
    cacheConfiguration.setCacheMode(CacheMode.REPLICATED);

    return new IgniteC2monCache(name, cacheConfiguration);
  }

  @Override
  public AbstractC2monCacheFactory getCachingFactory() {
    return this;
  }
}
