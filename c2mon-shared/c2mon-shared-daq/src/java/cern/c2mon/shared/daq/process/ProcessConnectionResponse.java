/******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2005-2013 CERN.
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

package cern.c2mon.shared.daq.process;

import org.apache.log4j.Logger;
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
  private static final Logger LOGGER = Logger.getLogger(ProcessConnectionResponse.class);

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
