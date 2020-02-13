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
import cern.c2mon.server.common.equipment.AbstractEquipmentCacheObject;
import cern.c2mon.server.common.equipment.Equipment;
import cern.c2mon.server.common.process.Process;
import cern.c2mon.server.common.supervision.Supervised;
import cern.c2mon.shared.common.ConfigurationException;
import cern.c2mon.shared.common.supervision.SupervisionEntity;
import lombok.extern.slf4j.Slf4j;

import java.util.function.BiConsumer;

import static cern.c2mon.server.common.util.KotlinAPIs.applyNotNull;

/**
 * @author Szymon Halastra, Alexandros Papageorgiou Koufidis
 */
@Slf4j
public abstract class AbstractSupervisedService<T extends Supervised> extends AbstractCacheServiceImpl<T> implements SupervisedCacheService<T> {

  // TODO (Alex) Most previous supervision notifications were connected to a Tag. Do we want this behaviour?

  protected final AliveTagService aliveTagService;
  protected final SupervisionStateTagService stateTagService;
  private final CommFaultService commFaultService;

  public AbstractSupervisedService(final C2monCache<T> cache,
                                   final AliveTagService aliveTagService,
                                   final CommFaultService commFaultService,
                                   final SupervisionStateTagService stateTagService) {
    super(cache, new DefaultCacheFlow<>());
    this.aliveTagService = aliveTagService;
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

  public void updateControlTagCacheIds(Supervised supervised) {
    try {
      final AbstractEquipmentCacheObject abstractEquipment =
        (supervised instanceof AbstractEquipmentCacheObject)
          ? (AbstractEquipmentCacheObject) supervised
          : null;
      final SupervisionEntity entity = (supervised instanceof Process)
        ? SupervisionEntity.PROCESS : (supervised instanceof Equipment)
        ? SupervisionEntity.EQUIPMENT : SupervisionEntity.SUBEQUIPMENT;

      applyNotNull(supervised.getAliveTagId(), aliveTagId ->
        aliveTagService.getCache().computeQuiet(aliveTagId, aliveTimer -> {
          log.trace("Adding supervised id #{} to alive timer {} (#{})", supervised.getId(), aliveTimer.getName(), aliveTimer.getId());
          aliveTimer.setSupervisedId(supervised.getId());
          aliveTimer.setSupervisedEntity(entity);
          if (abstractEquipment != null) {
            applyNotNull(abstractEquipment.getCommFaultTagId(), aliveTimer::setCommFaultTagId);
          }
          applyNotNull(supervised.getStateTagId(), aliveTimer::setStateTagId);
        }));

      if (abstractEquipment != null) {
        applyNotNull(abstractEquipment.getCommFaultTagId(), commFaultTagId ->
          commFaultService.getCache().computeQuiet(commFaultTagId, commFaultTag -> {
            log.trace("Adding supervised id #{} to commFault tag {} (#{})", supervised.getId(), commFaultTag.getName(), commFaultTag.getId());
            commFaultTag.setSupervisedId(supervised.getId());
            commFaultTag.setSupervisedEntity(entity);
            commFaultTag.setEquipmentName(supervised.getName());
            applyNotNull(supervised.getAliveTagId(), commFaultTag::setAliveTagId);
            applyNotNull(supervised.getStateTagId(), commFaultTag::setStateTagId);
          }));
      }

      applyNotNull(supervised.getStateTagId(), stateTagId ->
        stateTagService.getCache().computeQuiet(stateTagId, supervisionStateTag -> {
          log.trace("Adding supervised id #{} to state tag {} (#{})", supervised.getId(), supervisionStateTag.getName(), supervisionStateTag.getId());
          supervisionStateTag.setSupervisedId(supervised.getId());
          supervisionStateTag.setSupervisedEntity(entity);
          applyNotNull(supervised.getAliveTagId(), supervisionStateTag::setAliveTagId);
          if (abstractEquipment != null) {
            applyNotNull(abstractEquipment.getCommFaultTagId(), supervisionStateTag::setCommFaultTagId);
          }
        }));
    } catch (CacheElementNotFoundException e) {
      throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE,
        String.format("Supervised tag (%s) not found for supervised #%d (%s).",
          supervised.getAliveTagId(), supervised.getId(), supervised.getName()));
    }
  }

  private void cascadeOnControlTagCaches(long supervisedId, BiConsumer<Long, SupervisedCacheService<? extends ControlTag>> action) {
    try {
      T supervised = cache.get(supervisedId);
      cache.executeTransaction(() -> {
        if (supervised.getAliveTagId() != null) {
          action.accept(supervised.getAliveTagId(), aliveTagService);
        }
        if (supervised instanceof AbstractEquipment && ((AbstractEquipment) supervised).getCommFaultTagId() != null) {
          action.accept(((AbstractEquipment) supervised).getCommFaultTagId(), commFaultService);
        }
        if (supervised.getStateTagId() != null) {
          action.accept(supervised.getStateTagId(), stateTagService);
        }
      });
    } catch (CacheElementNotFoundException e) {
      log.error("Could not find supervised object with id " + supervisedId + " to edit. Taking no action", e);
    }
  }
}
