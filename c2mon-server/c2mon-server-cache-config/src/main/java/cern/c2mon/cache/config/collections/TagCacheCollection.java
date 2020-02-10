package cern.c2mon.cache.config.collections;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.api.exception.CacheElementNotFoundException;
import cern.c2mon.cache.api.set.CacheCollection;
import cern.c2mon.cache.config.ClientQueryProvider;
import cern.c2mon.server.common.alive.AliveTag;
import cern.c2mon.server.common.commfault.CommFaultTag;
import cern.c2mon.server.common.datatag.DataTag;
import cern.c2mon.server.common.rule.RuleTag;
import cern.c2mon.server.common.supervision.SupervisionStateTag;
import cern.c2mon.server.common.tag.AbstractTagCacheObject;
import cern.c2mon.server.common.tag.Tag;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Aggregates [Control,Data,Rule]tag caches
 *
 * @author Alexandros Papageorgiou Koufidis
 */
@Named
@Slf4j
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
    return caches
      .parallelStream()
      .flatMap(cache -> ClientQueryProvider.queryByClientInput(cache, Tag::getName, regex).stream())
      .collect(Collectors.toSet());
  }


  /**
   * Adds the alarm to the list of alarms associated to this
   * tag (locks tag).
   * @param tagId the id of the tag
   * @param alarmId the id of the alarm
   * @throws CacheElementNotFoundException if the tag cannot be found in the cache
   */
  public void addAlarmToTag(long tagId, long alarmId) {
    log.trace("Adding Alarm {} reference from Tag {}",alarmId, tagId);
    doAcrossCaches(tagId, cache -> cache.computeQuiet(tagId, tag -> {
      if (!tag.getAlarmIds().contains(alarmId))
        tag.getAlarmIds().add(alarmId);
      }));
  }

  /**
   * Removes the Alarm from the list of alarms
   * attached to the Tag.
   *
   * @param tagId the Tag id
   * @param alarmId the id of the alarm to remove
   * @throws CacheElementNotFoundException if the tag cannot be found in the cache
   */
  public void removeAlarmFromTag(long tagId, long alarmId) {
    log.trace("Removing Alarm {} reference from Tag {}",alarmId, tagId);
    doAcrossCaches(tagId, cache -> cache.computeQuiet(tagId, tag -> tag.getAlarmIds().remove(alarmId)));
  }

  /**
   * Removes this rule from the list of those that need evaluating when
   * this tag is updated.
   * <p>
   * Note also adjusts text field of cache object.
   *
   * @param tagId     the id of the tag used in the rule (directly, not via another rule)
   * @param ruleTagId the id of the rule
   */
  public void removeRuleFromTag(long tagId, final Long ruleTagId) {
    log.trace("Removing RuleTag {} reference from Tag {}",ruleTagId, tagId);
    doAcrossCaches(tagId, cache ->
      cache.compute(tagId, tag -> {
        tag.getRuleIds().remove(ruleTagId);
        updateRuleIdsString(tag);
      }));
  }

  /**
   * Adds the rule to the list of those that need evaluating when
   * this tag is updated.
   * <p>
   * Note also adjust text field of cache object.
   */
  public void addRuleToTag(final long tagId, final Long ruleTagId) {
    log.trace("Adding RuleTag {} reference to Tag {}",ruleTagId, tagId);
    doAcrossCaches(tagId, cache ->
      cache.compute(tagId, tag -> {
        if (!tag.getRuleIds().contains(ruleTagId)) {
          tag.getRuleIds().add(ruleTagId);
          updateRuleIdsString(tag);
        }
      }));
  }

  private void updateRuleIdsString(Tag tag) {
    AbstractTagCacheObject cacheTag = (AbstractTagCacheObject) tag;

    StringBuilder bld = new StringBuilder();
    for (Long id : cacheTag.getRuleIds()) {
      bld.append(id).append(",");
    }

    cacheTag.setRuleIdsString(bld.toString());

    if (bld.length() > 0) {
      //remove ", "
      cacheTag.setRuleIdsString(bld.toString().substring(0, bld.length() - 1));
    }
  }
}
