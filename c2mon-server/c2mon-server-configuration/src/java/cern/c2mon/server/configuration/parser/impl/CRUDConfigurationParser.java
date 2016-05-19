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

import cern.c2mon.server.cache.TagFacadeGateway;
import cern.c2mon.server.configuration.parser.exception.ConfigurationParseException;
import cern.c2mon.server.configuration.parser.tasks.SequenceTask;
import cern.c2mon.server.configuration.parser.tasks.SequenceTaskFactory;
import cern.c2mon.server.configuration.parser.tasks.util.ConfigurationObjectTypeHandler;
import cern.c2mon.server.configuration.parser.util.ConfigurationParserUtil;
import cern.c2mon.shared.client.configuration.api.Configuration;
import cern.c2mon.shared.client.configuration.api.alarm.Alarm;
import cern.c2mon.shared.client.configuration.api.equipment.AbstractEquipment;
import cern.c2mon.shared.client.configuration.api.equipment.Equipment;
import cern.c2mon.shared.client.configuration.api.equipment.SubEquipment;
import cern.c2mon.shared.client.configuration.api.process.Process;
import cern.c2mon.shared.client.configuration.api.tag.DataTag;
import cern.c2mon.shared.client.configuration.api.tag.RuleTag;
import cern.c2mon.shared.client.configuration.api.tag.Tag;
import cern.c2mon.shared.client.configuration.api.util.ConfigurationObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Franz Ritter
 */
@Service
public class CRUDConfigurationParser {

  private SequenceTaskFactory sequenceTaskFactory;
  private ConfigurationParserUtil parserUtil;
  private ConfigurationObjectTypeHandler typeHandler;
  private TagFacadeGateway tagFacadeGateway;
  private HierarchicalConfigurationParser hierarchicParser;

  @Autowired
  public CRUDConfigurationParser(SequenceTaskFactory sequenceTaskFactory, ConfigurationParserUtil parserUtil,
                                 ConfigurationObjectTypeHandler typeHandler, TagFacadeGateway tagFacadeGateway,
                                 HierarchicalConfigurationParser hierarchicalConfigurationParser) {
    this.sequenceTaskFactory = sequenceTaskFactory;
    this.parserUtil = parserUtil;
    this.typeHandler = typeHandler;
    this.tagFacadeGateway = tagFacadeGateway;
    this.hierarchicParser = hierarchicalConfigurationParser;
  }
  // TODO: add RuleUpdates

  public List<SequenceTask> parseCRUDConfiguration(Configuration configuration) {

    if (!parserUtil.isEmptyCollection(configuration.getConfigurationItems())) {
      return parseCRUDList(configuration.getConfigurationItems());
    } else {

      // TODO throw exception
      throw new RuntimeException();
    }
  }

  /**
   * Parses a list of a configuration type and determines what kind of configuration needs to be parsed.
   *
   * @param configuration
   * @return
   */
  private List<SequenceTask> parseCRUDList(List<? extends ConfigurationObject> configurations) {
    List<SequenceTask> taskResultList = new ArrayList<>();
    SequenceTask tempConfigurationTask;

    for (ConfigurationObject configuration : configurations) {

      // ===================== Delete =====================

      if (configuration.isDeleted()) {

        taskResultList.add(parseDelete(configuration));

        // ===================== Update =====================

      } else if (configuration.isUpdate()) {

        taskResultList.add(parseUpdate(configuration));

        // ===================== Create =====================

      } else if (configuration.isCreate()) {

        taskResultList.addAll(parseCreate(configuration));
      } else {
        throw new IllegalArgumentException("Error while parsing the Configuration " + configuration.getClass() + ": No action flag set.");
      }
    }
    return taskResultList;
  }

  /**
   * Parses all delete configurations
   *
   * @param configuration the generic configuration
   * @return A delete {@link SequenceTask}
   */
  private SequenceTask parseDelete(ConfigurationObject configuration) {

    if (configuration.getId() == null) {
      configuration = typeHandler.setIdForConfigurationObject(configuration, configuration.getClass());
    }
    return sequenceTaskFactory.createDeleteSequenceTask(configuration);

  }

  /**
   * Parses all update configurations
   *
   * @param configuration the generic configuration
   * @return A update {@link SequenceTask}
   */
  private SequenceTask parseUpdate(ConfigurationObject configuration) {

    if (configuration.getId() == null) {
      configuration = typeHandler.setIdForConfigurationObject(configuration, configuration.getClass());
    }
    return sequenceTaskFactory.createUpdateSequenceTask(configuration);

  }

  /**
   * Parses all create configurations
   *
   * @param configuration the generic configuration
   * @return A list of create {@link SequenceTask}s
   */
  private List<SequenceTask> parseCreate(ConfigurationObject configuration) {

    // temporary List which holds all task for creating one object(including control tags)
    List<SequenceTask> createTasks = new ArrayList<>();

    if (configuration instanceof Process) {

      createTasks = getProcessCreateTasks((Process) configuration);

    } else if (configuration instanceof Equipment) {

      createTasks = getEquipmentCreateTasks((Equipment) configuration);

    } else if (configuration instanceof SubEquipment) {

      createTasks = getSubEquipmentCreateTasks((SubEquipment) configuration);

    } else if (configuration instanceof DataTag) {

      createTasks.add(getDataTagCreateTasks((DataTag) configuration));

    } else if (configuration instanceof Alarm) {

      createTasks.add(getAlarmCreateTasks((Alarm) configuration));

    } else if (configuration instanceof RuleTag) {

      createTasks.add(getRuleTagCreateTasks((RuleTag) configuration));

    } else {

      throw new IllegalArgumentException("Error occurred while a 'create' Configuration: " + configuration.getClass() + " not creatable.");

    }
    return createTasks;
  }

  //===========================================================================
  // single command (delete, update, create) methods
  //===========================================================================

  /**
   * Parses a list of Process 'create' configurations.
   * The parsing only effects the Processes and attached {@ling ControlTag}s.
   * {@link Equipment}s, {@link DataTag}s and {@link Alarm}s are excluded from the parsing.
   * <p/>
   * All retrieved {@link SequenceTask}s will be added to the task-list as side effect!
   *
   * @param tasks     List of configuration tasks which gets filled due to the side effect of this method.
   * @param processes list of all processes from the ConfigurationObject
   * @return List of configuration tasks for the server. This list is the same list as the one in the parameters which is extended with the configuration of this object.
   */
  private List<SequenceTask> getProcessCreateTasks(Process createProcess) {
    // Contains all tasks to create the equipment (including control tag creation)
    List<SequenceTask> equipmentCreateTasks = new ArrayList<>();

    SequenceTask tempTask;

    tempTask = sequenceTaskFactory.createCreateSequenceTask(createProcess);
    tempTask = parserUtil.setControlTags(tempTask, createProcess);
    equipmentCreateTasks.add(tempTask);

    // parse the attached objects of the process:
    List<SequenceTask> controlTagTasks = hierarchicParser.addControlTags(equipmentCreateTasks, createProcess, createProcess.getStatusTag(), createProcess.getAliveTag());
    equipmentCreateTasks = parserUtil.validateControlTags(equipmentCreateTasks, tempTask, controlTagTasks);

    return equipmentCreateTasks;
  }

  /**
   * Parses all {@link Equipment}s which only contains the information to 'create' a new Equipment.
   * The parsing only effects the Equipment and attached {@ling ControlTag}s.
   * {@link Alarm}s and {@link DataTag}s are excluded from the parsing.
   * <p/>
   * All retrieved {@link SequenceTask}s will be dded to the task-list as side effect!
   *
   * @param tasks List which gets filled due to the side effect of this method.
   * @param rules list of all Rules of the overlying {@link Equipment}
   * @return List of configuration tasks for the server. This list is the same list as the one in the parameters which is extended with the configuration of this object.
   */
  private List<SequenceTask> getEquipmentCreateTasks(Equipment createEquipment) {
    // Contains all tasks to create the equipment (including control tag creation)
    List<SequenceTask> equipmentCreateTasks = new ArrayList<>();

    SequenceTask tempTask;
    Process dummyProcess;

    Long processId = createEquipment.getParentProcessId() != null ? createEquipment.getParentProcessId() : typeHandler.getIdByName(createEquipment.getParentProcessName(), Process.class);

    // check information about the parent id
    if (typeHandler.cacheHasId(processId, Process.class)) {
      tempTask = sequenceTaskFactory.createCreateSequenceTask(createEquipment);
      tempTask = parserUtil.setControlTags(tempTask, createEquipment);

      // set parent Id
      dummyProcess = Process.builder().id(processId).build();
      equipmentCreateTasks.add(parserUtil.setParentId(tempTask, dummyProcess));

      // parse the attached objects of the equipment:
      List<SequenceTask> controlTagTasks = hierarchicParser.addControlTags(equipmentCreateTasks,
          createEquipment,
          createEquipment.getStatusTag(),
          createEquipment.getCommFaultTag(),
          createEquipment.getAliveTag());

      equipmentCreateTasks = parserUtil.validateControlTags(equipmentCreateTasks, tempTask, controlTagTasks);

    } else {
      throw new ConfigurationParseException("Creating of a new Equipment (id = " + createEquipment.getId() + ") failed: No Process with the id " + processId + " found");
    }

    return equipmentCreateTasks;
  }

  /**
   * Parses all {@link Equipment}s which only contains the information to 'create' a new Equipment.
   * The parsing only effects the Equipment and attached {@ling ControlTag}s.
   * {@link Alarm}s and {@link DataTag}s are excluded from the parsing.
   * <p/>
   * All retrieved {@link SequenceTask}s will be dded to the task-list as side effect!
   *
   * @param tasks List which gets filled due to the side effect of this method.
   * @param rules list of all Rules of the overlying {@link Equipment}
   * @return List of configuration tasks for the server. This list is the same list as the one in the parameters which is extended with the configuration of this object.
   */
  private List<SequenceTask> getSubEquipmentCreateTasks(SubEquipment createSubEquipment) {
    // Contains all tasks to create the equipment (including control tag creation)
    List<SequenceTask> subEquipmentCreateTasks = new ArrayList<>();

    SequenceTask tempTask;


    Long equipmentId = createSubEquipment.getParentEquipmentId() != null ? createSubEquipment.getParentEquipmentId() : typeHandler.getIdByName(createSubEquipment.getParentEquipmentName(), Equipment.class);

    // check information about the parent id
    if (typeHandler.cacheHasId(equipmentId, Equipment.class)) {
      tempTask = sequenceTaskFactory.createCreateSequenceTask(createSubEquipment);
      tempTask = parserUtil.setControlTags(tempTask, createSubEquipment);

      // set parent Id
      Equipment dummyEquipment = Equipment.builder().id(equipmentId).build();
      subEquipmentCreateTasks.add(parserUtil.setParentId(tempTask, dummyEquipment));

      // parse the attached objects of the equipment:
      List<SequenceTask> controlTagTasks = hierarchicParser.addControlTags(subEquipmentCreateTasks,
          createSubEquipment,
          createSubEquipment.getStatusTag(),
          createSubEquipment.getCommFaultTag(),
          createSubEquipment.getAliveTag());

      subEquipmentCreateTasks = parserUtil.validateControlTags(subEquipmentCreateTasks, tempTask, controlTagTasks);

    } else {
      throw new ConfigurationParseException("Creating of a new SubEquipment (id = " + createSubEquipment.getId() + ") failed: No Equipment with the id " + equipmentId + " found");
    }

    return subEquipmentCreateTasks;
  }

  /**
   * Parses all {@link Tag}s which only hold the information for a create.
   * All retrieved {@link SequenceTask}s will be added to the task-list as side effect!
   *
   * @param tasks List which gets filled due to the side effect of this method.
   * @param rules list of all Rules of the overlying {@link Tag}
   */
  private SequenceTask getDataTagCreateTasks(DataTag createDataTag) {
    SequenceTask resultTask;
    AbstractEquipment dummyEquipment;

    Long equipmentId = createDataTag.getParentId() != null ? createDataTag.getParentId() : typeHandler.getIdByName(createDataTag.getParentName(), Equipment.class);

    resultTask = sequenceTaskFactory.createCreateSequenceTask(createDataTag);

    if (typeHandler.cacheHasId(equipmentId, Equipment.class)) {

      dummyEquipment = Equipment.builder().id(equipmentId).build();

    } else if (typeHandler.cacheHasId(equipmentId, SubEquipment.class)) {

      dummyEquipment = SubEquipment.builder().id(equipmentId).build();

    } else {
      throw new ConfigurationParseException("Creating of a new DataTag (id = " + createDataTag.getId() + ") failed: No Equipment or SubEquipment with the id " + equipmentId + " found");
    }
    resultTask = parserUtil.setParentId(resultTask, dummyEquipment);

    return resultTask;
  }

  /**
   * Parses a {@link RuleTag} 'create' configurations.
   * The parsing only effects the RuleTags.
   * All retrieved {@link SequenceTask}s will be added to the task-list as side effect!
   *
   * @param tasks   List of configuration tasks which gets filled due to the side effect of this method.
   * @param ruleTag The configuration of the ruleTag
   * @return List of configuration tasks for the server. This list is the same list as the one in the parameters.
   */
  private SequenceTask getRuleTagCreateTasks(RuleTag ruleTag) {

    return sequenceTaskFactory.createCreateSequenceTask(ruleTag);
  }

  /**
   * Parses all {@link Alarms}s which only hold the information for a create.
   * All retrieved {@link SequenceTask}s will be added to the task-list as side effect!
   *
   * @param tasks List which gets filled due to the side effect of this method.
   * @param tagId the id to which the alarm belongs
   * @param rules the alarm which needs to be created
   */
  private SequenceTask getAlarmCreateTasks(Alarm createAlarm) {
    SequenceTask resultTask;
    DataTag dummyDataTag;

    Long tagId = createAlarm.getParentTagId() != null ? createAlarm.getParentTagId() : typeHandler.getIdByName(createAlarm.getParentTagName(), DataTag.class);

    // Check if the parent id exists
    if (tagFacadeGateway.isInTagCache(tagId)) {
      resultTask = sequenceTaskFactory.createCreateSequenceTask(createAlarm);

      // set parent Id
      dummyDataTag = DataTag.builder().id(tagId).build();
      parserUtil.setParentId(resultTask, dummyDataTag);

    } else {
      throw new ConfigurationParseException("Creating of a new Alarm (id = " + createAlarm.getId() + ") failed: No Tag with the id " + tagId + " found");
    }

    return resultTask;
  }

}
