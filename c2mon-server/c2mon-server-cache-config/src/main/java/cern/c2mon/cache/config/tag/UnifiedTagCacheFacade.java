package cern.c2mon.cache.config.tag;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.api.exception.CacheElementNotFoundException;
import cern.c2mon.cache.api.listener.BufferedCacheListener;
import cern.c2mon.cache.api.listener.CacheListener;
import cern.c2mon.cache.config.ClientQueryProvider;
import cern.c2mon.server.common.datatag.DataTag;
import cern.c2mon.server.common.rule.RuleTag;
import cern.c2mon.server.common.tag.Tag;
import cern.c2mon.shared.common.CacheEvent;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

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

  public boolean containsKey(long id) {
    try {
      return doAcrossCaches(id, cache -> cache.containsKey(id));
    } catch (CacheElementNotFoundException e) {
      return false;
    }
  }

  public Tag get(long id) {
    return doAcrossCaches(id, cache -> cache.get(id));
  }

  public Set<Long> getKeys() {
    return tagCaches.stream().flatMap(cache -> cache.getKeys().stream()).collect(Collectors.toSet());
  }

  public Collection<Tag> findByNameRegex(String regex) {
    return tagCaches.stream()
      .flatMap(cache -> ClientQueryProvider.queryByClientInput(cache, Tag::getName, regex).stream())
      .collect(Collectors.toSet());
  }

  public void addAlarmToTag(long tagId, long alarmId) {
    doAcrossCaches(tagId, cache -> cache.computeQuiet(tagId, tag -> tag.getAlarmIds().add(alarmId)));
  }

  public void removeAlarmFromTag(long tagId, long alarmId) {
    doAcrossCaches(tagId, cache -> cache.computeQuiet(tagId, tag -> tag.getAlarmIds().remove(alarmId)));
  }

  public void registerListener(CacheListener<Tag> listener, CacheEvent... events) {
    tagCaches.forEach(cache -> cache.getCacheListenerManager().registerListener(listener::apply, events));
  }

  public void registerBufferedListener(BufferedCacheListener<Tag> listener, CacheEvent... events) {
    tagCaches.forEach(cache -> cache.getCacheListenerManager().registerBufferedListener(tags -> listener.apply((List<Tag>) tags), events));
  }

  public void close() {
    tagCaches.forEach(cache -> cache.getCacheListenerManager().close());
  }

  private <R> R doAcrossCaches(long id, Function<C2monCache<? extends Tag>, R> cacheAction) {
    for (C2monCache<? extends Tag> cache : tagCaches) {
      if (cache.containsKey(id))
        return cacheAction.apply(cache);
    }

    throw new CacheElementNotFoundException();
  }
}
