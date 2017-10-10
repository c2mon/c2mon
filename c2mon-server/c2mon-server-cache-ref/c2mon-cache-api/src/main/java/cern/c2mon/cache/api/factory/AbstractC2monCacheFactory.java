package cern.c2mon.cache.api.factory;

import cern.c2mon.cache.api.Cache;

/**
 * @author Szymon Halastra
 */
public abstract class AbstractC2monCacheFactory {

  public AbstractC2monCacheFactory getFactory() {
    AbstractC2monCacheFactory abstractFactory = getCachingFactory();

    return abstractFactory;
  }

  public abstract Cache createCache(String name, Class<?> keyType, Class<?> valueType);

  public abstract AbstractC2monCacheFactory getCachingFactory();
}
