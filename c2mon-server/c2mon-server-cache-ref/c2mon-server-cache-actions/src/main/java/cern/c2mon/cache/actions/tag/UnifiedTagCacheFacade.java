package cern.c2mon.cache.actions.tag;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.api.exception.CacheElementNotFoundException;
import cern.c2mon.cache.api.listener.BufferedCacheListener;
import cern.c2mon.cache.api.listener.CacheListener;
import cern.c2mon.server.common.datatag.DataTag;
import cern.c2mon.server.common.rule.RuleTag;
import cern.c2mon.server.common.tag.Tag;
import cern.c2mon.shared.common.CacheEvent;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;

/**
 * NOT a real cache, but an aggregation of [control,datatag,rule] caches
 *
 * @author Alexandros Papageorgiou Koufidis
 */
@Service
public class UnifiedTagCacheFacade {

  private final List<C2monCache<? extends Tag>> tagCaches;

  @Inject
  public UnifiedTagCacheFacade(final C2monCache<RuleTag> ruleTagCacheRef, final C2monCache<DataTag> dataTagCacheRef) {
    tagCaches = Arrays.asList(ruleTagCacheRef, dataTagCacheRef);
  }

  public Tag get(long id) {
    for (C2monCache<? extends Tag> cache : tagCaches) {
      if (cache.containsKey(id))
        return cache.get(id);
    }

    throw new CacheElementNotFoundException();
  }

  public void registerListener(CacheListener listener, CacheEvent... events) {
    // TODO (Alex) Fix the type safety here
    tagCaches.forEach(cache -> cache.getCacheListenerManager().registerListener(listener, events));
  }

  public void registerBufferedListener(BufferedCacheListener listener, CacheEvent... events) {
    tagCaches.forEach(cache -> cache.getCacheListenerManager().registerBufferedListener(listener, events));
  }

  public void close() {
    tagCaches.forEach(cache -> cache.getCacheListenerManager().close());
  }
}
