/******************************************************************************
 * Copyright (C) 2010-2018 CERN. All rights not expressly granted are reserved.
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

import cern.c2mon.cache.actions.process.ProcessCacheObjectFactory;
import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.api.exception.CacheElementNotFoundException;
import cern.c2mon.server.cache.loading.ProcessDAO;
import cern.c2mon.server.common.alive.AliveTimer;
import cern.c2mon.server.common.alive.AliveTimerCacheObject;
import cern.c2mon.server.common.process.Process;
import cern.c2mon.server.configuration.ConfigurationLoader;
import cern.c2mon.server.configuration.handler.ProcessConfigHandler;
import cern.c2mon.server.configuration.impl.ProcessChange;
import cern.c2mon.shared.client.configuration.ConfigurationElement;
import cern.c2mon.shared.client.configuration.ConfigurationElementReport;
import cern.c2mon.shared.common.ConfigurationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Properties;

/**
 * See interface docs.
 *
 * @author Mark Brightwell
 */
@Slf4j
@Service
public class ProcessConfigTransactedImpl implements ProcessConfigHandler {

  private final ProcessCacheObjectFactory processCacheObjectFactory;

  private final C2monCache<Process> processCache;

  private final ProcessDAO processDAO;
  private final C2monCache<AliveTimer> aliveTimerCache;


  /**
   * Autowired constructor.
   *
   * @param processCache the cache bean
   * @param processDAO   the DAO bean
   */
  @Autowired
  public ProcessConfigTransactedImpl(final C2monCache<Process> processCache, final ProcessDAO processDAO,
                                     final C2monCache<AliveTimer> aliveTimerCache,
                                     final ProcessCacheObjectFactory processCacheObjectFactory) {
    super();
    this.processCache = processCache;
    this.processDAO = processDAO;
    this.aliveTimerCache = aliveTimerCache;
    this.processCacheObjectFactory = processCacheObjectFactory;
  }

  /**
   * Creates the process and inserts it into the cache and DB (DB first).
   *
   * Changing a process id is not currently allowed.
   *
   * @param element the configuration element
   */
  @Override
  @Transactional(value = "cacheTransactionManager")
  public ProcessChange create(final ConfigurationElement element) {
    return processCache.executeTransaction(() -> {
      Process process = processCacheObjectFactory.createCacheObject(element.getEntityId(), element.getElementProperties());
      processDAO.insert(process);
      processCache.putQuiet(process.getId(), process);
      updateControlTagInformation(process);
      return new ProcessChange(process.getId());
    });
  }

  /**
   * No changes to the Process configuration are currently passed to the DAQ
   * layer, but the Configuration object is already build into the logic below
   * (always empty and hence ignored in the {@link ConfigurationLoader}).
   *
   * @return change requiring DAQ reboot, but not to be sent to the DAQ layer
   * (not supported)
   */
  @Override
  @Transactional(value = "cacheTransactionManager")
  public ProcessChange update(final Long id, final Properties properties) {
    processCache.compute(id, process -> {
      processCacheObjectFactory.updateConfig(process, properties);
      processDAO.updateConfig(process);
    });

    return new ProcessChange(id);
  }

  @Override
  @Transactional(value = "cacheTransactionManager", propagation = Propagation.REQUIRES_NEW)
  public ProcessChange remove(final Long id, final ConfigurationElementReport processReport) {
    processDAO.deleteProcess(id);
    return new ProcessChange();
  }

  @Override
  public void removeEquipmentFromProcess(Long equipmentId, Long processId) {
    log.debug("Removing Process Equipment {} for processId {}", equipmentId, processId);
    processCache.compute(processId, process -> process.getEquipmentIds().remove(equipmentId));
  }

  /**
   * Ensures that the Alive-, Status- have appropriately the Process id set.
   *
   * @param process The equipment to which the control tags are assigned
   */
  private void updateControlTagInformation(final Process process) {
    // TODO (Alex) ComputeQuiet
    try {
      aliveTimerCache.compute(process.getAliveTagId(), aliveTimer -> {
        log.trace("Adding process id #{} to alive timer {} (#{})", process.getId(), aliveTimer.getRelatedName(), aliveTimer.getId());
        ((AliveTimerCacheObject) aliveTimer).setRelatedId(process.getId());
      });
    } catch (CacheElementNotFoundException e) {
      throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE,
        String.format("No Alive tag (%s) found for process #%d (%s).", process.getAliveTagId(), process.getId(), process.getName()));
    }
  }
}
