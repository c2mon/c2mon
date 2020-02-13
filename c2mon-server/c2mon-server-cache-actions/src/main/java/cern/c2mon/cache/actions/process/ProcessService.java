package cern.c2mon.cache.actions.process;

import cern.c2mon.cache.actions.alive.AliveTagService;
import cern.c2mon.cache.actions.equipment.EquipmentService;
import cern.c2mon.cache.actions.state.SupervisionStateTagService;
import cern.c2mon.cache.actions.subequipment.SubEquipmentService;
import cern.c2mon.cache.actions.supervision.AbstractSupervisedService;
import cern.c2mon.cache.actions.supervision.SupervisedCacheService;
import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.api.exception.CacheElementNotFoundException;
import cern.c2mon.cache.api.exception.TooManyQueryResultsException;
import cern.c2mon.server.common.alive.AliveTag;
import cern.c2mon.server.common.config.ServerProperties;
import cern.c2mon.server.common.process.Process;
import cern.c2mon.server.common.process.ProcessCacheObject;
import cern.c2mon.shared.common.supervision.SupervisionEntity;
import cern.c2mon.shared.common.supervision.SupervisionStatus;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.UnexpectedRollbackException;

import javax.inject.Inject;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Collection;
import java.util.Map;

/**
 * @author Szymon Halastra, Alexandros Papageorgiou Koufidis
 */
@Slf4j
@Service
public class ProcessService extends AbstractSupervisedService<Process> implements ProcessOperationService {

  @Getter
  protected SupervisedCacheService<Process> supervisedService;
  private EquipmentService equipmentService;
  private SubEquipmentService subEquipmentService;
  private ServerProperties properties;

  @Inject
  public ProcessService(C2monCache<Process> processCacheRef, EquipmentService equipmentService,
                        AliveTagService aliveTimerService, SubEquipmentService subEquipmentService,
                        ServerProperties properties, SupervisionStateTagService stateTagService) {
    super(processCacheRef, aliveTimerService, null, stateTagService);
    this.equipmentService = equipmentService;
    this.subEquipmentService = subEquipmentService;
    this.properties = properties;
  }

  /**
   * Records the start up time of the process and the host it is running on,
   * (and sets it's status to STARTUP - may remove this in the future as duplicate
   * of state tag of the DAQ)
   *
   * <p>Also starts the alive timer.
   *
   * @param processId   the Process id to start
   * @param hostName    the hostname of the Process
   * @param startupTime the start up time
   */
  @Override
  public Process start(Long processId, String hostName, Timestamp startupTime) {
    if (isRunning(processId)) {
      return cache.get(processId);
    }

    return cache.compute(processId, process -> {
      ProcessController.start(process, hostName, startupTime);
      log.trace("start " + (properties.isTestMode() ? "- TEST Mode " : "")
        + "- Process " + process.getName() + ", PIK " + process.getProcessPIK());

      ((ProcessCacheObject) process).setRequiresReboot(false);
      start(processId, startupTime.getTime());

//    if (getLocalConfig() != null && getLocalConfig().equals(ProcessCacheObject.LocalConfig.Y)) {
//      setSupervision(SupervisionStatus.RUNNING_LOCAL, message, timestamp);
//    } TODO (Alex) Review this
    });
  }

  @Override
  public void setErrorStatus(Long processId, String errorMessage) {
    cache.compute(processId, process -> applyErrorStatus(process, errorMessage));
  }

  @Override
  public Long getProcessIdFromAlive(Long aliveTimerId) {
    AliveTag aliveTimer = aliveTagService.getCache().get(aliveTimerId);
    if (aliveTimer.getSupervisedEntity() == SupervisionEntity.PROCESS) {
      return aliveTimer.getSupervisedId();
    } else if (aliveTimer.getSupervisedEntity() == SupervisionEntity.EQUIPMENT) {
      return equipmentService.getProcessId(aliveTimer.getSupervisedId());
    } else {
      Long equipmentId = subEquipmentService.getEquipmentIdForSubEquipment(aliveTimer.getSupervisedId());
      return equipmentService.getProcessId(equipmentId);
    }
  }

  @Override
  public Long getProcessIdFromControlTag(Long controlTagId) {
    Map<Long, Long> equipmentControlTags = equipmentService.getControlTags();
    Map<Long, Long> subEquipmentControlTags = subEquipmentService.getControlTags();
    if (equipmentControlTags.containsKey(controlTagId)) {
      Long equipmentId = equipmentControlTags.get(controlTagId);
      return equipmentService.getProcessId(equipmentId);
    } else if (subEquipmentControlTags.containsKey(controlTagId)) {
      Long subEquipmentId = subEquipmentControlTags.get(controlTagId);
      return subEquipmentService.getEquipmentIdForSubEquipment(subEquipmentId);
    } else return null;
  }

  public Process getProcessIdFromName(String name) {
    final Collection<Process> queryResults = cache.query(process -> process.getName().matches(name));

    if (queryResults.size() > 1)
      throw new TooManyQueryResultsException();

    return queryResults.stream()
      .findFirst()
      .orElseThrow(CacheElementNotFoundException::new);
  }

  @Override
  public Boolean isRebootRequired(Long processId) {
    return cache.get(processId).getRequiresReboot();
  }

  @Override
  public void setRequiresReboot(Long processId, boolean reboot) {
    cache.compute(processId, process -> ((ProcessCacheObject) process).setRequiresReboot(reboot));
  }

  @Override
  public void setProcessPIK(Long processId, Long processPIK) {
    cache.compute(processId, process -> ((ProcessCacheObject) process).setProcessPIK(processPIK));
  }

  @Override
  public void setLocalConfig(Long processId, ProcessCacheObject.LocalConfig localType) {
    cache.compute(processId, process -> ((ProcessCacheObject) process).setLocalConfig(localType));
  }

  /**
   * Adds an equipment reference to the process that contains it.
   *
   * @param equipmentId the equipment to add
   * @param processId   the process to add the equipment reference to
   * @throws UnexpectedRollbackException if this operation fails
   */
  public void addEquipmentToProcess(Long equipmentId, Long processId) {
    log.debug("Adding Process Equipment {} for processId {}", equipmentId, processId);
    cache.compute(processId, process -> process.getEquipmentIds().add(equipmentId));

  }

  /**
   * Removes an equipment reference from the process that contains it.
   *
   * @param equipmentId the equipment to remove
   * @param processId   the process to remove the equipment reference from
   * @throws UnexpectedRollbackException if this operation fails
   */
  public void removeEquipmentFromProcess(Long equipmentId, Long processId) {
    log.debug("Removing Process Equipment {} for processId {}", equipmentId, processId);
    cache.compute(processId, process -> process.getEquipmentIds().remove(equipmentId));
  }

  private void applyErrorStatus(final Process process, final String errorMessage) {
    if (process.getStateTagId() == null || !stateTagService.getCache().containsKey(process.getStateTagId())) {
      log.error("Unable to find State tag id (" + process.getStateTagId() + ") for process " + process.getName()
        + " taking no action...");
      return;
    }

    stateTagService.getCache().compute(process.getStateTagId(), state ->
      state.setSupervision(SupervisionStatus.DOWN, errorMessage, Timestamp.from(Instant.now())));
  }
}
