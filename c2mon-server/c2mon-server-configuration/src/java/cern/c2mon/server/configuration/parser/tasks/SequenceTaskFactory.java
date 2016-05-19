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

import cern.c2mon.server.configuration.parser.exception.ConfigurationParseException;
import cern.c2mon.server.configuration.parser.tasks.util.ConfigurationObjectTypeHandler;
import cern.c2mon.server.configuration.parser.tasks.util.TaskOrder;
import cern.c2mon.shared.client.configuration.ConfigConstants.Action;
import cern.c2mon.shared.client.configuration.ConfigurationElement;
import cern.c2mon.shared.client.configuration.api.alarm.AlarmCondition;
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

  private ConfigurationObjectTypeHandler typeChecker;


  @Autowired
  public SequenceTaskFactory(ConfigurationObjectTypeHandler configurationObjectTypeHandler) {
    this.typeChecker = configurationObjectTypeHandler;
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
   * Takes a {@link ConfigurationObject} and the type of the object and creates a {@link SequenceTask}.
   * The object must hold the information for a update. Otherwise this method throw an exception which is than reported to the client.
   *
   * @param confObject Object which holds the Information to create a {@link SequenceTask}
   * @return SequenceTask based on the {@link ConfigurationObject}
   */
  public <T extends ConfigurationObject> SequenceTask createUpdateSequenceTask(ConfigurationObject confObject) {
    Class<T> clazz = (Class<T>) confObject.getClass();
    T object = clazz.cast(confObject);

    if (typeChecker.cacheHasId(object.getId(), clazz)) {

      ConfigurationElement element = createSetupConfigurationElement(confObject, clazz);

      return buildUpdateSequenceTask(element, object, clazz);

    } else {
      throw new ConfigurationParseException("Updating of " + clazz.getSimpleName() + " (id = " + object.getId() + ") failed: The object is unknown to the sever.");
    }
  }

  /**
   * Takes a {@link ConfigurationObject} and the type of the object and creates a {@link SequenceTask}.
   * The object must hold the information for a creation. Otherwise this method throw an exception which is than reported to the client.
   *
   * @param confObject Object which holds the Information to create a {@link SequenceTask}
   * @return SequenceTask based on the {@link ConfigurationObject}
   */
  public <T extends ConfigurationObject> SequenceTask createCreateSequenceTask(ConfigurationObject confObject) {
    Class<T> clazz = (Class<T>) confObject.getClass();
    T object = clazz.cast(confObject);

    ConfigurationElement element = createSetupConfigurationElement(confObject, clazz);

    return buildCreateSequenceTask(element, object);
  }

  /**
   * Takes a {@link ConfigurationObject} and the type of the object and creates a {@link SequenceTask}.
   * The object must hold the information for a creation. Otherwise this method throw an exception which is than reported to the client.
   *
   * @param confObject Object which holds the Information to create a {@link SequenceTask}
   * @return SequenceTask based on the {@link ConfigurationObject}
   */
  public <T extends ConfigurationObject> SequenceTask createDeleteSequenceTask(ConfigurationObject confObject) {
    Class<T> clazz = (Class<T>) confObject.getClass();
    T obj = clazz.cast(confObject);

    ConfigurationElement element = createSetupConfigurationElement(confObject, clazz);

    return buildDeleteSequenceTask(element, obj);
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
    SequenceTask result;

    ConfigurationElement element = createSetupConfigurationElement(confObj, clazz);

    // downcast the confObj to the actual type
    T object = clazz.cast(confObj);

    // receive the properties information out of the confObj
    // check if the the ConfigurationObject holds the information to delete a instance
    if (object.isDeleted()) {

      result = buildDeleteSequenceTask(element, object);

    } else {

      // if the tag already exists try to create a update
      if (typeChecker.cacheHasId(object.getId(), clazz)) {

        result = buildUpdateSequenceTask(element, object, clazz);

      } else {

        result = buildCreateSequenceTask(element, object);
      }
    }

    return result;
  }

  //===========================================================================
  // separate parser methods (create, delete, update)
  //===========================================================================

  private <T extends ConfigurationObject> SequenceTask buildCreateSequenceTask(ConfigurationElement element, T object) {

    Properties properties;
    TaskOrder order;
    Long objectId = object.getId();

    // check if the id ist set. If not set it automatically
    if (objectId == null) {
      objectId = typeChecker.getAutoGeneratedId(object);

      object.setId(objectId);
      element.setEntityId(objectId);
    }

    if (typeChecker.cacheHasId(objectId, object.getClass())) {
      throw new IllegalArgumentException("Error while parsing a 'create' Configuration:" +
          " Id " + objectId + " of the class "+object.getClass().getSimpleName()+" already known to the server");
    }

    // parsing of the object parameters
    if (object.requiredFieldsGiven()) {

      element.setAction(Action.CREATE);
      order = typeChecker.getCreateTaskOrder(object);
      properties = extractPropertiesFromField(object, object.getClass());
      setDefaultValues(properties, object);

      element.setElementProperties(properties);

      return typeChecker.buildTaskInstance(element, order, object.getClass());

    } else {
      throw new ConfigurationParseException("Creating " + object.getClass().getSimpleName() + " (id = " + object.getId() + ") failed: Not enough arguments.");
    }
  }

  private <T extends ConfigurationObject> SequenceTask buildUpdateSequenceTask(ConfigurationElement element, T object, final Class<T> clazz) {

    element.setAction(Action.UPDATE);
    TaskOrder order = typeChecker.getUpdateTaskOrder(object);
    Properties properties = extractPropertiesFromField(object, clazz);

    // put the received properties from the confObj into the ConfigurationElement
    element.setElementProperties(properties);

    if (properties.isEmpty()) {
      return null;
    } else {
      return typeChecker.buildTaskInstance(element, order, clazz);
    }
  }

  /**
   * Build a {@link SequenceTask} which holds all information for deleting a object of the server.
   * Only call this method if the @param object isDelete method resolves true.
   *
   * @param element The predefined Configuration Element for the server configuration.
   * @param object  the configuration objects which holds all information for the {@link ConfigurationElement}
   * @param <T>     The type of the ConfigurationObject.
   * @return the Sequence Task for deleting.
   */
  private <T extends ConfigurationObject> SequenceTask buildDeleteSequenceTask(ConfigurationElement element, T object) {

    Properties properties = new Properties();
    TaskOrder order;


    if (typeChecker.cacheHasId(object.getId(), object.getClass())) {

      element.setAction(Action.REMOVE);
      order = typeChecker.getDeleteTaskOrder(object);

      element.setElementProperties(properties);

    } else {
      throw new ConfigurationParseException("Deleting of " + object.getClass().getSimpleName() + " (id = " + object.getId() + ") failed: The object is unknown to the sever.");
    }

    return typeChecker.buildTaskInstance(element, order, object.getClass());
  }

  //===========================================================================
  // parsing helper methods
  //===========================================================================

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

  //===========================================================================
  // other helper methods
  //===========================================================================

  private <T extends ConfigurationObject> ConfigurationElement createSetupConfigurationElement(ConfigurationObject confObj, Class<T> clazz) {
    ConfigurationElement element = new ConfigurationElement();

    // set basic information of the ConfigurationElement, based on the type of the confObj
    element.setEntity(typeChecker.getEntity(clazz));
    element.setEntityId(confObj.getId());
    element.setSequenceId(-1L);

    return element;
  }
}
