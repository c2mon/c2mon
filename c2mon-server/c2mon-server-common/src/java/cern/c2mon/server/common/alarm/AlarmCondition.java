/*******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2004 - 2010 CERN. This program is free software; you can
 * redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version. This program is distributed
 * in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received
 * a copy of the GNU General Public License along with this program; if not,
 * write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA 02111-1307, USA.
 * 
 * Author: TIM team, tim.support@cern.ch
 ******************************************************************************/
package cern.c2mon.server.common.alarm;


import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import cern.c2mon.shared.util.parser.SimpleXMLParser;


/**
 * <b>Imported as-is into C2MON.</b>
 * 
 * <p>Common interface for defining TIM alarm conditions.
 *
 * AlarmCondition objects are used in the TIM system (by the AlarmCacheObject
 * as well as the Alarm entity bean) in order to provide a simple means for
 * finding out whether the state of an alarm is supposed to be "active" or
 * "terminated" when a new value arrives.
 *
 * AlarmCondition is Serializable. Make sure to define a serialVersionUID in
 * all subclasses in order to make sure that no serialization problems occur
 * after minor modifications in the classes!
 *
 * @author Jan Stowisek
 * @version $Revision: 1.13 $ ($Date: 2007/07/04 12:38:53 $ - $State: Exp $)
 * @see cern.c2mon.server.alarm.AlarmLocal
 * @see cern.c2mon.server.common.alarm.AlarmCacheObject
 * @see cern.laser.source.FaultState
 */

public abstract class AlarmCondition implements Serializable {

  /** Serial version UID */
  private static final long serialVersionUID = 963875467077605494L;

  /** 
   * The active fault state descriptor. Copied from
   * <code>cern.laser.source.alarmsysteminterface.FaultState</code>
   * to avoid dependencies.
   */
  public final static String ACTIVE = "ACTIVE";
  
  /** 
   * The terminate fault state descriptor. Copied from
   * <code>cern.laser.source.alarmsysteminterface.FaultState</code>
   * to avoid dependencies.
   */
  public final static String TERMINATE = "TERMINATE";

  private static SimpleXMLParser xmlParser = null;


  /**
   * Returns the appropriate alarm state (i.e. the fault state descriptor
   * in LASER) for the given tag value. 
   * The only allowed return values are FaultState.TERMINATE or FaultState.ACTIVE.
   */
  public abstract String evaluateState(Object value);

  /**
   * Clone method
   * @return a deep clone of this AlarmCondition object.
   */
  public abstract Object clone();
  

  /** 
   * Returns a standardised XML representation of the AlarmCondition object.
   * 
   * @throws RuntimeException if errors occur during encoding to XML
   */
  public final synchronized String toConfigXML() {
    // The concrete subclass of AlarmCondition
    Class conditionClass = this.getClass();
    // The declared fields of this subclass
    Field[] fields = conditionClass.getDeclaredFields();
    // Temporary variable for constructing the XML string
    StringBuffer str = new StringBuffer();
    // Temporary variable for storing the XML name of a field
    String fieldXMLName = null;
    // Temporary variable for storing the class name of a field's value
    String fieldClassName = null;
    // Temporary variable for storing the value of a field
    Object fieldVal = null;

    /* Open the <AlarmCondition> tag */
    str.append("<AlarmCondition class=\"");
    str.append(conditionClass.getName());
    str.append("\">\n");

    for (int i = 0; i < fields.length; i++) {
      if (Modifier.isProtected(fields[i].getModifiers())
          && !Modifier.isFinal(fields[i].getModifiers())) {
        try {
          fieldVal = fields[i].get(this);
          if (fieldVal != null) {
            fieldClassName = fieldVal.getClass().getName();
            fieldXMLName = encodeFieldName(fields[i].getName());

            str.append("  <");
            str.append(fieldXMLName);
            str.append(" type=\"");
            if (fieldClassName.indexOf("java.lang") == -1) {
              str.append(fieldClassName);
            }
            else {
              str.append(fieldClassName.substring(10));
            }
            str.append("\">");
            str.append(fieldVal);
            str.append("</");
            str.append(fieldXMLName);
            str.append(">\n");
          }
        } catch (IllegalAccessException iae) {
          iae.printStackTrace();
          throw new RuntimeException(iae);
        }
      }
    }

    str.append("</AlarmCondition>\n");
    return str.toString();
  }

  /**
   * Create an AlarmCondition object from its standardized XML representation.
   * @param pElement DOM element containing the XML representation of an 
   * AlarmCondition object, as created by the toConfigXML() method.
   */
  public static final synchronized AlarmCondition fromConfigXML(Element pElement) {
    Class alarmConditionClass = null;
    AlarmCondition alarmCondition = null;

    try {
      alarmConditionClass = Class.forName(pElement.getAttribute("class"));
      alarmCondition = (AlarmCondition) alarmConditionClass.newInstance();
    } catch (ClassNotFoundException cnfe) {
      cnfe.printStackTrace();
      throw new RuntimeException(cnfe);
    } catch (IllegalAccessException iae) {
      iae.printStackTrace();
      throw new RuntimeException(iae);
    } catch (InstantiationException ie) {
      ie.printStackTrace();
      throw new RuntimeException(ie);
    }          
    
    NodeList fields = pElement.getChildNodes();
    Node fieldNode = null;
    int fieldsCount = fields.getLength();
    String fieldName;
    String fieldValueString;

    for (int i = 0; i < fieldsCount; i++) {
      fieldNode = fields.item(i);
      if (fieldNode.getNodeType() == Node.ELEMENT_NODE) {
        fieldName = fieldNode.getNodeName();
        fieldValueString = fieldNode.getFirstChild().getNodeValue();
        try {
          Field field = alarmConditionClass.getDeclaredField(decodeFieldName(fieldName));
          String fieldTypeName = fieldNode.getAttributes().getNamedItem("type").getNodeValue();
          if (fieldTypeName.equals("Integer")) {
            field.set(alarmCondition, Integer.valueOf(fieldValueString));
          } else if (fieldTypeName.equals("Boolean")) {
            field.set(alarmCondition, Boolean.valueOf(fieldValueString));
          } else if (fieldTypeName.equals("Float")) {
            field.set(alarmCondition, Float.valueOf(fieldValueString));
          } else if (fieldTypeName.equals("Double")) {
            field.set(alarmCondition, Double.valueOf(fieldValueString));
          } else if (fieldTypeName.equals("Short")) {
            field.set(alarmCondition, Short.valueOf(fieldValueString));
          } else if (fieldTypeName.equals("short")) {
            field.setShort(alarmCondition, Short.parseShort(fieldValueString));
          } else if (fieldTypeName.equals("int")) {
            field.setInt(alarmCondition, Integer.parseInt(fieldValueString));
          } else if (fieldTypeName.equals("float")) {
            field.setFloat(alarmCondition, Float.parseFloat(fieldValueString));
          } else if (fieldTypeName.equals("double")) {
            field.setDouble(alarmCondition, Double.parseDouble(fieldValueString));
          } else if (fieldTypeName.equals("long")) {
            field.setLong(alarmCondition, Long.parseLong(fieldValueString));
          } else if (fieldTypeName.equals("byte")) {
            field.setByte(alarmCondition, Byte.parseByte(fieldValueString));
          } else if (fieldTypeName.equals("char")) {
            field.setChar(alarmCondition, fieldValueString.charAt(0));
          } else if (fieldTypeName.equals("boolean")) {
            field.setBoolean(alarmCondition, Boolean.getBoolean(fieldValueString));          
          } else {
            field.set(alarmCondition, fieldValueString);
          }
        } catch (NoSuchFieldException nsfe) {
          nsfe.printStackTrace();
          throw new RuntimeException(nsfe);
        } catch (IllegalAccessException iae) {
          iae.printStackTrace();
          throw new RuntimeException(iae);
        }
      }
    }
    // Return the fully configured HardwareAddress object
    return alarmCondition;
  }

  /**
   * Create an AlarmCondition object from its standardized XML representation.
   * 
   * @param pElement DOM element containing the XML representation of an
   * AlarmCondition object, as created by the toConfigXML() method.
   * 
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
  

  //----------------------------------------------------------------------------
  // Private utility methods
  //----------------------------------------------------------------------------

  /**
   * Decodes a field name from XML notation (e.g. my-field-name) to a valid Java
   * field name (e.g. myFieldName)
   */
  private static final String decodeFieldName(final String pXmlFieldName) {
    // StringBuffer for constructing the resulting field name
    StringBuffer str = new StringBuffer();
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
    StringBuffer str = new StringBuffer();
    // Number of characters in the field name
    int fieldNameLength = pFieldName.length();

    char currentChar;    
    for (int i= 0; i != fieldNameLength; i++) {
      currentChar =  pFieldName.charAt(i);
      if (Character.isUpperCase(currentChar)) {
        str.append('-');
        str.append(Character.toLowerCase(currentChar));
      } else {
        str.append(currentChar);
      }
    }
    return str.toString();
  }
  
//  public static void main(String[] args) {
//    try {
//      SimpleXMLParser parser = new SimpleXMLParser();
//      AlarmCondition c = new ValueAlarmCondition(Boolean.FALSE);
//      AlarmCondition c2 = null;
//      System.out.println(c.toConfigXML());
//      c2 = AlarmCondition.fromConfigXML(parser.parse(c.toConfigXML()).getDocumentElement());
//      System.out.println(c2.toConfigXML());
//      c = new RangeAlarmCondition(new Float(3), new Float(5));
//      System.out.println(c.toConfigXML());
//      c2 = AlarmCondition.fromConfigXML(parser.parse(c.toConfigXML()).getDocumentElement());
//      System.out.println(c2.toConfigXML());
//      c = new RangeAlarmCondition(new Float(3), null);
//      System.out.println(c.toConfigXML());
//      c2 = AlarmCondition.fromConfigXML(parser.parse(c.toConfigXML()).getDocumentElement());
//      System.out.println(c2.toConfigXML());
//    }
//    catch (ParserConfigurationException pce) {
//      pce.printStackTrace();
//    }
//    catch (Exception e) {
//      e.printStackTrace();
//    }
//  }
}
