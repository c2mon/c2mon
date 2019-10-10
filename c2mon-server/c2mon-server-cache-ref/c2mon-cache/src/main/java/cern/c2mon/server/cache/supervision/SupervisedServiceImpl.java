package cern.c2mon.server.cache.supervision;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.api.listener.CacheEvent;
import cern.c2mon.server.cache.alivetimer.AliveTimerService;
import cern.c2mon.server.common.alive.AliveTimer;
import cern.c2mon.server.common.process.ProcessCacheObject;
import cern.c2mon.server.common.supervision.Supervised;
import cern.c2mon.shared.client.supervision.SupervisionEvent;
import cern.c2mon.shared.client.supervision.SupervisionEventImpl;
import lombok.extern.slf4j.Slf4j;

import java.sql.Timestamp;

import static cern.c2mon.shared.common.supervision.SupervisionConstants.SupervisionEntity;
import static cern.c2mon.shared.common.supervision.SupervisionConstants.SupervisionStatus;

/**
 * @author Szymon Halastra
 * @author Alexandros Papageorgiou Koufidis
 */
@Slf4j
public class SupervisedServiceImpl<T extends Supervised> implements SupervisedService<T> {

  protected final C2monCache<T> cacheRef;

  // TODO Most previous supervision notifications were connected to a Tag. Do we want this behaviour?

  private final AliveTimerService aliveTimerService;

  private final C2monCache<AliveTimer> aliveTimerCache;

  private SupervisionEntity supervisionEntity;

  public SupervisedServiceImpl(final C2monCache<T> cacheRef, final AliveTimerService aliveTimerService) {
    this.cacheRef = cacheRef;
    this.aliveTimerService = aliveTimerService;
    this.aliveTimerCache = aliveTimerService.getCache();
  }

  @Override
  public void start(Long id) {
    cacheRef.executeTransaction(() -> {
      T supervised = cacheRef.get(id);
      boolean wasRunning = isRunning(supervised);
      start(supervised);
      cacheRef.put(id, supervised);
      if (!wasRunning)
        cacheRef.notifyListenersOf(CacheEvent.SUPERVISION_CHANGE, supervised);
    });
  }

  @Override
  public void stop(Long id, Timestamp timestamp) {
    cacheRef.executeTransaction(() -> {
      T supervised = cacheRef.get(id);
      boolean wasRunning = isRunning(supervised);
      stop(supervised, timestamp);
      cacheRef.put(id, supervised);
      if (wasRunning)
        cacheRef.notifyListenersOf(CacheEvent.SUPERVISION_CHANGE, supervised);
    });
  }

  @Override
  public void resume(Long id, Timestamp timestamp, String message) {
    cacheRef.executeTransaction(() -> {
      T supervised = cacheRef.get(id);
      if (!supervised.getSupervisionStatus().equals(SupervisionStatus.RUNNING)) {
        resume(supervised, timestamp, message);
        cacheRef.put(id, supervised);
      }
    });
  }

  @Override
  public void suspend(Long id, Timestamp timestamp, String message) {
    cacheRef.executeTransaction(() -> {
      T supervised = cacheRef.get(id);
      if (isRunning(supervised) || isUncertain(supervised)) {
        suspend(supervised, timestamp, message);
        cacheRef.put(id, supervised);
      }
    });
  }

  @Override
  public boolean isRunning(final T supervised) {
    return supervised.getSupervisionStatus() != null
      && (supervised.getSupervisionStatus().equals(SupervisionStatus.STARTUP)
      || supervised.getSupervisionStatus().equals(SupervisionStatus.RUNNING)
      || supervised.getSupervisionStatus().equals(SupervisionStatus.RUNNING_LOCAL));
  }

  @Override
  public boolean isRunning(final Long id) {
    return cacheRef.executeTransaction(() -> isRunning(cacheRef.get(id)));
  }

  @Override
  public boolean isUncertain(final T supervised) {
    return supervised.getSupervisionStatus() != null && supervised.getSupervisionStatus().equals(SupervisionStatus.UNCERTAIN);
  }

  @Override
  public SupervisionEvent getSupervisionStatus(final Long id) {
    return cacheRef.executeTransaction(() -> {
      T supervised = cacheRef.get(id);
      if (log.isTraceEnabled()) {
        log.trace("Getting supervision status: " + getSupervisionEntity() + " " + supervised.getName() + " is " + supervised.getSupervisionStatus());
      }
      Timestamp supervisionTime;
      String supervisionMessage;
      if (supervised.getStatusTime() != null) {
        supervisionTime = supervised.getStatusTime();
      } else {
        supervisionTime = new Timestamp(System.currentTimeMillis());
      }
      if (supervised.getStatusDescription() != null) {
        supervisionMessage = supervised.getStatusDescription();
      } else {
        supervisionMessage = getSupervisionEntity() + " " + supervised.getName() + " is " + supervised.getSupervisionStatus();
      }
      return new SupervisionEventImpl(getSupervisionEntity(), id, supervised.getName(), supervised.getSupervisionStatus(),
        supervisionTime, supervisionMessage);
    });
  }

  @Override
  public void refreshAndNotifyCurrentSupervisionStatus(final Long id) {
    cacheRef.executeTransaction(() -> {
      T supervised = cacheRef.get(id);
      supervised.setStatusTime(new Timestamp(System.currentTimeMillis()));
      cacheRef.put(supervised.getId(), supervised);
      cacheRef.notifyListenersOf(CacheEvent.SUPERVISION_UPDATE, supervised);
    });
  }

  @Override
  public void removeAliveTimer(final Long supervisedId) {
    T supervised = cacheRef.get(supervisedId);
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
    } else {
      throw new NullPointerException("Called method with null alive id");
    }
  }

  @Override
  public SupervisionEntity getSupervisionEntity() {
    return this.supervisionEntity;
  }

  @Override
  public void setSupervisionEntity(SupervisionEntity entity) {
    this.supervisionEntity = entity;
  }

  @Override
  public void loadAndStartAliveTag(final Long supervisedId) {
    T supervised = cacheRef.get(supervisedId);
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
   */
  @Override
  public void start(final T supervised) {
    if (supervised.getAliveTagId() != null) {
      aliveTimerService.start(supervised.getAliveTagId());
    }
    supervised.setSupervisionStatus(SupervisionStatus.STARTUP);
    supervised.setStatusDescription(supervised.getSupervisionEntity() + " " + supervised.getName() + " was started");
    supervised.setStatusTime(new Timestamp(System.currentTimeMillis()));
  }

  @Override
  public void stop(final T supervised, final Timestamp timestamp) {
    if (supervised.getAliveTagId() != null) {
      aliveTimerService.stop(supervised.getAliveTagId());
    }
    supervised.setSupervisionStatus(SupervisionStatus.DOWN);
    supervised.setStatusTime(timestamp);
    supervised.setStatusDescription(supervised.getSupervisionEntity() + " " + supervised.getName() + " was stopped");
  }

  private void resume(final T supervised, final Timestamp timestamp, final String message) {
    supervised.setSupervisionStatus(SupervisionStatus.RUNNING);

    if (supervised instanceof ProcessCacheObject) {
      ProcessCacheObject process = (ProcessCacheObject) supervised;
      if (process.getLocalConfig() != null && process.getLocalConfig().equals(ProcessCacheObject.LocalConfig.Y)) {
        supervised.setSupervisionStatus(SupervisionStatus.RUNNING_LOCAL);
      }
    }

    supervised.setStatusTime(timestamp);
    supervised.setStatusDescription(message);
  }

  private void suspend(final T supervised, final Timestamp timestamp, final String message) {
    supervised.setSupervisionStatus(SupervisionStatus.DOWN);
    supervised.setStatusDescription(message);
    supervised.setStatusTime(timestamp);
  }
}
