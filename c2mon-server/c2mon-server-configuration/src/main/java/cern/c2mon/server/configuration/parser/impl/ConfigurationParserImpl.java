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
import cern.c2mon.server.configuration.parser.factory.EntityFactory;
import cern.c2mon.server.configuration.parser.factory.ParserFactorySelector;
import cern.c2mon.shared.client.configuration.ConfigConstants;
import cern.c2mon.shared.client.configuration.ConfigurationElement;
import cern.c2mon.shared.client.configuration.api.Configuration;
import cern.c2mon.shared.client.configuration.api.util.ConfigurationEntity;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.List;


/**
 * This class holds all information about a Configuration object to translate
 * this object in a List of {@link ConfigurationElement}.
 *
 * @author Franz Ritter
 */
@Slf4j
@Named
public class ConfigurationParserImpl implements ConfigurationParser {

  private final ParserFactorySelector factorySelector;

  @Inject
  public ConfigurationParserImpl(ParserFactorySelector factorySelector) {
    this.factorySelector = factorySelector;
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

    for (ConfigurationEntity configurationEntity : entities) {
      EntityFactory entityFactory = factorySelector.getEntityFactory(configurationEntity);

      if (configurationEntity.isDeleted()) {
        try {
          results.addAll(entityFactory.deleteInstance(configurationEntity));
        } catch (ConfigurationParseException ex) {
          log.debug("Element {} (name = {}) already deleted. Detailed reason: {}", configurationEntity.getId(), configurationEntity.getName(), ex.getMessage());
        }
      } else if (configurationEntity.isUpdated()) {
        try {
          results.add(entityFactory.updateInstance(configurationEntity));
        } catch (EntityDoesNotExistException e) {
          results.add(createMissingEntity(configurationEntity));
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

  private static ConfigurationElement createMissingEntity(ConfigurationEntity configurationEntity) {
    ConfigurationElement missingEntity = new ConfigurationElement();
    missingEntity.setStatus(ConfigConstants.Status.WARNING);
    missingEntity.setEntityId(configurationEntity.getId());
    missingEntity.setAction(ConfigConstants.Action.UPDATE);
    missingEntity.setConfigId(-1L);
    missingEntity.setEntity(ConfigConstants.Entity.MISSING);
    return missingEntity;
  }

}