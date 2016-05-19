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

import cern.c2mon.server.configuration.parser.exception.ConfigurationParseException;
import cern.c2mon.server.configuration.parser.tasks.SequenceTask;
import cern.c2mon.server.configuration.parser.tasks.SequenceTaskFactory;
import cern.c2mon.server.configuration.parser.util.ConfigurationParserUtil;
import cern.c2mon.shared.client.configuration.ConfigConstants;
import cern.c2mon.shared.client.configuration.api.Configuration;
import cern.c2mon.shared.client.configuration.api.alarm.Alarm;
import cern.c2mon.shared.client.configuration.api.equipment.AbstractEquipment;
import cern.c2mon.shared.client.configuration.api.equipment.Equipment;
import cern.c2mon.shared.client.configuration.api.equipment.SubEquipment;
import cern.c2mon.shared.client.configuration.api.process.Process;
import cern.c2mon.shared.client.configuration.api.tag.*;
import cern.c2mon.shared.client.configuration.api.util.ConfigurationObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Franz Ritter
 */
@Service
public class HierarchicalConfigurationParser {

  private SequenceTaskFactory sequenceTaskFactory;
  private ConfigurationParserUtil parserUtil;


  @Autowired
  public HierarchicalConfigurationParser(SequenceTaskFactory sequenceTaskFactory, ConfigurationParserUtil parserUtil) {
    this.sequenceTaskFactory = sequenceTaskFactory;
    this.parserUtil = parserUtil;
  }

  public List<SequenceTask> parseHierarchicalConfiguration(Configuration configuration) {

    List<SequenceTask> taskResultList = new ArrayList<>();

    // parse all processes for create, update and remove
    if (!parserUtil.isEmptyCollection(configuration.getProcesses())) {
      taskResultList = addProcesses(taskResultList, configuration.getProcesses());
    }

    // parse all rules for create, update and remove
    if (!parserUtil.isEmptyCollection(configuration.getRules())) {
      taskResultList = addRules(taskResultList, configuration.getRules());
    }

    return taskResultList;
  }

  //===========================================================================
  // recursive parser methods (for hierarchical config)
  //===========================================================================

  /**
   * Parses all {@link Process} and underlying {@link ConfigurationsequenceTaskFactoryObject}s.
   * All retrieved {@link SequenceTask}s will be added to the task-list as side effect!
   *
   * @param tasks     List which gets filled due to the side effect of this method.
   * @param processes list of all processes from the ConfigurationObject
   * @return List of configuration tasks for the server. This list is the same list as the one in the parameters which is extended with the configuration of this object.
   */
  private List<SequenceTask> addProcesses(List<SequenceTask> tasks, List<Process> processes) {
    SequenceTask tempTask;
    for (Process process : processes) {
      tempTask = sequenceTaskFactory.createSequenceTask(process);
      tempTask = parserUtil.setControlTags(tempTask, process);
      tasks.add(tempTask);

      // parse the attached objects of the process:
      List<SequenceTask> controlTagTasks = addControlTags(tasks, process, process.getStatusTag(), process.getAliveTag());
      tasks = parserUtil.validateControlTags(tasks, tempTask, controlTagTasks);
      tasks = addEquipments(tasks, process.getEquipments(), process);
    }

    return tasks;
  }


  /**
   * Parses all {@link Equipment} and underlying ConfigurationObjects.
   * All retrieved {@link SequenceTask}s will be added to the task-list as side effect!
   *
   * @param tasks      List which gets filled due to the side effect of this method.
   * @param equipments list of all equipments of the overlying object
   * @param parent     Overlying Process which holds this Equipment
   * @return List of configuration tasks for the server. This list is the same list as the one in the parameters which is extended with the configuration of this object.
   */
  private List<SequenceTask> addEquipments(List<SequenceTask> tasks, List<Equipment> equipments, Process parent) {
    SequenceTask tempTask;
    for (Equipment equipment : equipments) {
      tempTask = sequenceTaskFactory.createSequenceTask(equipment);
      tempTask = parserUtil.setControlTags(tempTask, equipment);
      tasks.add(parserUtil.setParentId(tempTask, parent));

      // parse the attached objects of the equipment:
      List<SequenceTask> controlTagTasks = addControlTags(tasks, equipment, equipment.getStatusTag(), equipment.getCommFaultTag(), equipment.getAliveTag());
      tasks = parserUtil.validateControlTags(tasks, tempTask, controlTagTasks);

      tasks = addDataTags(tasks, equipment, equipment.getDataTags());
      tasks = addCommandTags(tasks, equipment, equipment.getCommandTags());
      tasks = addSubEquipments(tasks, equipment.getSubEquipments(), equipment);
    }
    return tasks;
  }


  /**
   * Parses all {@link SubEquipment} and underlying ConfigurationObjects.
   * All retrieved {@link SequenceTask}s will be added to the task-list as side effect!
   *
   * @param tasks         List which gets filled due to the side effect of this method.
   * @param subEquipments list of all subEquipments of the overlying object
   * @param parent        Overlying Equipment which holds this Equipment
   * @return List of configuration tasks for the server. This list is the same list as the one in the parameters which is extended with the configuration of this object.
   */
  private List<SequenceTask> addSubEquipments(List<SequenceTask> tasks, List<SubEquipment> subEquipments, Equipment parent) {
    SequenceTask tempTask;
    for (SubEquipment subEquipment : subEquipments) {
      tempTask = sequenceTaskFactory.createSequenceTask(subEquipment);
      tempTask = parserUtil.setControlTags(tempTask, subEquipment);
      tasks.add(parserUtil.setParentId(tempTask, parent));

      // parse the attached objects of the subEquipment:
      List<SequenceTask> controlTagTasks = addControlTags(tasks, subEquipment, subEquipment.getStatusTag(), subEquipment.getCommFaultTag(), subEquipment
          .getAliveTag());
      tasks = parserUtil.validateControlTags(tasks, tempTask, controlTagTasks);
      tasks = addDataTags(tasks, subEquipment, subEquipment.getDataTags());
    }
    return tasks;
  }

  /**
   * Parses all {@link DataTag}s and underlying {@link Alarm}s.
   * All retrieved {@link SequenceTask}s will be added to the task-list as side effect!
   *
   * @param tasks  List which gets filled due to the side effect of this method.
   * @param parent Overlying Object which holds this Tag.
   * @param tags   list of all DataTags of the overlying object
   * @return List of configuration tasks for the server. This list is the same list as the one in the parameters which is extended with the configuration of this object.
   */
  private List<SequenceTask> addDataTags(List<SequenceTask> tasks, ConfigurationObject parent, List<DataTag> tags) {
    SequenceTask tempTask;

    for (DataTag tag : tags) {
      if (parent instanceof AbstractEquipment) {
        tempTask = sequenceTaskFactory.createSequenceTask(tag);
        tasks.add(parserUtil.setParentId(tempTask, parent));
      } else {
        throw new ConfigurationParseException("Parsing DataTag " + tag.getId() + " failed. Containing ClassType dosent exist.");
      }
      tasks = addAlarms(tasks, tag.getAlarms(), tag);
    }

    return tasks;
  }

  /**
   * Parses all {@link CommandTag}s.
   * CommandTags are only attached to equipments. Because of that there is no need to use the setParentId() method
   * for setting the equipmentId after calling the createSequenceTask() method.
   * All retrieved {@link SequenceTask}s will be added to the task-list as side effect!
   *
   * @param tasks  List which gets filled due to the side effect of this method.
   * @param parent Overlying equipment which holds this Tags.
   * @param tags   list of all CommandTags of the overlying equipment
   * @return List of configuration tasks for the server. This list is the same list as the one in the parameters which is extended with the configuration of this object.
   */
  private List<SequenceTask> addCommandTags(List<SequenceTask> tasks, Equipment parent, List<CommandTag> tags) {
    SequenceTask tempSeq;

    for (CommandTag tag : tags) {
      tempSeq = sequenceTaskFactory.createSequenceTask(tag);

      //set the parentId (equipmentId)
      if (tempSeq != null
          && !tempSeq.getConfigurationElement().getElementProperties().isEmpty()
          && tempSeq.getConfigurationElement().getAction().equals(ConfigConstants.Action.CREATE)) {
        tempSeq.getConfigurationElement().getElementProperties().setProperty("equipmentId", String.valueOf(parent.getId()));
      }
      tasks.add(tempSeq);
    }

    return tasks;
  }

  public List<SequenceTask> addControlTags(List<SequenceTask> tasks, ConfigurationObject parent, ControlTag... tags) {
    SequenceTask tempSeq;
    List<SequenceTask> result = new ArrayList<>();

    for (Tag tag : tags) {
      if (tag != null) {

        if (parent instanceof Process || parent instanceof AbstractEquipment) {
          tempSeq = sequenceTaskFactory.createSequenceTask(tag);

          if (tempSeq != null) {
            tasks.add(parserUtil.setParentId(tempSeq, parent));
            result.add(tempSeq);
          }

        } else {
          throw new ConfigurationParseException("Parsing ControlTag " + tag.getId() + " failed. Containing ClassType does not exist.");
        }

        tasks = addAlarms(tasks, tag.getAlarms(), tag);
      }
    }
    return result;
  }

  /**
   * Parses all {@link Alarm}s.
   * All retrieved {@link SequenceTask}s will be added to the task-list as side effect!
   *
   * @param tasks  List which gets filled due to the side effect of this method.
   * @param alarms list of all Alarms of the overlying object
   * @param parent Overlying Object which holds this Tag.
   * @return List of configuration tasks for the server. This list is the same list as the one in the parameters which is extended with the configuration of this object.
   */
  private List<SequenceTask> addAlarms(List<SequenceTask> tasks, List<Alarm> alarms, ConfigurationObject parent) {
    SequenceTask tempSeq;
    for (Alarm alarm : alarms) {
      if (parent instanceof Tag) {

        tempSeq = sequenceTaskFactory.createSequenceTask(alarm);
        if (tempSeq != null) {

          if (!tempSeq.getConfigurationElement().getElementProperties().isEmpty() && tempSeq.getConfigurationElement().getAction().equals(ConfigConstants.Action.CREATE)) {
            tempSeq.getConfigurationElement().getElementProperties().setProperty("dataTagId", String.valueOf(parent.getId()));
          }

        }
        tasks.add(tempSeq);
      } else {
        throw new ConfigurationParseException("Parsing Alarm " + alarm.getId() + " failed. Containing ClassType does not exist.");
      }
    }
    return tasks;
  }

  /**
   * Parses all {@link RuleTag}s and underlying {@link Alarm}s.
   * All retrieved {@link SequenceTask}s will be added to the task-list as side effect!
   *
   * @param tasks List which gets filled due to the side effect of this method.
   * @param rules list of all Rules of the overlying {@link Tag}
   * @return List of configuration tasks for the server. This list is the same list as the one in the parameters which is extended with the configuration of this object.
   */
  private List<SequenceTask> addRules(List<SequenceTask> tasks, List<RuleTag> rules) {
    SequenceTask tempSeq;
    for (RuleTag rule : rules) {
      tempSeq = sequenceTaskFactory.createSequenceTask(rule);
      tasks.add(tempSeq);
      tasks = addAlarms(tasks, rule.getAlarms(), rule);
    }
    return tasks;
  }


}
