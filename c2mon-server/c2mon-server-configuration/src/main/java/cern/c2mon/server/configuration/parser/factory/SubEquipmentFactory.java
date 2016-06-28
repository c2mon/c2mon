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
import cern.c2mon.server.cache.SubEquipmentCache;
import cern.c2mon.server.cache.loading.EquipmentDAO;
import cern.c2mon.server.cache.loading.SequenceDAO;
import cern.c2mon.server.cache.loading.SubEquipmentDAO;
import cern.c2mon.server.configuration.parser.exception.ConfigurationParseException;
import cern.c2mon.shared.client.configuration.ConfigConstants;
import cern.c2mon.shared.client.configuration.ConfigurationElement;
import cern.c2mon.shared.client.configuration.api.equipment.Equipment;
import cern.c2mon.shared.client.configuration.api.equipment.SubEquipment;
import cern.c2mon.shared.client.configuration.api.tag.CommFaultTag;
import cern.c2mon.shared.client.configuration.api.tag.StatusTag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by fritter on 01/06/16.
 */
@Service
public class SubEquipmentFactory extends EntityFactory<SubEquipment> {

  private SubEquipmentCache subEquipmentCache;
  private SubEquipmentDAO subEquipmentDAO;
  private EquipmentCache equipmentCache;
  private EquipmentDAO equipmentDAO;
  private SequenceDAO sequenceDAO;
  private ControlTagFactory controlTagFactory;

  @Autowired
  public SubEquipmentFactory(SubEquipmentCache subEquipmentCache, SubEquipmentDAO subEquipmentDAO, SequenceDAO sequenceDAO, ControlTagFactory controlTagFactory,
                             EquipmentFactory equipmentFactory, EquipmentCache equipmentCache, EquipmentDAO equipmentDAO) {
    this.subEquipmentCache = subEquipmentCache;
    this.subEquipmentDAO = subEquipmentDAO;
    this.sequenceDAO = sequenceDAO;
    this.controlTagFactory = controlTagFactory;
    this.equipmentCache = equipmentCache;
    this.equipmentDAO = equipmentDAO;
  }

  @Override
  public List<ConfigurationElement> createInstance(SubEquipment configurationEntity) {
    List<ConfigurationElement> configurationElements = new ArrayList<>();

    Long equipmentId = configurationEntity.getEquipmentId() != null
        ? configurationEntity.getEquipmentId() : equipmentDAO.getIdByName(configurationEntity.getParentEquipmentName());
    configurationEntity.setEquipmentId(equipmentId);

    // check information about the parent id
    if (equipmentCache.hasKey(equipmentId)) {

      ConfigurationElement createSubEquipment = doCreateInstance(configurationEntity);
      configurationEntity = setDefaultControlTags(configurationEntity);

      configurationElements.addAll(controlTagFactory.createInstance(configurationEntity.getCommFaultTag()));
      configurationElements.addAll(controlTagFactory.createInstance(configurationEntity.getStatusTag()));
      configurationElements.addAll(controlTagFactory.createInstance(configurationEntity.getAliveTag()));

      createSubEquipment.getElementProperties().setProperty("statusTagId", configurationEntity.getStatusTag().getId().toString());
      createSubEquipment.getElementProperties().setProperty("commFaultTagId", configurationEntity.getCommFaultTag().getId().toString());
      createSubEquipment.getElementProperties().setProperty("aliveTagId", configurationEntity.getAliveTag().getId().toString());

      configurationElements.add(createSubEquipment);

      return configurationElements;
    } else {
      throw new ConfigurationParseException("Creating of a new SubEquipment (id = " + configurationEntity.getId() + ") failed: No Equipment with the id " + equipmentId + " found");
    }
  }

  /**
   * Checks if the Equipment has a defined {@link CommFaultTag} or {@link StatusTag}.
   * If not a automatic Status tag will be created and attached to the equipment configuration.
   *
   * @param process The Equipment which contains the information of an create.
   * @return The same equipment from the parameters attached with the status tag information.
   */
  protected static SubEquipment setDefaultControlTags(SubEquipment subEquipment) {

    if (subEquipment.getCommFaultTag() == null) {

      CommFaultTag commfaultTag = CommFaultTag.create(subEquipment.getName() + ":COMM_FAULT")
          .description("Communication fault tag for subEquipment " + subEquipment.getName())
          .build();
      subEquipment.setCommFaultTag(commfaultTag);
    }

    if (subEquipment.getStatusTag() == null) {

      StatusTag statusTag = StatusTag.create(subEquipment.getName() + ":STATUS")
          .description("Status tag for subEquipment " + subEquipment.getName())
          .build();
      subEquipment.setStatusTag(statusTag);
    }

    subEquipment.getCommFaultTag().setProcessId(subEquipment.getId());
    subEquipment.getStatusTag().setProcessId(subEquipment.getId());

    if (subEquipment.getAliveTag() != null && subEquipment.getAliveTag().getAddress() != null) {
      subEquipment.getAliveTag().setProcessId(subEquipment.getId());
    } else {
      throw new ConfigurationParseException("Creating of a new SubEquipment (id = " + subEquipment.getId() + ") failed: You have to specify a AliveTag with DataTagAddress");
    }

    return subEquipment;
  }

  @Override
  Long createId(SubEquipment configurationEntity) {
    return configurationEntity.getId() != null ? configurationEntity.getId() : sequenceDAO.getNextEquipmentId();
  }

  @Override
  Long getId(SubEquipment configurationEntity) {
    return configurationEntity.getId() != null ? configurationEntity.getId() : subEquipmentDAO.getIdByName(configurationEntity.getName());
  }

  @Override
  boolean cacheHasEntity(Long id) {
    return subEquipmentCache.hasKey(id);
  }

  @Override
  ConfigConstants.Entity getEntity() {
    return ConfigConstants.Entity.SUBEQUIPMENT;
  }
}
