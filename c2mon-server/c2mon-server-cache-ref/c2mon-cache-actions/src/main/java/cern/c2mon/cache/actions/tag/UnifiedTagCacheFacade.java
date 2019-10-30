package cern.c2mon.cache.actions.tag;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.api.listener.CacheListener;
import cern.c2mon.cache.api.listener.CacheListenerManager;
import cern.c2mon.server.common.control.ControlTag;
import cern.c2mon.server.common.datatag.DataTag;
import cern.c2mon.server.common.rule.RuleTag;
import cern.c2mon.server.common.tag.Tag;
import cern.c2mon.shared.common.CacheEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class UnifiedTagCacheFacade implements CacheListenerManager<Tag> {

  private final List<C2monCache<? extends Tag>> tagCaches;

  @Autowired
  public UnifiedTagCacheFacade(final C2monCache<RuleTag> ruleTagCacheRef, final C2monCache<ControlTag> controlTagCacheRef,
                               final C2monCache<DataTag> dataTagCacheRef) {
    tagCaches = Arrays.asList(ruleTagCacheRef, dataTagCacheRef, controlTagCacheRef);
  }

  @Override
  public void notifyListenersOf(CacheEvent event, Tag source) {
    // No-op? This should probably not be called directly? The listeners are automatically doing this, if registered
    // Currently leaving this as an exception to see if the control flow gets here
    throw new UnsupportedOperationException(
      "Attempting to call listeners on the tag cache facade, instead of the real caches. " +
        "This operation is not allowed");
  }

  @Override
  public void registerListener(CacheListener listener, CacheEvent... events) {
    tagCaches.forEach(cache -> cache.registerListener(listener, events));
  }

  @Override
  public void deregisterListener(CacheListener listener) {
    tagCaches.forEach(cache -> cache.deregisterListener(listener));
  }
}
