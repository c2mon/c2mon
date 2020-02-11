/*******************************************************************************
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
 ******************************************************************************/

package cern.c2mon.server.configuration.parser.factory;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.server.cache.loading.EquipmentDAO;
import cern.c2mon.server.cache.loading.ProcessDAO;
import cern.c2mon.server.cache.loading.SequenceDAO;
import cern.c2mon.server.common.process.Process;
import cern.c2mon.server.configuration.parser.exception.ConfigurationParseException;
import cern.c2mon.shared.client.configuration.ConfigConstants;
import cern.c2mon.shared.client.configuration.ConfigurationElement;
import cern.c2mon.shared.client.configuration.api.equipment.Equipment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Franz Ritter
 */
@Service
class EquipmentFactory extends EntityFactory<Equipment> {

  private EquipmentDAO equipmentDAO;
  private SequenceDAO sequenceDAO;
  private C2monCache<Process> processCache;
  private ProcessDAO processDAO;

  @Autowired
  public EquipmentFactory(C2monCache<cern.c2mon.server.common.equipment.Equipment> equipmentCache, EquipmentDAO equipmentDAO, SequenceDAO sequenceDAO,
                          C2monCache<Process> processCache, ProcessDAO processDAO) {
    super(equipmentCache);
    this.equipmentDAO = equipmentDAO;
    this.sequenceDAO = sequenceDAO;
    this.processCache = processCache;
    this.processDAO = processDAO;
  }

  @Override
  public List<ConfigurationElement> createInstance(Equipment equipment) {
    List<ConfigurationElement> configurationElements = new ArrayList<>();

    Long processId = equipment.getProcessId() != null
        ? equipment.getProcessId() : processDAO.getIdByName(equipment.getParentProcessName());
    equipment.setProcessId(processId);

    // check information about the parent id
    if (processCache.containsKey(processId)) {

      ConfigurationElement createEquipment = doCreateInstance(equipment);

      configurationElements.add(createEquipment);

      return configurationElements;
    } else {
      throw new ConfigurationParseException("Error creating equipment #" + equipment.getId() + ": " +
          "Specified parent process does not exist!");
    }
  }

  @Override
  Long createId(Equipment configurationEntity) {
    if (configurationEntity.getName() != null && equipmentDAO.getIdByName(configurationEntity.getName()) != null) {
      throw new ConfigurationParseException("Error creating equipment " + configurationEntity.getName() + ": " +
          "Name already exists!");
    } else {
      return configurationEntity.getId() != null ? configurationEntity.getId() : sequenceDAO.getNextEquipmentId();
    }
  }

  @Override
  Long getId(Equipment configurationEntity) {
    return configurationEntity.getId() != null ? configurationEntity.getId() : equipmentDAO.getIdByName(configurationEntity.getName());
  }

  @Override
  public ConfigConstants.Entity getEntity() {
    return ConfigConstants.Entity.EQUIPMENT;
  }
}
