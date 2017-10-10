package cern.c2mon.server.cache;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import cern.c2mon.cache.api.Cache;
import cern.c2mon.cache.api.service.AbstractEquipmentService;
import cern.c2mon.server.cache.commfault.CommFaultService;
import cern.c2mon.server.common.commfault.CommFaultTag;
import cern.c2mon.server.common.equipment.AbstractEquipment;

/**
 * @author Szymon Halastra
 */
public class CoreAbstractEquipmentService<T extends AbstractEquipment> implements AbstractEquipmentService {

  private Cache<Long, T> cache;

  private Cache<Long, CommFaultTag> commFaultTagCache;

  public CoreAbstractEquipmentService(Cache<Long, T> cache, CommFaultService commFaultService) {
    this.cache = cache;
    this.commFaultTagCache = commFaultService.getCache();
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
