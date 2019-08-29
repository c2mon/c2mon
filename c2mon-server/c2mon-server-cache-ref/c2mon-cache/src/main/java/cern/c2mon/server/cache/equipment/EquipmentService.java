package cern.c2mon.server.cache.equipment;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.api.service.AbstractEquipmentService;
import cern.c2mon.cache.api.service.SupervisedService;
import cern.c2mon.server.cache.CoreAbstractEquipmentService;
import cern.c2mon.server.cache.SupervisedServiceImpl;
import cern.c2mon.server.cache.alivetimer.AliveTimerService;
import cern.c2mon.server.cache.commfault.CommFaultService;
import cern.c2mon.server.common.equipment.Equipment;
import lombok.Getter;
import lombok.experimental.Delegate;
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
public class EquipmentService implements SupervisedService<Equipment>, AbstractEquipmentService {

  @Delegate(types = SupervisedEquipmentService.class)
  private SupervisedService<Equipment> supervisedService;

  @Delegate(types = AbstractEquipmentService.class)
  private AbstractEquipmentService coreEquipmentService;

  @Getter
  private C2monCache<Equipment> equipmentCacheRef;

  @Autowired
  public EquipmentService(final C2monCache<Equipment> equipmentCacheRef,
                          final AliveTimerService aliveTimerService, final CommFaultService commFaultService) {
    this.equipmentCacheRef = equipmentCacheRef;

    this.supervisedService = new SupervisedServiceImpl(equipmentCacheRef, aliveTimerService);
    this.coreEquipmentService = new CoreAbstractEquipmentService<>(equipmentCacheRef, commFaultService);
  }

  //TODO: write this method
  public Collection<? extends Long> getDataTagIds(long equipmentId) {
    return null;
  }

  /**
   * This interface serves only as a static type reference to {@link SupervisedService}<{@link Equipment}>
   * <p>
   * That is required only for the {@code @Delegate} Lombok annotation above
   *
   * @see <a href=https://projectlombok.org/features/experimental/Delegate>Lombok Delegate docs</a>
   */
  private interface SupervisedEquipmentService extends SupervisedService<Equipment> {
  }
}
