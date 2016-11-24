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
package cern.c2mon.server.configuration.handler.transacted;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.UnexpectedRollbackException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import cern.c2mon.server.cache.ControlTagCache;
import cern.c2mon.server.cache.ControlTagFacade;
import cern.c2mon.server.cache.ProcessCache;
import cern.c2mon.server.cache.ProcessFacade;
import cern.c2mon.server.cache.loading.ProcessDAO;
import cern.c2mon.server.common.control.ControlTag;
import cern.c2mon.server.common.control.ControlTagCacheObject;
import cern.c2mon.server.common.process.Process;
import cern.c2mon.server.configuration.ConfigurationLoader;
import cern.c2mon.server.configuration.impl.ProcessChange;
import cern.c2mon.server.daq.JmsContainerManager;
import cern.c2mon.shared.client.configuration.ConfigurationElement;
import cern.c2mon.shared.client.configuration.ConfigurationElementReport;
import cern.c2mon.shared.common.ConfigurationException;

/**
 * See interface docs.
 *
 * @author Mark Brightwell
 *
 */
@Service
public class ProcessConfigTransactedImpl implements ProcessConfigTransacted {

  /**
   * Class logger.
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(ProcessConfigTransactedImpl.class);

  /**
   * Reference to facade.
   */
  private final ProcessFacade processFacade;

  /**
   * Reference to cache.
   */
  private final ProcessCache processCache;

  /**
   * Reference to DAO.
   */
  private final ProcessDAO processDAO;

  private final ControlTagCache controlCache;

  /**
   * Autowired constructor.
   *
   * @param processFacade the facade bean
   * @param processCache the cache bean
   * @param processDAO the DAO bean
   * @param jmsContainerManager JmsContainerManager bean
   * @param controlCache the control tag cache
   * @param controlTagFacade The control tag facade
   */
  @Autowired
  public ProcessConfigTransactedImpl(final ProcessFacade processFacade, final ProcessCache processCache, final ProcessDAO processDAO,
      final JmsContainerManager jmsContainerManager, ControlTagCache controlCache, ControlTagFacade controlTagFacade) {
    super();
    this.processFacade = processFacade;
    this.processCache = processCache;
    this.processDAO = processDAO;
    this.controlCache = controlCache;
  }

  /**
   * Creates the process and inserts it into the cache and DB (DB first).
   *
   * <p>
   * Changing a process id is not currently allowed.
   *
   * @param element
   *          the configuration element
   * @throws IllegalAccessException
   *           not thrown (inherited from common facade interface)
   */
  @Override
  @Transactional(value = "cacheTransactionManager")
  public ProcessChange doCreateProcess(final ConfigurationElement element) throws IllegalAccessException {
    processCache.acquireWriteLockOnKey(element.getEntityId());
    try {
      Process process = (Process) processFacade.createCacheObject(element.getEntityId(), element.getElementProperties());
      processDAO.insert(process);
      processCache.putQuiet(process);

      updateControlTagInformation(element, process);

      return new ProcessChange(process.getId());
    } finally {
      processCache.releaseWriteLockOnKey(element.getEntityId());
    }
  }

  /**
   * No changes to the Process configuration are currently passed to the DAQ
   * layer, but the Configuration object is already build into the logic below
   * (always empty and hence ignored in the {@link ConfigurationLoader}).
   *
   * @param id
   * @param properties
   * @return change requiring DAQ reboot, but not to be sent to the DAQ layer
   *         (not supported)
   * @throws IllegalAccessException
   */
  @Override
  @Transactional(value = "cacheTransactionManager")
  public ProcessChange doUpdateProcess(final Long id, final Properties properties) throws IllegalAccessException {

    processCache.acquireWriteLockOnKey(id);
    try {
      Process processCopy = processCache.getCopy(id);

      processFacade.updateConfig(processCopy, properties);
      processDAO.updateConfig(processCopy);
      processCache.put(id, processCopy);
    }
    finally {
      processCache.releaseWriteLockOnKey(id);
    }

    return new ProcessChange(id);
  }

  @Override
  @Transactional(value = "cacheTransactionManager", propagation = Propagation.REQUIRES_NEW)
  public ProcessChange doRemoveProcess(final Process process, final ConfigurationElementReport processReport) {
    processDAO.deleteProcess(process.getId());
    return new ProcessChange();
  }

  @Override
  public void removeEquipmentFromProcess(Long equipmentId, Long processId) {
    LOGGER.debug("Removing Process Equipments for process " + processId);
    try {
      processCache.acquireWriteLockOnKey(processId);
      try {
        Process process = processCache.get(processId);
        process.getEquipmentIds().remove(equipmentId);
      } finally {
        processCache.releaseWriteLockOnKey(processId);
      }
    } catch (RuntimeException e) {
      throw new UnexpectedRollbackException("Unable to remove equipment reference in process.", e);
    }
  }

  /**
   * Ensures that the Alive-, Status- and CommFault Tags have appropriately the Process id set.
   * @param process The equipment to which the control tags are assigned
   */
  private List<ProcessChange> updateControlTagInformation(final ConfigurationElement element, final Process process) {

      List<ProcessChange> changes = new ArrayList<ProcessChange>(3);
      Long processId = process.getId();

      ControlTag aliveTagCopy = controlCache.getCopy(process.getAliveTagId());
      if (aliveTagCopy != null) {
        setProcessId((ControlTagCacheObject) aliveTagCopy, processId);
      } else {
        throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE,
            String.format("No Alive tag (%s) found for process #%d (%s).", process.getAliveTagId(), process.getId(), process.getName()));
      }

      ControlTag statusTagCopy = controlCache.getCopy(process.getStateTagId());
      if (statusTagCopy != null) {
        setProcessId((ControlTagCacheObject) statusTagCopy, processId);
      } else {
        throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE,
            String.format("No Status tag (%s) found for process #%d (%s).", process.getStateTagId(), process.getId(), process.getName()));
      }

      return changes;
  }

  private void setProcessId(ControlTagCacheObject copy, Long processId) {
    String logMsg = String.format("Adding process id #%s to control tag #%s", processId, copy.getId());
    LOGGER.trace(logMsg);
    copy.setProcessId(processId);
    controlCache.putQuiet(copy);
  }
}
