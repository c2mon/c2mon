package cern.c2mon.server.rule;

import cern.c2mon.cache.actions.AbstractCacheServiceImpl;
import cern.c2mon.cache.actions.tag.TagController;
import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.api.flow.DefaultCacheFlow;
import cern.c2mon.server.common.alarm.Alarm;
import cern.c2mon.server.common.rule.RuleTag;
import cern.c2mon.server.common.rule.RuleTagCacheObject;
import cern.c2mon.server.common.tag.Tag;
import cern.c2mon.shared.common.datatag.TagQualityStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static cern.c2mon.shared.common.CacheEvent.*;

/**
 * @author Szymon Halastra, Alexandros Papageorgiou Koufidis
 */
@Slf4j
@Service
public class RuleTagService extends AbstractCacheServiceImpl<RuleTag> {

  private final RuleEvaluator ruleEvaluator;
  private C2monCache<Alarm> alarmCache;
  private AtomicInteger updateCount = new AtomicInteger(0);

  @Inject
  public RuleTagService(final C2monCache<RuleTag> ruleCacheRef, final RuleEvaluator ruleEvaluator, C2monCache<Alarm> alarmCache) {
    super(ruleCacheRef, new DefaultCacheFlow<>());
    this.ruleEvaluator = ruleEvaluator;
    this.alarmCache = alarmCache;
  }

  @PostConstruct
  public void init() {
    getCache().getCacheListenerManager().registerListener(ruleTag -> {
      alarmCache.removeAll(new HashSet<>(ruleTag.getAlarmIds()));
      // TODO (Alex) How should we add event listeners to removeAll as well to ensure proper delete cascading?
      cache.removeAll(new HashSet<>(ruleTag.getRuleIds()));
    }, REMOVED);

    getCache().getCacheListenerManager().registerListener(ruleTag -> ruleEvaluator.evaluateRule(ruleTag.getId()),
      INSERTED, UPDATE_ACCEPTED);
  }

  /**
   * Adds the rule to the list of those that need evaluating when
   * this tag is updated.
   * <p>
   * Note also adjust text field of cache object.
   */
  public void addDependentRuleToTag(final Tag tag, final Long ruleTagId) {
    cache.compute(tag.getId(), ruleTag -> {
      RuleTagCacheObject cacheRuleTag = (RuleTagCacheObject) ruleTag;
      cacheRuleTag.getRuleIds().add(ruleTagId);
      updateRuleIdsString(cacheRuleTag);
    });
  }

  /**
   * Removes this rule from the list of those that need evaluating when
   * this tag is updated.
   * <p>
   * Note also adjusts text field of cache object.
   *
   * @param tag       the tag used in the rule (directly, not via another rule)
   * @param ruleTagId the id of the rule
   */
  public void removeDependentRuleFromTag(final Tag tag, final Long ruleTagId) {
    // TODO (Alex) I think the ruleIds will be in the datatag, not the ruletag right?
    // TODO So we need the other cache here as well
    cache.compute(tag.getId(), ruleTag -> {
      RuleTagCacheObject cacheRuleTag = (RuleTagCacheObject) ruleTag;
      cacheRuleTag.getRuleIds().remove(ruleTagId);
      updateRuleIdsString(cacheRuleTag);
    });
  }

  private void updateRuleIdsString(RuleTagCacheObject cacheRuleTag) {
    StringBuilder bld = new StringBuilder();
    for (Long id : cacheRuleTag.getRuleIds()) {
      bld.append(id).append(",");
    }

    cacheRuleTag.setRuleIdsString(bld.toString());

    if (bld.length() > 0) {
      cacheRuleTag.setRuleIdsString(bld.toString().substring(0, bld.length() - 1)); //remove ", "
    }
  }

  public void setParentSupervisionIds(Long parentRuleId) {
    // TODO (Alex) Fill this in
  }

  public void updateAndValidate(final Long id, final Object value, final String valueDescription, final Timestamp timestamp) {
    if (!cache.containsKey(id))
      log.error("Unable to locate rule #{} in cache - no update performed.", id);

    cache.compute(id, ruleTag -> {
      if (!TagController.filterout(ruleTag, value, valueDescription, null, null)) {
        validateWithValue(value, valueDescription, timestamp, ruleTag);
      } else {
        log.trace("Filtering out repeated update for rule {}", id);
      }
    });
  }

  private void validateWithValue(Object value, String valueDescription, Timestamp timestamp, RuleTag ruleTag) {
    TagController.validate(ruleTag);
    TagController.setValue(ruleTag, value, valueDescription);
    ((RuleTagCacheObject) ruleTag).setEvalTimestamp(timestamp);
    updateCount.incrementAndGet();
    log((RuleTagCacheObject) ruleTag);
  }

  public void setQuality(final long id,
                         final Collection<TagQualityStatus> flagsToAdd,
                         final Collection<TagQualityStatus> flagsToRemove,
                         final Map<TagQualityStatus, String> qualityDescription,
                         final Timestamp timestamp) {
    cache.compute(id, ruleTag -> setQuality(ruleTag, flagsToAdd, flagsToRemove, qualityDescription, timestamp));
  }

  /**
   * Locking of the tag is handled within the public wrapper methods.
   */
  private void setQuality(final RuleTag tag,
                          final Collection<TagQualityStatus> flagsToAdd,
                          final Collection<TagQualityStatus> flagsToRemove,
                          final Map<TagQualityStatus, String> qualityDescription,
                          final Timestamp timestamp) {
    if (flagsToRemove == null && flagsToAdd == null) {
      log.warn("Attempting to set quality in TagFacade with no Quality flags to remove or set!");
    }

    if (flagsToRemove != null) {
      for (TagQualityStatus status : flagsToRemove) {
        tag.getDataTagQuality().removeInvalidStatus(status);
      }
    }
    if (flagsToAdd != null) {
      for (TagQualityStatus status : flagsToAdd) {
        tag.getDataTagQuality().addInvalidStatus(status, qualityDescription.get(status));
      }
    }
    ((RuleTagCacheObject) tag).setEvalTimestamp(timestamp);
  }

  /**
   * Logs the rule in the specific log4j log, using the log4j renderer in the configuration file
   * (done after every update).
   * @param ruleTagCacheObject the cache object to log
   */
  private void log(final RuleTagCacheObject ruleTagCacheObject) {
    if (log.isInfoEnabled()) {
      log.info(ruleTagCacheObject.toString());
    } else if (updateCount.get() % 10000 == 0) {
      log.warn("Total rule updates to the cache so far: " + updateCount);
    }
  }
}
