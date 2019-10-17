package cern.c2mon.server.cache.subequipment;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.server.cache.BaseEquipmentServiceImpl;
import cern.c2mon.server.cache.alivetimer.AliveTimerService;
import cern.c2mon.server.cache.commfault.CommFaultService;
import cern.c2mon.server.common.subequipment.SubEquipment;
import cern.c2mon.shared.common.supervision.SupervisionConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;

/**
 * @author Szymon Halastra
 * @author Alexandros Papageorgiou Koufidis
 */
@Slf4j
@Service
public class SubEquipmentService extends BaseEquipmentServiceImpl<SubEquipment> implements SubEquipmentOperations {

  @Autowired
  public SubEquipmentService(C2monCache<SubEquipment> subEquipmentCacheRef, CommFaultService commFaultService, AliveTimerService aliveTimerService) {
    super(subEquipmentCacheRef, commFaultService, aliveTimerService, SupervisionConstants.SupervisionEntity.SUBEQUIPMENT);
  }

  @Override
  public Long getEquipmentIdForSubEquipment(Long subEquipmentId) {
    return null;
  }

  @Override
  public void addSubEquipmentToEquipment(Long subEquipmentId, Long equipmentId) {
    // TODO Fill these in
  }

  @Override
  public Collection<Long> getDataTagIds(Long subEquipmentId) {
    return null;
  }

  @Override
  public void removeSubEquipmentFromEquipment(Long equipmentId, Long subEquipmentId) {

  }
}
