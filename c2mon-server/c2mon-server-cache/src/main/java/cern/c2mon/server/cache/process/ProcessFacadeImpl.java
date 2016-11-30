/******************************************************************************
 * Copyright (C) 2010-2016 CERN. All rights not expressly granted are reserved.
 *
 * This file is part of the CERN Control and Monitoring Platform 'C2MON'.
 * C2MON is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the license.
 *
 * C2MON is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
 * more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with C2MON. If not, see <http://www.gnu.org/licenses/>.
 *****************************************************************************/
package cern.c2mon.server.cache.process;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;
import java.util.Random;

import cern.c2mon.server.common.config.ServerProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import cern.c2mon.server.cache.AliveTimerCache;
import cern.c2mon.server.cache.AliveTimerFacade;
import cern.c2mon.server.cache.EquipmentFacade;
import cern.c2mon.server.cache.ProcessCache;
import cern.c2mon.server.cache.ProcessFacade;
import cern.c2mon.server.cache.SubEquipmentFacade;
import cern.c2mon.server.cache.common.AbstractSupervisedFacade;
import cern.c2mon.server.common.alive.AliveTimer;
import cern.c2mon.server.common.process.Process;
import cern.c2mon.server.common.process.ProcessCacheObject;
import cern.c2mon.server.common.process.ProcessCacheObject.LocalConfig;
import cern.c2mon.shared.common.ConfigurationException;
import cern.c2mon.shared.common.supervision.SupervisionConstants.SupervisionEntity;
import cern.c2mon.shared.common.supervision.SupervisionConstants.SupervisionStatus;
import cern.c2mon.shared.daq.config.ProcessConfigurationUpdate;

/**
 * Facade object containing all the logic for modifying a ProcessCacheObject.
 * @author Mark Brightwell
 *
 */
@Service
public class ProcessFacadeImpl extends AbstractSupervisedFacade<Process> implements ProcessFacade {

  /**
   * Class logger.
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(ProcessFacadeImpl.class);

  /** PIK numbers limit (max) */
  private static final int PIK_MAX = 999999;
  /** PIK numbers limit (min) */
  private static final int PIK_MIN = 100000;

  private EquipmentFacade equipmentFacade;

  private SubEquipmentFacade subEquipmentFacade;

  private ProcessCache processCache;

  private AliveTimerCache aliveTimerCache;

  private ServerProperties properties;

  @Autowired
  public ProcessFacadeImpl(final EquipmentFacade equipmentFacade,
                           final ProcessCache processCache,
                           final SubEquipmentFacade subEquipmentFacade,
                           final AliveTimerCache aliveTimerCache,
                           final AliveTimerFacade aliveTimerFacade,
                           final ServerProperties properties) {
    super(processCache, aliveTimerCache, aliveTimerFacade);
    this.equipmentFacade = equipmentFacade;
    this.processCache = processCache;
    this.subEquipmentFacade = subEquipmentFacade;
    this.aliveTimerCache = aliveTimerCache;
    this.properties = properties;
  }

  @Override
  public Process createCacheObject(final Long id, final Properties properties) {
    ProcessCacheObject process = new ProcessCacheObject(id);
    configureCacheObject(process, properties);
    process.setSupervisionStatus(SupervisionStatus.DOWN);
    validateConfig(process);
    return process;
  }

  protected ProcessConfigurationUpdate configureCacheObject(final Process process, final Properties properties) {
    ProcessCacheObject processCacheObject = (ProcessCacheObject) process;
    ProcessConfigurationUpdate configurationUpdate = new ProcessConfigurationUpdate();
    configurationUpdate.setProcessId(process.getId());
    String tmpStr = null;
    if (properties.getProperty("name") != null) {
      processCacheObject.setName(properties.getProperty("name"));
    }
    if (properties.getProperty("description") != null) {
      processCacheObject.setDescription(properties.getProperty("description"));
    }
    if ((tmpStr = properties.getProperty("aliveInterval")) != null) {
      try {
        processCacheObject.setAliveInterval(Integer.valueOf(tmpStr));
      }
      catch (NumberFormatException e) {
        throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "NumberFormatException: Unable to convert parameter \"aliveInterval\" to Integer: " + tmpStr);
      }
    }
    if ((tmpStr = properties.getProperty("aliveTagId")) != null) {
      try {
        processCacheObject.setAliveTagId(Long.valueOf(tmpStr));
      }
      catch (NumberFormatException e) {
        throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "NumberFormatException: Unable to convert parameter \"aliveTagId\" to Long: " + tmpStr);
      }
    }
    if ((tmpStr = properties.getProperty("stateTagId")) != null || (tmpStr = properties.getProperty("statusTagId")) != null ) {
      try {
        processCacheObject.setStateTagId(Long.valueOf(tmpStr));
      }
      catch (NumberFormatException e) {
        throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "NumberFormatException: Unable to convert parameter \"stateTagId\" to Long: " + tmpStr);
      }
    }
    if ((tmpStr = properties.getProperty("maxMessageSize")) != null) {
      try {
        processCacheObject.setMaxMessageSize(Integer.parseInt(tmpStr));
      }
      catch (NumberFormatException e) {
        throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "NumberFormatException: Unable to convert parameter \"maxMessageSize\" to int: " + tmpStr);
      }
    }

    if ((tmpStr = properties.getProperty("maxMessageDelay")) != null) {
      try {
        processCacheObject.setMaxMessageDelay(Integer.parseInt(tmpStr));
      }
      catch (NumberFormatException e) {
        throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "NumberFormatException: Unable to convert parameter \"maxMessageDelay\" to int: " + tmpStr);
      }
    }

    return configurationUpdate;
  }

  /**
   * Adds an Equipment to the list of Equipments under this Process.
   * @param processCacheObject the Process
   * @param pEquipmentId id of the Equipment to add
   */
  public void addEquipmentId(final ProcessCacheObject processCacheObject, final Long pEquipmentId) {
    if (!processCacheObject.getEquipmentIds().contains(pEquipmentId)) {
      processCacheObject.getEquipmentIds().add(pEquipmentId);
    }
  }

  /**
   * Removes an Equipment from the list of Equipments under this Process.
   * @param processCacheObject the process
   * @param pEquipmentId the id of the Equipment
   */
  public void removeEquipmentId(final ProcessCacheObject processCacheObject, final Long pEquipmentId) {
    if (processCacheObject.getEquipmentIds().contains(pEquipmentId)) {
      processCacheObject.getEquipmentIds().remove(pEquipmentId);
    }
  }

  @Override
  protected final void stop(final Process process, Timestamp timestamp) {
    processCache.acquireWriteLockOnKey(process.getId());
    try {
      ProcessCacheObject processCacheObject = (ProcessCacheObject) process;
      processCacheObject.setCurrentHost(null);
      processCacheObject.setStartupTime(null);
      processCacheObject.setRequiresReboot(Boolean.FALSE);
      processCacheObject.setProcessPIK(null);
      processCacheObject.setLocalConfig(null);
      super.stop(process, timestamp);
    } finally {
      processCache.releaseWriteLockOnKey(process.getId());
    }
  }

  @Override
  public Process start(final Long processId, final String pHostName, final Timestamp pStartupTime) {
    Process process = null;
    processCache.acquireWriteLockOnKey(processId);
    try {
      process = processCache.get(processId);

      if (properties.isTestMode()) {
        // If the TEST Mode is on
        startLocal(process, pHostName, pStartupTime);
        LOGGER.trace("start - TEST Mode - Process " + process.getName()
            + ", PIK " + process.getProcessPIK());
      } else {
        // If the TEST Mode is off
        start(process, pHostName, pStartupTime);
        LOGGER.trace("start - Process " + process.getName()
            + ", PIK " + process.getProcessPIK());
      }
      processCache.put(processId, process);
    } finally {
      processCache.releaseWriteLockOnKey(processId);
    }

    return process;
  }

  /**
   * Records the start up time of the process and the host it is running on,
   * (and sets it's status to STARTUP - may remove this in the future as duplicate
   * of state tag of the DAQ)
   *
   * <p>Also starts the alive timer.
   *
   * <p>Please note, that in case of a cache reference to the process it is up to the calling
   * method to acquire a write lock. In case of a copy it is the calling method that has
   * to take care of committing the changes made to the process object back to the cache.
   *
   * @param process the Process that is starting
   * @param pHostName the hostname of the Process
   * @param pStartupTime the start up time
   */
  private void start(final Process process, final String pHostName, final Timestamp pStartupTime) {
    ProcessCacheObject processCacheObject = (ProcessCacheObject) process;
    if (!isRunning(processCacheObject)) {
      final Long newPIK = createProcessPIK();
      processCacheObject.setCurrentHost(pHostName);
      processCacheObject.setStartupTime(pStartupTime);
      processCacheObject.setRequiresReboot(Boolean.FALSE);
      processCacheObject.setProcessPIK(newPIK);
      processCacheObject.setLocalConfig(LocalConfig.Y);
      super.start(processCacheObject, pStartupTime);
    }
  }

  /**
   * Records the start up time of the process and the host it is running on,
   * (and sets it's status to STARTUP - may remove this in the future as duplicate
   * of state tag of the DAQ)
   *
   * <p>Also starts the alive timer.
   *
   * <p>Please note, that in case of a cache reference to the process it is up to the calling
   * method to acquire a write lock. In case of a copy it is the calling method that has
   * to take care of committing the changes made to the process object back to the cache.
   *
   * <p>This function does not check if the process is Running and use to be called by the TEST mode
   * since it will force the DAQ to start
   *
   * @param process the Process that is starting
   * @param pHostName the hostname of the Process
   * @param pStartupTime the start up time
   */
  private void startLocal(final Process process, final String pHostName, final Timestamp pStartupTime) {
    ProcessCacheObject processCacheObject = (ProcessCacheObject) process;
    final Long newPIK = createProcessPIK();
    processCacheObject.setCurrentHost(pHostName);
    processCacheObject.setStartupTime(pStartupTime);
    processCacheObject.setRequiresReboot(Boolean.FALSE);
    processCacheObject.setProcessPIK(newPIK);
    processCacheObject.setLocalConfig(LocalConfig.Y);
    super.start(processCacheObject, pStartupTime);
  }

  @Override
  public void errorStatus(final Long processId, final String errorMessage) {
    processCache.acquireWriteLockOnKey(processId);
    try {
      Process process = processCache.get(processId);
      errorStatus(process, errorMessage);
      processCache.put(processId, process);
    } finally {
      processCache.releaseWriteLockOnKey(processId);
    }
  }

  private void errorStatus(final Process process, final String errorMessage) {
      ProcessCacheObject processCacheObject = (ProcessCacheObject) process;
      processCacheObject.setSupervisionStatus(SupervisionStatus.DOWN);
  }

  /**
   * Validate the configuration of the Process object.
   * If the configuration information contained in the object
   * is inconsistent or doesn't meet a set of predefined
   * constraints, a ConfigurationException will be thrown.
   * The ConfigurationException will contain more information
   * about the source of the problem.
   * @throws ConfigurationException
   */
  protected void validateConfig(final Process process) throws ConfigurationException {
    processCache.acquireReadLockOnKey(process.getId());
    try {
      ProcessCacheObject processCacheObject = (ProcessCacheObject) process;
      if (processCacheObject.getId() == null) {
        throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "Parameter \"id\" cannot be null");
      }
      if (processCacheObject.getName() == null) {
        throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "Parameter \"name\" cannot be null");
      }
      if (processCacheObject.getName().length() == 0 || processCacheObject.getName().length() > 60) {
        throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "Parameter \"name\" must be 1 to 60 characters long");
      }
      if (!ProcessCacheObject.PROCESS_NAME_PATTERN.matcher(processCacheObject.getName()).matches()) {
        throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "Parameter \"name\" must match the following pattern: " + ProcessCacheObject.PROCESS_NAME_PATTERN.toString());
      }
      if (processCacheObject.getDescription() == null) {
        throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "Parameter \"description\" cannot be null");
      }
      if (processCacheObject.getDescription().length() == 0 || processCacheObject.getDescription().length() > 100) {
        throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "Parameter \"description\" must be 1 to 100 characters long");
      }
      if (processCacheObject.getStateTagId() == null) {
        throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "Parameter \"stateTagId\" cannot be null");
      }
      if (processCacheObject.getAliveTagId() == null) {
        throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "Parameter \"aliveTagId\" cannot be null");
      }
      if (processCacheObject.getAliveInterval() < 10000) {
        throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "Parameter \"aliveInterval\" must be >= 10000 milliseconds");
      }
      if (processCacheObject.getMaxMessageSize() < 1) {
        throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "Parameter \"maxMessageSize\" must be >= 1");
      }
      if (processCacheObject.getMaxMessageDelay() < 100) {
        throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "Parameter \"maxMessageDelay\" must be >= 100");
      }
      if (processCacheObject.getEquipmentIds() == null) {
        throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "Collection \"equipmentIds\" cannot be null");
      }
    } finally {
      processCache.releaseReadLockOnKey(process.getId());
    }
  }

  @Override
  public Collection<Long> getDataTagIds(final Long processId) {
    processCache.acquireReadLockOnKey(processId);
    try {
      ProcessCacheObject process = (ProcessCacheObject) processCache.get(processId);
      LinkedList<Long> dataTagIds = new LinkedList<Long>();
      for (long equipmentId : process.getEquipmentIds()) {
        dataTagIds.addAll(equipmentFacade.getDataTagIds(equipmentId));
      }
      return dataTagIds;
    } finally {
      processCache.releaseReadLockOnKey(processId);
    }
  }

  @Override
  public Long getProcessIdFromAlive(final Long aliveTimerId) {
    AliveTimer aliveTimer = aliveTimerCache.getCopy(aliveTimerId);
    if (aliveTimer.isProcessAliveType()) {
      return aliveTimer.getRelatedId();
    } else if (aliveTimer.isEquipmentAliveType()) {
      return equipmentFacade.getProcessIdForAbstractEquipment(aliveTimer.getRelatedId());
    } else {
      Long equipmentId = subEquipmentFacade.getEquipmentIdForSubEquipment(aliveTimer.getRelatedId());
      return equipmentFacade.getProcessIdForAbstractEquipment(equipmentId);
    }
  }

  @Override
  public Long getProcessIdFromControlTag(final Long controlTagId) {
    Map<Long, Long> equipmentControlTags = equipmentFacade.getAbstractEquipmentControlTags();
    Map<Long, Long> subEquipmentControlTags = subEquipmentFacade.getAbstractEquipmentControlTags();
    if (equipmentControlTags.containsKey(controlTagId)) {
      Long equipmentId = equipmentControlTags.get(controlTagId);
      return equipmentFacade.getProcessIdForAbstractEquipment(equipmentId);
    } else if (subEquipmentControlTags.containsKey(controlTagId)) {
      Long subEquipmentId = subEquipmentControlTags.get(controlTagId);
      return subEquipmentFacade.getEquipmentIdForSubEquipment(subEquipmentId);
    } else return null;
  }

  @Override
  protected SupervisionEntity getSupervisionEntity() {
    return SupervisionEntity.PROCESS;
  }

  @Override
  public Boolean isRebootRequired(final Long processId) {
    processCache.acquireReadLockOnKey(processId);
    try {
      ProcessCacheObject process = (ProcessCacheObject) processCache.get(processId);
      return process.getRequiresReboot();
    } finally {
      processCache.releaseReadLockOnKey(processId);
    }
  }

  @Override
  public void requiresReboot(final Long processId, final Boolean reboot) {
    processCache.acquireWriteLockOnKey(processId);
    try {
      ProcessCacheObject process = (ProcessCacheObject) processCache.get(processId);
      process.setRequiresReboot(reboot);
      processCache.put(processId, process);
    } finally {
      processCache.releaseWriteLockOnKey(processId);
    }
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

    return Long.valueOf(pik);
  }

  @Override
  public void setProcessPIK(final Long processId, final Long processPIK) {
    processCache.acquireWriteLockOnKey(processId);
    try {
      final ProcessCacheObject processCacheObject = (ProcessCacheObject) processCache.getCopy(processId);
      // Set the PIK
      processCacheObject.setProcessPIK(processPIK);
      processCache.put(processId, processCacheObject);
    } finally {
      processCache.releaseWriteLockOnKey(processId);
    }
  }

  @Override
  public void setLocalConfig(final Long processId, final LocalConfig localConfig) {
    processCache.acquireWriteLockOnKey(processId);
    try {
      final ProcessCacheObject processCacheObject = (ProcessCacheObject) processCache.getCopy(processId);
      processCacheObject.setLocalConfig(localConfig);
      processCache.put(processId, processCacheObject);
    } finally {
      processCache.releaseWriteLockOnKey(processId);
    }
  }
}
