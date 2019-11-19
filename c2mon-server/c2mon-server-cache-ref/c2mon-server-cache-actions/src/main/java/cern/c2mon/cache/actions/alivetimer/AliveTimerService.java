package cern.c2mon.cache.actions.alivetimer;

import cern.c2mon.cache.actions.AbstractCacheServiceImpl;
import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.api.exception.CacheElementNotFoundException;
import cern.c2mon.server.common.alive.AliveTimer;
import cern.c2mon.server.common.alive.AliveTimerCacheObject;
import cern.c2mon.server.common.supervision.Supervised;
import cern.c2mon.shared.common.supervision.SupervisionConstants;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

import static cern.c2mon.shared.common.supervision.SupervisionConstants.SupervisionEntity.*;

/**
 * Manages operations on {@link AliveTimerCacheObject}s
 *
 * @author Szymon Halastra, Alexandros Papageorgiou Koufidis
 * @see AliveTimer
 */
@Slf4j
@Service
public class AliveTimerService extends AbstractCacheServiceImpl<AliveTimer> {

  @Inject
  public AliveTimerService(C2monCache<AliveTimer> aliveTimerCacheRef) {
    super(aliveTimerCacheRef, new AliveTimerC2monCacheFlow());
  }

  public boolean isRegisteredAliveTimer(final Long id) {
    return cache.containsKey(id);
  }

  /**
   * Same as {@link AliveTimerService#start(Long)}, but will start the object
   * regardless of previous state (active or not)
   */
  public void startOrUpdateTimestamp(@NonNull Long aliveTimerId) throws NullPointerException {
    setAliveTimerAsActive(aliveTimerId, true, true);
  }

  /**
   * Find the {@code AliveTimer} object with {@code aliveTimerId} in the cache
   * and if it is stopped (not active), then do
   *
   * <ul>
   *   <li>{@code AliveTimer#setActive(true)}
   *   <li>{@code AliveTimer#setLastUpdate(now)}
   *   <li>Reinsert into cache
   * </ul>
   * <p>
   * The timestamp will not be updated, unless there is a change.
   * The cache object will not be reinserted, unless there is a change.
   *
   * @param aliveTimerId the alive timer id for the object to be force started
   * @throws NullPointerException when {@code aliveTimerId} is null
   */
  public void start(@NonNull Long aliveTimerId) throws NullPointerException {
    setAliveTimerAsActive(aliveTimerId, true, false);
  }

  /**
   * Find the {@code AliveTimer} object with {@code aliveTimerId} in the cache
   * and if it is started (active), then do
   *
   * <ul>
   *   <li>{@code AliveTimer#setActive(false)}
   *   <li>{@code AliveTimer#setLastUpdate(now)}
   *   <li>Reinsert into cache
   * </ul>
   * <p>
   * The timestamp will not be updated, unless there is a change.
   * The cache object will not be reinserted, unless there is a change.
   *
   * @param aliveTimerId the alive timer id for the object to be force started
   * @throws NullPointerException when {@code aliveTimerId} is null
   */
  public void stop(@NonNull Long aliveTimerId) throws NullPointerException {
    setAliveTimerAsActive(aliveTimerId, false, false);
  }

  /**
   * Check whether this alive timer has expired. Adds an additional time buffer
   * as some clocks may be slightly out of sync.
   *
   * @return true if the alive timer is active and it has not been updated since
   * at least "aliveInterval" milliseconds.
   */
  public boolean hasExpired(final Long aliveTimerId) {
    AliveTimer aliveTimer = cache.get(aliveTimerId);
    return (System.currentTimeMillis() - aliveTimer.getLastUpdate() > aliveTimer.getAliveInterval() + aliveTimer.getAliveInterval() / 3);
  }

  /**
   * Will set all previously inactive {@link AliveTimer}s as active
   * <p>
   * Timestamps will not be affected on previously active {@code AliveTimer}s
   */
  public void startAllInactiveTimers() {
    log.debug("Starting all alive timers in the cache.");
    filterAndSetActive(true);
  }

  /**
   * Will set all previously inactive {@link AliveTimer}s as inactive (stopped)
   * <p>
   * Timestamps will not be affected on previously inactive {@code AliveTimer}s
   */
  public void stopAllActiveTimers() {
    log.debug("Stopping all alive timers in the cache.");
    filterAndSetActive(false);
  }

  /**
   * Stops and removes this alive by alive id. Should only be
   * used when it is no longer reference by a supervised object
   * (for instance on reconfiguration error recovery).
   *
   * @param aliveId id of the alive
   */
  public void removeAliveTimer(long aliveId) {
    stop(aliveId);
    cache.remove(aliveId);
  }

  public void createAliveTimerFor(Supervised supervised) {
    AliveTimerCacheObject aliveTimer = new AliveTimerCacheObject(supervised.getAliveTagId(), supervised.getId(), supervised.getName(),
      supervised.getStateTagId(), "", supervised.getAliveInterval());
    setAliveTimerType(supervised.getSupervisionEntity(), aliveTimer);
    cache.put(aliveTimer.getId(), aliveTimer);
  }

  private void setAliveTimerType(SupervisionConstants.SupervisionEntity supervisionEntity, AliveTimerCacheObject aliveTimer){
    if (supervisionEntity == PROCESS)
      aliveTimer.setAliveType(AliveTimer.ALIVE_TYPE_PROCESS);
    else if (supervisionEntity == EQUIPMENT)
      aliveTimer.setAliveType(AliveTimer.ALIVE_TYPE_EQUIPMENT);
    else if (supervisionEntity == SUBEQUIPMENT)
      aliveTimer.setAliveType(AliveTimer.ALIVE_TYPE_SUBEQUIPMENT);
  }

  private void filterAndSetActive(boolean active) {
    try {
      for (AliveTimer aliveTimer : cache.query(aliveTimer -> aliveTimer.isActive() != active)) {
        log.debug("Attempting to set alive timer " + aliveTimer.getId() + " and dependent alive timers to " + active);
        aliveTimer.setActive(active);
        aliveTimer.setLastUpdate(System.currentTimeMillis());
        cache.put(aliveTimer.getId(), aliveTimer);
      }
    } catch (Exception e) {
      log.error("Unable to retrieve list of alive timers from cache when attempting to set new active status to " + active, e);
    }
  }

  private void setAliveTimerAsActive(long aliveTimerId, boolean active, boolean forceTimestampUpdate) {
    log.debug("Attempting to set alive timer " + aliveTimerId + " and dependent alive timers to " + active);

    try {
      cache.compute(aliveTimerId, aliveTimer -> {
        if (forceTimestampUpdate || aliveTimer.setActive(active))
          aliveTimer.setLastUpdate(System.currentTimeMillis());
      });
    } catch (CacheElementNotFoundException cacheEx) {
      log.error("Cannot locate the AliveTimer in the cache (Id is " + aliveTimerId + ") - unable to stop it.");
    } catch (Exception e) {
      log.error("Unable to stop the alive timer " + aliveTimerId, e);
    }
  }
}
