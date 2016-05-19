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

import cern.c2mon.client.core.configuration.ConfigurationRequestSender;
import cern.c2mon.client.core.configuration.DataTagConfiguration;
import cern.c2mon.shared.client.configuration.ConfigurationReport;
import cern.c2mon.shared.client.configuration.api.Configuration;
import cern.c2mon.shared.client.configuration.api.tag.DataTag;
import cern.c2mon.shared.client.configuration.api.tag.Tag;
import cern.c2mon.shared.common.datatag.DataTagAddress;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static cern.c2mon.client.core.configuration.util.ConfigurationUtil.*;

/**
 * @author Franz Ritter
 */
@Service("dataTagConfiguration")
public class DataTagConfigurationImpl implements DataTagConfiguration {

  private ConfigurationRequestSender configurationRequestSender;

  @Autowired
  DataTagConfigurationImpl(ConfigurationRequestSender configurationRequestSender) {
    this.configurationRequestSender = configurationRequestSender;
  }


  @Override
  public ConfigurationReport createDataTag(Long equipmentId, String name, Class<?> dataType, DataTagAddress address) {

    return createDataTag(equipmentId, DataTag.create(name, dataType, address).build());

  }

  @Override
  public ConfigurationReport createDataTag(Long equipmentId, DataTag dataTag) {


    List<DataTag> dummyDataTagList = new ArrayList<>();
    dummyDataTagList.add(dataTag);

    return createDataTags(equipmentId, dummyDataTagList);
  }

  @Override
  public ConfigurationReport createDataTags(Long equipmentId, List<DataTag> tags) {

    // validate the Configuration object
    validateIsCreate(tags);

    // Set parent Ids to the configuration
    for (Tag tag : tags) {
      tag.setParentId(equipmentId);
    }

    Configuration config = new Configuration();
    config.setConfigurationItems(tags);

    return configurationRequestSender.applyConfiguration(config, null);
  }

  @Override
  public ConfigurationReport createDataTag(String equipmentName, String name, Class<?> dataType, DataTagAddress address) {

    return createDataTag(equipmentName, DataTag.create(name, dataType, address).build());

  }

  @Override
  public ConfigurationReport createDataTag(String equipmentName, DataTag dataTag) {


    List<DataTag> dummyDataTagList = new ArrayList<>();
    dummyDataTagList.add(dataTag);

    return createDataTags(equipmentName, dummyDataTagList);
  }

  @Override
  public ConfigurationReport createDataTags(String equipmentName, List<DataTag> tags) {

    // validate the Configuration object
    validateIsCreate(tags);

    // Set parent Ids to the configuration
    for (DataTag tag : tags) {
      tag.setParentName(equipmentName);
    }

    Configuration config = new Configuration();
    config.setConfigurationItems(tags);

    return configurationRequestSender.applyConfiguration(config, null);
  }

  @Override
  public ConfigurationReport updateTag(Tag tag) {

    List<Tag> dummyTagList = new ArrayList<>();
    dummyTagList.add(tag);

    return updateTags(dummyTagList);
  }

  @Override
  public ConfigurationReport updateTags(List<Tag> tags) {

    // validate the Configuration object
    validateIsUpdate(tags);

    Configuration config = new Configuration();
    config.setConfigurationItems(tags);

    return configurationRequestSender.applyConfiguration(config, null);
  }

  @Override
  public ConfigurationReport removeTag(Long id) {

    List<Long> dummyTagIdList = new ArrayList<>();
    dummyTagIdList.add(id);

    return removeTags(dummyTagIdList);
  }

  @Override
  public ConfigurationReport removeTags(List<Long> ids) {

    List<Tag> tagsToDelete = new ArrayList<>();

    for (Long id : ids) {
      tagsToDelete.add(DataTag.builder().id(id).deleted(true).build());
    }

    Configuration config = new Configuration();
    config.setConfigurationItems(tagsToDelete);

    return configurationRequestSender.applyConfiguration(config, null);
  }

  @Override
  public ConfigurationReport removeTag(String name) {

    List<String> dummyTagNameList = new ArrayList<>();
    dummyTagNameList.add(name);

    return removeTagsByName(dummyTagNameList);
  }

  @Override
  public ConfigurationReport removeTagsByName(List<String> tagNames) {

    List<Tag> tagsToDelete = new ArrayList<>();

    for (String tagName : tagNames) {
      tagsToDelete.add(DataTag.builder().name(tagName).deleted(true).build());
    }

    Configuration config = new Configuration();
    config.setConfigurationItems(tagsToDelete);

    return configurationRequestSender.applyConfiguration(config, null);
  }
}
