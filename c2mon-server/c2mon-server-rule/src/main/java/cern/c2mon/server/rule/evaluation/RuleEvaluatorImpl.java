/******************************************************************************
 * Copyright (C) 2010-2020 CERN. All rights not expressly granted are reserved.
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

import static cern.c2mon.shared.common.type.TypeConverter.getType;

import java.sql.Timestamp;
import java.util.*;
import java.util.Map.Entry;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

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
import cern.c2mon.shared.common.datatag.DataTagQuality;
import cern.c2mon.shared.common.datatag.DataTagQualityImpl;
import cern.c2mon.shared.common.datatag.util.TagQualityStatus;
import cern.c2mon.shared.common.rule.RuleInputValue;
import cern.c2mon.shared.daq.lifecycle.Lifecycle;
import cern.c2mon.shared.rule.RuleEvaluationException;

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
   * Triggers an evaluation of all rules that depend on the given tag.
   * 
   * @param tag the tag for which all depending rules shall be triggered.
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
   * the id does not belong to a rule a warning message is logged. 
   * 
   * @param pRuleId The id of a rule.
   */
  @Override
  public final void evaluateRule(final Long pRuleId) {
    log.trace("evaluateRule() called for #{}", pRuleId);

    // We synchronize on the rule reference object from the cache
    // in order to avoid simultaneous evaluations for the same rule
    if (ruleTagCache.isWriteLockedByCurrentThread(pRuleId)) {
        log.warn("Attention: I already have a write lock on rule {}", pRuleId);
    }
    ruleTagCache.acquireWriteLockOnKey(pRuleId);
    
    final Timestamp ruleResultTimestamp = new Timestamp(System.currentTimeMillis());

    try {
      RuleTag rule = ruleTagCache.get(pRuleId);

      if (rule.getRuleExpression() != null) {
        doEvaluateRule(rule, ruleResultTimestamp);
      } else {
        log.error("Unable to evaluate rule #{} as RuleExpression is null", pRuleId);
        ruleUpdateBuffer.invalidate(pRuleId, TagQualityStatus.UNINITIALISED, "Rule expression is empty. Please check the configuration.", ruleResultTimestamp);
      }
    } catch (CacheElementNotFoundException cacheEx) {
      log.error("Rule #{} not found in cache - unable to evaluate it.", pRuleId, cacheEx);
    } catch (Exception e) {
      log.error("Unexpected Error caught while retrieving #{} from rule cache.", pRuleId, e);
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
  
  
  /**
   * Performs the actual rule evaluation
   * @param rule The rule that need evaluating
   * @param ruleResultTimestamp The timestamp that shall be used for updating the rule cache.
   */
  private void doEvaluateRule(RuleTag rule, Timestamp ruleResultTimestamp) {
    Map<Long, Tag> tags = new HashMap<>();
    Class<?> ruleResultClass = getRuleResultClass(rule);
    
    try {
      // Can throw CacheElementNotFoundException
      tags = getRuleInputTags(rule);

      Object value = rule.getRuleExpression().evaluate(new HashMap<Long, RuleInputValue>(tags), ruleResultClass);
      ruleUpdateBuffer.update(rule.getId(), value, "Rule result", ruleResultTimestamp);
    
    } catch (CacheElementNotFoundException cacheEx) {
      ruleUpdateBuffer.invalidate(rule.getId(), TagQualityStatus.UNDEFINED_TAG,
          "Unable to evaluate rule as cannot find required Tag in cache: " + cacheEx.getMessage(), ruleResultTimestamp);
    } catch (RuleEvaluationException re) {
      log.trace("Problem evaluating expresion for rule #{} - Force rule evaluation and set invalid quality UNKNOWN_REASON ({})", rule.getId(), re.getMessage());
      
      DataTagQuality ruleQuality = getInvalidTagQuality(tags);
      Object value = rule.getRuleExpression().forceEvaluate(new Hashtable<Long, RuleInputValue>(tags), ruleResultClass);
      ruleUpdateBuffer.invalidate(rule.getId(), value, TagQualityStatus.UNKNOWN_REASON, ruleQuality.getDescription(), ruleResultTimestamp);
    } catch (Exception e) {
      log.error("Unexpected Error evaluating expresion of rule #{} - invalidating rule with quality UNKNOWN_REASON", rule.getId(), e);
      ruleUpdateBuffer.invalidate(rule.getId(), TagQualityStatus.UNKNOWN_REASON, e.getMessage(), ruleResultTimestamp);
    }
  }
  
  /**
   * Retrieves from the cache all required input tags to evaluate the given rule
   * @param rule The rule tag
   * @return Map of Tag objects with tag id as key
   */
  private Map<Long, Tag> getRuleInputTags(RuleTag rule) {
    final Set<Long> ruleInputTagIds = rule.getRuleExpression().getInputTagIds();

    // Retrieve all input tags for the rule
    final Map<Long, Tag> tags = new HashMap<>(ruleInputTagIds.size());

    Tag tag = null;
    for (Long inputTagId : ruleInputTagIds) {
      // We don't use a read lock here, because a tag change would anyway
      // result in another rule evaluation look for tag in datatag, rule and control caches
      try {
        tag = tagLocationService.get(inputTagId);
        tags.put(inputTagId, tag);
      } catch (CacheElementNotFoundException cacheEx) {
        log.warn("Failed to locate tag with id {} in any tag cache (during rule evaluation) - unable to evaluate rule.", rule.getId(), inputTagId, cacheEx);
        throw cacheEx;
      }
    }
    
    return tags;
  }
  
  private Class<?> getRuleResultClass(RuleTag rule) {
    Class<?> ruleResultClass = getType(rule.getDataType());
    if (ruleResultClass == null) {
      ruleResultClass = String.class;
    }
    
    return ruleResultClass;
  }


  /**
   * In case a tag is invalid, the invalidity can be a result of multiple
   * invalid tags that belong to the rule.
   * <p/>
   * IMPORTANT! <br/>
   * This should only be called if we know that the Rule is Invalid.
   *
   * This is because a rule can be VALID, even though it contains INVALID tags. In such a case
   * calling this method will give the wrong result.
   *
   * @param ruleInputValues the rule input values.
   * @return an overall Datatag quality from all the Tags belonging to this rule
   */
  private DataTagQuality getInvalidTagQuality(Map<Long, Tag> ruleInputValues) {

    DataTagQuality invalidRuleQuality = new DataTagQualityImpl();
    invalidRuleQuality.validate();

    for (Tag inputValue : ruleInputValues.values()) {
      // Check, if value tag is valid or not
      if (!inputValue.isValid()) {
        // Add Invalidations flags to the the rule
        Map<TagQualityStatus, String> qualityStatusMap = inputValue.getDataTagQuality().getInvalidQualityStates();
        for (Entry<TagQualityStatus, String> entry : qualityStatusMap.entrySet()) {
          invalidRuleQuality.addInvalidStatus(entry.getKey(), entry.getValue());
        }
      }
    }
    return invalidRuleQuality;
  }
}
