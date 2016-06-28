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

import cern.c2mon.server.cache.EquipmentCache;
import cern.c2mon.server.cache.ProcessCache;
import cern.c2mon.server.cache.loading.EquipmentDAO;
import cern.c2mon.server.cache.loading.ProcessDAO;
import cern.c2mon.server.cache.loading.SequenceDAO;
import cern.c2mon.server.configuration.parser.exception.ConfigurationParseException;
import cern.c2mon.shared.client.configuration.ConfigConstants;
import cern.c2mon.shared.client.configuration.ConfigurationElement;
import cern.c2mon.shared.client.configuration.api.equipment.Equipment;
import cern.c2mon.shared.client.configuration.api.tag.CommFaultTag;
import cern.c2mon.shared.client.configuration.api.tag.StatusTag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Franz Ritter
 */
@Service
public class EquipmentFactory extends EntityFactory<Equipment> {

  private EquipmentCache equipmentCache;
  private EquipmentDAO equipmentDAO;
  private SequenceDAO sequenceDAO;
  private ProcessCache processCache;
  private ProcessDAO processDAO;
  private ControlTagFactory controlTagFactory;

  @Autowired
  public EquipmentFactory(EquipmentCache equipmentCache, EquipmentDAO equipmentDAO, SequenceDAO sequenceDAO, ControlTagFactory controlTagFactory,
                          ProcessCache processCache, ProcessDAO processDAO) {
    this.equipmentCache = equipmentCache;
    this.equipmentDAO = equipmentDAO;
    this.sequenceDAO = sequenceDAO;
    this.controlTagFactory = controlTagFactory;
    this.processCache = processCache;
    this.processDAO = processDAO;
  }

  @Override
  public List<ConfigurationElement> createInstance(Equipment configurationEntity) {
    List<ConfigurationElement> configurationElements = new ArrayList<>();

    Long processId = configurationEntity.getProcessId() != null
        ? configurationEntity.getProcessId() : processDAO.getIdByName(configurationEntity.getParentProcessName());
    configurationEntity.setProcessId(processId);

    // check information about the parent id
    if (processCache.hasKey(processId)) {

      ConfigurationElement createEquipment = doCreateInstance(configurationEntity);
      configurationEntity = setDefaultControlTags(configurationEntity);

      configurationElements.addAll(controlTagFactory.createInstance(configurationEntity.getCommFaultTag()));
      configurationElements.addAll(controlTagFactory.createInstance(configurationEntity.getStatusTag()));

      if (configurationEntity.getAliveTag() != null) {
        configurationElements.addAll(controlTagFactory.createInstance(configurationEntity.getAliveTag()));
        createEquipment.getElementProperties().setProperty("aliveTagId", configurationEntity.getAliveTag().getId().toString());
      }

      createEquipment.getElementProperties().setProperty("statusTagId", configurationEntity.getStatusTag().getId().toString());
      createEquipment.getElementProperties().setProperty("commFaultTagId", configurationEntity.getCommFaultTag().getId().toString());


      configurationElements.add(createEquipment);

      return configurationElements;
    } else {
      throw new ConfigurationParseException("Creating of a new Equipment (id = " + configurationEntity.getId() + ") failed: No Process with the id " + processId + " found");
    }
  }

  /**
   * Checks if the Equipment has a defined {@link CommFaultTag} or {@link StatusTag}.
   * If not a automatic ControlTag tag will be created and attached to the equipment configuration.
   *
   * @param equipment The Equipment which contains the information of an create.
   * @return The same equipment from the parameters attached with the controlTag tag information.
   */
  protected static Equipment setDefaultControlTags(Equipment equipment) {

    if (equipment.getCommFaultTag() == null) {

      CommFaultTag commfaultTag = CommFaultTag.create(equipment.getName() + ":COMM_FAULT")
          .description("Communication fault tag for equipment " + equipment.getName())
          .build();
      equipment.setCommFaultTag(commfaultTag);
    }

    if (equipment.getStatusTag() == null) {

      StatusTag statusTag = StatusTag.create(equipment.getName() + ":STATUS")
          .description("Status tag for equipment " + equipment.getName())
          .build();
      equipment.setStatusTag(statusTag);
    }

    equipment.getCommFaultTag().setProcessId(equipment.getId());
    equipment.getStatusTag().setProcessId(equipment.getId());

    if (equipment.getAliveTag() != null) {
      equipment.getAliveTag().setProcessId(equipment.getId());
    }

    return equipment;
  }

  @Override
  Long createId(Equipment configurationEntity) {
    return configurationEntity.getId() != null ? configurationEntity.getId() : sequenceDAO.getNextEquipmentId();
  }

  @Override
  Long getId(Equipment configurationEntity) {
    return configurationEntity.getId() != null ? configurationEntity.getId() : equipmentDAO.getIdByName(configurationEntity.getName());
  }

  @Override
  boolean cacheHasEntity(Long id) {
    return equipmentCache.hasKey(id);
  }

  @Override
  ConfigConstants.Entity getEntity() {
    return ConfigConstants.Entity.EQUIPMENT;
  }
}
