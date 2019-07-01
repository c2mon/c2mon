package cern.c2mon.cache.api.factory;

import cern.c2mon.cache.api.AbstractCache;
import cern.c2mon.shared.common.Cacheable;

/**
 * @author Szymon Halastra
 */
public abstract class AbstractCacheFactory {

  // Why two methods for the same thing? Is getCachingFactory a hook for sth?
  public AbstractCacheFactory getFactory() {
    return getCachingFactory();
  }

  public abstract <K, V extends Cacheable> AbstractCache createCache(String name, Class<K> keyType, Class<V> valueType);

  public abstract AbstractCacheFactory getCachingFactory();
}

