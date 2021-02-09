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
import cern.c2mon.server.cache.loading.SequenceDAO;
import cern.c2mon.server.cache.loading.SubEquipmentDAO;
import cern.c2mon.server.common.equipment.Equipment;
import cern.c2mon.server.configuration.parser.exception.ConfigurationParseException;
import cern.c2mon.shared.client.configuration.ConfigConstants;
import cern.c2mon.shared.client.configuration.ConfigurationElement;
import cern.c2mon.shared.client.configuration.api.equipment.SubEquipment;
import cern.c2mon.shared.client.configuration.api.tag.CommFaultTag;
import cern.c2mon.shared.client.configuration.api.tag.StatusTag;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Franz Ritter
 */
@Named
@Singleton
class SubEquipmentFactory extends EntityFactory<SubEquipment> {

  private SubEquipmentDAO subEquipmentDAO;
  private C2monCache<cern.c2mon.server.common.equipment.Equipment> equipmentCache;
  private EquipmentDAO equipmentDAO;
  private final AliveTagFactory aliveTagFactory;
  private final CommFaultTagFactory commFaultTagFactory;
  private final SupervisionStateTagFactory stateTagFactory;
  private SequenceDAO sequenceDAO;

  @Inject
  public SubEquipmentFactory(C2monCache<cern.c2mon.server.common.subequipment.SubEquipment> subEquipmentCache, SubEquipmentDAO subEquipmentDAO,
                             SequenceDAO sequenceDAO, C2monCache<Equipment> equipmentCache, EquipmentDAO equipmentDAO,
                             AliveTagFactory aliveTagFactory, CommFaultTagFactory commFaultTagFactory, SupervisionStateTagFactory stateTagFactory) {
    super(subEquipmentCache);
    this.subEquipmentDAO = subEquipmentDAO;
    this.sequenceDAO = sequenceDAO;
    this.equipmentCache = equipmentCache;
    this.equipmentDAO = equipmentDAO;
    this.aliveTagFactory = aliveTagFactory;
    this.commFaultTagFactory = commFaultTagFactory;
    this.stateTagFactory = stateTagFactory;
  }

  @Override
  public List<ConfigurationElement> createInstance(SubEquipment subEquipment) {
    List<ConfigurationElement> configurationElements = new ArrayList<>();

    Long equipmentId = subEquipment.getEquipmentId() != null
        ? subEquipment.getEquipmentId()
      : equipmentDAO.getIdByName(subEquipment.getParentEquipmentName());
    subEquipment.setEquipmentId(equipmentId);

    // check information about the parent id
    if (equipmentCache.containsKey(equipmentId)) {

      ConfigurationElement createSubEquipment = doCreateInstance(subEquipment);
      subEquipment = setDefaultControlTags(subEquipment);

      configurationElements.addAll(commFaultTagFactory.createInstance(subEquipment.getCommFaultTag()));
      configurationElements.addAll(stateTagFactory.createInstance(subEquipment.getStatusTag()));
      configurationElements.addAll(aliveTagFactory.createInstance(subEquipment.getAliveTag()));

      createSubEquipment.getElementProperties().setProperty("statusTagId", subEquipment.getStatusTag().getId().toString());
      createSubEquipment.getElementProperties().setProperty("commFaultTagId", subEquipment.getCommFaultTag().getId().toString());
      createSubEquipment.getElementProperties().setProperty("aliveTagId", subEquipment.getAliveTag().getId().toString());

      configurationElements.add(createSubEquipment);

      return configurationElements;
    } else {
      throw new ConfigurationParseException("Error creating subequipment #" + subEquipment.getId() + ": " +
              "Specified parent equipment does not exist!");
    }

    // check information about the parent id
   /* if (!equipmentCache.containsKey(equipmentId)) {
      throw new ConfigurationParseException("Error creating subequipment #" + subEquipment.getId() + ": " +
          "Specified parent equipment does not exist!");
    }
    
    ConfigurationElement createSubEquipment = doCreateInstance(subEquipment);

    // If the user specified any custom tag info, use it (otherwise it will be created by the handler
    if (subEquipment.getAliveTag() != null) {
      configurationElements.addAll(aliveTagFactory.createInstance(subEquipment.getAliveTag()));
      createSubEquipment.getElementProperties().setProperty("aliveTagId", subEquipment.getAliveTag().getId().toString());
    }
    if (subEquipment.getCommFaultTag() != null) {
      configurationElements.addAll(commFaultTagFactory.createInstance(subEquipment.getCommFaultTag()));
      createSubEquipment.getElementProperties().setProperty("commFaultTagId", subEquipment.getCommFaultTag().getId().toString());
    }
    if (subEquipment.getStatusTag() != null) {
      configurationElements.addAll(stateTagFactory.createInstance(subEquipment.getStatusTag()));
      createSubEquipment.getElementProperties().setProperty("stateTagId", subEquipment.getStatusTag().getId().toString());
    }
    
    configurationElements.add(createSubEquipment);

    return configurationElements;*/
  }

  /**
   * Checks if the Equipment has a defined {@link CommFaultTag} or {@link StatusTag}.
   * If not a automatic Status tag will be created and attached to the equipment configuration.
   *
   * @param subEquipment The Equipment which contains the information of an create.
   * @return The same equipment from the parameters attached with the status tag information.
   */
  protected static SubEquipment setDefaultControlTags(SubEquipment subEquipment) {

    if (subEquipment.getCommFaultTag() == null) {

      CommFaultTag commfaultTag = CommFaultTag.create(subEquipment.getName() + ":COMM_FAULT")
              .description("Communication fault tag for sub equipment " + subEquipment.getName())
              .build();
      subEquipment.setCommFaultTag(commfaultTag);
    }

    if (subEquipment.getStatusTag() == null) {

      StatusTag statusTag = StatusTag.create(subEquipment.getName() + ":STATUS")
              .description("Status tag for sub equipment " + subEquipment.getName())
              .build();
      subEquipment.setStatusTag(statusTag);
    }

    subEquipment.getCommFaultTag().setProcessId(subEquipment.getId());
    subEquipment.getStatusTag().setProcessId(subEquipment.getId());

    if (subEquipment.getAliveTag() != null && subEquipment.getAliveTag().getAddress() != null) {
      subEquipment.getAliveTag().setProcessId(subEquipment.getId());
    } else {
      throw new ConfigurationParseException("Error creating sub equipment #" + subEquipment.getId() + ": " +
              "No alive tag address was specified!");
    }

    return subEquipment;
  }

  @Override
  Long createId(SubEquipment configurationEntity) {
    if (configurationEntity.getName() != null && equipmentDAO.getIdByName(configurationEntity.getName()) != null) {
      throw new ConfigurationParseException("Error creating sub equipment " + configurationEntity.getName() + ": " +
          "Name already exists!");
    } else {
      return configurationEntity.getId() != null ? configurationEntity.getId() : sequenceDAO.getNextEquipmentId();
    }
  }

  @Override
  Long getId(SubEquipment configurationEntity) {
    return configurationEntity.getId() != null ? configurationEntity.getId() : subEquipmentDAO.getIdByName(configurationEntity.getName());
  }

  @Override
  public ConfigConstants.Entity getEntity() {
    return ConfigConstants.Entity.SUBEQUIPMENT;
  }
}
