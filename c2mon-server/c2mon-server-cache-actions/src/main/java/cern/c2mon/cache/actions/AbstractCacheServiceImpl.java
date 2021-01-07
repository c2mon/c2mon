package cern.c2mon.cache.actions;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.api.flow.CacheUpdateFlow;
import cern.c2mon.cache.api.flow.DefaultCacheFlow;
import cern.c2mon.shared.common.Cacheable;
import lombok.Getter;

import java.util.function.BiPredicate;

/**
 * Abstract implementation of the abstract cache service
 *
 * @param <T> Generic type
 */
public abstract class AbstractCacheServiceImpl<T extends Cacheable> implements AbstractCacheService<T> {

  @Getter
  protected C2monCache<T> cache;

  /**
   * Implementation of the abstract cache service using the C2monCache and a predicate of two arguments
   * @param cache c2mon cache
   * @param c2monCacheFlow predicate of two arguments
   */
  public AbstractCacheServiceImpl(C2monCache<T> cache, BiPredicate<T, T> c2monCacheFlow) {
    this(cache, new DefaultCacheFlow<>(c2monCacheFlow));
  }

  /**
   * Implementation of the abstract cache service using the C2monCache and the interface containing
   * business logic methods related to putting an object in the cache
   * @param cache c2mon cache
   * @param c2monCacheFlow interface containing business logic methods related to putting an object in the cache
   */
  public AbstractCacheServiceImpl(C2monCache<T> cache, CacheUpdateFlow<T> c2monCacheFlow) {
    this.cache = cache;
    cache.setCacheUpdateFlow(c2monCacheFlow);
  }
}
