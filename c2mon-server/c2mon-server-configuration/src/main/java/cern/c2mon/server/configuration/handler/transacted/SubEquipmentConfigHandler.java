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
import cern.c2mon.cache.actions.process.ProcessXMLProvider;
import cern.c2mon.cache.actions.subequipment.SubEquipmentCacheObjectFactory;
import cern.c2mon.cache.actions.subequipment.SubEquipmentService;
import cern.c2mon.server.cache.loading.SubEquipmentDAO;
import cern.c2mon.server.common.subequipment.SubEquipment;
import cern.c2mon.server.common.subequipment.SubEquipmentCacheObject;
import cern.c2mon.server.configuration.impl.ProcessChange;
import cern.c2mon.shared.client.configuration.ConfigConstants;
import cern.c2mon.shared.client.configuration.ConfigurationElement;
import cern.c2mon.shared.client.configuration.ConfigurationElementReport;
import cern.c2mon.shared.daq.config.SubEquipmentUnitAdd;
import cern.c2mon.shared.daq.config.SubEquipmentUnitRemove;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;
import java.util.Properties;

/**
 * @author Alexandros Papageorgiou
 */
@Named
@Slf4j
public class SubEquipmentConfigHandler extends AbstractEquipmentConfigHandler<SubEquipment> {

  private final SubEquipmentService subEquipmentService;
  private final EquipmentService equipmentService;

  @Inject
  public SubEquipmentConfigHandler(final SubEquipmentService subEquipmentService,
                                   final SubEquipmentDAO subEquipmentDAO,
                                   final SubEquipmentCacheObjectFactory subEquipmentCacheObjectFactory,
                                   final ProcessXMLProvider processXMLProvider,
                                   final AliveTagConfigHandler aliveTagConfigEventHandler,
                                   final DataTagService dataTagService,
                                   final DataTagConfigHandler dataTagConfigTransacted,
                                   final EquipmentService equipmentService,
                                   final ControlTagHandlerCollection controlTagHandlerCollection) {
    super(subEquipmentService.getCache(), subEquipmentDAO, subEquipmentCacheObjectFactory,
      processXMLProvider, aliveTagConfigEventHandler, dataTagService, dataTagConfigTransacted, controlTagHandlerCollection);
    this.subEquipmentService = subEquipmentService;
    this.equipmentService = equipmentService;
  }

  @Override
  protected List<ProcessChange> createReturnValue(SubEquipment subEquipment, ConfigurationElement element) {
    List<ProcessChange> changes = super.createReturnValue(subEquipment, element);

    SubEquipmentUnitAdd subEquipmentUnitAdd = new SubEquipmentUnitAdd(element.getSequenceId(), subEquipment.getId(), subEquipment.getParentId(),
      processXMLProvider.getSubEquipmentConfigXML((SubEquipmentCacheObject) subEquipment));

    changes.add(new ProcessChange(equipmentService.getProcessId(subEquipment.getParentId()), subEquipmentUnitAdd));
    return changes;
  }

  @Override
  public List<ProcessChange> update(Long id, Properties properties) {
    removeKeyIfExists(properties, "parent_equip_id");
    removeKeyIfExists(properties, "equipmentId");
    return super.update(id, properties);
  }

  @Override
  protected Long getProcessId(SubEquipment subEquipment) {
    return equipmentService.getProcessId(subEquipment.getParentId());
  }

  @Override
  public List<ProcessChange> remove(Long id, ConfigurationElementReport report) {
    SubEquipmentCacheObject subEquipmentCacheObject = (SubEquipmentCacheObject) cache.get(id);
    List<ProcessChange> cascadeChanges = cascadeRemoveDatatags(dataTagService.getDataTagIdsBySubEquipmentId(id), report);
    cascadeChanges.addAll(super.remove(id, report));
    cascadeChanges.addAll(controlTagHandlerCollection.cascadeRemove(subEquipmentCacheObject, report));

    return cascadeChanges;
  }

  @Override
  protected List<ProcessChange> removeReturnValue(SubEquipment subEquipment, ConfigurationElementReport report) {
    List<ProcessChange> processChanges = super.removeReturnValue(subEquipment, report);

    SubEquipmentUnitRemove subEquipmentUnitRemove = new SubEquipmentUnitRemove(0L, subEquipment.getId(), subEquipment.getParentId());

    Long processId = getProcessId(subEquipment);

    processChanges.add(new ProcessChange(processId, subEquipmentUnitRemove));

    return processChanges;
  }

  /**
   * Removes the subequipments attached to this equipment.
   * Exceptions are caught, added to the report and thrown
   * up to interrupt the equipment removal.
   *
   * <p>Call within Equipment lock.
   *
   * @param equipmentId     the equipment id for which the subequipments should be removed
   * @param equipmentReport the report at the equipment level
   */
  public void removeSubEquipmentsByEqId(long equipmentId, ConfigurationElementReport equipmentReport) {
    for (Long subEquipmentId : subEquipmentService.getSubEquipmentIdsFor(equipmentId)) {
      ConfigurationElementReport subEquipmentReport = new ConfigurationElementReport(ConfigConstants.Action.REMOVE, ConfigConstants.Entity.SUBEQUIPMENT, subEquipmentId);
      equipmentReport.addSubReport(subEquipmentReport);
      try {
        remove(subEquipmentId, subEquipmentReport);
      } catch (Exception ex) {
        subEquipmentReport.setFailure("Exception caught - aborting removal of subequipment "
          + subEquipmentId, ex);
        throw new RuntimeException("Aborting reconfiguration as unable to remove subequipment.", ex);
      }

    }
  }

}
