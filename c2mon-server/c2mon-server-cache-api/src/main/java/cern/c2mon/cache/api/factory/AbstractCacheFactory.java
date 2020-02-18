package cern.c2mon.cache.api.factory;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.shared.common.Cacheable;

/**
 * Provides an implementation-agnostic way to obtain caches from the
 * underlying system.
 *
 * @author Szymon Halastra
 */
public abstract class AbstractCacheFactory {

  // Why two methods for the same thing? Is getCachingFactory a hook for sth?
  public AbstractCacheFactory getFactory() {
    return getCachingFactory();
  }

  /**
   * Returns the existing cache, if one exists with the same name,
   * or creates a new one
   *
   * Should never return null
   */
  public abstract <V extends Cacheable> C2monCache<V> createCache(String name, Class<V> valueType);

  public abstract AbstractCacheFactory getCachingFactory();
}

