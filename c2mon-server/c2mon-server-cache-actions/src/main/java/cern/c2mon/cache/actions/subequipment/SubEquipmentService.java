package cern.c2mon.cache.actions.subequipment;

import cern.c2mon.cache.actions.alive.AliveTagService;
import cern.c2mon.cache.actions.commfault.CommFaultService;
import cern.c2mon.cache.actions.equipment.BaseEquipmentServiceImpl;
import cern.c2mon.cache.actions.equipment.EquipmentService;
import cern.c2mon.cache.actions.state.SupervisionStateTagService;
import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.server.common.subequipment.SubEquipment;
import cern.c2mon.server.common.subequipment.SubEquipmentCacheObject;
import cern.c2mon.shared.common.Cacheable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

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
                             final AliveTagService aliveTimerService,
                             final SupervisionStateTagService stateTagService) {
    super(subEquipmentCacheRef, commFaultService, aliveTimerService, stateTagService);
    this.equipmentService = equipmentService;
  }

  @PostConstruct
  public void init() {
    // TODO (Alex) Create INSERTED listener to cascade register this into the equipment cache, if needed
    // TODO (Alex) Create REMOVED listener to cascade remove the associated aliveTag,CommFault caches, if needed

    // TODO (Alex) Create INSERTED listener to cascade register this aliveTag,CommFault caches, if needed
//    if (abstractEquipment.getAliveTagId() != null) {
//      commonEquipmentFacade.loadAndStartAliveTag(abstractEquipment.getId());
//    }
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
  public void removeSubEquipmentFromEquipment(Long subEquipmentId) {
    cache.remove(subEquipmentId);
  }

  @Override
  public Long getProcessId(Long abstractEquipmentId) {
    return equipmentService.getProcessId(cache.get(abstractEquipmentId).getParentId());
  }

  public List<Long> getSubEquipmentIdsFor(long equipmentId) {
    return cache.query(subEq -> subEq.getParentId() == equipmentId)
      .stream()
      .map(Cacheable::getId)
      .collect(Collectors.toList());
  }
}
