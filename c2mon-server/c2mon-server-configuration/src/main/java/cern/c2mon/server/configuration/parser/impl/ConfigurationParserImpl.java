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
package cern.c2mon.server.configuration.parser.impl;

import cern.c2mon.server.configuration.parser.ConfigurationParser;
import cern.c2mon.server.configuration.parser.exception.ConfigurationParseException;
import cern.c2mon.server.configuration.parser.exception.EntityDoesNotExistException;
import cern.c2mon.server.configuration.parser.factory.*;
import cern.c2mon.shared.client.configuration.ConfigConstants;
import cern.c2mon.shared.client.configuration.ConfigurationElement;
import cern.c2mon.shared.client.configuration.api.Configuration;
import cern.c2mon.shared.client.configuration.api.alarm.Alarm;
import cern.c2mon.shared.client.configuration.api.equipment.Equipment;
import cern.c2mon.shared.client.configuration.api.equipment.SubEquipment;
import cern.c2mon.shared.client.configuration.api.process.Process;
import cern.c2mon.shared.client.configuration.api.tag.*;
import cern.c2mon.shared.client.configuration.api.util.ConfigurationEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;


/**
 * This class holds all information about a Configuration object to translate
 * this object in a List of {@link ConfigurationElement}.
 *
 * @author Franz Ritter
 */
@Slf4j
@Component
public class ConfigurationParserImpl implements ConfigurationParser {

  private AlarmFactory alarmFactory;
  private CommandTagFactory commandTagFactory;
  private DataTagFactory dataTagFactory;
  private EquipmentFactory equipmentFactory;
  private ProcessFactory processFactory;
  private RuleTagFactory ruleTagFactory;
  private SubEquipmentFactory subEquipmentFactory;
  private AliveTagFactory aliveTagFactory;
  private CommFaultTagFactory commFaultTagFactory;

  @Autowired
  public ConfigurationParserImpl(
    AlarmFactory alarmFactory, CommandTagFactory commandTagFactory,
    DataTagFactory dataTagFactory, EquipmentFactory equipmentFactory, ProcessFactory processFactory, RuleTagFactory ruleTagFactory,
    SubEquipmentFactory subEquipmentFactory, AliveTagFactory aliveTagFactory, CommFaultTagFactory commFaultTagFactory) {
    this.alarmFactory = alarmFactory;
    this.commandTagFactory = commandTagFactory;
    this.dataTagFactory = dataTagFactory;
    this.equipmentFactory = equipmentFactory;
    this.processFactory = processFactory;
    this.ruleTagFactory = ruleTagFactory;
    this.subEquipmentFactory = subEquipmentFactory;
    this.aliveTagFactory = aliveTagFactory;
    this.commFaultTagFactory = commFaultTagFactory;
  }

  @Override
  public List<ConfigurationElement> parse(Configuration configuration) {
    if (configuration.getEntities() != null && !configuration.getEntities().isEmpty()) {

      return parseConfigurationList(configuration.getEntities());
    } else {

      throw new ConfigurationParseException("Empty configuration received!");
    }
  }

  /**
   * Parses a list of entities to be configured and transform them into
   * {@link ConfigurationElement} instances.
   *
   * @param entities Objects which holds the information to create a {@link ConfigurationElement}.
   * @return A {@link ConfigurationElement} for the server configuration.
   */
  @SuppressWarnings("unchecked")
  private List<ConfigurationElement> parseConfigurationList(List<? extends ConfigurationEntity> entities) {
    List<ConfigurationElement> results = new ArrayList<>();
    EntityFactory entityFactory;

    for (ConfigurationEntity configurationEntity : entities) {
      entityFactory = getEntityFactory(configurationEntity);

      if (configurationEntity.isDeleted()) {
        try {
          results.add(entityFactory.deleteInstance(configurationEntity));
        } catch (ConfigurationParseException ex) {
          log.debug("Element {} (name = {}) already deleted. Detailed reason: {}", configurationEntity.getId(), configurationEntity.getName(), ex.getMessage());
        }
      } else if (configurationEntity.isUpdated()) {
        try {
          results.add(entityFactory.updateInstance(configurationEntity));
        } catch (EntityDoesNotExistException e) {
          ConfigurationElement missingEntity = new ConfigurationElement();
          missingEntity.setStatus(ConfigConstants.Status.WARNING);
          missingEntity.setEntityId(configurationEntity.getId());
          missingEntity.setAction(ConfigConstants.Action.UPDATE);
          missingEntity.setConfigId(-1L);
          missingEntity.setEntity(ConfigConstants.Entity.MISSING);
          results.add(missingEntity);
          log.warn(e.getMessage());
        }
      } else if (configurationEntity.isCreated()) {
        results.addAll(entityFactory.createInstance(configurationEntity));
      } else {
        throw new ConfigurationParseException("Error while parsing a " + configurationEntity.getClass() + ": No action flag set!");
      }
    }
    return results;
  }

  /**
   * Determine the correct {@link EntityFactory} based on the instance of the
   * {@link ConfigurationEntity}.
   *
   * @param entity A entity for creating a {@link ConfigurationElement}.
   * @return The corresponding factory.
   */
  private EntityFactory getEntityFactory(ConfigurationEntity entity) {
    if (entity instanceof Process) {
      return processFactory;
    }
    if (entity instanceof Equipment) {
      return equipmentFactory;
    }
    if (entity instanceof SubEquipment) {
      return subEquipmentFactory;
    }
    if (entity instanceof AliveTag) {
      return aliveTagFactory;
    }
//    if (entity instanceof StatusTag) {
//      return statusTagFactory;
//    }
    if (entity instanceof CommFaultTag) {
      return commFaultTagFactory;
    }
    if (entity instanceof DataTag) {
      return dataTagFactory;
    }
    if (entity instanceof RuleTag) {
      return ruleTagFactory;
    }
    if (entity instanceof Alarm) {
      return alarmFactory;
    }
    if (entity instanceof CommandTag) {
      return commandTagFactory;
    }
    throw new IllegalArgumentException("No EntityFactory for class " + entity.getClass() + " could be determined!");
  }

}


