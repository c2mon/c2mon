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
import cern.c2mon.server.common.config.ServerProperties;
import cern.c2mon.server.common.process.Process;
import cern.c2mon.server.common.process.ProcessCacheObject;
import cern.c2mon.server.common.supervision.Supervised;
import cern.c2mon.shared.client.supervision.SupervisionEvent;
import cern.c2mon.shared.common.supervision.SupervisionConstants;

/**
 * @author Szymon Halastra
 */
@Slf4j
@Service
public class ProcessService implements CoreService, ProcessOperationService, SupervisedService {

  private SupervisedService<Process> supervisedService;

  private ProcessOperationService processOperationService;

  private EquipmentService equipmentService;

//  private SubEquipmentService subEquipmentService;

  private C2monCache aliveTimerCache;

  private C2monCache<Long, Process> processCache;

  private ServerProperties properties;

  @Autowired
  public ProcessService(final EquipmentService equipmentService, final AliveTimerService aliveTimerService,
                        final C2monCache<Long, Process> processCache, final ServerProperties properties) {
    this.equipmentService = equipmentService;
    this.processCache = processCache;
    this.aliveTimerCache = aliveTimerService.getCache();
    this.properties = properties;

    this.processOperationService = new ProcessOperationServiceImpl(processCache, equipmentService, aliveTimerService, properties);
  }

  @Override
  public C2monCache getCache() {
    return processCache;
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
    return null;
  }

  @Override
  public void refreshAndNotifyCurrentSupervisionStatus(Long id) {

  }

  @Override
  public void start(Long id, Timestamp timestamp) {

  }

  @Override
  public void stop(Long id, Timestamp timestamp) {

  }

  @Override
  public void resume(Long id, Timestamp timestamp, String message) {

  }

  @Override
  public void suspend(Long id, Timestamp timestamp, String message) {

  }

  @Override
  public boolean isRunning(Supervised supervised) {
    return false;
  }

  @Override
  public boolean isRunning(Long id) {
    return false;
  }

  @Override
  public boolean isUncertain(Supervised supervised) {
    return false;
  }

  @Override
  public void removeAliveTimer(Long id) {

  }

  @Override
  public void loadAndStartAliveTag(Long supervisedId) {

  }

  @Override
  public void removeAliveDirectly(Long aliveId) {

  }

  @Override
  public SupervisionConstants.SupervisionEntity getSupervisionEntity() {
    return SupervisionConstants.SupervisionEntity.PROCESS;
  }

  @Override
  public void setSupervisionEntity(SupervisionConstants.SupervisionEntity entity) {

  }
}

