package cern.c2mon.server.cache.subequipment;

import java.util.Map;

import cern.c2mon.server.cache.CoreAbstractEquipmentService;
import cern.c2mon.server.cache.SupervisedServiceImpl;
import lombok.experimental.Delegate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.api.service.AbstractEquipmentService;
import cern.c2mon.cache.api.service.SupervisedService;
import cern.c2mon.server.cache.alivetimer.AliveTimerService;
import cern.c2mon.server.cache.commfault.CommFaultService;
import cern.c2mon.server.common.subequipment.SubEquipment;

/**
 * @author Szymon Halastra
 */
@Slf4j
@Service
public class SubEquipmentService implements SupervisedService<SubEquipment>, AbstractEquipmentService {

  private C2monCache<SubEquipment> subEquipmentCacheRef;

  private interface SubEquipmentSupervisedService extends SupervisedService<SubEquipment>{}

  @Delegate(types = SubEquipmentSupervisedService.class)
  private SupervisedService<SubEquipment> supervisedService;

  @Delegate(types = AbstractEquipmentService.class)
  private AbstractEquipmentService abstractEquipmentService;

  private CommFaultService commFaultService;

  private AliveTimerService aliveTimerService;

  public SubEquipmentService() {
  }

  @Autowired
  public SubEquipmentService(C2monCache<SubEquipment> subEquipmentCacheRef, CommFaultService commFaultService, AliveTimerService aliveTimerService) {
    this.subEquipmentCacheRef = subEquipmentCacheRef;
    this.commFaultService = commFaultService;
    this.aliveTimerService = aliveTimerService;

    this.supervisedService = new SupervisedServiceImpl<>(subEquipmentCacheRef, aliveTimerService);
    this.abstractEquipmentService = new CoreAbstractEquipmentService<>(subEquipmentCacheRef, commFaultService);
  }

  public C2monCache getCache() {
    return subEquipmentCacheRef;
  }
}
