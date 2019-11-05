package cern.c2mon.cache.api.flow;

import cern.c2mon.shared.common.CacheEvent;
import cern.c2mon.shared.common.Cacheable;

import java.util.HashSet;
import java.util.Set;

/**
 * A simple cache flow that allows all distinct (different than older value) updates through
 */
public class DefaultC2monCacheFlow<T extends Cacheable> implements C2monCacheFlow<T> {

  @Override
  public boolean preInsertValidate(T older, T newer) {
    return !newer.equals(older);
  }

  @Override
  public Set<CacheEvent> postInsertEvents(T older, T newer) {
    Set<CacheEvent> results = new HashSet<>();
    if (older == null)
      results.add(CacheEvent.INSERTED);
    return results;
  }
}
