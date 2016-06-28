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

package cern.c2mon.shared.daq.process;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/**
 * This class is used by:
 *  - the DAQ core to receive a Process Identification Key
 * request from C2MON
 *  - the C2MON to send the response from the DAQ core when
 *  it asks for the PIK
 * 
 * The class is serialized as XML before being sent and
 * deserialized as Object after being received.
 * 
 * Simpleframework XML needs always a value
 * 
 * @author vilches
 */
@Root(name = "process-connection-response")
public class ProcessConnectionResponse implements ProcessResponse {

  /** Log4j instance */
  private static final Logger LOGGER = LoggerFactory.getLogger(ProcessConnectionResponse.class);

  /**
   * Constant of the PIK REJECTED. All number which are zero or below
   * will lead to a stop of the DAQ.
   */
  public static final Long PIK_REJECTED = 0L;
  
  /**
   * Constant of the NO_PIK as default value
   */
  public static final Long NO_PIK = -1L;
  
  /**
   * Constant of NO_PROCESS as default value for process name
   */
  public static final String NO_PROCESS = "NO_PROCESS";

  /**
    The Process Name of the daq process
   */
  @Element
  private String processName = NO_PROCESS;

  /** 
   *  Process Identifier Key (PIK) per DAQ instance
   */
  @Element
  private Long processPIK = NO_PIK;

  /**
   * Empty Constructor
   * 
   *  - processName is NO_PROCESS by default
   *  - processPIK is NO_PIK by default
   * 
   */
  public ProcessConnectionResponse() {}

  /**
   * Constructor
   * 
   * @param processName The name of the process
   * @param processPIK Process Identifier Key (PIK)
   */
  public ProcessConnectionResponse(final String processName, final Long processPIK) {
    this.processName = processName;
    this.processPIK = processPIK;
  }

  /**
   * Sets the process name of the DAQ process.
   * 
   * @param processName The name of the process.
   */
  public final void setProcessName(final String processName) {
    this.processName = processName;
  }

  /**
   * Returns the process name of the DAQ process.
   * 
   * @return The process name.
   */
  public final String getProcessName() {
    return processName;
  }

  /**
   * Sets the PIK of the process.
   * 
   * @param processPIK The process PIK
   */
  public final void setprocessPIK(final Long processPIK) {
    this.processPIK = processPIK;
  }

  /**
   * Returns the process PIK
   * 
   * @return The process PIK
   */
  public final Long getProcessPIK() {
    return this.processPIK;
  }

  @Override
  public final String toString() {
    return ("Process Name: " + this.processName + ", " + "Process PIK: " + this.processPIK);
  }


}
