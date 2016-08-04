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

import cern.c2mon.server.cache.CommandTagCache;
import cern.c2mon.server.cache.DataTagCache;
import cern.c2mon.server.cache.EquipmentCache;
import cern.c2mon.server.cache.loading.EquipmentDAO;
import cern.c2mon.server.cache.loading.SequenceDAO;
import cern.c2mon.server.configuration.parser.exception.ConfigurationParseException;
import cern.c2mon.shared.client.configuration.ConfigConstants;
import cern.c2mon.shared.client.configuration.ConfigurationElement;
import cern.c2mon.shared.client.configuration.api.tag.CommandTag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author Franz Ritter
 */
@Service
public class CommandTagFactory extends EntityFactory<CommandTag> {

  private SequenceDAO sequenceDAO;
  private CommandTagCache commandTagCache;
  private DataTagCache dataTagCache;
  private EquipmentDAO equipmentDAO;
  private EquipmentCache equipmentCache;

  @Autowired
  public CommandTagFactory(SequenceDAO sequenceDAO, CommandTagCache commandTagCache, DataTagCache dataTagCache, EquipmentCache equipmentCache, EquipmentDAO equipmentDAO) {
    this.sequenceDAO = sequenceDAO;
    this.commandTagCache = commandTagCache;
    this.dataTagCache = dataTagCache;
    this.equipmentCache = equipmentCache;
    this.equipmentDAO = equipmentDAO;

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
    return configurationEntity.getId() != null ? configurationEntity.getId() : sequenceDAO.getNextTagId();
  }

  @Override
  Long getId(CommandTag configurationEntity) {
    return configurationEntity.getId() != null ? configurationEntity.getId() : dataTagCache.get(configurationEntity.getName()).getId();
  }

  @Override
  boolean cacheHasEntity(Long id) {
    return commandTagCache.hasKey(id);
  }

  @Override
  ConfigConstants.Entity getEntity() {
    return ConfigConstants.Entity.COMMANDTAG;
  }
}
