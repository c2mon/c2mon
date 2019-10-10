package cern.c2mon.server.cache;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.api.service.CommonEquipmentOperations;
import cern.c2mon.server.cache.alivetimer.AliveTimerService;
import cern.c2mon.server.cache.commfault.CommFaultService;
import cern.c2mon.server.cache.supervision.SupervisedService;
import cern.c2mon.server.cache.supervision.SupervisedServiceImpl;
import cern.c2mon.server.common.commfault.CommFaultTag;
import cern.c2mon.server.common.equipment.AbstractEquipment;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author Szymon Halastra
 * @author Alexandros Papageorgiou Koufidis
 */
public class BaseEquipmentServiceImpl<T extends AbstractEquipment> implements CommonEquipmentOperations {

  @Getter
  private C2monCache<T> c2monCache;

  private C2monCache<CommFaultTag> commFaultTagCache;

  @Getter
  private SupervisedService<T> supervisedService;

  protected BaseEquipmentServiceImpl(C2monCache<T> c2monCache, CommFaultService commFaultService, AliveTimerService aliveTimerService) {
    this.c2monCache = c2monCache;
    this.commFaultTagCache = commFaultService.getCache();

    supervisedService = new SupervisedServiceImpl<>(c2monCache, aliveTimerService);
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
