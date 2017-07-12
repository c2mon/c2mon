package cern.c2mon.server.jcacheref.prototype.alive;

import java.io.Serializable;
import java.util.Iterator;

import javax.cache.Cache;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cern.c2mon.server.common.alive.AliveTimer;

/**
 * @author Szymon Halastra
 */

@Slf4j
@Service
public class AliveTimerCacheService implements Serializable {

  private Cache<Long, AliveTimer> aliveTimerCache;

  @Autowired
  public AliveTimerCacheService(Cache<Long, AliveTimer> aliveTimerCache) {
    this.aliveTimerCache = aliveTimerCache;
  }

  /**
   * Activate this alive timer.
   */
  public void start(final Long id) {
    aliveTimerCache.invoke(id, new AliveTimerManager(), AliveTimerManager.START);
  }

  public void stop(final Long id) {
    aliveTimerCache.invoke(id, new AliveTimerManager(), AliveTimerManager.STOP);
  }

  public void update(final Long id) {
    aliveTimerCache.invoke(id, new AliveTimerManager(), AliveTimerManager.UPDATE);
  }

  /**
   * Check whether this alive timer has expired.
   * @return true if the alive timer is active and it has not been updated since
   * at least "aliveInterval" milliseconds.
   */
  public boolean hasExpired(final Long aliveTimerId) {
    return (boolean) aliveTimerCache.invoke(aliveTimerId, new AliveTimerManager(), AliveTimerManager.HAS_EXPIRED);
  }

  public void startAllTimers() {
    log.debug("Starting all alive timers in cache.");
    try {
      Iterator<Cache.Entry<Long, AliveTimer>> entries= aliveTimerCache.iterator();
      while(entries.hasNext()) {
        start(entries.next().getValue().getId());
      }
    } catch (Exception e) {
      log.error("Unable to retrieve list of alive timers from cache when attempting to start the timers.", e);
    }
  }

  public void stopAllTimers() {
    log.debug("Stopping all alive timers in the cache.");
    try {
      Iterator<Cache.Entry<Long, AliveTimer>> entries= aliveTimerCache.iterator();
      while(entries.hasNext()) {
        stop(entries.next().getValue().getId());
      }
    } catch (Exception e) {
      log.error("Unable to retrieve list of alive timers from cache when attempting to stop all timers.", e);
    }
  }

//  @Override
//  public void generateFromEquipment(AbstractEquipment abstractEquipment) {
//    String type;
//    if (abstractEquipment instanceof Equipment) {
//      type = AliveTimer.ALIVE_TYPE_EQUIPMENT;
//    } else {
//      type = AliveTimer.ALIVE_TYPE_SUBEQUIPMENT;
//    }
//    AliveTimer aliveTimer = new AliveTimerCacheObject(abstractEquipment.getAliveTagId(), abstractEquipment.getId(), abstractEquipment.getName(),
//            abstractEquipment.getStateTagId(), type, abstractEquipment.getAliveInterval());
//    aliveTimerCache.put(aliveTimer.getId(), aliveTimer);
//  }
//
//  @Override
//  public void generateFromProcess(Process process) {
//    AliveTimer aliveTimer = new AliveTimerCacheObject(process.getAliveTagId(), process.getId(), process.getName(),
//            process.getStateTagId(), AliveTimer.ALIVE_TYPE_PROCESS, process.getAliveInterval());
//    aliveTimerCache.put(aliveTimer.getId(), aliveTimer);
//  }
}
