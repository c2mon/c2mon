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

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.api.exception.CacheElementNotFoundException;
import cern.c2mon.cache.config.collections.TagCacheCollection;
import cern.c2mon.server.common.rule.RuleTag;
import cern.c2mon.server.common.tag.Tag;
import cern.c2mon.server.rule.RuleEvaluator;
import cern.c2mon.server.rule.config.RuleProperties;
import cern.c2mon.shared.common.CacheEvent;
import cern.c2mon.shared.common.datatag.DataTagQuality;
import cern.c2mon.shared.common.datatag.DataTagQualityImpl;
import cern.c2mon.shared.common.datatag.TagQualityStatus;
import cern.c2mon.shared.common.rule.RuleInputValue;
import cern.c2mon.shared.rule.RuleEvaluationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import static cern.c2mon.shared.common.type.TypeConverter.getType;

/**
 * Contains evaluate methods wrapping calls to the rule engine.
 * This class contains the logic of locating the required rule
 * input tags in the cache. The result of the evaluation is passed
 * to the RuleUpdateBuffer where rapid successive updates are
 * clustered into a single update.
 *
 * @author mbrightw
 */
@Slf4j
@Service
public class RuleEvaluatorImpl implements RuleEvaluator {

    private final C2monCache<RuleTag> ruleTagCache;

    /**
     * This temporary buffer is used to filter out intermediate rule evaluation results.
     */
    private final RuleUpdateBuffer ruleUpdateBuffer;

    private final TagCacheCollection unifiedTagCacheFacade;

    private final RuleProperties properties;

    @Autowired
    public RuleEvaluatorImpl(C2monCache<RuleTag> ruleTagCache,
                             RuleUpdateBuffer ruleUpdateBuffer,
                             TagCacheCollection unifiedTagCacheFacade,
                             RuleProperties properties) {
        super();
        this.ruleTagCache = ruleTagCache;
        this.ruleUpdateBuffer = ruleUpdateBuffer;
        this.unifiedTagCacheFacade = unifiedTagCacheFacade;
        this.properties = properties;
    }

    /**
     * Registers to tag caches.
     */
    @PostConstruct
    public void init() {
        unifiedTagCacheFacade.registerListener(tag -> {
            try {
                evaluateRules(tag);
            } catch (Exception e) {
                log.error("Error caught when evaluating dependent rules ({}) of #{}", tag.getRuleIds(), tag.getId(), e);
            }
        }, CacheEvent.UPDATE_ACCEPTED, CacheEvent.CONFIRM_STATUS);
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

        final Timestamp ruleResultTimestamp = new Timestamp(System.currentTimeMillis());

        try {
            ruleTagCache.compute(pRuleId, rule -> {
                if (rule.getRuleExpression() != null) {
                    doEvaluateRule(pRuleId, ruleResultTimestamp, rule);
                } else {
                    log.error("Unable to evaluate rule #{} as RuleExpression is null", pRuleId);
                    ruleUpdateBuffer.invalidate(pRuleId, TagQualityStatus.UNINITIALISED, "Rule expression is empty. Please check the configuration.", ruleResultTimestamp);
                }
            });
        } catch (CacheElementNotFoundException cacheEx) {
            log.error("Rule #{} not found in cache - unable to evaluate it.", pRuleId, cacheEx);
        } catch (Exception e) {
            log.error("Unexpected Error caught while retrieving #{} from rule cache.", pRuleId, e);
            ruleUpdateBuffer.invalidate(pRuleId, TagQualityStatus.UNKNOWN_REASON, e.getMessage(), ruleResultTimestamp);
        }
    }

    /**
     * Performs the actual rule evaluation
     *
     * @param pRuleId             The id of a rule.
     * @param ruleResultTimestamp The timestamp that shall be used for updating the rule cache.
     * @param rule                The rule that need evaluating
     */
    private void doEvaluateRule(Long pRuleId, Timestamp ruleResultTimestamp, RuleTag rule) {

        Map<Long, Tag> tags = new HashMap<>();

        // Retrieve class type of resulting value, in order to cast correctly
        // the evaluation result
        Class<?> ruleResultClass = getRuleResultClass(rule);

        try {
            // Can throw CacheElementNotFoundException
            tags = getRuleInputTags(rule);

            Object value = rule.getRuleExpression().evaluate(new HashMap<Long, RuleInputValue>(tags), ruleResultClass);
            ruleUpdateBuffer.update(pRuleId, value, "Rule result", ruleResultTimestamp);
        } catch (CacheElementNotFoundException cacheEx) {
            ruleUpdateBuffer.invalidate(pRuleId, TagQualityStatus.UNDEFINED_TAG,
                    "Unable to evaluate rule as cannot find required Tag in cache: " + cacheEx.getMessage(), ruleResultTimestamp);
        } catch (RuleEvaluationException re) {
            log.trace("Problem evaluating expresion for rule #{} - Force rule evaluation and set invalid quality UNKNOWN_REASON ({})", rule.getId(), re.getMessage());

            DataTagQuality ruleQuality = getInvalidTagQuality(tags);
            rule.getRuleExpression().forceEvaluate(new Hashtable<Long, RuleInputValue>(tags), ruleResultClass);
            ruleUpdateBuffer.invalidate(rule.getId(), TagQualityStatus.UNKNOWN_REASON, ruleQuality.getDescription(), ruleResultTimestamp);

        } catch (Exception e) {
            log.error("Unexpected Error evaluating expresion of rule #{} - invalidating rule with quality UNKNOWN_REASON", pRuleId, e);
            ruleUpdateBuffer.invalidate(pRuleId, TagQualityStatus.UNKNOWN_REASON, e.getMessage(), ruleResultTimestamp);
        }
    }

    /**
     * Retrieves from the cache all required input tags to evaluate the given rule
     *
     * @param rule The rule tag
     * @return Map of Tag objects with tag id as key
     */
    private Map<Long, Tag> getRuleInputTags(RuleTag rule) {
        final Set<Long> ruleInputTagIds = rule.getRuleExpression().getInputTagIds();

        // Retrieve all input tags for the rule
        final Map<Long, Tag> tags = new HashMap<>(ruleInputTagIds.size());
        Tag tag = null;
        Long actualTag = null;
        for (Long inputTagId : ruleInputTagIds) {
            actualTag = inputTagId;
            // We don't use a read lock here, because a tag change would anyway
            // result in another rule evaluation
            // look for tag in datatag, rule and control caches
            tag = unifiedTagCacheFacade.get(inputTagId);

            // put reference to cache object in map
            tags.put(inputTagId, tag);
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
     * <p>
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
                for (Map.Entry<TagQualityStatus, String> entry : qualityStatusMap.entrySet()) {
                    invalidRuleQuality.addInvalidStatus(entry.getKey(), entry.getValue());
                }
            }
        }
        return invalidRuleQuality;
    }

}
