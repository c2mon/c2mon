package cern.c2mon.cache.actions.equipment;

import cern.c2mon.cache.actions.alive.AliveTagService;
import cern.c2mon.cache.actions.datatag.DataTagService;
import cern.c2mon.cache.actions.state.SupervisionStateTagService;
import cern.c2mon.cache.actions.supervision.AbstractSupervisedService;
import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.api.service.CommonEquipmentOperations;
import cern.c2mon.server.common.commfault.CommFaultTag;
import cern.c2mon.server.common.equipment.AbstractEquipment;
import cern.c2mon.shared.common.supervision.SupervisionEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author Szymon Halastra, Alexandros Papageorgiou Koufidis
 */
public abstract class BaseEquipmentServiceImpl<T extends AbstractEquipment> extends AbstractSupervisedService<T>
  implements CommonEquipmentOperations {

  private C2monCache<CommFaultTag> commFaultTagCache;

  protected BaseEquipmentServiceImpl(final C2monCache<T> cache,
                                     final C2monCache<CommFaultTag> commFaultTagCache,
                                     final AliveTagService aliveTimerService,
                                     final SupervisionEntity supervisionEntity,
                                     final DataTagService dataTagService,
                                     final SupervisionStateTagService stateTagService) {
    super(cache, supervisionEntity, aliveTimerService,dataTagService, stateTagService);
    this.commFaultTagCache = commFaultTagCache;
  }

  @Override
  public void removeCommFaultTag(final Long abstractEquipmentId) {
    T equipment = cache.get(abstractEquipmentId);
    Long commFaultId = equipment.getCommFaultTagId();
    if (commFaultId != null) {
      commFaultTagCache.remove(commFaultId);
    }
  }

  @Override
  public Map<Long, Long> getControlTags() {
    HashMap<Long, Long> returnMap = new HashMap<>();
    Set<Long> equipmentKeys = commFaultTagCache.getKeys();
    for (Long equipmentId : equipmentKeys) {
      AbstractEquipment equipment = cache.get(equipmentId);
      Long aliveId = equipment.getAliveTagId();
      if (aliveId != null) {
        returnMap.put(aliveId, equipmentId);
      }
      Long stateId = equipment.getStateTagId();
      if (stateId != null) {
        returnMap.put(stateId, equipmentId);
      }
      Long commFaultId = equipment.getCommFaultTagId();
      if (commFaultId != null) {
        returnMap.put(commFaultId, equipmentId);
      }
    }
    return returnMap;
  }
}
