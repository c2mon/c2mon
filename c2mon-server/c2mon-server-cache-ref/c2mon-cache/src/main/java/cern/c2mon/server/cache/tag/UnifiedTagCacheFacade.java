package cern.c2mon.server.cache.tag;

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
  public UnifiedTagCacheFacade(final C2monCache<RuleTag> ruleTagCacheRef, final C2monCache<DataTag> dataTagCacheRef,
                               final C2monCache<ControlTag> controlTagCacheRef) {
    tagCaches = Arrays.asList(ruleTagCacheRef, dataTagCacheRef, controlTagCacheRef);
  }

  @Override
  public void notifyListenersOf(CacheEvent event, Tag source) {
// No-op? This should probably not be called directly? The listeners are automatically doing this, if registered
  }

  @Override
  public void registerListener(CacheListener listener, CacheEvent... events) {
      tagCaches.forEach(cache -> cache.registerListener(listener,events));
  }

  @Override
  public void deregisterListener(CacheListener listener) {
      tagCaches.forEach(cache -> cache.deregisterListener(listener));
  }
}
