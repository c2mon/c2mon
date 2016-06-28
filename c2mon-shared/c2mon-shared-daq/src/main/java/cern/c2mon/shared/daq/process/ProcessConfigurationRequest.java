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
  private final static Logger LOGGER = LoggerFactory.getLogger(ProcessConfigurationRequest.class);
  
  
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
