package cern.c2mon.server.cache.subequipment;

import java.sql.Timestamp;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.api.service.AbstractEquipmentService;
import cern.c2mon.cache.api.service.SupervisedService;
import cern.c2mon.server.cache.alivetimer.AliveTimerService;
import cern.c2mon.server.cache.commfault.CommFaultService;
import cern.c2mon.server.common.subequipment.SubEquipment;
import cern.c2mon.shared.client.supervision.SupervisionEvent;
import cern.c2mon.shared.common.supervision.SupervisionConstants;

/**
 * @author Szymon Halastra
 */
@Slf4j
@Service
public class SubEquipmentService implements SupervisedService<SubEquipment>, AbstractEquipmentService {

  private C2monCache<Long, SubEquipment> subEquipmentCacheRef;

  private SupervisedService<SubEquipment> supervisedService;

  private AbstractEquipmentService abstractEquipmentService;

  private CommFaultService commFaultService;

  private AliveTimerService aliveTimerService;

//  @Autowired
//  public SubEquipmentService(C2monCache<Long, SubEquipment> subEquipmentCacheRef, CommFaultService commFaultService, AliveTimerService aliveTimerService) {
//    this.subEquipmentCacheRef = subEquipmentCacheRef;
//    this.commFaultService = commFaultService;
//    this.aliveTimerService = aliveTimerService;
//
//    this.supervisedService = new SupervisedServiceImpl<>(subEquipmentCacheRef, aliveTimerService);
//    this.abstractEquipmentService = new CoreAbstractEquipmentService<>(subEquipmentCacheRef, commFaultService);
//  }

  public C2monCache getCache() {
    return subEquipmentCacheRef;
  }

  @Override
  public SupervisionEvent getSupervisionStatus(Long id) {
    return supervisedService.getSupervisionStatus(id);
  }

  @Override
  public void refreshAndNotifyCurrentSupervisionStatus(Long id) {
    supervisedService.refreshAndNotifyCurrentSupervisionStatus(id);
  }

  @Override
  public void start(Long id, Timestamp timestamp) {
    supervisedService.start(id, timestamp);
  }

  @Override
  public void start(SubEquipment supervised, Timestamp timestamp) {
    supervisedService.start(supervised, timestamp);
  }

  @Override
  public void stop(SubEquipment supervised, Timestamp timestamp) {
    supervisedService.stop(supervised, timestamp);
  }

  @Override
  public void stop(Long id, Timestamp timestamp) {
    supervisedService.stop(id, timestamp);
  }

  @Override
  public void resume(Long id, Timestamp timestamp, String message) {
    supervisedService.resume(id, timestamp, message);
  }

  @Override
  public void suspend(Long id, Timestamp timestamp, String message) {
    supervisedService.suspend(id, timestamp, message);
  }

  @Override
  public boolean isRunning(SubEquipment supervised) {
    return supervisedService.isRunning(supervised);
  }

  @Override
  public boolean isRunning(Long id) {
    return supervisedService.isRunning(id);
  }

  @Override
  public boolean isUncertain(SubEquipment supervised) {
    return supervisedService.isUncertain(supervised);
  }

  @Override
  public void removeAliveTimer(Long id) {
    supervisedService.removeAliveTimer(id);
  }

  @Override
  public void loadAndStartAliveTag(Long supervisedId) {
    supervisedService.loadAndStartAliveTag(supervisedId);
  }

  @Override
  public void removeAliveDirectly(Long aliveId) {
    supervisedService.removeAliveDirectly(aliveId);
  }

  @Override
  public SupervisionConstants.SupervisionEntity getSupervisionEntity() {
    return SupervisionConstants.SupervisionEntity.SUBEQUIPMENT;
  }

  @Override
  public Long getProcessIdForAbstractEquipment(Long abstractEquipmentId) {
    return abstractEquipmentService.getProcessIdForAbstractEquipment(abstractEquipmentId);
  }

  @Override
  public Map<Long, Long> getAbstractEquipmentControlTags() {
    return abstractEquipmentService.getAbstractEquipmentControlTags();
  }

  @Override
  public void removeCommFault(Long abstractEquipmentId) {
    abstractEquipmentService.removeCommFault(abstractEquipmentId);
  }
}
