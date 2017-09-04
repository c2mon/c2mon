package cern.c2mon.server.cache.process;

import java.sql.Timestamp;
import java.util.Collection;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.api.CoreService;
import cern.c2mon.cache.api.service.SupervisedService;
import cern.c2mon.server.cache.alivetimer.AliveTimerService;
import cern.c2mon.server.cache.equipment.EquipmentService;
import cern.c2mon.server.common.alive.AliveTimer;
import cern.c2mon.server.common.config.ServerProperties;
import cern.c2mon.server.common.process.Process;
import cern.c2mon.server.common.process.ProcessCacheObject;
import cern.c2mon.shared.client.supervision.SupervisionEvent;
import cern.c2mon.shared.common.supervision.SupervisionConstants;

/**
 * @author Szymon Halastra
 */
@Slf4j
@Service
public class ProcessService implements CoreService, ProcessOperationService, SupervisedService<Process> {

  private SupervisedService<Process> supervisedService;

  private final ProcessOperationService processOperationService;

  private final EquipmentService equipmentService;

//  private SubEquipmentService subEquipmentService;

  private final C2monCache<Long, AliveTimer> aliveTimerCacheRef;

  private final C2monCache<Long, Process> processCacheRef;

  private final ServerProperties properties;

  @Autowired
  public ProcessService(final EquipmentService equipmentService, final AliveTimerService aliveTimerService,
                        final C2monCache<Long, Process> processCacheRef, final ServerProperties properties) {
    this.equipmentService = equipmentService;
    this.processCacheRef = processCacheRef;
    this.aliveTimerCacheRef = aliveTimerService.getCache();
    this.properties = properties;

    this.processOperationService = new ProcessOperationServiceImpl(processCacheRef, equipmentService, aliveTimerService, properties);
  }

  @Override
  public C2monCache getCache() {
    return processCacheRef;
  }

  @Override
  public Process start(Long processId, String hostName, Timestamp startupTime) {
    return processOperationService.start(processId, hostName, startupTime);
  }

  @Override
  public Collection<Long> getDataTagIds(Long processId) {
    return processOperationService.getDataTagIds(processId);
  }

  @Override
  public void errorStatus(Long processId, String errorMessage) {
    processOperationService.errorStatus(processId, errorMessage);
  }

  @Override
  public Long getProcessIdFromAlive(Long aliveTimerId) {
    return processOperationService.getProcessIdFromAlive(aliveTimerId);
  }

  @Override
  public Long getProcessIdFromControlTag(Long controlTagId) {
    return processOperationService.getProcessIdFromControlTag(controlTagId);
  }

  @Override
  public Boolean isRebootRequired(Long processId) {
    return processOperationService.isRebootRequired(processId);
  }

  @Override
  public void requiresReboot(Long processId, Boolean reboot) {
    processOperationService.requiresReboot(processId, reboot);
  }

  @Override
  public void setProcessPIK(Long processId, Long processPIK) {
    processOperationService.setProcessPIK(processId, processPIK);
  }

  @Override
  public void setLocalConfig(Long processId, ProcessCacheObject.LocalConfig localType) {
    processOperationService.setLocalConfig(processId, localType);
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
  public void start(Process supervised, Timestamp timestamp) {
    supervisedService.start(supervised, timestamp);
  }

  @Override
  public void stop(Process supervised, Timestamp timestamp) {
    processCacheRef.lockOnKey(supervised.getId());
    try {
      ProcessCacheObject processCacheObject = (ProcessCacheObject) supervised;
      processCacheObject.setCurrentHost(null);
      processCacheObject.setStartupTime(null);
      processCacheObject.setRequiresReboot(Boolean.FALSE);
      processCacheObject.setProcessPIK(null);
      processCacheObject.setLocalConfig(null);
      supervisedService.stop(supervised, timestamp);
    } finally {
      processCacheRef.unlockOnKey(supervised.getId());
    }
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
  public boolean isRunning(Process supervised) {
    return supervisedService.isRunning(supervised);
  }

  @Override
  public boolean isRunning(Long id) {
    return supervisedService.isRunning(id);
  }

  @Override
  public boolean isUncertain(Process supervised) {
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
    return SupervisionConstants.SupervisionEntity.PROCESS;
  }
}

