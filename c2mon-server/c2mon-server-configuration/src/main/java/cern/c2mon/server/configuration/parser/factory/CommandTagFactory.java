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
import cern.c2mon.server.common.equipment.Equipment;
import cern.c2mon.server.configuration.parser.exception.ConfigurationParseException;
import cern.c2mon.shared.client.configuration.ConfigConstants;
import cern.c2mon.shared.client.configuration.ConfigurationElement;
import cern.c2mon.shared.client.configuration.api.tag.CommandTag;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.Collections;
import java.util.List;

import static cern.c2mon.cache.config.ClientQueryProvider.queryByClientInput;

/**
 * Creates {@link ConfigurationElement} out of {@link CommandTag}s received in the MQ
 *
 * @author Alexandros Papageorgiou, Franz Ritter
 */
@Named
@Singleton
class CommandTagFactory extends EntityFactory<CommandTag> {

  private final SequenceDAO sequenceDAO;
  private final EquipmentDAO equipmentDAO;
  private final C2monCache<Equipment> equipmentCache;
  private final C2monCache<cern.c2mon.shared.common.command.CommandTag> commandTagCache;

  @Inject
  public CommandTagFactory(C2monCache<cern.c2mon.shared.common.command.CommandTag> commandTagCache,
                           C2monCache<Equipment> equipmentCache,
                           SequenceDAO sequenceDAO,
                           EquipmentDAO equipmentDAO) {
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
    if (equipmentCache.containsKey(equipmentId)) {

      commandTag.setEquipmentId(equipmentId);
      return Collections.singletonList(doCreateInstance(commandTag));

    } else {
      throw new ConfigurationParseException("Error creating command tag #" + commandTag.getId() + ": " +
          "Specified parent equipment does not exist!");
    }
  }

  @Override
  Long createId(CommandTag configurationEntity) {
    if (configurationEntity.getName() != null
      && !queryByClientInput(commandTagCache, commandTag -> commandTag.getName(), configurationEntity.getName()).isEmpty()) {
        throw new ConfigurationParseException("Error creating command tag " + configurationEntity.getName() + ": " +
            "Name already exists!");
    } else {
      return configurationEntity.getId() != null ? configurationEntity.getId() : sequenceDAO.getNextTagId();
    }
  }

  @Override
  Long getId(CommandTag entity) {
    return entity.getId() != null
      ? entity.getId()
      : queryByClientInput(commandTagCache, commandTag -> commandTag.getName(), entity.getName())
        .stream().findAny()
        .orElseThrow(() -> new ConfigurationParseException("Command tag " + entity.getName() + " does not exist!"))
        .getId();
  }

  @Override
  public ConfigConstants.Entity getEntity() {
    return ConfigConstants.Entity.COMMANDTAG;
  }
}
