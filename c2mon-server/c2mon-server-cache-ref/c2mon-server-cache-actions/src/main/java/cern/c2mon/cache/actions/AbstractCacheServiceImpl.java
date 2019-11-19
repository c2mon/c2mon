package cern.c2mon.cache.actions;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.api.flow.C2monCacheUpdateFlow;
import cern.c2mon.cache.api.flow.DefaultC2monCacheFlow;
import cern.c2mon.shared.common.Cacheable;
import lombok.Getter;

import java.util.function.BiPredicate;

public abstract class AbstractCacheServiceImpl<T extends Cacheable> implements AbstractCacheService<T> {

  @Getter
  protected C2monCache<T> cache;

  public AbstractCacheServiceImpl(C2monCache<T> cache, BiPredicate<T, T> c2monCacheFlow) {
    this(cache, new DefaultC2monCacheFlow<>(c2monCacheFlow));
  }

  public AbstractCacheServiceImpl(C2monCache<T> cache, C2monCacheUpdateFlow<T> c2monCacheFlow) {
    this.cache = cache;
    cache.setCacheUpdateFlow(c2monCacheFlow);
  }
}
