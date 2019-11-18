package cern.c2mon.cache.api.flow;

import cern.c2mon.shared.common.CacheEvent;
import cern.c2mon.shared.common.Cacheable;

import java.util.HashSet;
import java.util.Set;
import java.util.function.BiPredicate;

/**
 * A simple cache flow that allows all distinct (different than older value) updates through
 */
public class DefaultC2monCacheFlow<T extends Cacheable> implements C2monCacheUpdateFlow<T> {

  private BiPredicate<T, T> olderIsBeforeNewer;

  public DefaultC2monCacheFlow() {
    this((t, t2) -> true);
  }

  public DefaultC2monCacheFlow(BiPredicate<T, T> olderIsBeforeNewer) {
    this.olderIsBeforeNewer = olderIsBeforeNewer;
  }

  @Override
  public boolean preInsertValidate(T older, T newer) {
    return
      older == null // true for first item inserted
        || (!newer.equals(older) && olderIsBeforeNewer.test(older, newer));
  }

  @Override
  public Set<CacheEvent> postInsertEvents(T older, T newer) {
    Set<CacheEvent> results = new HashSet<>();
    if (older == null)
      results.add(CacheEvent.INSERTED);
    return results;
  }
}
