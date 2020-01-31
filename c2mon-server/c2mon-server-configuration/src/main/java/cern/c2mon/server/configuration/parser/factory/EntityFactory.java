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
import cern.c2mon.server.configuration.parser.exception.ConfigurationParseException;
import cern.c2mon.server.configuration.parser.exception.EntityDoesNotExistException;
import cern.c2mon.server.configuration.parser.util.ReflectionService;
import cern.c2mon.shared.client.configuration.ConfigConstants;
import cern.c2mon.shared.client.configuration.ConfigurationElement;
import cern.c2mon.shared.client.configuration.api.util.ConfigurationEntity;
import cern.c2mon.shared.common.Cacheable;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Properties;

import static cern.c2mon.server.configuration.parser.util.ReflectionService.extractPropertiesFromField;

/**
 * General factory which provides the methods to build a 'create', 'update' and 'delete' {@link ConfigurationElement} instance.
 * Classes which inherit from this class should be able to create a {@link ConfigurationElement} based on the information
 * of a {@link ConfigurationEntity}.
 *
 * @author Franz Ritter
 */
@RequiredArgsConstructor
public abstract class EntityFactory<T extends ConfigurationEntity> {

  private final C2monCache<? extends Cacheable> cache;

  /**
   * Internal method to get all {@link ConfigurationEntity} information for a create.
   * The information gets retrieved thorough reflection.
   *
   * @param configurationEntity The entity which contains the information for a 'create' {@link ConfigurationElement}.
   * @return A {@link ConfigurationElement} which creates than a Entity.
   */
  protected ConfigurationElement doCreateInstance(T configurationEntity) {
    ConfigurationElement element = createSetupConfigurationElement();
    Long entityId = createId(configurationEntity);

    if (hasEntity(entityId)) {
      throw new ConfigurationParseException("Error creating entity: "
          + configurationEntity.getClass().getSimpleName() + " (name = " + configurationEntity.getName() + ", id = "
          + entityId + ") already exists!");
    }

    configurationEntity.setId(entityId);
    element.setEntityId(entityId);
    element.setAction(ConfigConstants.Action.CREATE);
    element.setElementProperties(getCreateProperties(configurationEntity));

    return element;

  }

  /**
   * Method to parse all {@link ConfigurationEntity} information for a 'update'.
   * The information gets retrieved thorough reflection.
   *
   * @param configurationEntity The entity which contains the information for a 'update' {@link ConfigurationElement}.
   * @return A {@link ConfigurationElement} which updates than a Entity.
   */
  public ConfigurationElement updateInstance(T configurationEntity) {
    ConfigurationElement element = createSetupConfigurationElement();
    Long entityId = getId(configurationEntity);

    if (hasEntity(entityId)) {
      element.setEntityId(entityId);
      element.setAction(ConfigConstants.Action.UPDATE);
      element.setElementProperties(getUpdateProperties(configurationEntity));
      return element;

    } else {
      throw new EntityDoesNotExistException(entityId, configurationEntity.getClass().getSimpleName(), configurationEntity.getName());
    }
  }

  /**
   * External method to get the id for a 'delete'.
   *
   * @param configurationEntity The entity which contains the id for a 'delete' {@link ConfigurationElement}.
   * @return A {@link ConfigurationElement} which deletes than a Entity.
   */
  public ConfigurationElement deleteInstance(T configurationEntity) {
    ConfigurationElement element = createSetupConfigurationElement();
    Long entityId = getId(configurationEntity);

    if (hasEntity(entityId)) {
      element.setEntityId(entityId);
      element.setAction(ConfigConstants.Action.REMOVE);
      return element;

    } else {
      throw new EntityDoesNotExistException(entityId, configurationEntity.getClass().getSimpleName(), configurationEntity.getName());
    }
  }

  /**
   * Method to parse all {@link ConfigurationEntity} information for a 'create'.
   *
   * @param configurationEntity The entity which contains the information for a 'create' {@link ConfigurationElement}.
   * @return A {@link ConfigurationElement} which creates than a Entity.
   */
  public abstract List<ConfigurationElement> createInstance(T configurationEntity);

  /**
   * Creates a unique id for a {@link ConfigurationEntity}.
   *
   * @param configurationEntity The Entity which needs a unique Id.
   * @return A unique Id.
   */
  abstract Long createId(T configurationEntity);

  /**
   * Retrieves the id of the given {@link ConfigurationEntity}.
   *
   * @param configurationEntity The entity where the id belongs to.
   * @return The id of the entity.
   */
  abstract Long getId(T configurationEntity);

  /**
   * Checks the cache which belongs to a {@link ConfigurationEntity} if the id is already known to the cache.
   *
   * @param id The id which needs to be checked.
   * @return True if the id is known to the cache.
   */
  boolean hasEntity(Long id) {
      return id != null && cache.containsKey(id);
  }

  /**
   * Determine the corresponding {@link ConfigConstants.Entity} to the {@link ConfigurationEntity}.
   *
   * @return The correct {@link ConfigConstants.Entity}.
   */
  public abstract ConfigConstants.Entity getEntity();

  /**
   * Helper method for calling the reflection procedure for parsing the create information of a {@link ConfigurationEntity} object.
   *
   * @param configurationEntity The entity which holds the information.
   * @return The {@link Properties} with the information of the {@link ConfigurationEntity}.
   */
  private Properties getCreateProperties(T configurationEntity) {
    Properties result = extractPropertiesFromField(configurationEntity, configurationEntity.getClass());
    result = ReflectionService.setDefaultValues(result, configurationEntity);

    return result;
  }

  /**
   * Helper method for calling the reflection procedure for parsing the update information of a {@link ConfigurationEntity} object.
   *
   * @param configurationEntity The entity which holds the information.
   * @return The {@link Properties} with the information of the {@link ConfigurationEntity}.
   */
  private Properties getUpdateProperties(T configurationEntity) {
    return extractPropertiesFromField(configurationEntity, configurationEntity.getClass());
  }

  /**
   * Helper method to initialize a {@link ConfigurationElement}.
   *
   * @return A initialized {@link ConfigurationElement}.
   */
  private ConfigurationElement createSetupConfigurationElement() {
    ConfigurationElement element = new ConfigurationElement();

    // set basic information of the ConfigurationElement, based on the type of the confObj
    element.setEntity(getEntity());
    element.setSequenceId(-1L);
    element.setElementProperties(new Properties());

    return element;
  }
}
