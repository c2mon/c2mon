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

import cern.c2mon.cache.actions.datatag.DataTagService;
import cern.c2mon.cache.actions.equipment.EquipmentService;
import cern.c2mon.cache.api.factory.AbstractCacheObjectFactory;
import cern.c2mon.server.cache.ProcessXMLProvider;
import cern.c2mon.server.cache.loading.ConfigurableDAO;
import cern.c2mon.server.common.equipment.Equipment;
import cern.c2mon.server.configuration.impl.ProcessChange;
import cern.c2mon.shared.client.configuration.ConfigConstants.Action;
import cern.c2mon.shared.client.configuration.ConfigConstants.Entity;
import cern.c2mon.shared.client.configuration.ConfigurationElement;
import cern.c2mon.shared.client.configuration.ConfigurationElementReport;
import cern.c2mon.shared.daq.config.EquipmentUnitAdd;
import cern.c2mon.shared.daq.config.EquipmentUnitRemove;
import cern.c2mon.shared.daq.config.IChange;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

/**
 * Equipment configuration transacted methods.
 *
 * @author Mark Brightwell
 */
@Service
@Slf4j
public class EquipmentConfigHandler extends AbstractEquipmentConfigHandler<Equipment> {

  private final EquipmentService equipmentService;

  private final CommandTagConfigHandler commandTagConfigHandler;

  private final SubEquipmentConfigHandler subEquipmentConfigTransacted;

  @Autowired
  public EquipmentConfigHandler(
    final EquipmentService equipmentService,
    final ConfigurableDAO<Equipment> subEquipmentDAO,
    final AbstractCacheObjectFactory<Equipment> subEquipmentCacheObjectFactory,
    final ProcessXMLProvider processXMLProvider,
    final AliveTimerConfigHandler aliveTagConfigEventHandler,
    final DataTagService dataTagService,
    final DataTagConfigHandler dataTagConfigTransacted, CommandTagConfigHandler commandTagConfigHandler, SubEquipmentConfigHandler subEquipmentConfigTransacted) {
    super(equipmentService.getCache(), subEquipmentDAO, subEquipmentCacheObjectFactory, processXMLProvider, aliveTagConfigEventHandler, dataTagService, dataTagConfigTransacted);
    this.equipmentService = equipmentService;
    this.commandTagConfigHandler = commandTagConfigHandler;
    this.subEquipmentConfigTransacted = subEquipmentConfigTransacted;
  }

  @Override
  protected List<ProcessChange> createReturnValue(Equipment equipment, ConfigurationElement element) {
    List<ProcessChange> result = super.create(element);

    equipmentService.addEquipmentToProcess(equipment.getId(), equipment.getProcessId());

    // Please note, that the Equipment XML configuration is also containing the Alive tag configuration.
    // It's therefore not required to send an additional ProcessChange object for creating it.
    EquipmentUnitAdd equipmentUnitAdd = new EquipmentUnitAdd(element.getSequenceId(),
      equipment.getId(), processXMLProvider.getEquipmentConfigXML(equipment.getId()));

    result.add(new ProcessChange(equipment.getProcessId(), equipmentUnitAdd));

    return result;
  }

  @Override
  public List<ProcessChange> update(Long id, Properties properties) {
    removeKeyIfExists(properties, "processId");
    return super.update(id, properties);
  }

  @Override
  protected Long getProcessId(Equipment cacheable) {
    return cacheable.getProcessId();
  }

  @Override
  protected void doPreRemove(Equipment equipment, ConfigurationElementReport report) {
    //WARNING: outside equipment lock, as all these use methods that access a Process (to create ProcessChange object)!
    removeEquipmentDataTags(equipment, report);
    removeEquipmentCommands(equipment, report);
    removeSubEquipments(new ArrayList<>(equipment.getSubEquipmentIds()), report);

//    TODO (Alex)
//     removeControlTags();
  }

  @Override
  protected List<ProcessChange> removeReturnValue(Equipment equipment, ConfigurationElementReport report) {
    List<ProcessChange> processChanges = super.removeReturnValue(equipment, report);

    IChange equipmentUnitRemove = new EquipmentUnitRemove(0L, equipment.getId()); //id is reset

   processChanges.add(new ProcessChange(equipment.getProcessId(), equipmentUnitRemove));

    return processChanges;
  }

  /**
   * Removes the subequipments attached to this equipment.
   * Exceptions are caught, added to the report and thrown
   * up to interrupt the equipment removal.
   *
   * <p>Call within Equipment lock.
   *
   * @param subEquipmentIds the equipment ids for which the subequipments should be removed
   * @param equipmentReport the report at the equipment level
   */
  private void removeSubEquipments(Collection<Long> subEquipmentIds, ConfigurationElementReport equipmentReport) {
    for (Long subEquipmentId : new ArrayList<>(subEquipmentIds)) {
      ConfigurationElementReport subEquipmentReport = new ConfigurationElementReport(Action.REMOVE, Entity.SUBEQUIPMENT, subEquipmentId);
      equipmentReport.addSubReport(subEquipmentReport);
      try {
        subEquipmentConfigTransacted.remove(subEquipmentId, subEquipmentReport);
      } catch (Exception ex) {
        subEquipmentReport.setFailure("Exception caught - aborting removal of subequipment "
          + subEquipmentId, ex);
        throw new RuntimeException("Aborting reconfiguration as unable to remove subequipment.", ex);
      }

    }
  }

  /**
   * Removes the tags for this equipment. The DAQ is not informed as
   * this method is only called when the whole Equipment is removed.
   *
   * <p>Call within equipment lock.
   *
   * @param equipment for which the tags should be removed
   * @throws RuntimeException if fail to remove tag
   */
  private void removeEquipmentDataTags(Equipment equipment, ConfigurationElementReport equipmentReport) {
    for (Long dataTagId : new ArrayList<>(dataTagService.getDataTagIdsByEquipmentId(equipment.getId()))) {
      ConfigurationElementReport tagReport = new ConfigurationElementReport(Action.REMOVE, Entity.DATATAG, dataTagId);
      equipmentReport.addSubReport(tagReport);
      dataTagConfigTransacted.remove(dataTagId, tagReport);
    }
  }

  /**
   * Removes all command tags associated with this equipment.
   *
   * @param equipment       reference
   * @param equipmentReport report to add subreports to
   */
  private void removeEquipmentCommands(Equipment equipment, ConfigurationElementReport equipmentReport) {
    for (Long commandTagId : new ArrayList<>(equipment.getCommandTagIds())) {
      ConfigurationElementReport commandReport = new ConfigurationElementReport(Action.REMOVE, Entity.COMMANDTAG, commandTagId);
      equipmentReport.addSubReport(commandReport);
      commandTagConfigHandler.removeCommandTag(commandTagId, commandReport);
    }
  }

}
