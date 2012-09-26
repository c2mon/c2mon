/*******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2004 - 2012 CERN. This program is free software; you can
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
package cern.c2mon.driver.opcua.connection.dcom;

/**
 * <b>COM and OPC Error Codes</b><br>
 * 
 * The OPC error codes were taken from the following page:
 * <a href="http://lhcb-online.web.cern.ch/lhcb-online/ecs/opcevaluation/htmlspef/Error.html">
 * Summary of OPC Error Codes</a><br>
 *
 * @author Matthias Braeger
 */
public enum DCOMErrorConstant {
  OPC_E_INVALIDHANDLE(0xC0040001L, "The value of the handle is invalid."),
  OPC_E_BADTYPE(0xC0040004L, "The server cannot convert the data between the requested data type and the canonical data type."),
  OPC_E_PUBLIC(0xC0040005L, "The requested operation cannot be done on a public group."),
  OPC_E_BADRIGHTS(0xC0040006L, "The Items AccessRights do not allow the operation."),
  OPC_E_UNKNOWNITEMID(0xC0040007L, "The item is no longer available in the server address space"),
  OPC_E_INVALIDITEMID(0xC0040008L, "The item definition doesn't conform to the server's syntax."),
  OPC_E_INVALIDFILTER(0xC0040009L, "The filter string was not valid"),
  OPC_E_UNKNOWNPATH(0xC004000AL, "The item's access path is not known to the server."),
  OPC_E_RANGE(0xC004000BL, "The value was out of range."),
  OPC_E_DUPLICATENAME(0xC004000CL, "Duplicate name not allowed."),
  OPC_S_UNSUPPORTEDRATE(0x0004000DL, "The server does not support the requested data rate but will use the closest available rate."),
  OPC_S_CLAMP(0x0004000EL, "A value passed to WRITE was accepted but the output was clamped."),
  OPC_S_INUSE(0x0004000FL, "The operation cannot be completed because the object still has references that exist."),
  OPC_E_INVALIDCONFIGFILE(0xC0040010L, "The server's configuration file is an invalid format."),
  OPC_E_NOTFOUND(0xC0040011L, "The server could not locate the requested object."),
  OPC_E_INVALID_PID(0xC0040203L, "The server does not recognise the passed property ID."),
  OPC_E_DEADBANDNOTSET(0xC0040400L, "The item deadband has not been set for this item."),
  OPC_E_DEADBANDNOTSUPPORTED(0xC0040401L, "The item does not support deadband."),
  OPC_E_NOBUFFERING(0xC0040402L, "The server does not support buffering of data items that are collected at a faster rate than the group update rate."),
  OPC_E_INVALIDCONTINUATIONPOINT(0xC0040403L, "The continuation point is not valid."),
  OPC_S_DATAQUEUEOVERFLOW(0x00040404L, "Data Queue Overflow - Some value transitions were lost."),
  OPC_E_RATENOTSET(0xC0040405L, "Server does not support requested rate."),
  OPC_E_NOTSUPPORTED(0xC0040406L, "The server does not support writing of quality and/or timestamp");
  
  /** COM Error code */
  private Long errCode;
  /** The description of the error code */
  private String description;
  
  
  /**
   * Private Enum constructor
   * 
   * @param code Error code
   * @param description The description of the error code
   */
  private DCOMErrorConstant(final Long code, final String description) {
    this.errCode = code;
    this.description = description;
  }
  
  /**
   * @return The error code of the constant
   */
  public Long getCode() {
    return errCode;
  }
  
  /** 
   * @return The error description.
   */
  public String getDescription() {
    return description;
  }
  
  
  /**
   * Returns the error code as hex-decimal and the description of the error constant in a well formated way.
   * @return code - description
   */
  @Override
  public String toString() {
    return "0x" + Long.toHexString(errCode).toUpperCase() + " - " + description;
  }
}
