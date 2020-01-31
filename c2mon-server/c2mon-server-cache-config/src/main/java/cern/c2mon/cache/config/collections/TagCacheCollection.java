package cern.c2mon.cache.config.collections;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.api.set.CacheCollection;
import cern.c2mon.cache.config.ClientQueryProvider;
import cern.c2mon.server.common.alive.AliveTag;
import cern.c2mon.server.common.commfault.CommFaultTag;
import cern.c2mon.server.common.datatag.DataTag;
import cern.c2mon.server.common.rule.RuleTag;
import cern.c2mon.server.common.supervision.SupervisionStateTag;
import cern.c2mon.server.common.tag.Tag;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Aggregates [Control,Data,Rule]tag caches
 *
 * @author Alexandros Papageorgiou Koufidis
 */
@Named
public class TagCacheCollection extends CacheCollection<Tag> {

  @Inject
  public TagCacheCollection(final C2monCache<RuleTag> ruleTagCacheRef,
                            final C2monCache<DataTag> dataTagCacheRef,
                            final C2monCache<AliveTag> aliveTagCacheRef,
                            final C2monCache<CommFaultTag> commFaultTagCacheRef,
                            final C2monCache<SupervisionStateTag> stateTagCacheRef) {
    super(ruleTagCacheRef, dataTagCacheRef, aliveTagCacheRef, commFaultTagCacheRef, stateTagCacheRef);
  }

  public Collection<Tag> findByNameRegex(String regex) {
    return Stream.of(caches)
      .parallel()
      .flatMap(cache -> ClientQueryProvider.queryByClientInput(cache, Tag::getName, regex).stream())
      .collect(Collectors.toSet());
  }

  public void addAlarmToTag(long tagId, long alarmId) {
    doAcrossCaches(tagId, cache -> cache.computeQuiet(tagId, tag -> tag.getAlarmIds().add(alarmId)));
  }

  public void removeAlarmFromTag(long tagId, long alarmId) {
    doAcrossCaches(tagId, cache -> cache.computeQuiet(tagId, tag -> tag.getAlarmIds().remove(alarmId)));
  }
}
