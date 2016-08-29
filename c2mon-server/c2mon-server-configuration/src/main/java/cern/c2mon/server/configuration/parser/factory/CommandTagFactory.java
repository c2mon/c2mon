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

import java.util.Collections;
import java.util.List;

import cern.c2mon.shared.client.configuration.api.tag.DataTag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cern.c2mon.server.cache.CommandTagCache;
import cern.c2mon.server.cache.EquipmentCache;
import cern.c2mon.server.cache.loading.EquipmentDAO;
import cern.c2mon.server.cache.loading.SequenceDAO;
import cern.c2mon.server.configuration.parser.exception.ConfigurationParseException;
import cern.c2mon.shared.client.configuration.ConfigConstants;
import cern.c2mon.shared.client.configuration.ConfigurationElement;
import cern.c2mon.shared.client.configuration.api.tag.CommandTag;

/**
 * @author Franz Ritter
 */
@Service
public class CommandTagFactory extends EntityFactory<CommandTag> {

  private final SequenceDAO sequenceDAO;
  private final EquipmentDAO equipmentDAO;
  private final EquipmentCache equipmentCache;
  private final CommandTagCache commandTagCache;

  @Autowired
  public CommandTagFactory(CommandTagCache commandTagCache, EquipmentCache equipmentCache, SequenceDAO sequenceDAO, EquipmentDAO equipmentDAO) {
    super(commandTagCache);
    this.sequenceDAO = sequenceDAO;
    this.equipmentCache = equipmentCache;
    this.equipmentDAO = equipmentDAO;
    this.commandTagCache = commandTagCache;
  }

  @Override
  public List<ConfigurationElement> createInstance(CommandTag commandTag) {

    Long equipmentId = commandTag.getEquipmentId() != null
        ? commandTag.getEquipmentId() : equipmentDAO.getIdByName(commandTag.getEquipmentName());

    // Check if the parent id exists
    if (equipmentCache.hasKey(equipmentId)) {

      commandTag.setEquipmentId(equipmentId);
      return Collections.singletonList(doCreateInstance(commandTag));

    } else {
      throw new ConfigurationParseException("Error creating commandtag #" + commandTag.getId() + ": " +
          "Specified parent equipment does not exist!");
    }
  }

  @Override
  Long createId(CommandTag configurationEntity) {
    if (configurationEntity.getName() != null && commandTagCache.getCommandTagId(configurationEntity.getName()) != null) {
      throw new ConfigurationParseException("Error creating commandtag " + configurationEntity.getName() + ": " +
          "Name already exists");
    } else {
      return configurationEntity.getId() != null ? configurationEntity.getId() : sequenceDAO.getNextTagId();
    }
  }

  @Override
  Long getId(CommandTag entity) {
    Long id;

    if (entity.getId() != null) {
      id = entity.getId();
    } else {
      if (commandTagCache.getCommandTagId(entity.getName()) != null) {
        id = commandTagCache.getCommandTagId(entity.getName());
      } else {
        throw new ConfigurationParseException("CommandTag " + entity.getName() + " does not exist!");
      }
    }
    return id;
  }

  @Override
  ConfigConstants.Entity getEntity() {
    return ConfigConstants.Entity.COMMANDTAG;
  }
}
