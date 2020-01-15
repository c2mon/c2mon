package cern.c2mon.cache.actions;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.api.flow.CacheUpdateFlow;
import cern.c2mon.cache.api.flow.DefaultCacheFlow;
import cern.c2mon.shared.common.Cacheable;
import lombok.Getter;

import java.util.function.BiPredicate;

public abstract class AbstractCacheServiceImpl<T extends Cacheable> implements AbstractCacheService<T> {

  @Getter
  protected C2monCache<T> cache;

  public AbstractCacheServiceImpl(C2monCache<T> cache, BiPredicate<T, T> c2monCacheFlow) {
    this(cache, new DefaultCacheFlow<>(c2monCacheFlow));
  }

  public AbstractCacheServiceImpl(C2monCache<T> cache, CacheUpdateFlow<T> c2monCacheFlow) {
    this.cache = cache;
    cache.setCacheUpdateFlow(c2monCacheFlow);
  }
}
