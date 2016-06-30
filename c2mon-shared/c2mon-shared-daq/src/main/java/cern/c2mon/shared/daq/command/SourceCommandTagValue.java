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
package cern.c2mon.shared.daq.command;


import java.io.Serializable;

import cern.c2mon.shared.daq.messaging.ServerRequest;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


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
@Data
@Slf4j
public class SourceCommandTagValue implements ServerRequest, Serializable, Cloneable {
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
   * Mode of the command tag.
   * @see cern.c2mon.shared.common.TagMode
   */
  protected short mode;

  /**
   * Log4j Logger for logging DataTag values.
   */
  protected static final Logger cmdlog = LoggerFactory.getLogger("SourceCommandTagLogger");


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
  }

  public void log() {
    cmdlog.info(this.toString());
  }

  @Override
  public String toString() {
    return "COMMAND" + '\t' + this.getId() + '\t' + this.getName() + '\t' + this.getDataType() + '\t' + this.getValue();
  }
}
