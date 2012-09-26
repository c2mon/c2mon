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
 * <b>COM Error Codes</b><br>
 *  
 * The COM error codes were taken from the Microsoft documentation:
 * <li><a href="http://msdn.microsoft.com/en-us/library/windows/desktop/dd542642%28v=vs.85%29.aspx">
 * COM Error Codes (Generic)</a><br>
 * <li><a href="http://msdn.microsoft.com/en-us/library/windows/desktop/dd319335%28v=vs.85%29.aspx">
 * More general Error Constants</a>
 * <p>
 * Please add here additional error codes, if needed.
 *
 * @author Matthias Braeger
 */
public enum COMErrorConstant {
  CO_E_RUNAS_LOGON_FAILURE(0x8000401AL, "Access is denied. The server process could not be started because the configured identity is incorrect. Check the user name and password."),
  RPC_E_DISCONNECTED(0x80010108L, "The object invoked has disconnected from its clients. Re-initialize your OPC Server Connection."),
  OLE_E_NOCONNECTION(0x80040004L, "There is no connection for this connection ID"),
  REGDB_E_CLASSNOTREG(0x80040154L, "Class not registered. The OPC Server, or a component needed to make the OPC connection is not registered with Windows. This may mean that you simply need to register a DLL or OCX file."),
  REGDB_E_IIDNOTREG(0x80040155L, "Interface not registered. The OPC Server does not support the interface that you are trying to connect to. Examples may include item browsing, asynchronous I/O or OPC DA v2.x or 3.x interfaces etc."),
  CO_E_CLASSSTRING(0x800401F3L, "Invalid class string. The GUID/CLSID of the specified OPC Server is not valid."),
  SELFREG_E_TYPELIB(0x80040200L, "Unable to impersonate DCOM Client OR Unknown OLE status code. DCOM security problem, typically on the client side. This error typically occurs when trying to specify a callback address for asynchronous I/O."),
  CONNECT_E_CANNOTCONNECT(0x80040202L, "Cannot Connect. Error typically occurs when a call is made to advise on the connection point. This often means that the common file OPCProxy.dll is out of sync with other common OPC files."),
  ERROR_FILE_NOT_FOUND(0x80070002L, "The system cannot find the file specified. Re-install your software."),
  ERROR_ACCESS_DENIED(0x80070005L, "General access denied error. You need to configure your DCOM Security settings."),
  E_INVALIDARG(0x80070057L, "The parameter is incorrect. The OPC Server has rejected your request, indicating that the parameter(s) you specified are not valid for the type of request being made. You will need more details on the actual OPC calls being made between the Client and Server."),
  ERROR_SERVICE_REQUEST_TIMEOUT(0x8007041DL, "The service did not respond to the start or control request in a timely fashion. Specific to Windows Services. The service did not start within the allowed time-frame. This indicates an initialization problem with the Windows service."),
  ERROR_TIMEOUT(0x800705B4L, "A timeout occured. The OPC device has stopped responding (hung) or you may need to increase your timeout settings."),
  RPC_S_FP_DIV_ZERO(0x800706EAL, "A floating-point underflow occurred at the RPC server."),
  /** TODO: Find correct constant name for error code. */
  RPC_E_INCOMPATIBLE_STUB(0x80070725L, "Incompatible version of the RPC stub."),
  CO_E_SERVER_EXEC_FAILURE(0x80080005L, "Server execution failed. There is a problem with the OPC Server preventing it from being started by Windows. This may be the result of file-permissions, DCOM Security permissions, or a lack of resources."),
  E_NOINTERFACE(0x80004002L, "No such interface supported. The OPC Server does not support the interface that you are trying to connect to. Examples may include Item Browsing, Asynchronous I/O or OPC DA v2.x or 3.x interfaces etc."),
  E_FAIL(0x80004005L, "Unspecified error. The most common message seen, that yields the least information. In these cases you often need to check the event-logs at your OPC Server for more information."),
  /** TODO: Find correct constant name for error code. */
  RPC_S_UNAVAILABLE(0x800706BAL, "The RPC server is unavailable. The OPC Server could not be contacted. This is usually the result of a firewall blocking the application.");
  
  
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
  private COMErrorConstant(final Long code, final String description) {
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
