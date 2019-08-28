package cern.c2mon.server.cache;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.api.factory.AbstractCacheFactory;
import cern.c2mon.server.cache.loader.CacheLoaderDAO;
import cern.c2mon.server.cache.loader.SimpleCacheLoaderDAO;
import cern.c2mon.server.cache.loader.common.SimpleCacheLoader;
import cern.c2mon.shared.common.Cacheable;

/**
 *
 * @param <V>
 * @author Alexandros Papageorgiou Koufidis
 */
public class AbstractSimpleCacheConfig<V extends Cacheable> extends AbstractCacheConfig<V, SimpleCacheLoader<V>> {
  private final CacheLoaderDAO<V> cacheLoaderDAORef;

  protected AbstractSimpleCacheConfig(final AbstractCacheFactory cachingFactory, final CacheName cacheName, final Class<V> classRef, final CacheLoaderDAO<V> cacheLoaderDAORef) {
    super(cachingFactory, cacheName, classRef);
    this.cacheLoaderDAORef = cacheLoaderDAORef;
  }

  protected SimpleCacheLoader<V> createCacheLoader(C2monCache<V> cache) {
    return new SimpleCacheLoader<>(cache, cacheLoaderDAORef);
  }
}