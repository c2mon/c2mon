/******************************************************************************
 * Copyright (C) 2010-2020 CERN. All rights not expressly granted are reserved.
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
package cern.c2mon.shared.client.alarm.condition;


import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.apache.commons.lang.ArrayUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import cern.c2mon.shared.common.type.TypeConverter;
import cern.c2mon.shared.util.parser.SimpleXMLParser;


/**
 * Common interface for defining alarm conditions.
 * <p/>
 * AlarmCondition objects provide a simple means for
 * finding out whether the state of an alarm is supposed to be "active" or
 * "terminated" when a new Tag value arrives.
 * <p/>
 * AlarmCondition is Serializable. Make sure to define a serialVersionUID in
 * all subclasses in order to make sure that no serialization problems occur
 * after minor modifications in the classes!
 *
 * @author Jan Stowisek, Matthias Braeger
 */
@JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, include=JsonTypeInfo.As.PROPERTY, property="@class")
public abstract class AlarmCondition implements Serializable {

  /**
   * The active fault state descriptor.
   */
  public static final String ACTIVE = "ACTIVE";

  /**
   * The terminate fault state descriptor.
   */
  public static final String TERMINATE = "TERMINATE";

  /** Serial version UID */
  private static final long serialVersionUID = 963875467077605494L;

  private static final Object LOCK = new Object();

  private static final String EMPTY_STRING = "";

  private static SimpleXMLParser xmlParser;

  /**
   * Returns the appropriate alarm state (ACTIVE or TERMINATE) for the given tag value.
   * @param value Usually the latest tag value for which the alarm state shall be evaluated
   * @return true, if the alarm state for the given value is evaluated to ACTIVE
   */
  public abstract boolean evaluateState(Object value);

  /**
   * Clone method
   * @return a deep clone of this AlarmCondition object.
   */
  @Override
  public abstract Object clone();

  /**
   * This method should be overwritten by every alarm condition Class
   * @return A human readable description of the condition
   */
  public String getDescription() {
    return EMPTY_STRING;
  }

  /**
   * @return a representation of the condition class as XML format
   */
  public final String getXMLCondition() {
    return this.toConfigXML();
  }

  /**
   * @return A standardised XML representation of the AlarmCondition object.
   *
   * @throws RuntimeException if errors occur during encoding to XML
   */
  public final String toConfigXML() {
    // The concrete subclass of AlarmCondition
    Class<?> conditionClass = this.getClass();
    // The declared fields of this subclass
    Field[] fields = getAllFields(conditionClass);

    // Temporary variable for constructing the XML string
    StringBuilder str = new StringBuilder(100);
    // Temporary variable for storing the XML name of a field
    String fieldXMLName = null;
    // Temporary variable for storing the class name of a field's value
    String fieldClassName = null;
    // Temporary variable for storing the value of a field
    Object fieldVal = null;

    /* Open the <AlarmCondition> tag */
    str.append("<AlarmCondition class=\"")
       .append(conditionClass.getName())
       .append("\">\n");

    synchronized(LOCK) {
      for (Field field : fields) {
        if (!Modifier.isFinal(field.getModifiers())) {
          try {
            field.setAccessible(true);
            fieldVal = field.get(this);
            if (fieldVal != null) {
              fieldClassName = fieldVal.getClass().getName();
              fieldXMLName = encodeFieldName(field.getName());

              str.append("  <")
                 .append(fieldXMLName)
                 .append(" type=\"");
              if (fieldClassName.indexOf("java.lang") == -1) {
                str.append(fieldClassName);
              } else {
                str.append(fieldClassName.substring(10));
              }
              str.append("\">")
                 .append(fieldVal)
                 .append("</")
                 .append(fieldXMLName)
                 .append(">\n");
            }
          } catch (IllegalAccessException iae) {
            throw new RuntimeException(iae);
          }
        }
      }
    }

    str.append("</AlarmCondition>\n");
    return str.toString();
  }

  /**
   * Create an AlarmCondition object from its standardized XML representation.
   * @param element DOM element containing the XML representation of an
   * AlarmCondition object, as created by the toConfigXML() method.
   * @return The deserialized object from the XML configuration element.
   */
  public static final AlarmCondition fromConfigXML(Element element) {
    Class<?> alarmConditionClass = null;
    AlarmCondition alarmCondition = null;

    try {
      alarmConditionClass = Class.forName(element.getAttribute("class"));
      alarmCondition = (AlarmCondition) alarmConditionClass.newInstance();
    } catch (ClassNotFoundException | IllegalAccessException | InstantiationException ex) {
      throw new RuntimeException(ex);
    }

    setFields(element, alarmConditionClass, alarmCondition);

    // Return the fully configured HardwareAddress object
    return alarmCondition;
  }

  /**
   * Create an AlarmCondition object from its standardized XML representation.
   *
   * @param pXML the XML to parse as String
   * @throws RuntimeException if errors occur during parsing of XML
   */
  public static final synchronized AlarmCondition fromConfigXML(String pXML) {
    if (xmlParser == null) {
      try {
        xmlParser = new SimpleXMLParser();
      } catch (ParserConfigurationException e) {
        throw new RuntimeException(e);
      }
    }
    return fromConfigXML(xmlParser.parse(pXML).getDocumentElement());
  }

  private static Field[] getAllFields(Class<?> conditionClass) {
    Field[] fields = conditionClass.getDeclaredFields();

    Class<?> superClass = conditionClass.getSuperclass();
    while (superClass != null && superClass != AlarmCondition.class && superClass != Object.class) {
      fields = (Field[]) ArrayUtils.addAll(fields, superClass.getDeclaredFields());
      superClass = superClass.getSuperclass();
    }

    return fields;
  }

  private static Map<String, Field> getAllFieldsAsMap(Class<?> conditionClass) {
    Field[] fields = getAllFields(conditionClass);
    Map<String, Field> map = new HashMap<>(fields.length);

    for (Field field : fields) {
      map.put(field.getName(), field);
    }

    return map;
  }

  private static void setFields(Element element, Class<?> alarmConditionClass, AlarmCondition alarmCondition) {
    NodeList nodeFields = element.getChildNodes();
    Map<String, Field> classFields = getAllFieldsAsMap(alarmConditionClass);
    Node fieldNode = null;
    int fieldsCount = nodeFields.getLength();
    String fieldName;
    String fieldValueString;

    for (int i = 0; i < fieldsCount; i++) {
      fieldNode = nodeFields.item(i);
      if (fieldNode.getNodeType() == Node.ELEMENT_NODE) {
        fieldName = fieldNode.getNodeName();
        fieldValueString = fieldNode.getFirstChild().getNodeValue();
        try {
          Field field = classFields.get(decodeFieldName(fieldName));
          if (field == null) {
            throw new NoSuchFieldException("Field with name " + fieldName + " does not exist in class " + alarmConditionClass);
          }

          String fieldTypeName = fieldNode.getAttributes().getNamedItem("type").getNodeValue();
          field.setAccessible(true);
          field.set(alarmCondition, TypeConverter.cast(fieldValueString, fieldTypeName));
        } catch (Exception ex) {
          throw new RuntimeException(ex);
        }
      }
    }
  }

  /**
   * Decodes a field name from XML notation (e.g. my-field-name) to a valid Java
   * field name (e.g. myFieldName)
   */
  private static final String decodeFieldName(final String pXmlFieldName) {
    // StringBuffer for constructing the resulting field name
    StringBuilder str = new StringBuilder();
    // Number of characters in the XML-encoded field name
    int fieldNameLength = pXmlFieldName.length();

    char currentChar;
    for (int i= 0; i < fieldNameLength; i++) {
      currentChar = pXmlFieldName.charAt(i);
      if (currentChar == '-') {
        str.append(Character.toUpperCase(pXmlFieldName.charAt(++i)));
      } else {
        str.append(currentChar);
      }
    }
    return str.toString();
  }

  /**
   * Encodes a field name in Java notation (e.g. myFieldName) to an XML
   * field name (e.g. my-field-name).
   */
  private final String encodeFieldName(final String pFieldName) {
    // StringBuffer for constructing the resulting XML-encoded field name
    StringBuilder str = new StringBuilder();
    // Number of characters in the field name
    int fieldNameLength = pFieldName.length();

    char currentChar;
    for (int i= 0; i != fieldNameLength; i++) {
      currentChar =  pFieldName.charAt(i);
      if (Character.isUpperCase(currentChar)) {
        str.append('-')
           .append(Character.toLowerCase(currentChar));
      } else {
        str.append(currentChar);
      }
    }
    return str.toString();
  }

  @Override
  public final String toString() {
    return getXMLCondition();
  }
}
