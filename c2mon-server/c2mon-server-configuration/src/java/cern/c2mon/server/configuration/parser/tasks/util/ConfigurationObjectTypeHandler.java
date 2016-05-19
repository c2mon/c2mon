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
package cern.c2mon.server.configuration.parser.tasks.util;

import cern.c2mon.server.cache.*;
import cern.c2mon.server.cache.loading.EquipmentDAO;
import cern.c2mon.server.cache.loading.ProcessDAO;
import cern.c2mon.server.cache.loading.SequenceDAO;
import cern.c2mon.server.cache.loading.SubEquipmentDAO;
import cern.c2mon.server.configuration.dao.ConfigurationDAO;
import cern.c2mon.server.configuration.parser.exception.ConfigurationParseException;
import cern.c2mon.server.configuration.parser.tasks.SequenceTask;
import cern.c2mon.shared.client.configuration.ConfigConstants;
import cern.c2mon.shared.client.configuration.ConfigurationElement;
import cern.c2mon.shared.client.configuration.api.alarm.Alarm;
import cern.c2mon.shared.client.configuration.api.equipment.AbstractEquipment;
import cern.c2mon.shared.client.configuration.api.equipment.Equipment;
import cern.c2mon.shared.client.configuration.api.equipment.SubEquipment;
import cern.c2mon.shared.client.configuration.api.process.Process;
import cern.c2mon.shared.client.configuration.api.tag.*;
import cern.c2mon.shared.client.configuration.api.tag.CommandTag;
import cern.c2mon.shared.client.configuration.api.tag.AliveTag;
import cern.c2mon.shared.client.configuration.api.tag.CommFaultTag;
import cern.c2mon.shared.client.configuration.api.tag.ControlTag;
import cern.c2mon.shared.client.configuration.api.tag.StatusTag;
import cern.c2mon.shared.client.configuration.api.tag.RuleTag;
import cern.c2mon.shared.client.configuration.api.util.ConfigurationObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Franz Ritter
 */
@Service
public class ConfigurationObjectTypeHandler {

  private ProcessCache processCache;
  private EquipmentCache equipmentCache;
  private SubEquipmentCache subEquipmentCache;
  private ControlTagCache controlTagCache;
  private AliveTimerCache aliveTagCache;
  private CommFaultTagCache commFaultTagCache;
  private DataTagCache dataTagCache;
  private RuleTagCache ruleTagCache;
  private AlarmCache alarmCache;
  private CommandTagCache commandTagCache;
  private SequenceDAO sequenceDAO;
  private ProcessDAO processDAO;
  private EquipmentDAO equipmentDAO;
  private SubEquipmentDAO subEquipmentDAO;

  @Autowired
  public ConfigurationObjectTypeHandler(ProcessCache processCache, EquipmentCache equipmentCache, SubEquipmentCache subEquipmentCache, ControlTagCache controlTagCache,
                                        AliveTimerCache aliveTagCache, CommFaultTagCache commFaultTagCache, DataTagCache dataTagCache, RuleTagCache ruleTagCache,
                                        AlarmCache alarmCache, CommandTagCache commandTagCache, SequenceDAO sequenceDAO, EquipmentDAO equipmentDAO,
                                        ProcessDAO processDAO, SubEquipmentDAO subEquipmentDAO) {
    this.processCache = processCache;
    this.equipmentCache = equipmentCache;
    this.subEquipmentCache = subEquipmentCache;
    this.aliveTagCache = aliveTagCache;
    this.commFaultTagCache = commFaultTagCache;
    this.controlTagCache = controlTagCache;
    this.dataTagCache = dataTagCache;
    this.ruleTagCache = ruleTagCache;
    this.alarmCache = alarmCache;
    this.commandTagCache = commandTagCache;
    this.sequenceDAO = sequenceDAO;
    this.equipmentDAO = equipmentDAO;
    this.subEquipmentDAO = subEquipmentDAO;
    this.processDAO = processDAO;
  }

  /**
   * Checks the corresponding cache of a given ConfigurationObject class if a instance with the id already exists.
   *
   * @param id    id which is checked in the cache if its already there.
   * @param klass class type of a {@link ConfigurationObject}
   * @return boolean value if the id exists in the cache
   */
  public <T extends ConfigurationObject> boolean cacheHasId(long id, Class<T> klass) {
    if (klass.equals(Process.class)) {
      return processCache.hasKey(id);
    }
    if (klass.equals(Equipment.class)) {
      return equipmentCache.hasKey(id);
    }
    if (klass.equals(SubEquipment.class)) {
      return subEquipmentCache.hasKey(id);
    }
    if (klass.equals(AliveTag.class)) {
      return aliveTagCache.hasKey(id);
    }
    if (klass.equals(CommFaultTag.class)) {
      return commFaultTagCache.hasKey(id);
    }
    if (klass.equals(StatusTag.class)) {
      return controlTagCache.hasKey(id);
    }
    if (klass.equals(DataTag.class)) {
      return dataTagCache.hasKey(id);
    }
    if (klass.equals(RuleTag.class)) {
      return ruleTagCache.hasKey(id);
    }
    if (klass.equals(Alarm.class)) {
      return alarmCache.hasKey(id);
    }
    if (klass.equals(CommandTag.class)) {
      return commandTagCache.hasKey(id);
    }

    throw new IllegalArgumentException("No Cache for the Instances of class " + klass + " given");
  }

  /**
   * This method should only be used if no id is set on the configuration object.
   * The call of this method should not happen in the context of a create action.
   * This is because not set ids in the create gets auto generated.
   *
   * @param updateObject The configuration object which holds the name for the update.
   * @param klass The type of the configuration
   * @return The configuration object with the id set.
   */
  public <T extends ConfigurationObject> ConfigurationObject setIdForConfigurationObject(ConfigurationObject updateObject, Class<T> klass) {
    Long id;

    if (klass.equals(Process.class)) {

      id = getIdByName(((Process) updateObject).getName(), Process.class);

    } else if (klass.equals(Equipment.class)) {

      id = getIdByName(((Equipment) updateObject).getName(), Equipment.class);

    } else if (klass.equals(SubEquipment.class)) {

      id = getIdByName(((SubEquipment) updateObject).getName(), SubEquipment.class);

    } else if (Tag.class.isAssignableFrom(klass)) {

      id = getIdByName(((Tag) updateObject).getName(), DataTag.class);

    } else {
      throw new IllegalArgumentException("The object of the class " + klass.getName() + " does not hold a name for updating by name.");
    }

    if (id != null){
      updateObject.setId(id);
    }

    return updateObject;
  }

  public<T extends ConfigurationObject> Long getIdByName(String name, Class<T> klass){
    Long id;

    if (klass.equals(Process.class)) {

      id = processDAO.getIdByName(name) ;

    } else if (klass.equals(Equipment.class)) {

      id = equipmentDAO.getIdByName(name);

    } else if (klass.equals(SubEquipment.class)) {

      id = subEquipmentDAO.getIdByName(name);

    } else if (Tag.class.isAssignableFrom(klass)) {

      id = dataTagCache.get(name).getId();

    } else {
      throw new IllegalArgumentException("The name of the object from the class " + klass.getName() + " is unknown.");
    }

    return id;
  }

  /**
   * Retreives a {@link ConfigConstants.Entity} object based on the class type of a a {@link ConfigurationObject}
   *
   * @param klass type of the class
   * @param <T>   generic type
   * @return {@link ConfigConstants.Entity} instance based on the class type
   */
  public <T extends ConfigurationObject> ConfigConstants.Entity getEntity(Class<T> klass) {
    if (klass.equals(Process.class)) {
      return ConfigConstants.Entity.PROCESS;
    }
    if (klass.equals(Equipment.class)) {
      return ConfigConstants.Entity.EQUIPMENT;
    }
    if (klass.equals(SubEquipment.class)) {
      return ConfigConstants.Entity.SUBEQUIPMENT;
    }
    if (klass.equals(CommFaultTag.class) || klass.equals(AliveTag.class) || klass.equals(StatusTag.class)) {
      return ConfigConstants.Entity.CONTROLTAG;
    }
    if (klass.equals(DataTag.class)) {
      return ConfigConstants.Entity.DATATAG;
    }
    if (klass.equals(RuleTag.class)) {
      return ConfigConstants.Entity.RULETAG;
    }
    if (klass.equals(Alarm.class)) {
      return ConfigConstants.Entity.ALARM;
    }
    if (klass.equals(CommandTag.class)) {
      return ConfigConstants.Entity.COMMANDTAG;
    }

    throw new IllegalArgumentException("No Entity for the Instances of class " + klass + " given");
  }

  /**
   * Method to build a SequenceTask.
   *
   * @param element {@link ConfigurationElement} which is wrapped in the instance of the SequenceTask.
   * @param order   Order of the ConfigurationElement.
   * @param klass   Type of a {@link ConfigurationObject} which the defines the {@link ConfigurationElement} in the SequenceTask
   * @param <T>     generic type
   * @return Instance of the SequenceTask
   */
  public <T extends ConfigurationObject> SequenceTask buildTaskInstance(ConfigurationElement element, TaskOrder order, Class<T> klass) {
    if (!(klass.equals(Alarm.class) || klass.equals(SubEquipment.class) || klass.equals(Equipment.class) || klass.equals(Process.class)
        || klass.equals(AliveTag.class) || klass.equals(CommFaultTag.class) || klass.equals(StatusTag.class) || klass.equals(RuleTag.class)
        || klass.equals(DataTag.class) || klass.equals(CommandTag.class))) {

      throw new IllegalArgumentException("Not possible to create a SequenceTask object with the ConfigurationElement " + klass + ".");
    }

    return new SequenceTask(element, order);
  }

  /**
   * Get the order of a {@link ConfigurationObject} related to deleting
   *
   * @param object {@link ConfigurationObject} which determine the order to the {@link SequenceTask}
   * @return Order which will be added to a instance of {@link SequenceTask}
   */
  public TaskOrder getDeleteTaskOrder(ConfigurationObject object) {
    if (object instanceof Process) {
      return TaskOrder.PROCESS_DELETE;
    }
    if (object instanceof SubEquipment) {
      return TaskOrder.SUBEQUIPMENT_DELETE;
    }
    if (object instanceof Equipment) {
      return TaskOrder.EQUIPMENT_DELETE;
    }
    if (object instanceof Alarm) {
      return TaskOrder.ALARM_DELETE;
    }
    if (object instanceof ControlTag) {
      return TaskOrder.CONTROLTAG_DELETE;
    }
    if (object instanceof RuleTag) {
      return TaskOrder.RULETAG_DELETE;
    }
    if (object instanceof DataTag) {
      return TaskOrder.DATATAG_DELETE;
    }
    if (object instanceof CommandTag) {
      return TaskOrder.COMMANDTAG_DELETE;
    }
    return null;
  }

  /**
   * Get the order of a {@link ConfigurationObject} related to updating
   *
   * @param object {@link ConfigurationObject} which determine the order to the {@link SequenceTask}
   * @return Order which will be added to a instance of {@link SequenceTask}
   */
  public TaskOrder getUpdateTaskOrder(ConfigurationObject object) {
    if (object instanceof Process) {
      return TaskOrder.PROCESS_UPDATE;
    }
    if (object instanceof SubEquipment) {
      return TaskOrder.SUBEQUIPMENT_UPDATE;
    }
    if (object instanceof Equipment) {
      return TaskOrder.EQUIPMENT_UPDATE;
    }
    if (object instanceof Alarm) {
      return TaskOrder.ALARM_UPDATE;
    }
    if (object instanceof ControlTag) {
      return TaskOrder.CONTROLTAG_UPDATE;
    }
    if (object instanceof RuleTag) {
      return TaskOrder.RULETAG_UPDATE;
    }
    if (object instanceof DataTag) {
      return TaskOrder.DATATAG_UPDATE;
    }
    if (object instanceof CommandTag) {
      return TaskOrder.COMMANDTAG_UPDATE;
    }
    return null;
  }

  /**
   * Get the order of a {@link ConfigurationObject} related to creating
   *
   * @param object {@link ConfigurationObject} which determine the order to the {@link SequenceTask}
   * @return Order which will be added to a instance of {@link SequenceTask}
   */
  public TaskOrder getCreateTaskOrder(ConfigurationObject object) {
    if (object instanceof Process) {
      return TaskOrder.PROCESS_CREATE;
    }
    if (object instanceof SubEquipment) {
      return TaskOrder.SUBEQUIPMENT_CREATE;
    }
    if (object instanceof Equipment) {
      return TaskOrder.EQUIPMENT_CREATE;
    }
    if (object instanceof Alarm) {
      return TaskOrder.ALARM_CREATE;
    }
    if (object instanceof ControlTag) {
      return TaskOrder.CONTROLTAG_CREATE;
    }
    if (object instanceof RuleTag) {
      return TaskOrder.RULETAG_CREATE;
    }
    if (object instanceof DataTag) {
      return TaskOrder.DATATAG_CREATE;
    }
    if (object instanceof CommandTag) {
      return TaskOrder.COMMANDTAG_CREATE;
    }
    return null;
  }

  /**
   * Generates a default id for the given Object.
   * The id is generated through the backup database.
   *
   * @param object The Configuration object which holds no id.
   * @param <T> The Type of the objects which also the id generation call.
   * @return A new unique id.
   */
  public <T extends ConfigurationObject> Long getAutoGeneratedId(T object) {

    if (object instanceof Process) {

      return sequenceDAO.getNextProcessId();

    } else if (object instanceof AbstractEquipment) {

      return sequenceDAO.getNextEquipmentId();

    } else if (object instanceof Alarm) {

      return sequenceDAO.getNextAlarmId();

    } else if (object instanceof Tag) {

      return sequenceDAO.getNextTagId();
    } else {
      throw new ConfigurationParseException("No id specified for the object " + object.getClass() + ". Auto creation of the id is also not possible");
    }
  }
}
