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
import cern.c2mon.client.core.configuration.DataTagConfigurationManager;
import cern.c2mon.shared.client.configuration.ConfigurationReport;
import cern.c2mon.shared.client.configuration.api.Configuration;
import cern.c2mon.shared.client.configuration.api.tag.DataTag;
import cern.c2mon.shared.client.configuration.api.tag.Tag;
import cern.c2mon.shared.common.datatag.DataTagAddress;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

import static cern.c2mon.client.core.configuration.util.ConfigurationUtil.*;

/**
 * @author Franz Ritter
 */
@Service("dataTagConfigurationManager")
public class DataTagConfigurationManagerImpl implements DataTagConfigurationManager {

  private ConfigurationRequestSender configurationRequestSender;

  @Autowired
  DataTagConfigurationManagerImpl(ConfigurationRequestSender configurationRequestSender) {
    this.configurationRequestSender = configurationRequestSender;
  }

  @Override
  public ConfigurationReport createDataTag(String equipmentName, String name, Class<?> dataType, DataTagAddress address) {
    return createDataTag(equipmentName, DataTag.create(name, dataType, address).build());
  }

  @Override
  public ConfigurationReport createDataTag(String equipmentName, DataTag dataTag) {
    return createDataTags(equipmentName, Collections.singletonList(dataTag));
  }

  @Override
  public ConfigurationReport createDataTags(String equipmentName, List<DataTag> tags) {
    validateIsCreate(tags);

    for (DataTag tag : tags) {
      tag.setEquipmentName(equipmentName);
    }

    Configuration config = new Configuration();
    config.setEntities(tags);

    return configurationRequestSender.applyConfiguration(config, null);
  }

  @Override
  public ConfigurationReport updateDataTag(DataTag tag) {
    return updateDataTags(Collections.singletonList(tag));
  }

  @Override
  public ConfigurationReport updateDataTags(List<DataTag> tags) {
    validateIsUpdate(tags);

    Configuration config = new Configuration();
    config.setEntities(tags);

    return configurationRequestSender.applyConfiguration(config, null);
  }

  @Override
  public ConfigurationReport removeDataTagById(Long id) {
    return removeDataTagsById(Collections.singleton(id));
  }

  @Override
  public ConfigurationReport removeDataTagsById(Set<Long> ids) {
    List<Tag> tagsToDelete = new ArrayList<>();

    for (Long id : ids) {
      DataTag tagToDelete = new DataTag();
      tagToDelete.setId(id);
      tagToDelete.setDeleted(true);
      tagsToDelete.add(tagToDelete);
    }

    Configuration config = new Configuration();
    config.setEntities(tagsToDelete);

    return configurationRequestSender.applyConfiguration(config, null);
  }

  @Override
  public ConfigurationReport removeDataTag(String name) {
    return removeDataTags(Collections.singleton(name));
  }

  @Override
  public ConfigurationReport removeDataTags(Set<String> tagNames) {
    List<Tag> tagsToDelete = new ArrayList<>();

    for (String tagName : tagNames) {
      DataTag tagToDelete = new DataTag();
      tagToDelete.setName(tagName);
      tagToDelete.setDeleted(true);
      tagsToDelete.add(tagToDelete);
    }

    Configuration config = new Configuration();
    config.setEntities(tagsToDelete);

    return configurationRequestSender.applyConfiguration(config, null);
  }
}
