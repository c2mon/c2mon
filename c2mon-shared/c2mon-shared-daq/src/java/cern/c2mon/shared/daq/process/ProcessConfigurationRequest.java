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
 *  - the DAQ core to send a Process Configuration request to C2MON
 *  - the C2MON to get the request from the DAQ core and work with it 
 * 
 * The class is serialized as XML before being sent and
 * deserialized as Object after being received.
 * 
 * Simpleframework XML needs always a value
 * 
 * @author vilches
 */
@Root(name = "process-configuration-request")
public class ProcessConfigurationRequest implements ProcessRequest {
  private final static Logger LOGGER = Logger.getLogger(ProcessConfigurationRequest.class);
  
  
  /**
   * Constant of NO_PROCESS as default value for process name
   */
  public static final String NO_PROCESS = "NO_PROCESS";
  
  /**
   * Constant of the NO_PIK as default value
   */
  public static final Long NO_PIK = -1L;

  /**
   * Unique name of the Process that wishes to connect.
   */
  @Element
  protected String processName = NO_PROCESS;
  
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
   */
  public ProcessConfigurationRequest() {}
  
  /**
   * Constructor
   *    *  - processPIK is NO_PIK by default
   *    
   * @param processName name of the Process that wishes to connect
   */
  public ProcessConfigurationRequest(final String processName) {
    this.processName = processName;
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
   * @return the name of the process that wants to connect.
   */
  public final String getProcessName() {
    return this.processName;
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
    return ("Process Name: " + this.processName);
  }
}
