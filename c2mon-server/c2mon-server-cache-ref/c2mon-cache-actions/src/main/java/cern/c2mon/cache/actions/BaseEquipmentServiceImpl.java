package cern.c2mon.cache.actions;

import cern.c2mon.cache.actions.alivetimer.AliveTimerService;
import cern.c2mon.cache.actions.commfault.CommFaultService;
import cern.c2mon.cache.actions.supervision.AbstractSupervisedService;
import cern.c2mon.cache.actions.supervision.SupervisedCacheService;
import cern.c2mon.cache.actions.supervision.SupervisedCacheServiceDelegator;
import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.api.service.CommonEquipmentOperations;
import cern.c2mon.server.common.commfault.CommFaultTag;
import cern.c2mon.server.common.equipment.AbstractEquipment;
import cern.c2mon.shared.common.supervision.SupervisionConstants;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author Szymon Halastra
 * @author Alexandros Papageorgiou Koufidis
 */
public abstract class BaseEquipmentServiceImpl<T extends AbstractEquipment> implements CommonEquipmentOperations, SupervisedCacheServiceDelegator<T> {

  @Getter
  private C2monCache<T> c2monCache;

  private C2monCache<CommFaultTag> commFaultTagCache;

  @Getter
  private SupervisedCacheService<T> supervisedService;

  protected BaseEquipmentServiceImpl(C2monCache<T> c2monCache, CommFaultService commFaultService,
                                     AliveTimerService aliveTimerService, SupervisionConstants.SupervisionEntity supervisionEntity) {
    this.c2monCache = c2monCache;
    this.commFaultTagCache = commFaultService.getCache();

    supervisedService = new AbstractSupervisedService<>(supervisionEntity,c2monCache, aliveTimerService);
  }

  @Override
  public Long getProcessIdForAbstractEquipment(Long abstractEquipmentId) {
    //TODO: add missing implementation
    return null;
  }

  @Override
  public void removeCommFault(final Long abstractEquipmentId) {
    T equipment = c2monCache.get(abstractEquipmentId);
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
      AbstractEquipment equipment = c2monCache.get(equipmentId);
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
