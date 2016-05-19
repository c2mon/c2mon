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
package cern.c2mon.server.configuration.parser.util;

import cern.c2mon.server.cache.loading.SequenceDAO;
import cern.c2mon.server.configuration.parser.exception.ConfigurationParseException;
import cern.c2mon.server.configuration.parser.tasks.SequenceTask;
import cern.c2mon.shared.client.configuration.ConfigConstants;
import cern.c2mon.shared.client.configuration.api.equipment.AbstractEquipment;
import cern.c2mon.shared.client.configuration.api.equipment.Equipment;
import cern.c2mon.shared.client.configuration.api.equipment.SubEquipment;
import cern.c2mon.shared.client.configuration.api.process.Process;
import cern.c2mon.shared.client.configuration.api.tag.AliveTag;
import cern.c2mon.shared.client.configuration.api.tag.CommFaultTag;
import cern.c2mon.shared.client.configuration.api.tag.DataTag;
import cern.c2mon.shared.client.configuration.api.tag.StatusTag;
import cern.c2mon.shared.client.configuration.api.util.ConfigurationObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

/**
 * Class which holds methods for parsing hierarchic and crud configurations
 *
 * @author Franz Ritter
 */
@Service
public class ConfigurationParserUtil {

  private SequenceDAO sequenceDAO;

  @Autowired
  public ConfigurationParserUtil(SequenceDAO sequenceDAO) {
    this.sequenceDAO = sequenceDAO;
  }

  /**
   * Adds the id of an overlying {@link ConfigurationObject} to the Properties of a {@link SequenceTask} if the task is a CREATE
   * and the containing Object requires an id of the parent.
   *
   * @param task   already parsed task
   * @param parent overlying Object of the task.
   * @return the modified task, which contains the parentId in case that the task was a CREATE
   */
  public SequenceTask setParentId(SequenceTask task, ConfigurationObject parent) {
    if (task != null) {
      if (parent instanceof DataTag) {
        if (!task.getConfigurationElement().getElementProperties().isEmpty() && task.getConfigurationElement().getAction().equals(ConfigConstants.Action.CREATE))
          task.getConfigurationElement().getElementProperties().setProperty("dataTagId", String.valueOf(parent.getId()));
        return task;
      } else if (parent instanceof CommFaultTag) {
        if (!task.getConfigurationElement().getElementProperties().isEmpty() && task.getConfigurationElement().getAction().equals(ConfigConstants.Action.CREATE))
          task.getConfigurationElement().getElementProperties().setProperty("commFaultId", String.valueOf(parent.getId()));
        return task;
      } else if (parent instanceof AliveTag) {
        if (!task.getConfigurationElement().getElementProperties().isEmpty() && task.getConfigurationElement().getAction().equals(ConfigConstants.Action.CREATE))
          task.getConfigurationElement().getElementProperties().setProperty("aliveTagId", String.valueOf(parent.getId()));
        return task;
      } else if (parent instanceof SubEquipment) {
        if (!task.getConfigurationElement().getElementProperties().isEmpty() && task.getConfigurationElement().getAction().equals(ConfigConstants.Action.CREATE))
          task.getConfigurationElement().getElementProperties().setProperty("subEquipmentId", String.valueOf(parent.getId()));
        return task;
      } else if (parent instanceof Equipment) {
        if (!task.getConfigurationElement().getElementProperties().isEmpty() && task.getConfigurationElement().getAction().equals(ConfigConstants.Action.CREATE))
          task.getConfigurationElement().getElementProperties().setProperty("equipmentId", String.valueOf(parent.getId()));
        return task;
      } else if (parent instanceof Process) {
        if (!task.getConfigurationElement().getElementProperties().isEmpty() && task.getConfigurationElement().getAction().equals(ConfigConstants.Action.CREATE))
          task.getConfigurationElement().getElementProperties().setProperty("processId", String.valueOf(parent.getId()));
        return task;
      }
    }
    return task;
  }

  /**
   * Helper method to set control tag ids to process/equipments AFTER parsing the other props of the process/equipments Configuration.
   * If there is no controlTag defined the status mandatory statusTags will be automatically created.
   * Note: The controlTag information will be set directly in the properties and not in the configuration POJOs.
   *
   * @param task   task which properties get extended by the controlTag ids
   * @param object object which holds the tag with die id information
   * @return extended task. If not a CREATE task nothing happens
   */
  public SequenceTask setControlTags(SequenceTask task, ConfigurationObject object) {
    if (task != null && task.getConfigurationElement().getAction().equals(ConfigConstants.Action.CREATE)) {

      // Set all controlTagIds for processes
      if (object instanceof Process) {
        Process processObject = (Process) object;

        processObject = setProcessControlTagsOnDefault(processObject);

        task.getConfigurationElement().getElementProperties().setProperty("stateTagId", String.valueOf(processObject.getStatusTag().getId()));
        task.getConfigurationElement().getElementProperties().setProperty("aliveTagId", String.valueOf(processObject.getAliveTag().getId()));

        // Set all controlTagIds for equipments
      } else if (object instanceof AbstractEquipment) {
        AbstractEquipment equipmentObject = (AbstractEquipment) object;

        equipmentObject = setEquipmentControlTagsOnDefault(equipmentObject);

        task.getConfigurationElement().getElementProperties().setProperty("stateTagId", String.valueOf(equipmentObject.getStatusTag().getId()));
        task.getConfigurationElement().getElementProperties().setProperty("commFaultTagId", String.valueOf(equipmentObject.getCommFaultTag().getId()));

        if (equipmentObject.getAliveTag() != null) {
          task.getConfigurationElement().getElementProperties().setProperty("aliveTagId", String.valueOf(equipmentObject.getAliveTag().getId()));
        }

      } else
        throw new ConfigurationParseException("Try to set controlTagId, but  " + object.getClass() + " have no field for ControlTagIds.");
    }
    return task;
  }

  /**
   * Checks if the Process has a defined {@link AliveTag} or {@link StatusTag}.
   * If not a automatic Status tag will be created and attached to the process configuration.
   *
   * @param process The Process which contains the information of an create.
   * @return The same process from the parameters attached with the status tag information.
   */
  private Process setProcessControlTagsOnDefault(Process process) {

    Long controlTagId;

    if (process.getAliveTag() == null) {
      controlTagId = sequenceDAO.getNextTagId();

      // TODO: adapt naming
      AliveTag aliveTag = AliveTag.builder()
          .id(controlTagId)
          .description("Alive tag for process " + process.getName())
          .name(process.getName() + ":ALIVE")
          .build();

      process.setAliveTag(aliveTag);
    }

    if (process.getStatusTag() == null) {
      controlTagId = sequenceDAO.getNextTagId();

      StatusTag statusTag = StatusTag.builder()
          .id(controlTagId)
          .description("Status tag for process " + process.getName())
          .name(process.getName() + ":STATUS")
          .build();

      process.setStatusTag(statusTag);
    }

    return process;
  }

  /**
   * Checks if the Equipment has a defined {@link CommFaultTag} or {@link StatusTag}.
   * If not a automatic Status tag will be created and attached to the equipment configuration.
   *
   * @param process The Equipment which contains the information of an create.
   * @return The same equipment from the parameters attached with the status tag information.
   */
  private AbstractEquipment setEquipmentControlTagsOnDefault(AbstractEquipment equipment) {

    Long controlTagId;

    if (equipment.getCommFaultTag() == null) {
      controlTagId = sequenceDAO.getNextTagId();

      CommFaultTag commfaultTag = CommFaultTag.builder()
          .id(controlTagId)
          .description("Communication fault tag for equipment " + equipment.getName())
          .name(equipment.getName() + ":COMM_FAULT")
          .build();

      equipment.setCommFaultTag(commfaultTag);
    }

    if (equipment.getStatusTag() == null) {
      controlTagId = sequenceDAO.getNextTagId();

      StatusTag statusTag = StatusTag.builder()
          .id(controlTagId)
          .description("Status tag for equipment " + equipment.getName())
          .name(equipment.getName() + ":STATUS")
          .build();

      equipment.setStatusTag(statusTag);
    }

    return equipment;
  }

  /**
   * Check if the created {@link SequenceTask} are Create actions.
   * Call this method only with controlTags in order to make sure that only these are checked.
   *
   * @param tasks           The full list of all configuration task
   * @param parentTask      the parent of the ControlTag (might be a {@link Process} or an {@link Equipment}.
   * @param controlTagTasks The list of the controlTags which need to be validated.
   * @return The full list of all configuration task.
   */
  public List<SequenceTask> validateControlTags(List<SequenceTask> tasks, SequenceTask parentTask, List<SequenceTask> controlTagTasks) {
    if (parentTask == null) {
      for (SequenceTask controlTagTask : controlTagTasks) {
        if (controlTagTask.getConfigurationElement().getAction().equals(ConfigConstants.Action.CREATE)) {
          throw new ConfigurationParseException("Not possible to create a ControlTag for Process or Equipment when the parent already exists.");
        }
      }
    } else if (parentTask.getConfigurationElement().getAction().equals(ConfigConstants.Action.UPDATE)) {
      for (SequenceTask controlTagTask : controlTagTasks) {
        if (controlTagTask.getConfigurationElement().getAction().equals(ConfigConstants.Action.CREATE)) {
          throw new ConfigurationParseException("Not possible to create a ControlTag for " + parentTask.getConfigurationElement().getEntity() + ".");
        }
      }
    }
    return tasks;
  }

  /**
   * Returns the next Configuration Id from the backup database.
   *
   * @return next configuration Id
   */
  public Long getNextConfigId() {
    return sequenceDAO.getNextConfigId();
  }

  /**
   * Helper methods which checks if a collection is null or empty.
   * Because of Jackson serialization collections can be null.
   *
   * @param collection The field of the collection.
   * @return true if null or empty
   */
  public boolean isEmptyCollection(Collection collection) {
    return collection == null || collection.isEmpty();
  }

}
