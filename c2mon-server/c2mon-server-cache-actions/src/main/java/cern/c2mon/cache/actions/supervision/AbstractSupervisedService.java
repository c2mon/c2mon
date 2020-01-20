package cern.c2mon.cache.actions.supervision;

import cern.c2mon.cache.actions.AbstractCacheServiceImpl;
import cern.c2mon.cache.actions.alive.AliveTagService;
import cern.c2mon.cache.actions.commfault.CommFaultService;
import cern.c2mon.cache.actions.state.SupervisionStateTagService;
import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.api.exception.CacheElementNotFoundException;
import cern.c2mon.cache.api.flow.DefaultCacheFlow;
import cern.c2mon.server.common.control.ControlTag;
import cern.c2mon.server.common.equipment.AbstractEquipment;
import cern.c2mon.server.common.supervision.Supervised;
import lombok.extern.slf4j.Slf4j;

import java.util.function.BiConsumer;

/**
 * @author Szymon Halastra, Alexandros Papageorgiou Koufidis
 */
@Slf4j
public abstract class AbstractSupervisedService<T extends Supervised> extends AbstractCacheServiceImpl<T> implements SupervisedCacheService<T> {

  // TODO (Alex) Most previous supervision notifications were connected to a Tag. Do we want this behaviour?

  protected final AliveTagService aliveTimerService;
  protected final SupervisionStateTagService stateTagService;
  private final CommFaultService commFaultService;

  public AbstractSupervisedService(final C2monCache<T> cache,
                                   final AliveTagService aliveTimerService,
                                   final CommFaultService commFaultService,
                                   final SupervisionStateTagService stateTagService) {
    super(cache, new DefaultCacheFlow<>());
    this.aliveTimerService = aliveTimerService;
    this.commFaultService = commFaultService;
    this.stateTagService = stateTagService;
  }

  @Override
  public void start(long id, long timestamp) {
    if (!isRunning(id)) {
      cascadeOnControlTagCaches(id, (controlTagId, service) -> service.start(controlTagId, timestamp));
    }
  }

  @Override
  public void stop(long id, long timestamp) {
    cascadeOnControlTagCaches(id, (controlTagId, service) -> service.stop(controlTagId, timestamp));
  }

  @Override
  public void resume(long id, long timestamp, String message) {
//    dataTagService.resetQualityToValid(); TODO (Alex) Figure out how to get the datatag for a Supervised
    cascadeOnControlTagCaches(id, (controlTagId, service) -> service.resume(controlTagId, timestamp, message));
  }

  @Override
  public void suspend(long id, long timestamp, String message) {
    cascadeOnControlTagCaches(id, (controlTagId, service) -> service.suspend(controlTagId, timestamp, message));
  }

  public boolean isRunning(long supervisedId) {
    return stateTagService.isRunning(cache.get(supervisedId).getStateTagId());
  }

  private void cascadeOnControlTagCaches(long supervisedId, BiConsumer<Long, SupervisedCacheService<? extends ControlTag>> action) {
    try {
      T supervised = cache.get(supervisedId);
      cache.executeTransaction(() -> {
        if (supervised.getAliveTagId() != null) {
          action.accept(supervised.getAliveTagId(), aliveTimerService);
        }
        if (supervised instanceof AbstractEquipment && ((AbstractEquipment) supervised).getCommFaultTagId() != null) {
          action.accept(((AbstractEquipment) supervised).getCommFaultTagId(), commFaultService);
        }
        if (supervised.getStateTagId() != null) {
          action.accept(supervised.getStateTagId(), stateTagService);
        }
      });
    } catch (CacheElementNotFoundException e) {
      log.error("Could not find supervised object with id " + supervisedId + " to start. Taking no action", e);
    }
  }
}
