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
import cern.c2mon.shared.client.configuration.api.tag.RuleTag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


import static cern.c2mon.client.core.configuration.util.ConfigurationUtil.validateIsCreate;

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
}
