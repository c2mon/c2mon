package cern.c2mon.cache.actions.process;

import cern.c2mon.cache.actions.AbstractCacheServiceImpl;
import cern.c2mon.cache.actions.alive.AliveTagService;
import cern.c2mon.cache.actions.datatag.DataTagService;
import cern.c2mon.cache.actions.equipment.EquipmentService;
import cern.c2mon.cache.actions.state.SupervisionStateTagService;
import cern.c2mon.cache.actions.subequipment.SubEquipmentService;
import cern.c2mon.cache.actions.supervision.SupervisedCacheService;
import cern.c2mon.cache.actions.supervision.SupervisedCacheServiceDelegator;
import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.api.exception.CacheElementNotFoundException;
import cern.c2mon.cache.api.exception.TooManyQueryResultsException;
import cern.c2mon.cache.api.flow.DefaultCacheFlow;
import cern.c2mon.server.common.alive.AliveTag;
import cern.c2mon.server.common.config.ServerProperties;
import cern.c2mon.server.common.process.Process;
import cern.c2mon.server.common.process.ProcessCacheObject;
import cern.c2mon.shared.common.supervision.SupervisionEntity;
import cern.c2mon.shared.common.supervision.SupervisionStatus;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Collection;
import java.util.Map;
import java.util.Random;

/**
 * @author Szymon Halastra, Alexandros Papageorgiou Koufidis
 */
@Slf4j
@Service
public class ProcessService extends AbstractCacheServiceImpl<Process>
  implements ProcessOperationService, SupervisedCacheServiceDelegator<Process> {

  /**
   * PIK numbers limit (max)
   */
  private static final int PIK_MAX = 999999;
  /**
   * PIK numbers limit (min)
   */
  private static final int PIK_MIN = 100000;

  @Getter
  protected SupervisedCacheService<Process> supervisedService;
  private AliveTagService aliveTimerService;
  private EquipmentService equipmentService;
  private SubEquipmentService subEquipmentService;
  private ServerProperties properties;
  private SupervisionStateTagService stateTagService;

  @Inject
  public ProcessService(C2monCache<Process> processCacheRef, EquipmentService equipmentService,
                        AliveTagService aliveTimerService, SubEquipmentService subEquipmentService,
                        ServerProperties properties, DataTagService dataTagService, SupervisionStateTagService stateTagService) {
    super(processCacheRef, new DefaultCacheFlow<>());
    this.aliveTimerService = aliveTimerService;
    this.equipmentService = equipmentService;
    this.subEquipmentService = subEquipmentService;
    this.properties = properties;
    this.stateTagService = stateTagService;

    this.supervisedService = new SupervisedProcessServiceImpl(processCacheRef, aliveTimerService, dataTagService, stateTagService);
  }

  @Override
  public Process start(Long processId, String hostName, Timestamp startupTime) {
    return cache.compute(processId, process -> {
      if (properties.isTestMode()) {
        // If the TEST Mode is on
        forceStart(process, hostName, startupTime);
        log.trace("start - TEST Mode - Process " + process.getName()
          + ", PIK " + process.getProcessPIK());
      } else {
        // If the TEST Mode is off
        start(process, hostName, startupTime);
        log.trace("start - Process " + process.getName()
          + ", PIK " + process.getProcessPIK());
      }
    });
  }

  @Override
  public void setErrorStatus(Long processId, String errorMessage) {
    cache.compute(processId, process -> applyErrorStatus(process, errorMessage));
  }

  @Override
  public Long getProcessIdFromAlive(Long aliveTimerId) {
    AliveTag aliveTimer = aliveTimerService.getCache().get(aliveTimerId);
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
  public void setRequiresReboot(Long processId, Boolean reboot) {
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

  public boolean isRunning(long processId) {
    return stateTagService.isRunning(cache.get(processId).getStateTagId());
  }

  /**
   * Records the start up time of the process and the host it is running on,
   * (and sets it's status to STARTUP - may remove this in the future as duplicate
   * of state tag of the DAQ)
   * <p>
   * <p>Also starts the alive timer.
   *
   * @param process      the Process that is starting
   * @param pHostName    the hostname of the Process
   * @param pStartupTime the start up time
   */
  private void start(final Process process, final String pHostName, final Timestamp pStartupTime) {
    if (!stateTagService.isRunning(process.getStateTagId())) {
      forceStart(process, pHostName, pStartupTime);
    }
//    Supervised.super.resume(timestamp,message);
//    if (getLocalConfig() != null && getLocalConfig().equals(ProcessCacheObject.LocalConfig.Y)) {
//      setSupervision(SupervisionStatus.RUNNING_LOCAL, message, timestamp);
//    } TODO (Alex) Review this
  }

  @Override
  public void stop(long id, Timestamp timestamp) {
//    TODO (Alex) Make sure to also set these on the process
//    setCurrentHost(null);
//    setStartupTime(null);
//    setRequiresReboot(Boolean.FALSE);
//    setProcessPIK(null);
//    setLocalConfig(null);
  }

  /**
   * Records the start up time of the process and the host it is running on,
   * (and sets it's status to STARTUP - may remove this in the future as duplicate
   * of state tag of the DAQ). Also starts the alive timer.
   *
   * @param process      the Process that is starting
   * @param pHostName    the hostname of the Process
   * @param pStartupTime the start up time
   */
  private void forceStart(final Process process, final String pHostName, final Timestamp pStartupTime) {
    ProcessCacheObject processCacheObject = (ProcessCacheObject) process;
    final Long newPIK = createProcessPIK();
    processCacheObject.setCurrentHost(pHostName);
    processCacheObject.setStartupTime(pStartupTime);
    processCacheObject.setRequiresReboot(Boolean.FALSE);
    processCacheObject.setProcessPIK(newPIK);
    processCacheObject.setLocalConfig(ProcessCacheObject.LocalConfig.Y);

    stateTagService.getCache().compute(process.getStateTagId(), state -> state.start(pStartupTime));
  }

  /**
   * Creation of the random PIK (between PIK_MIN and PIK_MAX)
   */
  private Long createProcessPIK() {
    Random r = new Random();

    int pik = r.nextInt(PIK_MAX + 1);
    if (pik < PIK_MIN) {
      pik += PIK_MIN;
    }

    return (long) pik;
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
