package cern.c2mon.cache.actions;

import cern.c2mon.cache.actions.alivetimer.AliveTimerService;
import cern.c2mon.cache.actions.supervision.AbstractSupervisedService;
import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.api.service.CommonEquipmentOperations;
import cern.c2mon.server.common.commfault.CommFaultTag;
import cern.c2mon.server.common.equipment.AbstractEquipment;
import cern.c2mon.shared.common.supervision.SupervisionConstants;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author Szymon Halastra
 * @author Alexandros Papageorgiou Koufidis
 */
public abstract class BaseEquipmentServiceImpl<T extends AbstractEquipment> extends AbstractSupervisedService<T>
  implements CommonEquipmentOperations {

  private C2monCache<CommFaultTag> commFaultTagCache;

  protected BaseEquipmentServiceImpl(C2monCache<T> cache, C2monCache<CommFaultTag> commFaultTagCache,
                                     AliveTimerService aliveTimerService, SupervisionConstants.SupervisionEntity supervisionEntity) {
    super(cache, supervisionEntity, aliveTimerService);
    this.commFaultTagCache = commFaultTagCache;
  }

  @Override
  public Long getProcessIdForAbstractEquipment(Long abstractEquipmentId) {
    //TODO: add missing implementation
    return null;
  }

  @Override
  public void removeCommFault(final Long abstractEquipmentId) {
    T equipment = cache.get(abstractEquipmentId);
    Long commFaultId = equipment.getCommFaultTagId();
    if (commFaultId != null) {
      commFaultTagCache.remove(commFaultId);
    }
  }

  @Override
  public Map<Long, Long> getAbstractEquipmentControlTags() {
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
