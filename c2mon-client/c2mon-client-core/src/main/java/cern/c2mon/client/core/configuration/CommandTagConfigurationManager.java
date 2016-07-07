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
package cern.c2mon.client.core.configuration;

import cern.c2mon.shared.client.configuration.ConfigurationReport;
import cern.c2mon.shared.client.configuration.api.tag.CommandTag;
import cern.c2mon.shared.common.datatag.address.HardwareAddress;

import java.util.List;
import java.util.Set;

/**
 * The CommandTagConfigurationManager allows to apply create, update and delete
 * configurations for CommandTags.
 *
 * @author Franz Ritter
 */
public interface CommandTagConfigurationManager {


  /**
   * Creates a new {@link CommandTag} on the server.
   * <p>
   * The {@link CommandTag} is created with default parameters.
   *
   * @param equipmentName   The name of the overlying Equipment.
   * @param name            The name of the DataTag to be created.
   * @param dataType        The data type of the DataTag.
   * @param hardwareAddress The DataTag address of the dataTag.
   * @param clientTimeout   The duration after the client reports a timeout
   * @param execTimeout     The duration after the server reports a timeout
   * @param sourceTimeout   The duration after the source reports a timeout
   * @param sourceRetries   Number of times a data source should retry to
   *                        execute a command in case an attempted execution
   *                        fails.
   * @param rbacClass       RBAC class
   * @param rbacDevice      RBAC device
   * @param rbacProperty    RBAC property
   * @return A {@link ConfigurationReport} containing all details of the Tag
   * configuration, including if it was successful or not.
   * @see CommandTagConfigurationManager#CommandTag(String, CommandTag)
   */
  ConfigurationReport createCommandTag(String equipmentName, String name, Class<?> dataType, HardwareAddress
      hardwareAddress, Integer clientTimeout, Integer execTimeout, Integer sourceTimeout, Integer sourceRetries,
                                       String rbacClass, String rbacDevice, String rbacProperty);

  /**
   * Creates a new {@link CommandTag} on the server.
   * <p>
   * Next to the specified parameters the CommandTag is created with default
   * parameters.
   * <p>
   * Note: You have to use {@link CommandTag#create(String, Class,
   * HardwareAddress, Integer, Integer, Integer, Integer, String, String,
   * String)} to instantiate the 'dataTag' parameter of this method.
   *
   * @param equipmentName The name of the overlying Equipment.
   * @param dataTag       The {@link CommandTag} configuration for the 'create'.
   * @return A {@link ConfigurationReport} containing all details of the Tag
   * configuration, including if it was successful or not.
   * @see CommandTagConfigurationManager#createCommandTag(String, String,
   * Class, HardwareAddress, Integer, Integer, Integer, Integer, String,
   * String, String) (String, String, Class, DataTagAddress)
   */
  ConfigurationReport createCommandTag(String equipmentName, CommandTag dataTag);

  /**
   * Creates multiple new {@link CommandTag} on the server.
   * <p>
   * Next to the specified parameters the CommandTag are created with default
   * parameters.
   * <p>
   * Note: You have to use {@link CommandTag#create(String, Class,
   * HardwareAddress, Integer, Integer, Integer, Integer, String, String,
   * String)} to instantiate the 'dataTag' parameter of this method.
   *
   * @param equipmentName The name of the overlying Equipment.
   * @param dataTags      The list of {@link CommandTag} configurations for the
   *                      'create'.
   * @return A {@link ConfigurationReport} containing all details of the Tag
   * configuration, including if it was successful or not.
   */
  ConfigurationReport createCommandTags(String equipmentName, List<CommandTag> dataTags);

  /**
   * Updates a existing 'Tag' with the given parameters set in the
   * {@link {@link CommandTag}}object.
   * <p>
   * Note: You have to use one of the following methods to instantiate the
   * 'tag' parameter of this method.
   * <p>
   * {@link CommandTag#update(Long)}, {@link CommandTag#update(String)},
   *
   * @param tag The {@link CommandTag} configuration for the 'update'.
   * @return A {@link ConfigurationReport} containing all details of the
   * Process configuration, including if it was successful or not.
   */
  ConfigurationReport updateCommandTag(CommandTag tag);

  /**
   * Updates multiple existing 'Tags' with the given parameters set in the
   * {@link CommandTag} objects.
   * <p>
   * Note: You have to use one of the following methods to instantiate the
   * 'tag' parameter of this method.
   * <p>
   * {@link CommandTag#update(Long)}, {@link CommandTag#update(String)},
   *
   * @param tags The list of {@link CommandTag} configurations for the
   *             'updates'.
   * @return A {@link ConfigurationReport} containing all details of the Tag
   * configuration, including if it was successful or not.
   */
  ConfigurationReport updateCommandTags(List<CommandTag> tags);

  /**
   * Removes a existing 'CommandTag' with the given id.
   *
   * @param id The id of the CommandTag which needs to be removed.
   * @return A {@link ConfigurationReport} containing all details of the Tag
   * configuration, including if it was successful or not.
   */
  ConfigurationReport removeCommandTagById(Long id);

  /**
   * Removes multiple existing 'CommandTag' with the given ids.
   *
   * @param ids The list of ids of the CommandTag which needs to be removed.
   * @return A {@link ConfigurationReport} containing all details of the
   * Process configuration, including if it was successful or not.
   */
  ConfigurationReport removeCommandTagsById(Set<Long> ids);

  /**
   * Removes a existing 'CommandTag' with the given name.
   *
   * @param name The name of the Tag which needs to be removed.
   * @return A {@link ConfigurationReport} containing all details of the
   * Process configuration, including if it was successful or not.
   */
  ConfigurationReport removeCommandTag(String name);

  /**
   * Removes multiple existing 'CommandTag' with the given names.
   *
   * @param names The list of names of the CommandTags which needs to be
   *              removed.
   * @return A {@link ConfigurationReport} containing all details of the
   * Process configuration, including if it was successful or not.
   */
  ConfigurationReport removeCommandTags(Set<String> names);
}
