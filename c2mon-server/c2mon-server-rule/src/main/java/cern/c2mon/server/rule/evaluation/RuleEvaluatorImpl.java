/******************************************************************************
 * Copyright (C) 2010-2018 CERN. All rights not expressly granted are reserved.
 *
 * This file is part of the CERN Control and Monitoring Platform 'C2MON'.
 * C2MON is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the license.
 *
 * C2MON is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
 * more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with C2MON. If not, see <http://www.gnu.org/licenses/>.
 *****************************************************************************/
package cern.c2mon.server.rule.evaluation;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Service;

import cern.c2mon.server.cache.C2monCacheListener;
import cern.c2mon.server.cache.CacheRegistrationService;
import cern.c2mon.server.cache.RuleTagCache;
import cern.c2mon.server.cache.TagLocationService;
import cern.c2mon.server.cache.exception.CacheElementNotFoundException;
import cern.c2mon.server.common.config.ServerConstants;
import cern.c2mon.server.common.rule.RuleTag;
import cern.c2mon.server.common.tag.Tag;
import cern.c2mon.server.rule.RuleEvaluator;
import cern.c2mon.server.rule.config.RuleProperties;
import cern.c2mon.shared.common.datatag.TagQualityStatus;
import cern.c2mon.shared.daq.lifecycle.Lifecycle;
import cern.c2mon.shared.rule.RuleEvaluationException;

import static cern.c2mon.shared.common.type.TypeConverter.getType;

/**
 * Contains evaluate methods wrapping calls to the rule engine.
 * This class contains the logic of locating the required rule
 * input tags in the cache. The result of the evaluation is passed
 * to the RuleUpdateBuffer where rapid successive updates are
 * clustered into a single update.
 *
 * @author mbrightw
 *
 */
@Slf4j
@Service
public class RuleEvaluatorImpl implements C2monCacheListener<Tag>, SmartLifecycle, RuleEvaluator {

  private final RuleTagCache ruleTagCache;

  /** This temporary buffer is used to filter out intermediate rule evaluation results. */
  private final RuleUpdateBuffer ruleUpdateBuffer;

  private final TagLocationService tagLocationService;

  private final CacheRegistrationService cacheRegistrationService;

  private final RuleProperties properties;

  /**
   * Listener container lifecycle hook.
   */
  private Lifecycle listenerContainer;

  /**
   * Lifecycle flag.
   */
  private volatile boolean running = false;

  @Autowired
  public RuleEvaluatorImpl(RuleTagCache ruleTagCache,
                           RuleUpdateBuffer ruleUpdateBuffer,
                           TagLocationService tagLocationService,
                           CacheRegistrationService cacheRegistrationService,
                           RuleProperties properties) {
    super();
    this.ruleTagCache = ruleTagCache;
    this.ruleUpdateBuffer = ruleUpdateBuffer;
    this.tagLocationService = tagLocationService;
    this.cacheRegistrationService = cacheRegistrationService;
    this.properties = properties;
  }

  /**
   * Registers to tag caches.
   */
  @PostConstruct
  public void init() {
    listenerContainer = cacheRegistrationService.registerToAllTags(this, properties.getNumEvaluationThreads());
  }

  @Override
  public void notifyElementUpdated(Tag tag) {
    try {
      evaluateRules(tag);
    } catch (Exception e) {
      log.error("Error caught when evaluating dependend rules ({}) of #{}", tag.getRuleIds(), tag.getId(), e);
    }
  }

  /**
   * TODO rewrite javadoc
   * NB:
   * <UL>
   * <LI>This method DOES NOT CHECK whether the tag passed as a parameter is null.
   * The caller has to ensure that this method is always called with a
   * non-null parameter.
   * <LI>This method ASSUMES that the tag.getRuleIds() never returns null. This is
   * to be ensured by the DataTagCacheObject
   * </UL>
   *
   * evaluates rules that depend on tag
   */
  public void evaluateRules(final Tag tag) {
    // For each rule id related to the tag
    if (!tag.getRuleIds().isEmpty()) {
      log.trace("For rule #{} triggering re-evaluation of {} rules : {}", tag.getId(), tag.getRuleIds().size(), tag.getRuleIds());
      for (Long ruleId : tag.getRuleIds()) {
         evaluateRule(ruleId);
      }
    }
  }

  /**
   * Performs the rule evaluation for a given tag id. In case that
   * the id does not belong to a rule a warning message is logged to
   * log4j. Please note, that the rule will always use the time stamp
   * of the latest incoming data tag update.
   * @param pRuleId The id of a rule.
   */
  @Override
  public final void evaluateRule(final Long pRuleId) {
    log.trace("evaluateRule() called for #{}", pRuleId);

    final Timestamp ruleResultTimestamp = new Timestamp(System.currentTimeMillis());

    // We synchronize on the rule reference object from the cache
    // in order to avoid simultaneous evaluations for the same rule

    if (ruleTagCache.isWriteLockedByCurrentThread(pRuleId)) {
        log.warn("Attention: I already have a write lock on rule {}", pRuleId);
    }

    ruleTagCache.acquireWriteLockOnKey(pRuleId);

    try {
      RuleTag rule = ruleTagCache.get(pRuleId);

      if (rule.getRuleExpression() != null) {
        final Collection<Long> ruleInputTagIds = rule.getRuleExpression().getInputTagIds();

        // Retrieve all input tags for the rule
        final Map<Long, Object> tags = new HashMap<>(ruleInputTagIds.size());

        Tag tag = null;
        Long actualTag = null;
        try {
          for (Long inputTagId : ruleInputTagIds) {
            actualTag = inputTagId;
            // We don't use a read lock here, because a tag change would anyway
            // result in another rule evaluation
            // look for tag in datatag, rule and control caches
            tag = tagLocationService.get(inputTagId);

            // put reference to cache object in map
            tags.put(inputTagId, tag);
          }

          // Retrieve class type of resulting value, in order to cast correctly
          // the evaluation result
          Class<?> ruleResultClass = getType(rule.getDataType());

          Object value = rule.getRuleExpression().evaluate(tags, ruleResultClass);
          ruleUpdateBuffer.update(pRuleId, value, "Rule result", ruleResultTimestamp);
        } catch (CacheElementNotFoundException cacheEx) {
          log.warn("evaluateRule #{} - Failed to locate tag with id {} in any tag cache (during rule evaluation) - unable to evaluate rule.", pRuleId, actualTag, cacheEx);
          ruleUpdateBuffer.invalidate(pRuleId, TagQualityStatus.UNKNOWN_REASON,
              "Unable to evaluate rule as cannot find required Tag in cache: " + cacheEx.getMessage(), ruleResultTimestamp);
        } catch (RuleEvaluationException re) {
          // TODO change in rule engine: this should NOT be done using an
          // exception since it is normal behavior switched to trace
          log.trace("Problem evaluating expresion for rule #{} - invalidating rule with quality UNKNOWN_REASON ({})", pRuleId, re.getMessage());
          // switched from INACCESSIBLE in old code
          ruleUpdateBuffer.invalidate(pRuleId, TagQualityStatus.UNKNOWN_REASON, re.getMessage(), ruleResultTimestamp);
        } catch (Exception e) {
          log.error("Unexpected Error evaluating expresion of rule #{} - invalidating rule with quality UNKNOWN_REASON", pRuleId, e);
          // switched from INACCESSIBLE in old code
          ruleUpdateBuffer.invalidate(pRuleId, TagQualityStatus.UNKNOWN_REASON, e.getMessage(), ruleResultTimestamp);
        }
      } else {
        log.error("Unable to evaluate rule #{} as RuleExpression is null", pRuleId);
      }
    } catch (CacheElementNotFoundException cacheEx) {
      log.error("Rule #{} not found in cache - unable to evaluate it.", pRuleId, cacheEx);
    } catch (Exception e) {
      log.error("Unexpected Error caught while retrieving #{} from rule cache.", pRuleId, e);
      // switched from INACCESSIBLE in old code
      ruleUpdateBuffer.invalidate(pRuleId, TagQualityStatus.UNKNOWN_REASON, e.getMessage(), ruleResultTimestamp);
    } finally {
      ruleTagCache.releaseWriteLockOnKey(pRuleId);
    }
  }

  /**
   * Will evaluate the rule and put in cache (listeners will get update notification).
   */
  @Override
  public void confirmStatus(Tag tag) {
    notifyElementUpdated(tag);
  }

  @Override
  public boolean isAutoStartup() {
    return true;
  }

  @Override
  public void stop(Runnable runnable) {
    stop();
    runnable.run();
  }

  @Override
  public boolean isRunning() {
    return running;
  }

  @Override
  public void start() {
    log.debug("Starting rule evaluator");
    running = true;
    listenerContainer.start();
  }

  @Override
  public void stop() {
    log.debug("Stopping rule evaluator");
    listenerContainer.stop();
    running = false;
  }

  @Override
  public int getPhase() {
    return ServerConstants.PHASE_INTERMEDIATE;
  }
}
