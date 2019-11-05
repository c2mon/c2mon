package cern.c2mon.cache.actions.supervision;

import cern.c2mon.cache.actions.alivetimer.AliveTimerService;
import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.server.common.alive.AliveTimer;
import cern.c2mon.server.common.supervision.Supervised;
import cern.c2mon.shared.client.supervision.SupervisionEvent;
import cern.c2mon.shared.client.supervision.SupervisionEventImpl;
import lombok.Getter;
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

  @Getter
  private SupervisionEntity supervisionEntity;

  public SupervisedServiceImpl(SupervisionEntity supervisionEntity, final C2monCache<T> cacheRef, final AliveTimerService aliveTimerService) {
    this.supervisionEntity = supervisionEntity;
    this.cacheRef = cacheRef;
    this.aliveTimerService = aliveTimerService;
    this.aliveTimerCache = aliveTimerService.getCache();
    cacheRef.setCacheFlow(new AbstractSupervisedC2monCacheFlow<>());
  }

  @Override
  public T start(long id, Timestamp timestamp) {
    return cacheRef.executeTransaction(() -> {
      T supervised = cacheRef.get(id);
      supervised.start(timestamp);
      cacheRef.put(id, supervised);
      if (supervised.getAliveTagId() != null) {
        aliveTimerService.start(supervised.getAliveTagId());
      }
      return supervised;
    });
  }

  @Override
  public T stop(long id, Timestamp timestamp) {
    return cacheRef.executeTransaction(() -> {
      T supervised = cacheRef.get(id);
      if (supervised.getAliveTagId() != null) {
        aliveTimerService.stop(supervised.getAliveTagId());
      }
      supervised.stop(timestamp);
      cacheRef.put(id, supervised);
      return supervised;
    });
  }

  @Override
  public T resume(long id, Timestamp timestamp, String message) {
    return cacheRef.executeTransaction(() -> {
      T supervised = cacheRef.get(id);
      if (!supervised.getSupervisionStatus().equals(SupervisionStatus.RUNNING)) {
        supervised.resume(timestamp, message);
        cacheRef.put(id, supervised);
      }
      return supervised;
    });
  }

  @Override
  public T suspend(long id, Timestamp timestamp, String message) {
    return cacheRef.executeTransaction(() -> {
      T supervised = cacheRef.get(id);
      if (supervised.isRunning() || supervised.isUncertain()) {
        supervised.suspend(timestamp, message);
        cacheRef.put(id, supervised);
      }
      return supervised;
    });
  }

  @Override
  public boolean isRunning(long id) {
    return cacheRef.get(id).isRunning();
  }

  @Override
  public boolean isUncertain(long id) {
    return cacheRef.get(id).isUncertain();
  }

  @Override
  public SupervisionEvent getSupervisionStatus(long id) {
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
  public void refresh(long id) {
    cacheRef.executeTransaction(() -> {
      T supervised = cacheRef.get(id);
      supervised.setStatusTime(new Timestamp(System.currentTimeMillis()));
      cacheRef.put(supervised.getId(), supervised);
    });
  }

  @Override
  public void removeAliveTimer(long supervisedId) {
    T supervised = cacheRef.get(supervisedId);
    long aliveId = supervised.getAliveTagId();
    aliveTimerService.stop(aliveId);
    aliveTimerCache.remove(aliveId);

  }

  @Override
  public void removeAliveDirectly(long aliveId) {
    aliveTimerService.stop(aliveId);
    aliveTimerCache.remove(aliveId);
  }

  @Override
  public void loadAndStartAliveTag(long supervisedId) {
    T supervised = cacheRef.get(supervisedId);
    long aliveId = supervised.getAliveTagId();
    /*aliveTimerCache.loadFromDb(aliveId);*/ //TODO: implement loadFromDB, before that think how it should be implemented and designed
    aliveTimerService.start(aliveId);
  }
}
