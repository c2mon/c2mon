package cern.c2mon.cache.actions.supervision;

import cern.c2mon.cache.actions.AbstractCacheServiceImpl;
import cern.c2mon.cache.actions.alive.AliveTagService;
import cern.c2mon.cache.actions.datatag.DataTagService;
import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.server.common.supervision.Supervised;
import cern.c2mon.shared.common.supervision.SupervisionEntity;
import cern.c2mon.shared.common.supervision.SupervisionStatus;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.sql.Timestamp;

/**
 * @author Szymon Halastra, Alexandros Papageorgiou Koufidis
 */
@Slf4j
public abstract class AbstractSupervisedService<T extends Supervised> extends AbstractCacheServiceImpl<T> implements SupervisedCacheService<T> {

  // TODO Most previous supervision notifications were connected to a Tag. Do we want this behaviour?

  private final AliveTagService aliveTimerService;
  private final DataTagService dataTagService;

  @Getter
  private SupervisionEntity supervisionEntity;

  public AbstractSupervisedService(final C2monCache<T> cache, SupervisionEntity supervisionEntity,
                                   final AliveTagService aliveTimerService,
                                   final DataTagService dataTagService) {
    super(cache, new AbstractSupervisedCacheFlow<>());
    this.supervisionEntity = supervisionEntity;
    this.aliveTimerService = aliveTimerService;
    this.dataTagService = dataTagService;
  }

  @Override
  public T start(long id, Timestamp timestamp) {
    return cache.compute(id, supervised -> {
      supervised.start(timestamp);
      if (supervised.getAliveTagId() != null) {
        aliveTimerService.start(supervised.getAliveTagId(), timestamp.getTime());
      }
    });
  }

  @Override
  public T stop(long id, Timestamp timestamp) {
    return cache.compute(id, supervised -> {
      if (supervised.getAliveTagId() != null) {
        aliveTimerService.stop(supervised.getAliveTagId(), timestamp.getTime());
      }
      supervised.stop(timestamp);
    });
  }

  @Override
  public T resume(long id, Timestamp timestamp, String message) {
//    dataTagService.resetQualityToValid(); TODO (Alex) Figure out how to get the datatag for a Supervised

    return cache.compute(id, supervised -> {
      if (!supervised.getSupervisionStatus().equals(SupervisionStatus.RUNNING)) {
        supervised.resume(timestamp, message);
      }
    });
  }

  @Override
  public T suspend(long id, Timestamp timestamp, String message) {
    return cache.compute(id, supervised -> {
      if (supervised.isRunning() || supervised.isUncertain()) {
        supervised.suspend(timestamp, message);
      }
    });
  }
}
