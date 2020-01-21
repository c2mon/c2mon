package cern.c2mon.cache.actions.state;

import cern.c2mon.cache.actions.subequipment.SubEquipmentService;
import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.api.listener.CacheListener;
import cern.c2mon.server.common.supervision.SupervisionStateTag;
import cern.c2mon.shared.common.supervision.SupervisionEntity;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import static cern.c2mon.cache.actions.state.SupervisionCascadeLogMessages.autoAction;
import static cern.c2mon.shared.common.CacheEvent.SUPERVISION_CHANGE;

public class SupervisionStateTagUpdateCascader implements CacheListener<SupervisionStateTag> {

  private final C2monCache<SupervisionStateTag> stateTagCache;
  private final SubEquipmentService subEquipmentService;

  @Inject
  public SupervisionStateTagUpdateCascader(C2monCache<SupervisionStateTag> stateTagCache,
                                           SubEquipmentService subEquipmentService) {
    this.stateTagCache = stateTagCache;
    this.subEquipmentService = subEquipmentService;
  }

  @PostConstruct
  public void register() {
    // TODO (Alex) Do we want this on SUP_UPDATE? Does it make sense that we override a SUBEQ event when the parent changes?
    stateTagCache.getCacheListenerManager().registerListener(this, SUPERVISION_CHANGE);
  }

  @Override
  public void apply(SupervisionStateTag updatedState) {
    if (updatedState.getSupervisedEntity() == SupervisionEntity.PROCESS) {
      // Set new state to equipments?
    } else if (updatedState.getSupervisedEntity() == SupervisionEntity.EQUIPMENT) {
      // Set new state to all subequipments
      // TODO (Alex) Should this only cascade when the EQ goes down?
      subEquipmentService.getSubEquipmentIdsFor(updatedState.getSupervisedId())
        .forEach(id -> {
          if (SupervisionStateTagEvaluator.isRunning(updatedState)) {
            subEquipmentService.resume(id, updatedState.getTimestamp().getTime(), autoAction("resumed", updatedState));
          } else {
            subEquipmentService.suspend(id, updatedState.getTimestamp().getTime(), autoAction("suspended", updatedState));
          }
        });
    }
  }


}
