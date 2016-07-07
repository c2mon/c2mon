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
import cern.c2mon.shared.client.configuration.api.tag.DataTag;
import cern.c2mon.shared.client.configuration.api.tag.RuleTag;
import cern.c2mon.shared.client.configuration.api.tag.Tag;
import cern.c2mon.shared.common.datatag.DataTagAddress;

import java.util.List;
import java.util.Set;

/**
 * The DataTagConfigurationManager allows to apply create, update and delete
 * configurations for DataTags.
 *
 * @author Franz Ritter
 */
public interface DataTagConfigurationManager {

  /**
   * Creates a new 'DataTag' on the server with the given name, data type and
   * {@link DataTagAddress}. After a successful creation a DAQ which links to
   * the DataTagAddress can be started.
   * <p>
   * The DataTag is created with default parameters.
   *
   * @param equipmentName The name of the overlying Equipment.
   * @param name          The name of the DataTag to be created.
   * @param dataType      The data type of the DataTag.
   * @param address       The DataTag address of the dataTag.
   * @return A {@link ConfigurationReport} containing all details of the Tag
   * configuration, including if it was successful or not.
   * @see DataTagConfigurationManager#createDataTag(String, String, Class,
   * DataTagAddress)
   * @see DataTagConfigurationManager#createDataTag(String, DataTag)
   */
  ConfigurationReport createDataTag(String equipmentName, String name, Class<?> dataType, DataTagAddress address);

  /**
   * Creates a new 'DataTag' on the server with the given name, data type and
   * {@link DataTagAddress} set in the {@link DataTag} object.
   * After a successful creation a DAQ which links to the DataTagAddress can be
   * started.
   * <p>
   * Next to the specified parameters the DataTag is created with default
   * parameters.
   * <p>
   * Note: You have to use {@link DataTag#create(String, Class, DataTagAddress)}
   * to instantiate the 'dataTag' parameter of this method.
   *
   * @param equipmentName The name of the overlying Equipment.
   * @param dataTag       The {@link DataTag} configuration for the 'create'.
   * @return A {@link ConfigurationReport} containing all details of the Tag
   * configuration, including if it was successful or not.
   * @see DataTagConfigurationManager#createDataTag(String, String, Class,
   * DataTagAddress)
   * @see DataTagConfigurationManager#createDataTag(String, DataTag)
   */
  ConfigurationReport createDataTag(String equipmentName, DataTag dataTag);

  /**
   * Creates multiple new 'DataTags' on the server with the given names, data
   * types and {@link DataTagAddress}es set in the {@link DataTag} objects.
   * After a successful creation a DAQ which links to the DataTagAddresses can
   * be started.
   * <p>
   * Next to the specified parameters the DataTags are created with default
   * parameters.
   * <p>
   * Note: You have to use {@link DataTag#create(String, Class, DataTagAddress)}
   * to instantiate the 'dataTags' parameter of this method.
   *
   * @param equipmentName The name of the overlying Equipment.
   * @param dataTags      The list of {@link DataTag} configurations for the
   *                      'create'.
   * @return A {@link ConfigurationReport} containing all details of the Tag
   * configuration, including if it was successful or not.
   */
  ConfigurationReport createDataTags(String equipmentName, List<DataTag> dataTags);

  /**
   * Updates a existing 'Tag' with the given parameters set in the {@link Tag}
   * object.
   * <p>
   * Note: You have to use one of the following methods to instantiate the
   * 'tag' parameter of this method.
   * <p>
   * {@link DataTag#update(Long)}, {@link DataTag#update(String)},
   * {@link RuleTag#update(Long)}, {@link RuleTag#update(String)}
   *
   * @param tag The {@link Tag} configuration for the 'update'.
   * @return A {@link ConfigurationReport} containing all details of the
   * Process configuration, including if it was successful or not.
   */
  ConfigurationReport updateTag(Tag tag);

  /**
   * Updates multiple existing 'Tags' with the given parameters set in the
   * {@link Tag} objects.
   * <p>
   * Note: You have to use one of the following methods to instantiate the 'tag' parameter of this method.
   * <p>
   * {@link DataTag#update(Long)}, {@link DataTag#update(String)},
   * {@link RuleTag#update(Long)}, {@link RuleTag#update(String)}
   *
   * @param tags The list of {@link Tag} configurations for the 'updates'.
   * @return A {@link ConfigurationReport} containing all details of the Tag configuration,
   * including if it was successful or not.
   */
  ConfigurationReport updateTags(List<Tag> tags);

  /**
   * Removes a existing 'Tag' with the given id.
   *
   * @param id The id of the Tag which needs to be removed.
   * @return A {@link ConfigurationReport} containing all details of the Tag
   * configuration, including if it was successful or not.
   */
  ConfigurationReport removeTagById(Long id);

  /**
   * Removes multiple existing 'Tags' with the given ids.
   *
   * @param ids The list of ids of the Tags which needs to be removed.
   * @return A {@link ConfigurationReport} containing all details of the
   * Process configuration, including if it was successful or not.
   */
  ConfigurationReport removeTagsById(Set<Long> ids);

  /**
   * Removes a existing 'Tag' with the given name.
   *
   * @param name The name of the Tag which needs to be removed.
   * @return A {@link ConfigurationReport} containing all details of the
   * Process configuration, including if it was successful or not.
   */
  ConfigurationReport removeTag(String name);

  /**
   * Removes multiple existing 'Tags' with the given names.
   *
   * @param names The list of names of the Tags which needs to be removed.
   * @return A {@link ConfigurationReport} containing all details of the Process configuration,
   * including if it was successful or not.
   */
  ConfigurationReport removeTags(Set<String> names);


}
