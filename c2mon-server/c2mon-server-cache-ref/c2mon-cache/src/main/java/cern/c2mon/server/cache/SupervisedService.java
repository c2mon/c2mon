package cern.c2mon.server.cache;

import java.sql.Timestamp;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.api.service.SupervisedManager;
import cern.c2mon.server.cache.alivetimer.AliveTimerService;
import cern.c2mon.server.common.alive.AliveTimer;
import cern.c2mon.server.common.process.ProcessCacheObject;
import cern.c2mon.server.common.supervision.Supervised;
import cern.c2mon.shared.client.supervision.SupervisionEvent;
import cern.c2mon.shared.client.supervision.SupervisionEventImpl;
import cern.c2mon.shared.common.supervision.SupervisionConstants;

/**
 * @author Szymon Halastra
 */

@Slf4j
public class SupervisedService<T extends Supervised> implements SupervisedManager<T> {

  private C2monCache<Long, T> cache;

  private AliveTimerService aliveTimerService;

  private C2monCache aliveTimerCache;

  private SupervisionConstants.SupervisionEntity supervisionEntity;

  @Autowired
  public SupervisedService(final C2monCache<Long, T> cache, final AliveTimerService aliveTimerService) {
    this.cache = cache;
    this.aliveTimerService = aliveTimerService;
    this.aliveTimerCache = aliveTimerService.getCache();
  }

  @Override
  public void start(Long id, Timestamp timestamp) {
    cache.lockOnKey(id);
    try {
      T supervised = cache.get(id);
      start(supervised, timestamp);
      cache.put(id, supervised);
    } finally {
      cache.unlockOnKey(id);
    }
  }

  @Override
  public void stop(Long id, Timestamp timestamp) {
    cache.lockOnKey(id);
    try {
      T supervised = cache.get(id);
      stop(supervised, timestamp);
      cache.put(id, supervised);
    } finally {
      cache.unlockOnKey(id);
    }
  }

  @Override
  public void resume(Long id, Timestamp timestamp, String message) {
    cache.lockOnKey(id);
    try {
      T supervised = cache.get(id);
      if (!supervised.getSupervisionStatus().equals(SupervisionConstants.SupervisionStatus.RUNNING)) {
        resume(supervised, timestamp, message);
        cache.put(id, supervised);
      }
    } finally {
      cache.unlockOnKey(id);
    }
  }

  @Override
  public void suspend(Long id, Timestamp timestamp, String message) {
    cache.lockOnKey(id);
    try {
      T supervised = cache.get(id);
      if (isRunning(supervised) || isUncertain(supervised)) {
        suspend(supervised, timestamp, message);
        cache.put(id, supervised);
      }
    } finally {
      cache.unlockOnKey(id);
    }
  }

  @Override
  public boolean isRunning(final T supervised) {
    cache.lockOnKey(supervised.getId());
    try {
      return supervised.getSupervisionStatus() != null
              && (supervised.getSupervisionStatus().equals(SupervisionConstants.SupervisionStatus.STARTUP)
              || supervised.getSupervisionStatus().equals(SupervisionConstants.SupervisionStatus.RUNNING)
              || supervised.getSupervisionStatus().equals(SupervisionConstants.SupervisionStatus.RUNNING_LOCAL));
    } finally {
      cache.unlockOnKey(supervised.getId());
    }
  }

  @Override
  public boolean isRunning(final Long id) {
    cache.lockOnKey(id);
    try {
      return isRunning(cache.get(id));
    } finally {
      cache.unlockOnKey(id);
    }
  }

  @Override
  public boolean isUncertain(final T supervised) {
    cache.lockOnKey(supervised.getId());
    try {
      return supervised.getSupervisionStatus() != null
              && supervised.getSupervisionStatus().equals(SupervisionConstants.SupervisionStatus.UNCERTAIN);
    } finally {
      cache.unlockOnKey(supervised.getId());
    }
  }

  @Override
  public SupervisionEvent getSupervisionStatus(final Long id) {
    cache.lockOnKey(id);
    try {
      T supervised = cache.get(id);
      if (log.isTraceEnabled()) {
        log.trace("Getting supervision status: " + getSupervisionEntity() + " " + supervised.getName() + " is " + supervised.getSupervisionStatus());
      }
      Timestamp supervisionTime;
      String supervisionMessage;
      if (supervised.getStatusTime() != null) {
        supervisionTime = supervised.getStatusTime();
      }
      else {
        supervisionTime = new Timestamp(System.currentTimeMillis());
      }
      if (supervised.getStatusDescription() != null) {
        supervisionMessage = supervised.getStatusDescription();
      }
      else {
        supervisionMessage = getSupervisionEntity() + " " + supervised.getName() + " is " + supervised.getSupervisionStatus();
      }
      return new SupervisionEventImpl(getSupervisionEntity(), id, supervised.getName(), supervised.getSupervisionStatus(),
              supervisionTime, supervisionMessage);
    } finally {
      cache.unlockOnKey(id);
    }
  }

  @Override
  public void refreshAndNotifyCurrentSupervisionStatus(final Long id) {
    cache.lockOnKey(id);
    Timestamp refreshTime = new Timestamp(System.currentTimeMillis());
    try {
      T supervised = cache.get(id);
      supervised.setStatusTime(refreshTime);
      cache.put(supervised.getId(), supervised);
    } finally {
      cache.unlockOnKey(id);
    }
  }

  @Override
  public void removeAliveTimer(final Long supervisedId) {
    T supervised = cache.get(supervisedId);
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
    T supervised = cache.get(supervisedId);
    Long aliveId = supervised.getAliveTagId();
    aliveTimerCache.loadFromDb(aliveId);
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
  private void start(final T supervised, final Timestamp timestamp) {
    if (supervised.getAliveTagId() != null) {
      aliveTimerService.start(supervised.getAliveTagId());
    }
    supervised.setSupervisionStatus(SupervisionConstants.SupervisionStatus.STARTUP);
    supervised.setStatusDescription(supervised.getSupervisionEntity() + " " + supervised.getName() + " was started");
    supervised.setStatusTime(new Timestamp(System.currentTimeMillis()));
  }

  private void stop(final T supervised, final Timestamp timestamp) {
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
