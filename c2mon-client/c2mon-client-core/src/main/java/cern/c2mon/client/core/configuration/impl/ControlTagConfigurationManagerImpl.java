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
import cern.c2mon.client.core.configuration.ControlTagConfigurationManager;
import cern.c2mon.shared.client.configuration.ConfigurationReport;
import cern.c2mon.shared.client.configuration.api.Configuration;
import cern.c2mon.shared.client.configuration.api.tag.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static cern.c2mon.client.core.configuration.util.ConfigurationUtil.validateIsUpdate;

/**
 * @author Franz Ritter
 */
@Service("controlTagConfigurationManager")
public class ControlTagConfigurationManagerImpl implements ControlTagConfigurationManager {

  private ConfigurationRequestSender configurationRequestSender;

  @Override
  public ConfigurationReport updateAliveTag(AliveTag tag) {

    List<AliveTag> dummyTagList = new ArrayList<>();
    dummyTagList.add(tag);

    return updateAliveTags(dummyTagList);
  }

  @Override
  public ConfigurationReport updateAliveTags(List<AliveTag> tags) {

    // validate the Configuration object
    validateIsUpdate(tags);

    Configuration config = new Configuration();
    config.setEntities(tags);

    return configurationRequestSender.applyConfiguration(config, null);
  }

  @Override
  public ConfigurationReport updateCommFaultTag(CommFaultTag tag) {

    List<CommFaultTag> dummyTagList = new ArrayList<>();
    dummyTagList.add(tag);

    return updateCommFaultTags(dummyTagList);
  }

  @Override
  public ConfigurationReport updateCommFaultTags(List<CommFaultTag> tags) {

    // validate the Configuration object
    validateIsUpdate(tags);

    Configuration config = new Configuration();
    config.setEntities(tags);

    return configurationRequestSender.applyConfiguration(config, null);
  }

  @Override
  public ConfigurationReport updateStatusTag(StatusTag tag) {

    List<StatusTag> dummyTagList = new ArrayList<>();
    dummyTagList.add(tag);

    return updateStatusTags(dummyTagList);
  }

  @Override
  public ConfigurationReport updateStatusTags(List<StatusTag> tags) {

    // validate the Configuration object
    validateIsUpdate(tags);

    Configuration config = new Configuration();
    config.setEntities(tags);

    return configurationRequestSender.applyConfiguration(config, null);
  }
}
