package cern.c2mon.cache.actions.supervision;

import cern.c2mon.cache.actions.AbstractCacheServiceImpl;
import cern.c2mon.cache.actions.alive.AliveTagService;
import cern.c2mon.cache.actions.datatag.DataTagService;
import cern.c2mon.cache.actions.state.SupervisionStateTagService;
import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.api.exception.CacheElementNotFoundException;
import cern.c2mon.cache.api.flow.DefaultCacheFlow;
import cern.c2mon.server.common.supervision.Supervised;
import cern.c2mon.shared.common.supervision.SupervisionEntity;
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
  private final SupervisionStateTagService stateTagService;

  @Getter
  private SupervisionEntity supervisionEntity;

  public AbstractSupervisedService(final C2monCache<T> cache, SupervisionEntity supervisionEntity,
                                   final AliveTagService aliveTimerService,
                                   final DataTagService dataTagService,
                                   final SupervisionStateTagService stateTagService) {
    super(cache, new DefaultCacheFlow<>());
    this.supervisionEntity = supervisionEntity;
    this.aliveTimerService = aliveTimerService;
    this.dataTagService = dataTagService;
    this.stateTagService = stateTagService;
  }

  @Override
  public void start(long id, Timestamp timestamp) {
    try {
      T supervised = cache.get(id);
      if (supervised.getAliveTagId() != null) {
        aliveTimerService.start(supervised.getAliveTagId(), timestamp.getTime());
      } else if (supervised.getStateTagId() != null) {
        stateTagService.start(supervised.getStateTagId(), timestamp.getTime());
      }
    } catch (CacheElementNotFoundException e) {
      log.error("Could not find supervised object with id " + id + " to start. Taking no action", e);
    }
  }

  @Override
  public void stop(long id, Timestamp timestamp) {
    try {
      T supervised = cache.get(id);
      if (supervised.getAliveTagId() != null) {
        aliveTimerService.stop(supervised.getAliveTagId(), timestamp.getTime());
      } else if (supervised.getStateTagId() != null) {
        stateTagService.stop(supervised.getStateTagId(), timestamp.getTime());
      }
    } catch (CacheElementNotFoundException e) {
      log.error("Could not find supervised object with id " + id + " to start. Taking no action", e);
    }
  }

  @Override
  public void resume(long id, Timestamp timestamp, String message) {
//    dataTagService.resetQualityToValid(); TODO (Alex) Figure out how to get the datatag for a Supervised
    try {
      T supervised = cache.get(id);
      if (supervised.getAliveTagId() != null) {
        aliveTimerService.resume(supervised.getAliveTagId(), timestamp.getTime());
      } else if (supervised.getStateTagId() != null) {
        stateTagService.resume(supervised.getStateTagId(), timestamp.getTime());
      }
    } catch (CacheElementNotFoundException e) {
      log.error("Could not find supervised object with id " + id + " to start. Taking no action", e);
    }
  }

  @Override
  public void suspend(long id, Timestamp timestamp, String message) {
    try {
      T supervised = cache.get(id);
      if (supervised.getAliveTagId() != null) {
        aliveTimerService.suspend(supervised.getAliveTagId(), timestamp.getTime());
      } else if (supervised.getStateTagId() != null) {
        stateTagService.suspend(supervised.getStateTagId(), timestamp.getTime());
      }
    } catch (CacheElementNotFoundException e) {
      log.error("Could not find supervised object with id " + id + " to start. Taking no action", e);
    }
  }
}
