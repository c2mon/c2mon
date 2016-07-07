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
import cern.c2mon.client.core.configuration.RuleTagConfigurationManager;
import cern.c2mon.shared.client.configuration.ConfigurationReport;
import cern.c2mon.shared.client.configuration.api.Configuration;
import cern.c2mon.shared.client.configuration.api.tag.DataTag;
import cern.c2mon.shared.client.configuration.api.tag.RuleTag;
import cern.c2mon.shared.client.configuration.api.tag.Tag;
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
@Service("ruleTagConfigurationManager")
public class RuleTagConfigurationManagerImpl implements RuleTagConfigurationManager {

  private ConfigurationRequestSender configurationRequestSender;

  @Autowired
  RuleTagConfigurationManagerImpl(ConfigurationRequestSender configurationRequestSender) {
    this.configurationRequestSender = configurationRequestSender;
  }

  @Override
  public ConfigurationReport createRule(String ruleExpression, String name, Class<?> dataType) {

    return createRule(RuleTag.create(name, dataType, ruleExpression).build());
  }

  @Override
  public ConfigurationReport createRule(RuleTag createRuleTag) {

    List<RuleTag> dummyRuleList = new ArrayList<>();
    dummyRuleList.add(createRuleTag);

    return createRules(dummyRuleList);
  }

  @Override
  public ConfigurationReport createRules(List<RuleTag> ruleTags) {

    validateIsCreate(ruleTags);
    Configuration config = new Configuration();
    config.setEntities(ruleTags);

    return configurationRequestSender.applyConfiguration(config, null);
  }

  @Override
  public ConfigurationReport updateRuleTag(RuleTag tag) {

    List<RuleTag> dummyTagList = new ArrayList<>();
    dummyTagList.add(tag);

    return updateRuleTags(dummyTagList);
  }

  @Override
  public ConfigurationReport updateRuleTags(List<RuleTag> tags) {

    // validate the Configuration object
    validateIsUpdate(tags);

    Configuration config = new Configuration();
    config.setEntities(tags);

    return configurationRequestSender.applyConfiguration(config, null);
  }

  @Override
  public ConfigurationReport removeRuleTagById(Long id) {

    Set<Long> dummyTagIdList = new HashSet<>();
    dummyTagIdList.add(id);

    return removeRuleTagsById(dummyTagIdList);
  }

  @Override
  public ConfigurationReport removeRuleTagsById(Set<Long> ids) {

    List<RuleTag> tagsToDelete = new ArrayList<>();

    for (Long id : ids) {
      RuleTag deleteTag = new RuleTag();
      deleteTag.setId(id);
      deleteTag.setDeleted(true);
      tagsToDelete.add(deleteTag);
    }

    Configuration config = new Configuration();
    config.setEntities(tagsToDelete);

    return configurationRequestSender.applyConfiguration(config, null);
  }

  @Override
  public ConfigurationReport removeRuleTag(String name) {

    Set<String> dummyTagNameList = new HashSet<>();
    dummyTagNameList.add(name);

    return removeRuleTags(dummyTagNameList);
  }

  @Override
  public ConfigurationReport removeRuleTags(Set<String> tagNames) {

    List<RuleTag> tagsToDelete = new ArrayList<>();

    for (String tagName : tagNames) {
      RuleTag deleteTag = new RuleTag();
      deleteTag.setName(tagName);
      deleteTag.setDeleted(true);
      tagsToDelete.add(deleteTag);
    }

    Configuration config = new Configuration();
    config.setEntities(tagsToDelete);

    return configurationRequestSender.applyConfiguration(config, null);
  }
}
