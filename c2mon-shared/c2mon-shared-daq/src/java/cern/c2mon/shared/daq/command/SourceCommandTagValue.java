/******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2005-2010 CERN.
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * 
 * Author: TIM team, tim.support@cern.ch
 *****************************************************************************/
package cern.c2mon.shared.daq.command;


import java.io.Serializable;

import javax.jms.Topic;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import cern.c2mon.shared.common.type.TagDataType;


/**
 * The SourceCommandTagValue is a data transfer object. Every time the server
 * asks a DAQ process to execute a command, it creates a SourceCommandTagValue
 * object and sends it to the DAQ process concerned as an XML message 
 * (using the object's toXML method). The DAQ process decodes the XML message
 * and tries to execute the command.
 * 
 * @author Jan Stowisek
 * @version $Revision: 1.13 $ ($Date: 2006/04/21 13:06:30 $ - $State: Exp $)
 */

public class SourceCommandTagValue implements Serializable, Cloneable {
  /**
   * Unique numeric identifier of the command tag.
   */
  protected Long id;

  /**
   * Unique name of the command tag.
   */
  protected String name;

  /**
   * Identifier of the equipment unit to which the command tag is attached.
   */
  protected Long equipmentId;
  
  /**
   * Value of the command tag.
   */
  protected Object value;

  /**
   * Data type of the command tag's value
   */
  protected String dataType;

  /**
   * Numeric representation of the data type of the command tag's value
   */
  protected int dataTypeNumeric; 

  /**
   * Mode of the command tag.
   * @see cern.c2mon.shared.common.TagMode
   */
  protected short mode;

  /**
   * Log4j Logger for this class.
   */
  protected static final Logger log = Logger.getLogger(SourceCommandTagValue.class);

  /**
   * Log4j Logger for logging DataTag values.
   */
  protected static final Logger cmdlog = Logger.getLogger("SourceCommandTagLogger");
  
  
  /**
   * Default constructor
   */
  public SourceCommandTagValue() {/** Nothing to do */}

  /**
   * Constructor
   * @param pId          unique numeric identifier of the command tag
   * @param pName        unique name of the command tag
   * @param pEquipmentId unique identifier of the equipment unit to which the command tag is attached
   * @param pMode        current mode of the command tag
   * @param pValue       current value of the command tag
   * @param pDataType    data type of the command tag's current value
   */
  public SourceCommandTagValue(
      final Long pId, final String pName, final Long pEquipmentId, 
      final short pMode, final Object pValue, final String pDataType) {
    this.id = pId;
    this.name = pName;
    this.equipmentId = pEquipmentId;
    this.mode = pMode;
    this.value = pValue;
    this.dataType = pDataType;
    this.dataTypeNumeric = TagDataType.getDataTypeNumeric(pDataType);
  }

  /**
   * Get the unique numeric identifier of the command tag.
   * @return the unique numeric identifier of the command tag
   */
  public Long getId() {
    return this.id;
  }

  /**
   * Get the unique name of the command tag.
   * @return the unique name of the command tag
   */
  public String getName() {
    return this.name;
  }

  /**
   * Get the id of the equipment unit to which the command is attached.
   */
  public Long getEquipmentId() {
    return this.equipmentId;
  }

  /**
   * Get the command's current mode.
   * @return the mode of the command tag
   * @see cern.c2mon.shared.common.TagMode
   */
  public short getMode() {
    return this.mode;
  }


  /**
   * Get the command's value.
   * @return the value of the command (i.e. the value to be sent to the equipment)
   */
  public Object getValue() {
    return this.value;
  }

  /**
   * Get the data type of the command's value.
   * @return the data type of the command's value. Null if no value is set.
   */
  public String getDataType() {
      return dataType;
//    if (value != null) {
//      return value.getClass().getSimpleName();
//    } else {
//      return null;
//    }
  }

  public int getDataTypeNumeric() {
    return this.dataTypeNumeric;
  }

  public String toXML() {
    // <CommandTag id="..." name="..." equipment-id="...">
    StringBuffer str = new StringBuffer("<CommandTag id=\"");

    str.append(this.id);
    str.append("\" name=\"");
    str.append(this.name);
    str.append("\" equipment-id=\"");
    str.append(this.equipmentId);
    str.append("\">\n");

    // <value dataType="..."> ... </value>
    if (value != null) {
      str.append("  <value type=\"");
      str.append(this.dataType);
      str.append("\">");
      str.append(this.value);
      str.append("</value>\n");
    }
    
    // <mode> ... </mode>
    str.append("  <mode>");
    str.append(this.mode);
    str.append("</mode>\n");
    
    str.append("</CommandTag>\n");
    return str.toString();      
  }

  /**
   * Create a SourceCommandTagValue object from a DOM element.
   * @param domElement DOM element
   * SourceDataTagValue object
   * @return a SourceDataTagValue object.
   */
  public synchronized static SourceCommandTagValue fromXML(Element domElement) {
    SourceCommandTagValue cmd = new SourceCommandTagValue();
    // extract attributes 
    try {
      cmd.id = Long.valueOf(domElement.getAttribute("id"));
      cmd.name = domElement.getAttribute("name");
      cmd.equipmentId = Long.valueOf(domElement.getAttribute("equipment-id"));
    } catch (NumberFormatException nfe) {
      log.error(nfe);
      return null;
    } catch (Exception ex) {
      log.error(ex);
      return null;
    }

    NodeList fields = domElement.getChildNodes();
    String fieldName;
    String fieldValueString;
    Node fieldNode;
    int fieldsCount = fields.getLength();

    for (int i = 0; i < fieldsCount; i++) {
      fieldNode = fields.item(i);
      if (fieldNode.getNodeType() == 1) {
        fieldName = fieldNode.getNodeName();
        fieldValueString = fieldNode.getFirstChild().getNodeValue();

        if (fieldName.equals("value")) {
          cmd.dataType = fieldNode.getAttributes().item(0).getNodeValue();
          cmd.dataTypeNumeric = TagDataType.getDataTypeNumeric(cmd.dataType);
          
          if (cmd.dataType.equals("Integer")) {
            cmd.value = Integer.valueOf(fieldValueString);
          } else if (cmd.dataType.equals("Float")) {
            cmd.value = Float.valueOf(fieldValueString);
          } else if (cmd.dataType.equals("Double")) {
            cmd.value = Double.valueOf(fieldValueString);
          } else if (cmd.dataType.equals("Long")) {
            cmd.value = Long.valueOf(fieldValueString);
          } else if (cmd.dataType.equals("Boolean")) {
            cmd.value = Boolean.valueOf(fieldValueString);
          } else if (cmd.dataType.equals("String")) {
            cmd.value = fieldValueString;
          }
        }

        else if (fieldName.equals("mode")) {
          try {
            cmd.mode = Short.parseShort(fieldValueString);
          } catch (NumberFormatException nfe) {
            log.error("Cannot extract valid mode value from <mode> element.");
            return null;
          }
        }
      }// if      
    }// for
    return cmd;
  }
  
  
  public void log() {
    cmdlog.info(this);
  }    
}
