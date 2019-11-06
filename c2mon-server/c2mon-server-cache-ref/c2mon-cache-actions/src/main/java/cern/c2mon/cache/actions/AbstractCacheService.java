package cern.c2mon.cache.actions;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.api.flow.C2monCacheFlow;
import cern.c2mon.cache.api.flow.DefaultC2monCacheFlow;
import cern.c2mon.shared.common.Cacheable;

import java.util.function.BiPredicate;

public abstract class AbstractCacheService<T extends Cacheable> {

  protected C2monCache<T> cache;

  public AbstractCacheService(C2monCache<T> cache, BiPredicate<T, T> c2monCacheFlow) {
    this(cache, new DefaultC2monCacheFlow<>(c2monCacheFlow));
  }

  public AbstractCacheService(C2monCache<T> cache, C2monCacheFlow<T> c2monCacheFlow) {
    this.cache = cache;
    cache.setCacheFlow(c2monCacheFlow);
  }
}
