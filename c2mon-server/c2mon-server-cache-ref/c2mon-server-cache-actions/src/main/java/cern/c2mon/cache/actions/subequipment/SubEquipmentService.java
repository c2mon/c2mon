package cern.c2mon.cache.actions.subequipment;

import cern.c2mon.cache.actions.alivetimer.AliveTimerService;
import cern.c2mon.cache.actions.commfault.CommFaultService;
import cern.c2mon.cache.actions.datatag.DataTagService;
import cern.c2mon.cache.actions.equipment.BaseEquipmentServiceImpl;
import cern.c2mon.cache.actions.equipment.EquipmentService;
import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.server.common.subequipment.SubEquipment;
import cern.c2mon.server.common.subequipment.SubEquipmentCacheObject;
import cern.c2mon.shared.common.supervision.SupervisionConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.Collection;

/**
 * @author Szymon Halastra
 * @author Alexandros Papageorgiou Koufidis
 */
@Slf4j
@Service
public class SubEquipmentService extends BaseEquipmentServiceImpl<SubEquipment> implements SubEquipmentOperations {

  private final EquipmentService equipmentService;

  @Inject
  public SubEquipmentService(final C2monCache<SubEquipment> subEquipmentCacheRef,
                             final EquipmentService equipmentService,
                             final CommFaultService commFaultService,
                             final AliveTimerService aliveTimerService,
                             final DataTagService dataTagService) {
    super(subEquipmentCacheRef, commFaultService.getCache(), aliveTimerService, SupervisionConstants.SupervisionEntity.SUBEQUIPMENT,dataTagService);
    this.equipmentService = equipmentService;
  }

  @Override
  public Long getEquipmentIdForSubEquipment(Long subEquipmentId) {
    return cache.get(subEquipmentId).getParentId();
  }

  @Override
  public void addSubEquipmentToEquipment(Long subEquipmentId, Long equipmentId) {
    cache.compute(subEquipmentId, subEquipment ->
      ((SubEquipmentCacheObject) subEquipment).setParentId(equipmentId));
  }

  @Override
  public Collection<Long> getDataTagIds(Long subEquipmentId) {
    // TODO
    return null;
  }

  @Override
  public void removeSubEquipmentFromEquipment(Long equipmentId, Long subEquipmentId) {
    equipmentService.removeSubequipmentFromEquipment(subEquipmentId, equipmentId);
    cache.remove(subEquipmentId);
  }

  @Override
  public Long getProcessIdForAbstractEquipment(Long abstractEquipmentId) {
    return equipmentService.getProcessIdForAbstractEquipment(cache.get(abstractEquipmentId).getParentId());
  }
}
