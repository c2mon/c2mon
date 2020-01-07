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
import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.server.cache.loading.SubEquipmentDAO;
import cern.c2mon.server.common.subequipment.SubEquipment;
import cern.c2mon.server.common.subequipment.SubEquipmentCacheObject;
import cern.c2mon.server.configuration.impl.ProcessChange;
import cern.c2mon.shared.client.configuration.ConfigurationElement;
import cern.c2mon.shared.client.configuration.ConfigurationElementReport;
import cern.c2mon.shared.daq.config.SubEquipmentUnitAdd;
import cern.c2mon.shared.daq.config.SubEquipmentUnitRemove;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Properties;

/**
 * @author Alexandros Papageorgiou
 */
@Service
@Slf4j
public class SubEquipmentConfigHandler extends AbstractEquipmentConfigHandler<SubEquipment> {

  private final EquipmentService equipmentService;

  @Autowired
  public SubEquipmentConfigHandler(final C2monCache<SubEquipment> subEquipmentCache,
                                   final SubEquipmentDAO subEquipmentDAO,
                                   final SubEquipmentCacheObjectFactory subEquipmentCacheObjectFactory,
                                   final ProcessXMLProvider processXMLProvider,
                                   final AliveTimerConfigHandler aliveTagConfigEventHandler,
                                   final DataTagService dataTagService,
                                   final DataTagConfigHandler dataTagConfigTransacted,
                                   final EquipmentService equipmentService) {
    super(subEquipmentCache, subEquipmentDAO, subEquipmentCacheObjectFactory,
      processXMLProvider, aliveTagConfigEventHandler, dataTagService, dataTagConfigTransacted);
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
  protected List<ProcessChange> removeReturnValue(SubEquipment subEquipment, ConfigurationElementReport report) {
    List<ProcessChange> processChanges = super.removeReturnValue(subEquipment, report);

    SubEquipmentUnitRemove subEquipmentUnitRemove = new SubEquipmentUnitRemove(0L, subEquipment.getId(), subEquipment.getParentId());

    Long processId = getProcessId(subEquipment);

    processChanges.add(new ProcessChange(processId, subEquipmentUnitRemove));

    return processChanges;
  }

}
