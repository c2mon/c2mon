package cern.c2mon.cache.actions.equipment;

import cern.c2mon.cache.actions.alive.AliveTagService;
import cern.c2mon.cache.actions.commfault.CommFaultService;
import cern.c2mon.cache.actions.state.SupervisionStateTagService;
import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.server.common.equipment.Equipment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * @author Szymon Halastra, Alexandros Papageorgiou Koufidis
 */
@Slf4j
@Service
public class EquipmentService extends BaseEquipmentServiceImpl<Equipment> implements EquipmentOperations {

  @Inject
  public EquipmentService(final C2monCache<Equipment> equipmentCacheRef,
                          final AliveTagService aliveTimerService,
                          final CommFaultService commFaultService,
                          final SupervisionStateTagService stateTagService) {
    super(equipmentCacheRef, commFaultService, aliveTimerService, stateTagService);
  }

  @Override
  public Collection<Long> getEquipmentAlives() {
    return getCache().query(i -> true).stream().map(Equipment::getAliveTagId).collect(Collectors.toSet());
  }

  @Override
  public void removeCommandFromEquipment(Long equipmentId, Long commandId) {

  }

  @Override
  public void addCommandToEquipment(Long equipmentId, Long commandId) {

  }

  @Override
  public Long getProcessId(Long abstractEquipmentId) {
    return cache.get(abstractEquipmentId).getProcessId();
  }
}
