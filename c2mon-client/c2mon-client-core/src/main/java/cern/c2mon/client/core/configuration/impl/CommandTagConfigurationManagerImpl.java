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
package cern.c2mon.client.core.configuration.impl;

import cern.c2mon.client.core.configuration.CommandTagConfigurationManager;
import cern.c2mon.client.core.configuration.ConfigurationRequestSender;
import cern.c2mon.shared.client.configuration.ConfigurationReport;
import cern.c2mon.shared.client.configuration.api.Configuration;
import cern.c2mon.shared.client.configuration.api.tag.CommandTag;
import cern.c2mon.shared.common.datatag.address.HardwareAddress;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static cern.c2mon.client.core.configuration.util.ConfigurationUtil.validateIsCreate;
import static cern.c2mon.client.core.configuration.util.ConfigurationUtil.validateIsUpdate;

/**
 * @author Franz Ritter
 */
@Service("commandTagConfigurationManager")
public class CommandTagConfigurationManagerImpl implements CommandTagConfigurationManager {

  private ConfigurationRequestSender configurationRequestSender;

  @Autowired
  CommandTagConfigurationManagerImpl(ConfigurationRequestSender configurationRequestSender) {
    this.configurationRequestSender = configurationRequestSender;
  }

  @Override
  public ConfigurationReport createCommandTag(String equipmentName, String name, Class<?> dataType, HardwareAddress
      hardwareAddress, Integer clientTimeout, Integer execTimeout, Integer sourceTimeout, Integer sourceRetries,
                                              String rbacClass, String rbacDevice, String rbacProperty) {

    CommandTag tag = CommandTag.create(name, dataType, hardwareAddress, clientTimeout, execTimeout, sourceTimeout,
        sourceRetries, rbacClass, rbacDevice, rbacProperty).build();

    return createCommandTag(equipmentName, tag);
  }

  @Override
  public ConfigurationReport createCommandTag(String equipmentName, CommandTag commandTag) {

    List<CommandTag> dummyCommandTagList = new ArrayList<>();
    dummyCommandTagList.add(commandTag);

    return createCommandTags(equipmentName, dummyCommandTagList);
  }

  @Override
  public ConfigurationReport createCommandTags(String equipmentName, List<CommandTag> tags) {

    // validate the Configuration object
    validateIsCreate(tags);

    // Set parent Ids to the configuration
    for (CommandTag tag : tags) {
      tag.setEquipmentName(equipmentName);
    }

    Configuration config = new Configuration();
    config.setEntities(tags);

    return configurationRequestSender.applyConfiguration(config, null);
  }

  @Override
  public ConfigurationReport updateCommandTag(CommandTag tag) {

    List<CommandTag> dummyTagList = new ArrayList<>();
    dummyTagList.add(tag);

    return updateCommandTags(dummyTagList);
  }

  @Override
  public ConfigurationReport updateCommandTags(List<CommandTag> tags) {

    // validate the Configuration object
    validateIsUpdate(tags);

    Configuration config = new Configuration();
    config.setEntities(tags);

    return configurationRequestSender.applyConfiguration(config, null);
  }

  @Override
  public ConfigurationReport removeCommandTagById(Long id) {

    Set<Long> dummyTagIdList = new HashSet<>();
    dummyTagIdList.add(id);

    return removeCommandTagsById(dummyTagIdList);
  }

  @Override
  public ConfigurationReport removeCommandTagsById(Set<Long> ids) {

    List<CommandTag> tagsToDelete = new ArrayList<>();

    for (Long id : ids) {
      CommandTag deleteTag = new CommandTag();
      deleteTag.setId(id);
      deleteTag.setDeleted(true);

      tagsToDelete.add(deleteTag);
    }

    Configuration config = new Configuration();
    config.setEntities(tagsToDelete);

    return configurationRequestSender.applyConfiguration(config, null);
  }

  @Override
  public ConfigurationReport removeCommandTag(String name) {

    Set<String> dummyTagNameList = new HashSet<>();
    dummyTagNameList.add(name);

    return removeCommandTags(dummyTagNameList);
  }

  @Override
  public ConfigurationReport removeCommandTags(Set<String> tagNames) {

    List<CommandTag> tagsToDelete = new ArrayList<>();

    for (String tagName : tagNames) {
      CommandTag deleteTag = new CommandTag();
      deleteTag.setName(tagName);
      deleteTag.setDeleted(true);
      tagsToDelete.add(deleteTag);
    }

    Configuration config = new Configuration();
    config.setEntities(tagsToDelete);

    return configurationRequestSender.applyConfiguration(config, null);
  }
}
