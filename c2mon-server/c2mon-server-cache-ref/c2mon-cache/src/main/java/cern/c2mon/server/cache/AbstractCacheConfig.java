package cern.c2mon.server.cache;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.api.factory.AbstractCacheFactory;
import cern.c2mon.cache.api.loader.CacheLoader;
import cern.c2mon.shared.common.Cacheable;


/**
 * @param <V>
 * @param <CACHE_LOADER>
 * @author Alexandros Papageorgiou Koufidis
 */
public abstract class AbstractCacheConfig<V extends Cacheable, CACHE_LOADER extends CacheLoader<V>> {

  protected final AbstractCacheFactory cachingFactory;
  private final CacheName cacheName;
  private final Class<V> classRef;

  public AbstractCacheConfig(final AbstractCacheFactory cachingFactory, final CacheName cacheName, final Class<V> classRef) {
    this.cachingFactory = cachingFactory;
    this.cacheName = cacheName;
    this.classRef = classRef;
  }

  protected abstract CACHE_LOADER createCacheLoader(C2monCache<V> cache);

  public C2monCache<V> createCache() {
    C2monCache<V> cache = cachingFactory.createCache(cacheName.getLabel(), classRef);

    cache.setCacheLoader(createCacheLoader(cache));

    return cache;
  }
}