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
package cern.c2mon.server.configuration.parser.tasks;

import cern.c2mon.server.cache.*;
import cern.c2mon.server.configuration.parser.exception.ConfigurationParseException;
import cern.c2mon.server.configuration.parser.tasks.util.TaskOrder;
import cern.c2mon.shared.client.configuration.ConfigConstants.Action;
import cern.c2mon.shared.client.configuration.ConfigConstants.Entity;
import cern.c2mon.shared.client.configuration.ConfigurationElement;
import cern.c2mon.shared.client.configuration.api.alarm.Alarm;
import cern.c2mon.shared.client.configuration.api.alarm.AlarmCondition;
import cern.c2mon.shared.client.configuration.api.equipment.Equipment;
import cern.c2mon.shared.client.configuration.api.equipment.SubEquipment;
import cern.c2mon.shared.client.configuration.api.process.Process;
import cern.c2mon.shared.client.configuration.api.tag.*;
import cern.c2mon.shared.client.configuration.api.util.ConfigurationObject;
import cern.c2mon.shared.client.configuration.api.util.DefaultValue;
import cern.c2mon.shared.client.configuration.api.util.IgnoreProperty;
import cern.c2mon.shared.client.tag.TagMode;
import cern.c2mon.shared.common.datatag.DataTagAddress;
import cern.c2mon.shared.common.datatag.address.HardwareAddress;
import cern.c2mon.shared.common.metadata.Metadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.util.*;

/**
 * This class is a factory class to create {@link SequenceTask}.
 * For creating a {@link SequenceTask} the information of almost all {@link cern.c2mon.server.cache.C2monCache} are needed,
 * which this class provide.
 * {@link SequenceTask} are build based of the information which a {@link ConfigurationObject} holds.
 *
 * @author Franz Ritter
 */
@Service
public class SequenceTaskFactory {

  private static final Logger log = LoggerFactory.getLogger(SequenceTaskFactory.class);

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

  @Autowired
  public SequenceTaskFactory(ProcessCache processCache, EquipmentCache equipmentCache, SubEquipmentCache subEquipmentCache, ControlTagCache controlTagCache,
                             AliveTimerCache aliveTagCache, CommFaultTagCache commFaultTagCache, DataTagCache dataTagCache, RuleTagCache ruleTagCache,
                             AlarmCache alarmCache, CommandTagCache commandTagCache) {
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
  }

  /**
   * Takes a {@link ConfigurationObject} and the type of the object and creates a {@link SequenceTask}.
   *
   * @param confObject Object which holds the Information to create a {@link SequenceTask}
   * @return SequenceTask based on the {@link ConfigurationObject}
   */
  public SequenceTask createSequenceTask(ConfigurationObject confObject) {
    return buildSequenceTask(confObject, confObject.getClass());
  }

  /**
   * Build a {@link SequenceTask} based on the information of the {@link ConfigurationObject}.
   * The information to build the Task a provided by using reflection on the {@link ConfigurationObject}.
   * Because of that it is not important which instance of  {@link ConfigurationObject} is given.
   *
   * @param confObj Object which holds the Information to create a {@link SequenceTask}
   * @param clazz   class type of the object
   * @param <T>     generic type of the object
   * @return SequenceTask based on the {@link ConfigurationObject}, or null in case that the configuration object
   * only serves as empty container.
   */
  private <T extends ConfigurationObject> SequenceTask buildSequenceTask(ConfigurationObject confObj, Class<T> clazz) {
    ConfigurationElement element = new ConfigurationElement();
    TaskOrder order;
    Properties properties = new Properties();
    //downcast the confObj to the actual type
    T obj = clazz.cast(confObj);

    // set basic information of the ConfigurationElement, based on the type of the confObj
    element.setEntity(getEntity(clazz));
    element.setEntityId(confObj.getId());
    element.setSequenceId(-1l);

    // receive the properties information out of the confObj
    // check if the the ConfigurationObject holds the information to delete a instance
    if (obj.isDeleted()) {
      element.setAction(Action.REMOVE);
      order = getDeleteTaskOrder(obj);
    } else {

      // if the tag already exists try to create a update
      if (cacheHasId(obj.getId(), clazz)) {
        element.setAction(Action.UPDATE);
        order = getUpdateTaskOrder(obj);
        properties = extractPropertiesFromField(obj, clazz);
      } else {

        // look if all fields are given to crate a new instance
        if (obj.requiredFieldsGiven()) {
          element.setAction(Action.CREATE);
          order = getCreateTaskOrder(obj);
          properties = extractPropertiesFromField(obj, obj.getClass());
          setDefaultValues(properties, obj);
        } else {
          throw new ConfigurationParseException("Creating " + clazz.getSimpleName() + " (id = " + obj.getId() + ") failed. Not enough arguments.");
        }
      }
    }

    // put the received properties from the confObj into the ConfigurationElement
    element.setElementProperties(properties);

    // checks if the property have some information.
    // if not the ConfigurationObject served as shell object for a underlying ConfigurationObject
    if (properties.isEmpty() && !element.getAction().equals(Action.REMOVE)) {
      return null;
    } else {
      return buildTaskInstance(element, order, clazz);
    }
  }

  /**
   * Extract all data from the given POJO {@link ConfigurationObject} to a {@link Properties} object.
   * Fields of the class which holds the annotation {@link IgnoreProperty} are ignored.
   * Information in the properties are structured due a HashSet.
   * The Key value of the HashSet is the field name of a field in the ConfigurationObject.
   * The Value is the containing value of the field if the field is not equal to null.
   * It is important that the fields which needs to add to the properties have a getter method in the overlying class.
   *
   * @param object object which holds the Information to fill the properties
   * @param klass  the type of the object. Necessary to do reflection on the given object.
   * @return Properties which holds all relevant information of the {@link ConfigurationObject}
   */
  private <T extends ConfigurationObject> Properties extractPropertiesFromField(ConfigurationObject object, Class<T> klass) {
    Properties properties = new Properties();
    try {
      List<String> ignoreFields = new ArrayList<>();
      ignoreFields.add("class");
      T obj = klass.cast(object);

      // find all annotated fields, which don't belong to the Property
      for (Field field : getSuperFields(klass).values()) {
        if (field.getAnnotation(IgnoreProperty.class) != null)
          ignoreFields.add(field.getName());
      }
      BeanInfo info = Introspector.getBeanInfo(klass);
      PropertyDescriptor[] props = info.getPropertyDescriptors();

      // add all fields without annotation to the Properties
      for (PropertyDescriptor pd : props) {
        if (!ignoreFields.contains(pd.getName())) {
          if (pd.getReadMethod().invoke(obj) != null) {
            String tempProp;

            // check if the property is a TagMode. If so we have to call the ordinal() method manual because the enum toString method don't return the needed
            // number.
            if (pd.getPropertyType().equals(TagMode.class)) {
              tempProp = String.valueOf(((TagMode) pd.getReadMethod().invoke(obj)).ordinal());

            } else if (pd.getPropertyType().equals(DataTagAddress.class)) {
              // check if the property is a DataTagAddress. If so we have to call the toConfigXML() method because the server expect the xml string of a
              // DataTagAddress.
              tempProp = String.valueOf(((DataTagAddress) pd.getReadMethod().invoke(obj)).toConfigXML());

            } else if (pd.getPropertyType().equals(HardwareAddress.class)) {
              // check if the property is a HardWareAddress. If so we have to call the toConfigXML() method because the server expect the xml string of a
              // DataTagAddress.
              tempProp = String.valueOf(((HardwareAddress) pd.getReadMethod().invoke(obj)).toConfigXML());

            } else if (pd.getPropertyType().equals(AlarmCondition.class)) {
              // check if the property is a AlarmCondition. If so we have to call the getXMLCondition() method because the server expect the xml string of an
              // AlarmCondition.
              tempProp = String.valueOf(((AlarmCondition) pd.getReadMethod().invoke(obj)).getXMLCondition());

            } else if (pd.getPropertyType().equals(Metadata.class)) {
              tempProp = String.valueOf(Metadata.toJSON((Metadata) pd.getReadMethod().invoke(obj)));

            } else {
              // default call of all properties. Returns the standard toStringValue of the given Type
              tempProp = pd.getReadMethod().invoke(obj).toString();
            }

            properties.setProperty(pd.getName(), tempProp);
          }
        }
      }
    } catch (Exception e) {
      log.error("Error extracting properties", e);
    }
    return properties;
  }

  private Map<String, Field> getSuperFields(Class<?> klass) {
    Map<String, Field> result = new HashMap<>();
    if (klass == null) {
      return result;
    } else {
      result.putAll(getSuperFields(klass.getSuperclass()));
      for (Field field : klass.getDeclaredFields()) {
        result.put(field.getName(), field);
      }

      return result;
    }
  }

  /**
   * Because {@link ConfigurationObject}s are created with lombok the POJOs have no default values.
   * To provide default values to the fields of the ConfigurationObject a annotation is used.
   * This method extract the default values of all Fields in a ConfigurationObject and
   * put them into the properties.
   * <p/>
   * Because {@link Properties} is a collection additional infromation based on the default value
   * are added to the properties from the argument.
   * The return value is the same object than the method  properties argument.
   *
   * @param properties Collection which holds already parsed data from the {@link ConfigurationObject}.
   * @param object     The object which needs to check for default fields.
   * @return properties added with the default values of the given instance of {@link ConfigurationObject}.
   */
  private Properties setDefaultValues(Properties properties, ConfigurationObject object) {
    try {
      for (Field field : getSuperFields(object.getClass()).values()) {
        if (field.getAnnotation(DefaultValue.class) != null && !properties.containsKey(field.getName())) {

          // extract all default values from fields which Type is no enum
          if (field.getType().getEnumConstants() == null) {
            properties.setProperty(field.getName(), field.getType().getDeclaredConstructor(String.class).newInstance(field.getAnnotation(DefaultValue.class)
                .value()).toString());

            // receive all default values from fields which type is an enum
          } else {
            for (Object x : field.getType().getEnumConstants()) {
              if (x.toString().equals(field.getAnnotation(DefaultValue.class).value())) {
                if (field.getType().equals(TagMode.class)) {
                  properties.setProperty(field.getName(), String.valueOf(((TagMode) x).ordinal()));
                } else {
                  properties.setProperty(field.getName(), field.getType().cast(x).toString());
                }
              }
            }
          }
        }
      }
    } catch (Exception e) {
      log.error("Error setting default values", e);
    }
    return properties;
  }

  /**
   * Checks the corresponding cache of a given ConfigurationObject class if a instance with the id already exists.
   *
   * @param id    id which is checked in the cache if its already there.
   * @param klass class type of a {@link ConfigurationObject}
   * @return boolean value if the id exists in the cache
   */
  private <T extends ConfigurationObject> boolean cacheHasId(long id, Class<T> klass) {
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
   * Retreives a {@link Entity} object based on the class type of a a {@link ConfigurationObject}
   *
   * @param klass type of the class
   * @param <T>   generic type
   * @return {@link Entity} instance based on the class type
   */
  private <T extends ConfigurationObject> Entity getEntity(Class<T> klass) {
    if (klass.equals(Process.class)) {
      return Entity.PROCESS;
    }
    if (klass.equals(Equipment.class)) {
      return Entity.EQUIPMENT;
    }
    if (klass.equals(SubEquipment.class)) {
      return Entity.SUBEQUIPMENT;
    }
    if (klass.equals(CommFaultTag.class) || klass.equals(AliveTag.class) || klass.equals(StatusTag.class)) {
      return Entity.CONTROLTAG;
    }
    if (klass.equals(DataTag.class)) {
      return Entity.DATATAG;
    }
    if (klass.equals(RuleTag.class)) {
      return Entity.RULETAG;
    }
    if (klass.equals(Alarm.class)) {
      return Entity.ALARM;
    }
    if (klass.equals(CommandTag.class)) {
      return Entity.COMMANDTAG;
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
  private <T extends ConfigurationObject> SequenceTask buildTaskInstance(ConfigurationElement element, TaskOrder order, Class<T> klass) {
    if (!(klass.equals(Alarm.class) || klass.equals(SubEquipment.class) || klass.equals(Equipment.class) || klass.equals(Process.class)
        || klass.equals(AliveTag.class) || klass.equals(CommFaultTag.class) || klass.equals(StatusTag.class) || klass.equals(RuleTag.class)
        || klass.equals(DataTag.class) || klass.equals(CommandTag.class))) {

      throw new IllegalArgumentException("Not possible to create a SequenceTask object with the ConfigurationElement " + klass + ".");
    }

    return new SequenceTask(element, order);
  }


  /**
   * get the Order of a {@link ConfigurationObject} related to deleting
   *
   * @param object {@link ConfigurationObject} which determine the order to the {@link SequenceTask}
   * @return Order which will be added to a instance of {@link SequenceTask}
   */
  private TaskOrder getDeleteTaskOrder(ConfigurationObject object) {
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
   * get the Order of a {@link ConfigurationObject} related to updating
   *
   * @param object {@link ConfigurationObject} which determine the order to the {@link SequenceTask}
   * @return Order which will be added to a instance of {@link SequenceTask}
   */
  private TaskOrder getUpdateTaskOrder(ConfigurationObject object) {
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
   * get the Order of a {@link ConfigurationObject} related to creating
   *
   * @param object {@link ConfigurationObject} which determine the order to the {@link SequenceTask}
   * @return Order which will be added to a instance of {@link SequenceTask}
   */
  private TaskOrder getCreateTaskOrder(ConfigurationObject object) {
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

}
