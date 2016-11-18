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
package cern.c2mon.shared.client.request;


/**
 * This interface is used to indicate whether the <code>ClientRequest</code>
 * was executed successfully or not in the server side. Therefore it 
 * supports the following types:
 * <li> <b>REQUEST_EXECUTED_SUCCESSFULLY</b>: In case the request
 * was executed in the server without any problems.
 * <li> <b>REQUEST_FAILED</b>: In case the request failed.
 *
 * @author ekoufaki
 */
public interface ClientRequestErrorReport {

  /**
   * Enumeration for specifying the RequestExecutionStatus:
   *
   * @author ekoufaki
   */
  enum RequestExecutionStatus {
    /** 
     * In case the request was executed in
     * the server without any problems.
     */
    REQUEST_EXECUTED_SUCCESSFULLY,
    /** 
     * In case the request failed.
     */
    REQUEST_FAILED
  };
  
  /**
   * Returns a description of the Error that occured in the server,
   * while executing the ClientRequest.
   * 
   * @return Error description.
   */
  String getErrorMessage();

  /**
   * This method returns the Request Execution Status
   * @return Request Execution Status
   * @see RequestExecutionStatus
   */
  RequestExecutionStatus getRequestExecutionStatus();
}
