package cern.c2mon.server.cache.process;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.Random;

import lombok.extern.slf4j.Slf4j;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.api.service.SupervisedService;
import cern.c2mon.server.cache.SupervisedServiceImpl;
import cern.c2mon.server.cache.alivetimer.AliveTimerService;
import cern.c2mon.server.cache.equipment.EquipmentService;
import cern.c2mon.server.common.alive.AliveTimer;
import cern.c2mon.server.common.config.ServerProperties;
import cern.c2mon.server.common.process.Process;
import cern.c2mon.server.common.process.ProcessCacheObject;
import cern.c2mon.shared.common.supervision.SupervisionConstants;

/**
 * @author Szymon Halastra
 */
@Slf4j
public class ProcessOperationServiceImpl implements ProcessOperationService {

  /**
   * PIK numbers limit (max)
   */
  private static final int PIK_MAX = 999999;
  /**
   * PIK numbers limit (min)
   */
  private static final int PIK_MIN = 100000;

  private C2monCache<Long, Process> processCache;
  private C2monCache<Long, AliveTimer> aliveTimerCache;

  private EquipmentService equipmentService;

  private ServerProperties properties;

  private SupervisedService<Process> supervisedService;

  public ProcessOperationServiceImpl(C2monCache<Long, Process> processCache, EquipmentService equipmentService,
                                     AliveTimerService aliveTimerService, ServerProperties properties) {
    this.processCache = processCache;
    this.aliveTimerCache = aliveTimerService.getCache();
    this.equipmentService = equipmentService;
    this.properties = properties;

    this.supervisedService = new SupervisedServiceImpl<>(processCache, aliveTimerService);
  }

  @Override
  public Process start(Long processId, String hostName, Timestamp startupTime) {
    Process process = null;
    processCache.lockOnKey(processId);
    try {
      process = processCache.get(processId);

      if (properties.isTestMode()) {
        // If the TEST Mode is on
        startLocal(process, hostName, startupTime);
        log.trace("start - TEST Mode - Process " + process.getName()
                + ", PIK " + process.getProcessPIK());
      }
      else {
        // If the TEST Mode is off
        start(process, hostName, startupTime);
        log.trace("start - Process " + process.getName()
                + ", PIK " + process.getProcessPIK());
      }
      processCache.put(processId, process);
    } finally {
      processCache.unlockOnKey(processId);
    }

    return process;
  }

  @Override
  public Collection<Long> getDataTagIds(Long processId) {
    processCache.lockOnKey(processId);
    try {
      ProcessCacheObject process = (ProcessCacheObject) processCache.get(processId);
      LinkedList<Long> dataTagIds = new LinkedList<>();
      for (long equipmentId : process.getEquipmentIds()) {
        dataTagIds.addAll(equipmentService.getDataTagIds(equipmentId));
      }
      return dataTagIds;
    } finally {
      processCache.unlockOnKey(processId);
    }
  }

  @Override
  public void errorStatus(Long processId, String errorMessage) {
    processCache.lockOnKey(processId);
    try {
      Process process = processCache.get(processId);
      errorStatus(process, errorMessage);
      processCache.put(processId, process);
    } finally {
      processCache.unlockOnKey(processId);
    }
  }

  @Override
  public Long getProcessIdFromAlive(Long aliveTimerId) {
    AliveTimer aliveTimer = aliveTimerCache.get(aliveTimerId);
    if (aliveTimer.isProcessAliveType()) {
      return aliveTimer.getRelatedId();
    }
    else if (aliveTimer.isEquipmentAliveType()) {
      return equipmentService.getProcessIdForAbstractEquipment(aliveTimer.getRelatedId());
    }
    else {
      Long equipmentId = 0L; /*subEquipmentFacade.getEquipmentIdForSubEquipment(aliveTimer.getRelatedId()); */ //TODO: uncomment this code, when SubEquipmentService will be written
//      return equipmentService.getProcessIdForAbstractEquipment(equipmentId);
    }
  }

  @Override
  public Long getProcessIdFromControlTag(Long controlTagId) {
    Map<Long, Long> equipmentControlTags = equipmentService.getAbstractEquipmentControlTags();
    Map<Long, Long> subEquipmentControlTags = null; /*subEquipmentFacade.getAbstractEquipmentControlTags();*/ //TODO: uncomment this code, when SubEquipmentService will be written
    if (equipmentControlTags.containsKey(controlTagId)) {
      Long equipmentId = equipmentControlTags.get(controlTagId);
      return equipmentService.getProcessIdForAbstractEquipment(equipmentId);
    }
    else if (subEquipmentControlTags.containsKey(controlTagId)) {
      Long subEquipmentId = subEquipmentControlTags.get(controlTagId);
//      return subEquipmentFacade.getEquipmentIdForSubEquipment(subEquipmentId); //TODO: uncomment this code, when SubEquipmentService will be written
    }
    else return null;
  }

  @Override
  public Boolean isRebootRequired(Long processId) {
    processCache.lockOnKey(processId);
    try {
      ProcessCacheObject process = (ProcessCacheObject) processCache.get(processId);
      return process.getRequiresReboot();
    } finally {
      processCache.unlockOnKey(processId);
    }
  }

  @Override
  public void requiresReboot(Long processId, Boolean reboot) {
    processCache.lockOnKey(processId);
    try {
      ProcessCacheObject process = (ProcessCacheObject) processCache.get(processId);
      process.setRequiresReboot(reboot);
      processCache.put(processId, process);
    } finally {
      processCache.unlockOnKey(processId);
    }
  }

  @Override
  public void setProcessPIK(Long processId, Long processPIK) {
    processCache.lockOnKey(processId);
    try {
      final ProcessCacheObject processCacheObject = (ProcessCacheObject) processCache.get(processId);
      // Set the PIK
      processCacheObject.setProcessPIK(processPIK);
      processCache.put(processId, processCacheObject);
    } finally {
      processCache.unlockOnKey(processId);
    }
  }

  @Override
  public void setLocalConfig(Long processId, ProcessCacheObject.LocalConfig localType) {
    processCache.lockOnKey(processId);
    try {
      final ProcessCacheObject processCacheObject = (ProcessCacheObject) processCache.get(processId);
      processCacheObject.setLocalConfig(localType);
      processCache.put(processId, processCacheObject);
    } finally {
      processCache.unlockOnKey(processId);
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
   *
   * @param process      the Process that is starting
   * @param pHostName    the hostname of the Process
   * @param pStartupTime the start up time
   */
  private void start(final Process process, final String pHostName, final Timestamp pStartupTime) {
    ProcessCacheObject processCacheObject = (ProcessCacheObject) process;
    if (!supervisedService.isRunning(processCacheObject)) {
      final Long newPIK = createProcessPIK();
      processCacheObject.setCurrentHost(pHostName);
      processCacheObject.setStartupTime(pStartupTime);
      processCacheObject.setRequiresReboot(Boolean.FALSE);
      processCacheObject.setProcessPIK(newPIK);
      processCacheObject.setLocalConfig(ProcessCacheObject.LocalConfig.Y);
      supervisedService.start(processCacheObject, pStartupTime);
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
  private void startLocal(final Process process, final String pHostName, final Timestamp pStartupTime) {
    ProcessCacheObject processCacheObject = (ProcessCacheObject) process;
    final Long newPIK = createProcessPIK();
    processCacheObject.setCurrentHost(pHostName);
    processCacheObject.setStartupTime(pStartupTime);
    processCacheObject.setRequiresReboot(Boolean.FALSE);
    processCacheObject.setProcessPIK(newPIK);
    processCacheObject.setLocalConfig(ProcessCacheObject.LocalConfig.Y);
    supervisedService.start(processCacheObject, pStartupTime);
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
    processCacheObject.setSupervisionStatus(SupervisionConstants.SupervisionStatus.DOWN);
  }
}
