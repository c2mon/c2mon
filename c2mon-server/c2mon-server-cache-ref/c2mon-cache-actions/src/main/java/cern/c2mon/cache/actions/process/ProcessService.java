package cern.c2mon.cache.actions.process;

import cern.c2mon.cache.actions.alivetimer.AliveTimerService;
import cern.c2mon.cache.actions.equipment.EquipmentService;
import cern.c2mon.cache.actions.subequipment.SubEquipmentService;
import cern.c2mon.cache.actions.supervision.SupervisedCacheService;
import cern.c2mon.cache.actions.supervision.SupervisedCacheServiceDelegator;
import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.api.exception.CacheElementNotFoundException;
import cern.c2mon.server.common.alive.AliveTimer;
import cern.c2mon.server.common.config.ServerProperties;
import cern.c2mon.server.common.process.Process;
import cern.c2mon.server.common.process.ProcessCacheObject;
import cern.c2mon.shared.common.supervision.SupervisionConstants;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

/**
 * @author Szymon Halastra
 * @author Alexandros Papageorgiou Koufidis
 */
@Slf4j
@Service
public class ProcessService implements ProcessOperationService, SupervisedCacheServiceDelegator<Process> {

  /**
   * PIK numbers limit (max)
   */
  private static final int PIK_MAX = 999999;
  /**
   * PIK numbers limit (min)
   */
  private static final int PIK_MIN = 100000;

  @Getter
  protected C2monCache<Process> processCacheRef;
  @Getter
  protected SupervisedCacheService<Process> supervisedService;
  private C2monCache<AliveTimer> aliveTimerCache;
  private EquipmentService equipmentService;
  private SubEquipmentService subEquipmentService;
  private ServerProperties properties;

  @Inject
  public ProcessService(C2monCache<Process> processCacheRef, EquipmentService equipmentService,
                        AliveTimerService aliveTimerService, SubEquipmentService subEquipmentService, ServerProperties properties) {
    this.processCacheRef = processCacheRef;
    this.aliveTimerCache = aliveTimerService.getCache();
    this.equipmentService = equipmentService;
    this.subEquipmentService = subEquipmentService;
    this.properties = properties;

    this.supervisedService = new SupervisedProcessServiceImpl(processCacheRef, aliveTimerService);
  }

  @Override
  public Process start(Long processId, String hostName, Timestamp startupTime) {
    Optional<Process> returnProcess = processCacheRef.executeTransaction(() -> {
      Process process = processCacheRef.get(processId);

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
      processCacheRef.put(processId, process);

      return Optional.of(process);
    });

    return returnProcess.orElseThrow(CacheElementNotFoundException::new); //TODO: make better return in case of null
  }

  @Override
  public void setErrorStatus(Long processId, String errorMessage) {
    processCacheRef.executeTransaction(() -> {
      Process process = processCacheRef.get(processId);
      errorStatus(process, errorMessage);
      processCacheRef.put(processId, process);
    });
  }

  @Override
  public Long getProcessIdFromAlive(Long aliveTimerId) {
    AliveTimer aliveTimer = aliveTimerCache.get(aliveTimerId);
    if (aliveTimer.isProcessAliveType()) {
      return aliveTimer.getRelatedId();
    } else if (aliveTimer.isEquipmentAliveType()) {
      return equipmentService.getProcessIdForAbstractEquipment(aliveTimer.getRelatedId());
    } else {
      Long equipmentId = subEquipmentService.getEquipmentIdForSubEquipment(aliveTimer.getRelatedId());
      return equipmentService.getProcessIdForAbstractEquipment(equipmentId);
    }
  }

  @Override
  public Long getProcessIdFromControlTag(Long controlTagId) {
    Map<Long, Long> equipmentControlTags = equipmentService.getAbstractEquipmentControlTags();
    Map<Long, Long> subEquipmentControlTags = subEquipmentService.getAbstractEquipmentControlTags();
    if (equipmentControlTags.containsKey(controlTagId)) {
      Long equipmentId = equipmentControlTags.get(controlTagId);
      return equipmentService.getProcessIdForAbstractEquipment(equipmentId);
    } else if (subEquipmentControlTags.containsKey(controlTagId)) {
      Long subEquipmentId = subEquipmentControlTags.get(controlTagId);
      return subEquipmentService.getEquipmentIdForSubEquipment(subEquipmentId);
    } else return null;
  }

  @Override
  public Boolean isRebootRequired(Long processId) {
    return processCacheRef.get(processId).getRequiresReboot();
  }

  @Override
  public void setRequiresReboot(Long processId, Boolean reboot) {
    processCacheRef.executeTransaction(() -> {
      ProcessCacheObject process = (ProcessCacheObject) processCacheRef.get(processId);
      process.setRequiresReboot(reboot);
      processCacheRef.put(processId, process);
    });
  }

  @Override
  public void setProcessPIK(Long processId, Long processPIK) {
    processCacheRef.executeTransaction(() -> {
      final ProcessCacheObject processCacheObject = (ProcessCacheObject) processCacheRef.get(processId);
      // Set the PIK
      processCacheObject.setProcessPIK(processPIK);
      processCacheRef.put(processId, processCacheObject);
    });
  }

  @Override
  public void setLocalConfig(Long processId, ProcessCacheObject.LocalConfig localType) {
    processCacheRef.executeTransaction(() -> {
      final ProcessCacheObject processCacheObject = (ProcessCacheObject) processCacheRef.get(processId);
      processCacheObject.setLocalConfig(localType);
      processCacheRef.put(processId, processCacheObject);
    });
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
    if (!process.isRunning()) {
      forceStart(process, pHostName, pStartupTime);
    }
  }

  /**
   * Records the start up time of the process and the host it is running on,
   * (and sets it's status to STARTUP - may remove this in the future as duplicate
   * of state tag of the DAQ)
   * <p>
   * <p>Also starts the alive timer.
   * <p>
   * <p>Please note, that in case of a cache reference to the process it is up to the calling
   * method to acquire a write lock. In case of a copy it is the calling method that has
   * to take care of committing the changes made to the process object back to the cache.
   * <p>
   * <p>This function does not check if the process is Running and use to be called by the TEST mode
   * since it will force the DAQ to start
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
    processCacheObject.start(pStartupTime);
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

  private void errorStatus(final Process process, final String errorMessage) {
    ProcessCacheObject processCacheObject = (ProcessCacheObject) process;
    processCacheObject.setSupervision(SupervisionConstants.SupervisionStatus.DOWN, errorMessage, Timestamp.from(Instant.now()));
  }
}
