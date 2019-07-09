package cern.c2mon.server.cache.alivetimer;

import java.util.Optional;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.api.exception.CacheElementNotFoundException;
import cern.c2mon.server.common.alive.AliveTimer;

/**
 * @author Szymon Halastra
 */

@Slf4j
@Service
public class AliveTimerService {

  private C2monCache<AliveTimer> aliveTimerCacheRef;

//  @Autowired
//  public AliveTimerService(C2monCache<Long, AliveTimer> aliveTimerCacheRef) {
//    this.aliveTimerCacheRef = aliveTimerCacheRef;
//  }

  public C2monCache<AliveTimer> getCache() {
    return aliveTimerCacheRef;
  }

  public boolean isRegisteredAliveTimer(final Long id) {
    return aliveTimerCacheRef.containsKey(id);
  }

  public void update(final Long aliveId) {
    aliveTimerCacheRef.executeTransaction(() -> {
      try {
        AliveTimer aliveTimer = aliveTimerCacheRef.get(aliveId);
        update(aliveTimer);
        aliveTimerCacheRef.put(aliveId, aliveTimer);
      }
      catch (CacheElementNotFoundException cacheEx) {
        log.error("Cannot locate the AliveTimer in the cache (Id is " + aliveId + ") - unable to update it.", cacheEx);
      }
      catch (Exception e) {
        log.error("updatedAliveTimer() failed for an unknown reason: ", e);
      }

      return null;
    });
  }

  /**
   * Check whether this alive timer has expired.
   *
   * @return true if the alive timer is active and it has not been updated since
   * at least "aliveInterval" milliseconds.
   */
  public boolean hasExpired(final Long aliveTimerId) {
    Optional<Boolean> isExpired = aliveTimerCacheRef.executeTransaction(() -> {
      AliveTimer aliveTimer = aliveTimerCacheRef.get(aliveTimerId);
      return (System.currentTimeMillis() - aliveTimer.getLastUpdate() > aliveTimer.getAliveInterval() + aliveTimer.getAliveInterval() / 3);
    });

    return isExpired.get();
  }

  public void startAllTimers() {
    log.debug("Starting all alive timers in cache.");
    try {
      for (Long currentId : aliveTimerCacheRef.getKeys()) {
        start(currentId);
      }
    }
    catch (Exception e) {
      log.error("Unable to retrieve list of alive timers from cache when attempting to start the timers.", e);
    }
  }

  public void stopAllTimers() {
    log.debug("Stopping all alive timers in the cache.");
    try {
      for (Long currentId : aliveTimerCacheRef.getKeys()) {
        stop(currentId);
      }
    }
    catch (Exception e) {
      log.error("Unable to retrieve list of alive timers from cache when attempting to stop all timers.", e);
    }
  }

  public void start(Long id) {
    aliveTimerCacheRef.executeTransaction(() -> {
      try {
        AliveTimer aliveTimer = aliveTimerCacheRef.get(id);
        start(aliveTimer);
        aliveTimerCacheRef.put(id, aliveTimer);
      }
      catch (CacheElementNotFoundException cacheEx) {
        log.error("Cannot locate the AliveTimer in the cache (Id is " + id + ") - unable to start it.");
      }
      catch (Exception e) {
        log.error("Unable to start the alive timer " + id, e);
      }

      return null;
    });
  }

  public void stop(Long id) {
    aliveTimerCacheRef.executeTransaction(() -> {
      log.debug("Stopping alive timer " + id + " and dependent alive timers.");
      try {
        AliveTimer aliveTimer = aliveTimerCacheRef.get(id);
        stop(aliveTimer);

        aliveTimerCacheRef.put(id, aliveTimer);
      }
      catch (CacheElementNotFoundException cacheEx) {
        log.error("Cannot locate the AliveTimer in the cache (Id is " + id + ") - unable to stop it.");
      }
      catch (Exception e) {
        log.error("Unable to stop the alive timer " + id, e);
      }

      return null;
    });
  }

  /**
   * Update this alive timer. This method will reset the time of the last
   * update and thus relaunch the alive timer.
   */
  private void update(final AliveTimer aliveTimer) {
    // We only update the alive timer if the timestamp is >= the timestamp
    // of the last update. Otherwise, we ignore the update request and return
    // false
    // This is to avoid that alive timers that have been delayed on the network
    // start confusing the alive timer mechanism.


    aliveTimer.setActive(true);
    aliveTimer.setLastUpdate(System.currentTimeMillis());
    if (log.isDebugEnabled()) {
      StringBuffer str = new StringBuffer("Updated alive timer for ");
      str.append(AliveTimer.ALIVE_TYPE_PROCESS + " ");
      str.append(aliveTimer.getRelatedName());
      str.append(".");
      log.debug(str.toString());
    }
  }

  /**
   * Activate this alive timer.
   */
  private void start(final AliveTimer aliveTimer) {
    if (!aliveTimer.isActive()) {
      if (log.isDebugEnabled()) {
        StringBuffer str = new StringBuffer("start() : starting alive for ");
        str.append(AliveTimer.ALIVE_TYPE_PROCESS + " ");
        str.append(aliveTimer.getRelatedName());
        str.append(".");
        log.debug(str.toString());
      }
      aliveTimer.setActive(true);
      aliveTimer.setLastUpdate(System.currentTimeMillis());
    }
  }

  /**
   * Deactivate this alive timer if activated.
   */
  private void stop(final AliveTimer aliveTimer) {
    if (aliveTimer.isActive()) {
      if (log.isDebugEnabled()) {
        StringBuffer str = new StringBuffer("stop() : stopping alive for ");
        str.append(aliveTimer.getAliveTypeDescription() + " ");
        str.append(aliveTimer.getRelatedName());
        str.append(".");
        log.debug(str.toString());
      }
      aliveTimer.setActive(false);
      aliveTimer.setLastUpdate(System.currentTimeMillis());
    }
  }
}
