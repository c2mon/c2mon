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
import cern.c2mon.server.configuration.parser.tasks.SequenceTask;
import cern.c2mon.server.configuration.parser.util.ConfigurationParserUtil;
import cern.c2mon.shared.client.configuration.ConfigurationElement;
import cern.c2mon.shared.client.configuration.api.Configuration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This class holds all information about a Configuration object to translate
 * this object in a List of {@link ConfigurationElement}.
 *
 * @author Franz Ritter
 */
@Component
public class ConfigurationParserImpl implements ConfigurationParser {

  private CRUDConfigurationParser crudConfigurationParser;
  private HierarchicalConfigurationParser hierarchicalConfigurationParser;

  private ConfigurationParserUtil parserUtil;

  @Autowired
  public ConfigurationParserImpl(HierarchicalConfigurationParser hierarchicalConfigurationParser,
                                 CRUDConfigurationParser crudConfigurationParser,
                                 ConfigurationParserUtil parserUtil) {
    this.crudConfigurationParser = crudConfigurationParser;
    this.hierarchicalConfigurationParser = hierarchicalConfigurationParser;
    this.parserUtil = parserUtil;
  }

  /**
   * Parse the given {@link Configuration} into a list of {@link ConfigurationElement}
   * The information how each {@link ConfigurationElement} is build is provided by the fields of each {@link ConfigurationElement}.
   * After creating a List of single Tasks the Method sort all tasks to a specific order.
   * Because of the sorting the returning list will be handeld in the right way.
   *
   * @param configuration Object which holds all information for creating the list of {@link ConfigurationElement}
   * @return List of ConfigurationElement, which are used by the {@link cern.c2mon.server.configuration.ConfigurationLoader}
   */
  @Override
  public List<ConfigurationElement> parse(Configuration configuration) {
    List<SequenceTask> taskResultList;

    if (!parserUtil.isEmptyCollection(configuration.getProcesses()) || !parserUtil.isEmptyCollection(configuration.getRules())) {

      taskResultList = hierarchicalConfigurationParser.parseHierarchicalConfiguration(configuration);

    } else {

      taskResultList = crudConfigurationParser.parseCRUDConfiguration(configuration);

    }
    // create and return the actual configuration elements based on the parsed information in the taskResultList
    return finalizeConfiguration(configuration, taskResultList);
  }

  /**
   * Puts the configuration tasks in the right order.
   * Also removes all null values from the created {@link SequenceTask} list.
   * This null object represent shell objects which holds no information about the actual configuration.
   *
   * @param config        The main configuration object from the client.
   * @param sequenceTasks The created {@link SequenceTask}s created through the parsing of the {@link Configuration}.
   * @return The concrete {@link ConfigurationElement} for the server configuration.
   */
  private List<ConfigurationElement> finalizeConfiguration(Configuration config, List<SequenceTask> sequenceTasks) {
    List<ConfigurationElement> elements = new ArrayList<>();

    // After parsing configure given list and put it in the right order
    // remove all null taskResultList which are are created from shell-objects
    sequenceTasks.removeAll(Collections.singleton(null));
    Collections.sort(sequenceTasks);

    long confId = config.getConfigurationId() == null ? parserUtil.getNextConfigId() : config.getConfigurationId();
    long seqId = 0l;

    for (SequenceTask task : sequenceTasks) {
      ConfigurationElement element = task.getConfigurationElement();
      element.setSequenceId(seqId++);
      element.setConfigId(confId);
      elements.add(element);
    }

    return elements;
  }
}


