/*******************************************************************************
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
 ******************************************************************************/

package cern.c2mon.server.configuration.parser.util;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.util.*;

import cern.c2mon.server.configuration.parser.exception.ConfigurationParseException;
import cern.c2mon.shared.client.alarm.condition.ValueAlarmCondition;
import cern.c2mon.shared.client.configuration.api.util.ConfigurationEntity;
import cern.c2mon.shared.client.configuration.api.util.DefaultValue;
import cern.c2mon.shared.client.configuration.api.util.IgnoreProperty;
import cern.c2mon.shared.client.device.*;
import cern.c2mon.shared.client.metadata.Metadata;
import cern.c2mon.shared.client.tag.TagMode;
import cern.c2mon.shared.common.datatag.DataTagAddress;
import cern.c2mon.shared.common.datatag.address.HardwareAddress;

/**
 * Created by fritter on 31/05/16.
 */
public class ReflectionService {

  /**
   * Extract all data from the given POJO {@link ConfigurationEntity} to a {@link Properties} object.
   * Fields of the class which holds the annotation {@link IgnoreProperty} are ignored.
   * Information in the properties are structured due a HashSet.
   * The Key value of the HashSet is the field name of a field in the ConfigurationObject.
   * The Value is the containing value of the field if the field is not equal to null.
   * It is important that the fields which needs to add to the properties have a getter method in the overlying class.
   *
   * @param object object which holds the Information to fill the properties
   * @param klass  the type of the object. Necessary to do reflection on the given object.
   * @return Properties which holds all relevant information of the {@link ConfigurationEntity}
   */
   public static <T extends ConfigurationEntity> Properties extractPropertiesFromField(ConfigurationEntity object, Class<T> klass) {
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

            } else if (pd.getPropertyType().equals(ValueAlarmCondition.class)) {
              // check if the property is a AlarmCondition. If so we have to call the getXMLCondition() method because the server expect the xml string of an
              // AlarmCondition.
              tempProp = String.valueOf(((ValueAlarmCondition) pd.getReadMethod().invoke(obj)).getXMLCondition());

            } else if (pd.getPropertyType().equals(Metadata.class)) {
              tempProp = String.valueOf(Metadata.toJSON((Metadata) pd.getReadMethod().invoke(obj)));

            } else if (DeviceClassOrDeviceSerializableElement.class.isAssignableFrom(pd.getPropertyType())) {
              // check if the property is or type PropertyList, CommandList, DevicePropertyList or DeviceCommandList. If so,
              // the server expects the xml string as serialized in toConfigXml()
              tempProp = String.valueOf(((DeviceClassOrDeviceSerializableElement) pd.getReadMethod().invoke(obj)).toConfigXml());

            } else {
              // default call of all properties. Returns the standard toStringValue of the given Type
              tempProp = pd.getReadMethod().invoke(obj).toString();
            }

            properties.setProperty(pd.getName(), tempProp);
          }
        }
      }
    } catch (Exception e) {
      throw new ConfigurationParseException("Error extracting values from the configuration " + object + ": ", e);
    }
    return properties;
  }

  private static Map<String, Field> getSuperFields(Class<?> klass) {
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
   * Because {@link ConfigurationEntity}s are created with lombok the POJOs have no default values.
   * To provide default values to the fields of the ConfigurationObject a annotation is used.
   * This method extract the default values of all Fields in a ConfigurationObject and
   * put them into the properties.
   * <p/>
   * Because {@link Properties} is a collection additional infromation based on the default value
   * are added to the properties from the argument.
   * The return value is the same object than the method  properties argument.
   *
   * @param properties Collection which holds already parsed data from the {@link ConfigurationEntity}.
   * @param object     The object which needs to check for default fields.
   * @return properties added with the default values of the given instance of {@link ConfigurationEntity}.
   */
  public static Properties setDefaultValues(Properties properties, ConfigurationEntity object) {
    try {
      for (Field field : getSuperFields(object.getClass()).values()) {
        if (field.getAnnotation(DefaultValue.class) != null && !properties.containsKey(field.getName())) {

          // extract all default values from fields which Type is no enum
          if (field.getType().getEnumConstants() == null) {

            properties.setProperty(field.getName(),
                field.getType().getDeclaredConstructor(String.class).newInstance(field.getAnnotation(DefaultValue.class).value()).toString());

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
      throw new ConfigurationParseException("Error setting default values from the configuration " + object + ": ", e);
    }
    return properties;
  }
}
