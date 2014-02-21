package cern.c2mon.server.rule.evaluation;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Service;

import cern.c2mon.server.cache.CacheRegistrationService;
import cern.c2mon.server.cache.RuleTagCache;
import cern.c2mon.server.cache.TagLocationService;
import cern.c2mon.server.cache.C2monCacheListener;
import cern.c2mon.server.cache.exception.CacheElementNotFoundException;
import cern.c2mon.server.common.component.Lifecycle;
import cern.c2mon.server.common.config.ServerConstants;
import cern.c2mon.server.common.rule.RuleTag;
import cern.c2mon.server.common.tag.Tag;
import cern.c2mon.server.rule.RuleEvaluator;
import cern.c2mon.shared.common.datatag.TagQualityStatus;
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
@Service
public class RuleEvaluatorImpl implements C2monCacheListener<Tag>, SmartLifecycle, RuleEvaluator {

  private static final Logger LOGGER = Logger.getLogger(RuleEvaluatorImpl.class); 
  
  private final RuleTagCache ruleTagCache;
  
  /** This temporary buffer is used to filter out intermediate rule evaluation results. */
  private final RuleUpdateBuffer ruleUpdateBuffer;
  
  private final TagLocationService tagLocationService;
  
  private final CacheRegistrationService cacheRegistrationService;

  /**
   * Listener container lifecycle hook.
   */
  private Lifecycle listenerContainer;
  
  /**
   * Lifecycle flag.
   */
  private volatile boolean running = false;
  
  /**
   * Constructor.
   * @param ruleTagCache
   * @param ruleTagFacade
   * @param ruleUpdateBuffer
   * @param tagLocationService
   */
  @Autowired
  public RuleEvaluatorImpl(RuleTagCache ruleTagCache, RuleUpdateBuffer ruleUpdateBuffer, TagLocationService tagLocationService, CacheRegistrationService cacheRegistrationService) {
    super();
    this.ruleTagCache = ruleTagCache;
    this.ruleUpdateBuffer = ruleUpdateBuffer;
    this.tagLocationService = tagLocationService;
    this.cacheRegistrationService = cacheRegistrationService;
  }
  
  /**
   * Registers to tag caches.
   */
  @PostConstruct
  public void init() {   
    listenerContainer = cacheRegistrationService.registerToAllTags(this, 2);
  }

  @Override
  public void notifyElementUpdated(Tag tag) {    
    try {
      evaluateRules(tag);
    } catch (Exception e) {
      LOGGER.error("Error caught when evaluating rules dependent on Tag " + tag.getId() + "(these are " + tag.getRuleIds() + ")", e);
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
    //TODO no synch here, since no harm if rule is removed or added ?
    Iterator<Long> rulesIterator = tag.getRuleIds().iterator(); // Rule Ids Collection 
    // For each rule id related to the tag
    while (rulesIterator.hasNext()) {
       evaluateRule((Long) rulesIterator.next());
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
    if (LOGGER.isTraceEnabled()) {
      StringBuffer str = new StringBuffer("evaluateRule(");
      str.append(pRuleId);
      str.append(") called.");
      LOGGER.trace(str);
    }

    final Timestamp ruleResultTimestamp = new Timestamp(System.currentTimeMillis());

    // We synchronize on the rule reference object from the cache
    // in order to avoid simultaneous evaluations for the same rule
    ruleTagCache.acquireWriteLockOnKey(pRuleId);
    try {
      RuleTag rule = ruleTagCache.get(pRuleId);
      if (rule.getRuleExpression() != null) {
        final Collection<Long> ruleInputTagIds = rule.getRuleExpression().getInputTagIds();
        // Retrieve all input tags for the rule
        final Map<Long, Object> tags = new Hashtable<Long, Object>(ruleInputTagIds.size());

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
          Class<?> ruleResultClass = Class.forName("java.lang." + rule.getDataType());

          Object value = rule.getRuleExpression().evaluate(tags, ruleResultClass);
          ruleUpdateBuffer.update(pRuleId, value, "Rule result", ruleResultTimestamp);
        } catch (CacheElementNotFoundException cacheEx) {
          LOGGER.warn("evaluateRule - Failed to locate tag with id " + actualTag + " in any tag cache (during rule evaluation) - unable to evaluate rule.",
              cacheEx);
          ruleUpdateBuffer.invalidate(pRuleId, TagQualityStatus.UNKNOWN_REASON,
              "Unable to evaluate rule as cannot find required Tag in cache: " + cacheEx.getMessage(), ruleResultTimestamp);
        } catch (RuleEvaluationException re) {
          // TODO change in rule engine: this should NOT be done using an
          // exception since it is normal behavior switched to trace
          LOGGER.trace("evaluateRule - Problem evaluating expresion for rule with Id (" + pRuleId + ") - invalidating rule with quality UNKNOWN_REASON ("
              + re.getMessage() + ").");
          // switched from INACCESSIBLE in old code
          ruleUpdateBuffer.invalidate(pRuleId, TagQualityStatus.UNKNOWN_REASON, re.getMessage(), ruleResultTimestamp);
        } catch (Exception e) {
          LOGGER.error(
              "evaluateRule - Unexpected Error evaluating expresion of rule with Id (" + pRuleId + ") - invalidating rule with quality UNKNOWN_REASON", e);
          // switched from INACCESSIBLE in old code
          ruleUpdateBuffer.invalidate(pRuleId, TagQualityStatus.UNKNOWN_REASON, e.getMessage(), ruleResultTimestamp);
        }
      } else {
        LOGGER.error("evaluateRule - Unable to evaluate rule with Id (" + pRuleId + ") as RuleExpression is null.");
      }
    } catch (CacheElementNotFoundException cacheEx) {
      LOGGER.error("evaluateRule - Rule with id " + pRuleId + " not found in cache - unable to evaluate it.", cacheEx);
    } catch (Exception e) {
      LOGGER.error("evaluateRule - Unexpected Error caught while retrieving " + pRuleId + " from rule cache.", e);
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
    return false;
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
    LOGGER.debug("Starting rule evaluator");
    running = true;
    listenerContainer.start();
  }

  @Override
  public void stop() {
    LOGGER.debug("Stopping rule evaluator");
    listenerContainer.stop();
    running = false;    
  }

  @Override
  public int getPhase() {
    return ServerConstants.PHASE_INTERMEDIATE;    
  }
}
