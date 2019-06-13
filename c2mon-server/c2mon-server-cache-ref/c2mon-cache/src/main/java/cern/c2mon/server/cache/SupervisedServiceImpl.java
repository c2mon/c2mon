package cern.c2mon.server.cache;

import java.sql.Timestamp;

import lombok.extern.slf4j.Slf4j;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.api.service.SupervisedService;
import cern.c2mon.server.cache.alivetimer.AliveTimerService;
import cern.c2mon.server.common.process.ProcessCacheObject;
import cern.c2mon.server.common.supervision.Supervised;
import cern.c2mon.shared.client.supervision.SupervisionEvent;
import cern.c2mon.shared.common.supervision.SupervisionConstants;

/**
 * @author Szymon Halastra
 */

@Slf4j
public class SupervisedServiceImpl<T extends Supervised> implements SupervisedService<T> {

  private final C2monCache<Long, T> c2monCache;

  private final AliveTimerService aliveTimerService;

  private final C2monCache aliveTimerCache;

  private SupervisionConstants.SupervisionEntity supervisionEntity;

  public SupervisedServiceImpl(final C2monCache<Long, T> c2monCache, final AliveTimerService aliveTimerService) {
    this.c2monCache = c2monCache;
    this.aliveTimerService = aliveTimerService;
    this.aliveTimerCache = aliveTimerService.getCache();
  }

  @Override
  public void start(Long id, Timestamp timestamp) {
//    c2monCache.lockOnKey(id);
//    try {
//      T supervised = c2monCache.get(id);
//      start(supervised, timestamp);
//      c2monCache.put(id, supervised);
//    } finally {
//      c2monCache.unlockOnKey(id);
//    }
  }

  @Override
  public void stop(Long id, Timestamp timestamp) {
//    c2monCache.lockOnKey(id);
//    try {
//      T supervised = c2monCache.get(id);
//      stop(supervised, timestamp);
//      c2monCache.put(id, supervised);
//    } finally {
//      c2monCache.unlockOnKey(id);
//    }
  }

  @Override
  public void resume(Long id, Timestamp timestamp, String message) {
//    c2monCache.lockOnKey(id);
//    try {
//      T supervised = c2monCache.get(id);
//      if (!supervised.getSupervisionStatus().equals(SupervisionConstants.SupervisionStatus.RUNNING)) {
//        resume(supervised, timestamp, message);
//        c2monCache.put(id, supervised);
//      }
//    } finally {
//      c2monCache.unlockOnKey(id);
//    }
  }

  @Override
  public void suspend(Long id, Timestamp timestamp, String message) {
//    c2monCache.lockOnKey(id);
//    try {
//      T supervised = c2monCache.get(id);
//      if (isRunning(supervised) || isUncertain(supervised)) {
//        suspend(supervised, timestamp, message);
//        c2monCache.put(id, supervised);
//      }
//    } finally {
//      c2monCache.unlockOnKey(id);
//    }
  }

  @Override
  public boolean isRunning(final T supervised) {
    return supervised.getSupervisionStatus() != null
            && (supervised.getSupervisionStatus().equals(SupervisionConstants.SupervisionStatus.STARTUP)
            || supervised.getSupervisionStatus().equals(SupervisionConstants.SupervisionStatus.RUNNING)
            || supervised.getSupervisionStatus().equals(SupervisionConstants.SupervisionStatus.RUNNING_LOCAL));
  }

  @Override
  public boolean isRunning(final Long id) {
//    c2monCache.lockOnKey(id);
//    try {
//      return isRunning(c2monCache.get(id));
//    } finally {
//      c2monCache.unlockOnKey(id);
//    }

    return false;
  }

  @Override
  public boolean isUncertain(final T supervised) {
//    c2monCache.lockOnKey(supervised.getId());
//    try {
//      return supervised.getSupervisionStatus() != null
//              && supervised.getSupervisionStatus().equals(SupervisionConstants.SupervisionStatus.UNCERTAIN);
//    } finally {
//      c2monCache.unlockOnKey(supervised.getId());
//    }

    return false;
  }

  @Override
  public SupervisionEvent getSupervisionStatus(final Long id) {
//    c2monCache.lockOnKey(id);
//    try {
//      T supervised = c2monCache.get(id);
//      if (log.isTraceEnabled()) {
//        log.trace("Getting supervision status: " + getSupervisionEntity() + " " + supervised.getName() + " is " + supervised.getSupervisionStatus());
//      }
//      Timestamp supervisionTime;
//      String supervisionMessage;
//      if (supervised.getStatusTime() != null) {
//        supervisionTime = supervised.getStatusTime();
//      }
//      else {
//        supervisionTime = new Timestamp(System.currentTimeMillis());
//      }
//      if (supervised.getStatusDescription() != null) {
//        supervisionMessage = supervised.getStatusDescription();
//      }
//      else {
//        supervisionMessage = getSupervisionEntity() + " " + supervised.getName() + " is " + supervised.getSupervisionStatus();
//      }
//      return new SupervisionEventImpl(getSupervisionEntity(), id, supervised.getName(), supervised.getSupervisionStatus(),
//              supervisionTime, supervisionMessage);
//    } finally {
//      c2monCache.unlockOnKey(id);
//    }

    return null;
  }

  @Override
  public void refreshAndNotifyCurrentSupervisionStatus(final Long id) {
//    c2monCache.lockOnKey(id);
//    Timestamp refreshTime = new Timestamp(System.currentTimeMillis());
//    try {
//      T supervised = c2monCache.get(id);
//      supervised.setStatusTime(refreshTime);
//      c2monCache.put(supervised.getId(), supervised);
//    } finally {
//      c2monCache.unlockOnKey(id);
//    }
  }

  @Override
  public void removeAliveTimer(final Long supervisedId) {
    T supervised = c2monCache.get(supervisedId);
    Long aliveId = supervised.getAliveTagId();
    if (aliveId != null) {
      aliveTimerService.stop(aliveId);
      aliveTimerCache.remove(aliveId);
    }
  }

  @Override
  public void removeAliveDirectly(final Long aliveId) {
    if (aliveId != null) {
      aliveTimerService.stop(aliveId);
      aliveTimerCache.remove(aliveId);
    }
    else {
      throw new NullPointerException("Called method with null alive id");
    }
  }

  @Override
  public SupervisionConstants.SupervisionEntity getSupervisionEntity() {
    return this.supervisionEntity;
  }

  @Override
  public void setSupervisionEntity(SupervisionConstants.SupervisionEntity entity) {
    this.supervisionEntity = entity;
  }

  @Override
  public void loadAndStartAliveTag(final Long supervisedId) {
    T supervised = c2monCache.get(supervisedId);
    Long aliveId = supervised.getAliveTagId();
    /*aliveTimerCache.loadFromDb(aliveId);*/ //TODO: implement loadFromDB, before that think how it should be implemented and designed
    if (aliveId != null) {
      aliveTimerService.start(aliveId);
    }
  }

  /**
   * Sets the status of the Supervised object to STARTUP,
   * with associated message.
   * <p>
   * <p>Starts the alive timer if not already running.
   *
   * @param supervised supervised object
   * @param timestamp  time of the start
   */
  @Override
  public void start(final T supervised, final Timestamp timestamp) {
    if (supervised.getAliveTagId() != null) {
      aliveTimerService.start(supervised.getAliveTagId());
    }
    supervised.setSupervisionStatus(SupervisionConstants.SupervisionStatus.STARTUP);
    supervised.setStatusDescription(supervised.getSupervisionEntity() + " " + supervised.getName() + " was started");
    supervised.setStatusTime(new Timestamp(System.currentTimeMillis()));
  }

  @Override
  public void stop(final T supervised, final Timestamp timestamp) {
    if (supervised.getAliveTagId() != null) {
      aliveTimerService.stop(supervised.getAliveTagId());
    }
    supervised.setSupervisionStatus(SupervisionConstants.SupervisionStatus.DOWN);
    supervised.setStatusTime(timestamp);
    supervised.setStatusDescription(supervised.getSupervisionEntity() + " " + supervised.getName() + " was stopped");
  }

  private void resume(final T supervised, final Timestamp timestamp, final String message) {
    supervised.setSupervisionStatus(SupervisionConstants.SupervisionStatus.RUNNING);

    if (supervised instanceof ProcessCacheObject) {
      ProcessCacheObject process = (ProcessCacheObject) supervised;
      if (process.getLocalConfig() != null && process.getLocalConfig().equals(ProcessCacheObject.LocalConfig.Y)) {
        supervised.setSupervisionStatus(SupervisionConstants.SupervisionStatus.RUNNING_LOCAL);
      }
    }

    supervised.setStatusTime(timestamp);
    supervised.setStatusDescription(message);
  }

  private void suspend(final T supervised, final Timestamp timestamp, final String message) {
    supervised.setSupervisionStatus(SupervisionConstants.SupervisionStatus.DOWN);
    supervised.setStatusDescription(message);
    supervised.setStatusTime(timestamp);
  }
}
