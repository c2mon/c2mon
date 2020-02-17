package cern.c2mon.server.rule.evaluation;

import cern.c2mon.cache.actions.tag.TagController;
import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.api.exception.CacheElementNotFoundException;
import cern.c2mon.server.common.rule.RuleTag;
import cern.c2mon.server.common.rule.RuleTagCacheObject;
import cern.c2mon.shared.common.datatag.TagQualityStatus;
import lombok.extern.slf4j.Slf4j;

import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static cern.c2mon.server.rule.evaluation.RuleUpdateBuffer.INITIAL_BUFFER_SIZE;

/**
 * This class extends the Java <code>TimerTask</code> and
 * takes care of updating the oc4j cache via the <code>DataTagFacadeBean</code>.
 *
 * @author Matthias Braeger
 */
@Slf4j
class CacheUpdaterTask extends TimerTask {
  
  /**
   * The counters for checking the cycles that a specific rule is already been buffered.
   * When the counter exceeds the MAX_CYCLES_WAIT the <code>CacheUpdaterTask</code> forces
   * a cache update.
   */
  static final Map<Long, Integer> CYCLE_COUNTERS = new Hashtable<>(INITIAL_BUFFER_SIZE);


  /**
   * The maximum amount of check cycles that the cache updater shall 
   * wait before forcing a cache update for a particular rule.
   */
  private static final int MAX_CYCLES_WAIT = 6; // 6 * 75 = 450 ms <== max delay

  /** will be set to true, once the timer is running */
  static boolean isCacheUpdaterRunning = false;

  private final C2monCache<RuleTag> cache;
  private static final AtomicInteger updateCount = new AtomicInteger(0);

  public CacheUpdaterTask(C2monCache<RuleTag> cache) {
    this.cache = cache;
  }

  /**
   * This method is executed when it gets triggered
   * by its <code>Timer</code> instance. It then takes
   * care of the cache update for all rules that haven't
   * been updated since the last check.
   */
  public void run() {
    //keep logic in try clause as exception will kill the timer thread here
    try {
      Collection<RuleBufferObject> rulesToUpdate = null;

      // create first a copy of all rule objects that needs to be updated
      synchronized (RuleUpdateBuffer.BUFFER_LOCK) {
        // List of rules where the cache shall be updated
        rulesToUpdate = new ArrayList<RuleBufferObject>();
        // actual cycle counter
        Integer actCounter = null;
        // flag indicating, if the actual rule was updated since the last check
        boolean hasJustBeenUpdated = false;
        // true, if a cache update shall be forced due to an exceed of the MAX_CYCLE_WAIT
        boolean forceCacheUpdate = false;
        for (Map.Entry<Long, Boolean> entry : RuleUpdateBuffer.UPDATE_RECEIVED_FLAGS.entrySet()) {
          Long actTagId = entry.getKey();
          hasJustBeenUpdated = entry.getValue();
          actCounter = CYCLE_COUNTERS.get(actTagId);
          forceCacheUpdate = (actCounter != null && actCounter >= MAX_CYCLES_WAIT);

          if (!hasJustBeenUpdated || forceCacheUpdate) {
            // False ==> then we update the cache since there was no recent update of that rule
            // OR we the buffer was updated more than MAX_CYCLES_WAIT cycle in a row ==> force update
            RuleBufferObject rbo = new RuleBufferObject(RuleUpdateBuffer.RULE_OBJECT_BUF.get(actTagId));
            rulesToUpdate.add(rbo);
            if (forceCacheUpdate) {
              log.debug("CacheUpdaterTask() - Forcing a cache update for rule "
                  + actTagId + " since it was already delayed by "
                  + MAX_CYCLES_WAIT * RuleUpdateBuffer.BUFFER_TIMER_MILLIS + " ms.");
            }
          }
          else {
            // Set the flag to FALSE in order indicate a cache update at the next check
            RuleUpdateBuffer.UPDATE_RECEIVED_FLAGS.put(actTagId, Boolean.FALSE);
            if (actCounter == null) {
              CYCLE_COUNTERS.put(actTagId, 1);
            }
            else { // Increasing the counter
              CYCLE_COUNTERS.put(actTagId, actCounter + 1);
            }
          }
        }

        // Cleaning the buffer from all objects that are going to be put into the cache.
        Long actTagId;
        for (RuleBufferObject rbo : rulesToUpdate) {
          actTagId = rbo.getId();
          RuleUpdateBuffer.UPDATE_RECEIVED_FLAGS.remove(actTagId);
          RuleUpdateBuffer.RULE_OBJECT_BUF.remove(actTagId);
          CYCLE_COUNTERS.remove(actTagId);
        }

        if (RuleUpdateBuffer.UPDATE_RECEIVED_FLAGS.size() == 0) {
          log.trace("CacheUpdaterTask() - Canceling next cache updater check, because there are no more updates registered.");
          this.cancel();
          isCacheUpdaterRunning = false;
        }
      } // end of synchronization

      if (rulesToUpdate.size() > 0) {
        // Updating the cache
        for (RuleBufferObject rbo : rulesToUpdate) {
          if (rbo.getQualityCollection().isEmpty()) {
            log.trace("CacheUpdaterTask() - updating cache for rule id " + rbo.getId()
                + ": value=" + rbo.getValue()
                + ", description=" + rbo.getValueDesc()
                + ", timestamp=" + rbo.getTimestamp());
            try {
              updateAndValidate(rbo.getId(), rbo.getValue(), rbo.getValueDesc(), rbo.getTimestamp());
            } catch (CacheElementNotFoundException cacheEx) {
              log.warn("Unable to update rule (can happen during rule reconfiguration)", cacheEx);
            } catch (Exception exception) {
              log.warn("Unexpected error during rule evaluation", exception);
            }
          } else {
            log.trace("CacheUpdaterTask() - invalidating cache for rule id " + rbo.getId()
                + ": reasons=" + rbo.getQualityCollection()
                + ", descriptions=" + rbo.getQualityDescriptions()
                + ", timestamp=" + rbo.getTimestamp());
            try {
              setQuality(rbo.getId(), rbo.getQualityCollection(), null, rbo.getQualityDescriptions(), rbo.getTimestamp());
            } catch (CacheElementNotFoundException cacheEx) {
              log.warn("Unable to update rule as could not be located in cache (normal during rule reconfiguration)", cacheEx);
            }
          }
        } // end for
      }
    } catch (Exception ex) {
      log.error("Exception caught during rule update - should not be ignored!", ex);
    }
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
