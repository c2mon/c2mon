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
import cern.c2mon.cache.actions.process.ProcessService;
import cern.c2mon.server.cache.loading.ProcessDAO;
import cern.c2mon.server.common.process.Process;
import cern.c2mon.server.common.util.KotlinAPIs;
import cern.c2mon.server.configuration.config.ConfigurationProperties;
import cern.c2mon.server.configuration.impl.ProcessChange;
import cern.c2mon.server.daq.JmsContainerManager;
import cern.c2mon.shared.client.configuration.ConfigConstants;
import cern.c2mon.shared.client.configuration.ConfigurationElementReport;
import cern.c2mon.shared.common.CacheEvent;
import cern.c2mon.shared.daq.config.Change;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.UnexpectedRollbackException;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;

/**
 * Bean managing configuration updates to C2MON DataTags.
 *
 * @author Alexandros Papageorgiou
 */
@Slf4j
@Named
public class ProcessConfigHandler extends BaseConfigHandlerImpl<Process> {

  private final ProcessService processService;
  private final JmsContainerManager jmsContainerManager;
  private final EquipmentConfigHandler equipmentConfigTransacted;
  private final ControlTagHandlerCollection controlTagHandlerCollection;
  private final boolean allowRunningProcessRemoval;

  @Inject
  public ProcessConfigHandler(final ProcessDAO processDAO,
                              final ProcessCacheObjectFactory processCacheObjectFactory,
                              final ProcessService processService,
                              final ControlTagHandlerCollection controlTagHandlerCollection,
                              final ConfigurationProperties properties,
                              final JmsContainerManager jmsContainerManager,
                              final EquipmentConfigHandler equipmentConfigTransacted) {
    super(processService.getCache(), processDAO, processCacheObjectFactory, ArrayList::new);
    this.processService = processService;
    this.controlTagHandlerCollection = controlTagHandlerCollection;
    this.allowRunningProcessRemoval = properties.isAllowRunningProcessRemoval();
    this.jmsContainerManager = jmsContainerManager;
    this.equipmentConfigTransacted = equipmentConfigTransacted;
  }

  /**
   * Ensures that the Alive-, Status- have appropriately the Process id set.
   *
   * @param process The process to which the control tags are assigned
   */
  @Override
  protected void doPostCreate(Process process) {
    // TODO (Alex) Switch to CacheEvent.INSERTED ?
    jmsContainerManager.subscribe(process);

    cache.getCacheListenerManager().notifyListenersOf(CacheEvent.INSERTED, process);

    processService.updateControlTagCacheIds(process);
  }

  @Override
  public List<ProcessChange> update(Long id, Properties properties) {
    removeKeyIfExists(properties, "id");
    removeKeyIfExists(properties, "name");

    List<ProcessChange> processChanges = super.update(id, properties);

    // TODO (Alex) Should we also check for commFault and state tag id?
    if (properties.containsKey("aliveInterval") || properties.containsKey("aliveTagId")) {
      Process process = processService.getCache().get(id);
//      aliveTagService.updateBasedOnSupervised(process);
    }

    return processChanges;
  }

  @Override
  protected List<ProcessChange> updateReturnValue(Process process, Change change, Properties properties) {
    return Collections.singletonList(
      KotlinAPIs.apply(
        new ProcessChange(process.getId(), change),
        procChange -> procChange.requiresReboot(true))
    );
  }

  /**
   * Tries to remove the process and all its descendants. The process
   * itself is only completely removed if all the equipments, subequipments
   * and associated tags, commands are all removed successfully.
   * <p>
   * In the case of a failure, the removal is interrupted and the process
   * remains with whatever child objects remain at the point of failure.
   *
   * @param id     id of process
   * @param report the element report for the removal of the process, to which
   *               subreports can be attached
   */
  @Override
  public List<ProcessChange> remove(Long id, ConfigurationElementReport report) {
    boolean isRunning = processService.isRunning(id);

    // Save a copy to be able to cascade remove later
    Process process = cache.get(id);

    if (isRunning && !allowRunningProcessRemoval) {
      String message = "Unable to remove Process " + process.getName() + " as currently running - please stop it first.";
      log.warn(message);
      report.setFailure(message);
      return defaultValue.get();
    }

    return KotlinAPIs.applyNotNull(super.remove(id, report), __ -> {
      log.debug("Removing Process control tags for process {}", process.getName());
      controlTagHandlerCollection.cascadeRemove(process, report);

      jmsContainerManager.unsubscribe(process);
    });
  }

  @Override
  protected void doPreRemove(Process process, ConfigurationElementReport report) {
    Collection<Long> equipmentIds = new ArrayList<>(process.getEquipmentIds());

    //remove all associated equipment from system
    for (Long equipmentId : equipmentIds) {
      ConfigurationElementReport childElementReport = new ConfigurationElementReport(ConfigConstants.Action.REMOVE, ConfigConstants.Entity.EQUIPMENT, equipmentId);
      try {
        report.addSubReport(childElementReport);
        equipmentConfigTransacted.remove(equipmentId, childElementReport);
      } catch (RuntimeException ex) {
        log.error("Exception caught while applying the configuration change (Action, Entity, Entity id) = ("
          + ConfigConstants.Action.REMOVE + "; " + ConfigConstants.Entity.EQUIPMENT + "; " + equipmentId + ")", ex);
        childElementReport.setFailure("Exception caught while applying the configuration change.", ex);
        throw new UnexpectedRollbackException("Unexpected exception caught while removing an Equipment.", ex);
      }
    }
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

}
