package cern.c2mon.server.configuration.parser.impl;

import cern.c2mon.server.configuration.parser.ConfigurationParser;
import cern.c2mon.server.configuration.parser.exception.ConfigurationParseException;
import cern.c2mon.server.configuration.parser.tasks.SequenceTask;
import cern.c2mon.server.configuration.parser.tasks.SequenceTaskFactory;
import cern.c2mon.shared.client.configuration.ConfigConstants.Action;
import cern.c2mon.shared.client.configuration.ConfigurationElement;
import cern.c2mon.shared.client.configuration.api.Configuration;
import cern.c2mon.shared.client.configuration.api.alarm.Alarm;
import cern.c2mon.shared.client.configuration.api.equipment.Equipment;
import cern.c2mon.shared.client.configuration.api.equipment.SubEquipment;
import cern.c2mon.shared.client.configuration.api.process.Process;
import cern.c2mon.shared.client.configuration.api.tag.*;
import cern.c2mon.shared.client.configuration.api.util.ConfigurationObject;
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

  SequenceTaskFactory sequenceTaskFactory;

  @Autowired
  public ConfigurationParserImpl(SequenceTaskFactory sequenceTaskFactory) {
    this.sequenceTaskFactory = sequenceTaskFactory;
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
    List<ConfigurationElement> elements = new ArrayList<>();
    List<SequenceTask> tasks = new ArrayList<>();

    // need to check if there are any processes or Rules are given
    if (configuration.getProcesses() != null) {
      parseProcesses(tasks, configuration.getProcesses());
    }
    if (configuration.getRules() != null) {
      parseRules(tasks, configuration.getRules());
    }

    // After parsing configure given list and put it in the right order
    // remove all null tasks which are are created from shell-objects
    tasks.removeAll(Collections.singleton(null));
    Collections.sort(tasks);
    long seqId = 0l;
    for (SequenceTask task : tasks) {
      ConfigurationElement element = task.getConfigurationElement();
      element.setSequenceId(seqId++);
      element.setConfigId(-1l);
      elements.add(element);
    }
    return elements;
  }

  /**
   * Parses all {@link Process} and underlying {@link ConfigurationObject}s.
   * All retrieved {@link SequenceTask}s will be added to the task-list as side effect!
   *
   * @param tasks     List which gets filled due the side effect of this method
   * @param processes list of all processes from the ConfigurationObject
   */
  private void parseProcesses(List<SequenceTask> tasks, List<Process> processes) {
    SequenceTask tempSeq;
    for (Process process : processes) {
      tempSeq = sequenceTaskFactory.createSequenceTask(process);
      tempSeq = setControlTags(tempSeq, process);
      tasks.add(tempSeq);
      List<SequenceTask> tempList = parseControlTags(tasks, process, process.getStatusTag(), process.getAliveTag());
      validateControlTags(tempSeq, tempList);
      parseEquipments(tasks, process.getEquipments(), process);
    }
  }


  /**
   * Parses all {@link Equipment} and underlying ConfigurationObjects.
   * All retrieved {@link SequenceTask}s will be added to the task-list as side effect!
   *
   * @param tasks      List which gets filled due the side effect of this method
   * @param equipments list of all equipments of the overlying object
   * @param parent     Overlying Process which holds this Equipment
   */
  private void parseEquipments(List<SequenceTask> tasks, List<Equipment> equipments, Process parent) {
    SequenceTask tempSeq;
    for (Equipment equipment : equipments) {
      tempSeq = sequenceTaskFactory.createSequenceTask(equipment);
      tempSeq = setControlTags(tempSeq, equipment);
      tasks.add(setParentId(tempSeq, parent));

      // parse the attached objects of the equipment:
      List<SequenceTask> tempList = parseControlTags(tasks, equipment, equipment.getStatusTag(), equipment.getCommFaultTag(), equipment.getAliveTag());
      validateControlTags(tempSeq, tempList);
      parseDataTags(tasks, equipment, equipment.getDataTags());
      parseCommandTags(tasks, equipment, equipment.getCommandTags());
      parseSubEquipments(tasks, equipment.getSubEquipments(), equipment);
    }
  }


  /**
   * Parses all {@link SubEquipment} and underlying ConfigurationObjects.
   * All retrieved {@link SequenceTask}s will be added to the task-list as side effect!
   *
   * @param tasks         List which gets filled due the side effect of this method
   * @param subEquipments list of all subEquipments of the overlying object
   * @param parent        Overlying Equipment which holds this Equipment
   */
  private void parseSubEquipments(List<SequenceTask> tasks, List<SubEquipment> subEquipments, Equipment parent) {
    SequenceTask tempSeq;
    for (SubEquipment subEquipment : subEquipments) {
      tempSeq = sequenceTaskFactory.createSequenceTask(subEquipment);
      tempSeq = setControlTags(tempSeq, subEquipment);
      tasks.add(setParentId(tempSeq, parent));
      List<SequenceTask> tempList = parseControlTags(tasks, subEquipment, subEquipment.getStatusTag(), subEquipment.getCommFaultTag(), subEquipment.getAliveTag());
      validateControlTags(tempSeq, tempList);
      parseDataTags(tasks, subEquipment, subEquipment.getDataTags());
    }
  }

  /**
   * Parses all {@link DataTag}s and underlying {@link Alarm}s.
   * All retrieved {@link SequenceTask}s will be added to the task-list as side effect!
   *
   * @param tasks  List which gets filled due the side effect of this method.
   * @param parent Overlying Object which holds this Tag.
   * @param tags   list of all DataTags of the overlying object
   */
  private void parseDataTags(List<SequenceTask> tasks, ConfigurationObject parent, List<DataTag<Number>> tags) {
    SequenceTask tempSeq;

    for (DataTag tag : tags) {
      if (parent instanceof Equipment) {
        tempSeq = sequenceTaskFactory.createSequenceTask(tag);
        tasks.add(setParentId(tempSeq, parent));
      } else {
        throw new ConfigurationParseException("Parsing DataTag " + tag.getId() + " failed. Containing ClassType dosent exist.");
      }
      parseAlarms(tasks, tag.getAlarms(), tag);
    }
  }

  /**
   * Parses all {@link CommandTag}s.
   * CommandTags are only attached to equipments. Because of that there is no need to use the setParentId() method
   * for setting the equipmentId after calling the createSequenceTask() method.
   * All retrieved {@link SequenceTask}s will be added to the task-list as side effect!
   *
   * @param tasks  List which gets filled due the side effect of this method.
   * @param parent Overlying equipment which holds this Tags.
   * @param tags   list of all CommandTags of the overlying equipment
   */
  private void parseCommandTags(List<SequenceTask> tasks, Equipment parent, List<CommandTag> tags) {
    SequenceTask tempSeq;

    for (CommandTag tag : tags) {
      tempSeq = sequenceTaskFactory.createSequenceTask(tag);

      //set the parentId (equipmentId)
      if (tempSeq != null) {
        if (!tempSeq.getConfigurationElement().getElementProperties().isEmpty() && tempSeq.getConfigurationElement().getAction().equals(Action.CREATE))
          tempSeq.getConfigurationElement().getElementProperties().setProperty("equipmentId", String.valueOf(parent.getId()));
      }
      tasks.add(tempSeq);
    }
  }

  private List<SequenceTask> parseControlTags(List<SequenceTask> tasks, ConfigurationObject parent, ControlTag... tags) {
    SequenceTask tempSeq;
    List<SequenceTask> result = new ArrayList<>();

    for (Tag tag : tags) {
      if (tag != null) {
        if (parent instanceof Process || parent instanceof Equipment) {
          tempSeq = sequenceTaskFactory.createSequenceTask(tag);
          if (tempSeq != null) {
            tasks.add(setParentId(tempSeq, parent));
            result.add(tempSeq);
          }
        } else {
          throw new ConfigurationParseException("Parsing ControlTag " + tag.getId() + " failed. Containing ClassType dosent exist.");
        }
        parseAlarms(tasks, tag.getAlarms(), tag);
      }
    }
    return result;
  }

  /**
   * Parses all {@link Alarm}s.
   * All retrieved {@link SequenceTask}s will be added to the task-list as side effect!
   *
   * @param tasks  List which gets filled due the side effect of this method.
   * @param alarms list of all Alarms of the overlying object
   * @param parent Overlying Object which holds this Tag.
   */
  private void parseAlarms(List<SequenceTask> tasks, List<Alarm> alarms, ConfigurationObject parent) {
    SequenceTask tempSeq;
    for (Alarm alarm : alarms) {
      if (parent instanceof Tag) {
        tempSeq = sequenceTaskFactory.createSequenceTask(alarm);
        if (tempSeq != null) {
          if (!tempSeq.getConfigurationElement().getElementProperties().isEmpty() && tempSeq.getConfigurationElement().getAction().equals(Action.CREATE)) {
            tempSeq.getConfigurationElement().getElementProperties().setProperty("dataTagId", String.valueOf(parent.getId()));
          }
        }
        tasks.add(tempSeq);
      } else {
        throw new ConfigurationParseException("Parsing Alarm " + alarm.getId() + " failed. Containing ClassType dosent exist.");
      }
    }
  }

  /**
   * Parses all {@link RuleTag}s and underlying {@link Alarm}s.
   * All retrieved {@link SequenceTask}s will be added to the task-list as side effect!
   *
   * @param tasks List which gets filled due the side effect of this method.
   * @param rules list of all Rules of the overlying {@link Tag}
   */
  private void parseRules(List<SequenceTask> tasks, List<RuleTag> rules) {
    SequenceTask tempSeq;
    for (RuleTag rule : rules) {
      tempSeq = sequenceTaskFactory.createSequenceTask(rule);
      tasks.add(tempSeq);
      parseAlarms(tasks, rule.getAlarms(), rule);
    }
  }

  /**
   * Adds the id of an overlying {@link ConfigurationObject} to the Properties of a {@link SequenceTask} if the task is a CREATE
   * and the containing Object requires an id of the parent.
   *
   * @param task   already parsed task
   * @param parent overlying Object of the task.
   * @return the modified task, which contains the parentId in case that the task was a CREATE
   */
  private SequenceTask setParentId(SequenceTask task, ConfigurationObject parent) {
    if (task != null) {
      if (parent instanceof DataTag) {
        if (!task.getConfigurationElement().getElementProperties().isEmpty() && task.getConfigurationElement().getAction().equals(Action.CREATE))
          task.getConfigurationElement().getElementProperties().setProperty("dataTagId", String.valueOf(parent.getId()));
        return task;
      } else if (parent instanceof CommFaultTag) {
        if (!task.getConfigurationElement().getElementProperties().isEmpty() && task.getConfigurationElement().getAction().equals(Action.CREATE))
          task.getConfigurationElement().getElementProperties().setProperty("commFaultId", String.valueOf(parent.getId()));
        return task;
      } else if (parent instanceof AliveTag) {
        if (!task.getConfigurationElement().getElementProperties().isEmpty() && task.getConfigurationElement().getAction().equals(Action.CREATE))
          task.getConfigurationElement().getElementProperties().setProperty("aliveTagId", String.valueOf(parent.getId()));
        return task;
      } else if (parent instanceof SubEquipment) {
        if (!task.getConfigurationElement().getElementProperties().isEmpty() && task.getConfigurationElement().getAction().equals(Action.CREATE))
          task.getConfigurationElement().getElementProperties().setProperty("subEquipmentId", String.valueOf(parent.getId()));
        return task;
      } else if (parent instanceof Equipment) {
        if (!task.getConfigurationElement().getElementProperties().isEmpty() && task.getConfigurationElement().getAction().equals(Action.CREATE))
          task.getConfigurationElement().getElementProperties().setProperty("equipmentId", String.valueOf(parent.getId()));
        return task;
      } else if (parent instanceof Process) {
        if (!task.getConfigurationElement().getElementProperties().isEmpty() && task.getConfigurationElement().getAction().equals(Action.CREATE))
          task.getConfigurationElement().getElementProperties().setProperty("processId", String.valueOf(parent.getId()));
        return task;
      }
    }
    return task;
  }

  /**
   * Helper method to set control tag ids to process/equipments AFTER parsing the other props of the process Configuration.
   * This the ids will only set if the process task is a CREATE task.
   * If it is a CREATE taksthan all ids should set based of the object hierarchy.
   *
   * @param task   task which properties get extended by the controlTag ids
   * @param object object which holds the tag with die id information
   * @return extended task. If not a CREATE task nothing happens
   */
  private SequenceTask setControlTags(SequenceTask task, ConfigurationObject object) {
    if (task != null && task.getConfigurationElement().getAction().equals(Action.CREATE)) {

      // Set all controlTagIds for processes
      if (object instanceof Process) {
        task.getConfigurationElement().getElementProperties().setProperty("stateTagId", String.valueOf(((Process) object).getStatusTag().getId()));
        if (((Process) object).getAliveTag() != null) {
          task.getConfigurationElement().getElementProperties().setProperty("aliveTagId", String.valueOf(((Process) object).getAliveTag().getId()));
        }

        // Set all controlTagIds for equipments
      } else if (object instanceof Equipment) {
        task.getConfigurationElement().getElementProperties().setProperty("stateTagId", String.valueOf(((Equipment) object).getStatusTag().getId()));
        if (((Equipment) object).getAliveTag() != null) {
          task.getConfigurationElement().getElementProperties().setProperty("aliveTagId", String.valueOf(((Equipment) object).getAliveTag().getId()));
        }
        if (((Equipment) object).getCommFaultTag() != null) {
          task.getConfigurationElement().getElementProperties().setProperty("commFaultTagId", String.valueOf(((Equipment) object).getCommFaultTag().getId()));
        }
      } else
        throw new ConfigurationParseException("Try to set controlTagId, but  " + object.getClass() + " have no field for ControlTagIds.");
    }
    return task;
  }

  private void validateControlTags(SequenceTask parentTask, List<SequenceTask> tasks) {
    if (parentTask == null) {
      for (SequenceTask task : tasks) {
        if (task.getConfigurationElement().getAction().equals(Action.CREATE)) {
          throw new ConfigurationParseException("Not possible to create a ControlTag for Process or Equipment when the parent already exists.");
        }
      }
    } else if (parentTask.getConfigurationElement().getAction().equals(Action.UPDATE)) {
      for (SequenceTask task : tasks) {
        if (task.getConfigurationElement().getAction().equals(Action.CREATE)) {
          throw new ConfigurationParseException("Not possible to create a ControlTag for " + parentTask.getConfigurationElement().getEntity() + ".");
        }
      }
    }

  }
}


