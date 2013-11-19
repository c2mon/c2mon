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


import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/**
 * Class for representing ProcessDisconnectionRequest messages.
 * A DAQ process is expected to send a ProcessDisconnectionRequest message to
 * the server before shutting down. This is necessary for the application
 * server to know that a DAQ process was stopped rather than the victim
 * of a violent crash.
 * 
 * The disconnection message uniquely identifies a DAQ process by its name
 * and also carries the startup time as a "security question". 
 * 
 * Simpleframework XML needs always a value
 * 
 * @author vilches
 */

@Root(name = "process-disconnection-request")
public class ProcessDisconnectionRequest implements ProcessRequest {
  /**
   * Constant of the NO_PIK as default value
   */
  public static final Long NO_PIK = -1L;
  
  /**
   * Constant of the NO_ID as default value
   */
  public static final Long NO_ID = -1L;
  
  /**
   * Constant of NO_PROCESS as default value for process name
   */
  public static final String NO_PROCESS = "NO_PROCESS";
  
  /**
   * Identifier of the process to be disconnected.
   */
  @Element
  private Long processId = NO_ID;

  /** 
   * Name of the process to be disconnected. 
   */
  @Element
  private String processName = NO_PROCESS;
  
  /** 
   *  Process Identifier Key (PIK) per DAQ instance
   */
  @Element
  private Long processPIK = NO_PIK;

  /**
   * Exact time when the process was started. 
   * This timestamp is used by the server to verify that the message comes from
   * a process that is actually running.
   */
  @Element
  private long processStartupTime = 0L;

  /**
   * Empty Constructor
   * 
   *  - processName is NO_PROCESS by default
   *  - processID is NO_ID by default
   *  - processPIK is NO_PIK by default
   */
  public ProcessDisconnectionRequest() {}
  
  /**
   * Constructor.
   * @param pId process identified
   * @param pName process name
   * @param processPIK The process PIK
   * @param pStartupTime process startup time. 
   * This startup time MUST MATCH the timestamp that was sent with the
   * ProcessConfigurationRequest message.
   */
  public ProcessDisconnectionRequest(final Long pId, final String pName, final Long processPIK, final long pStartupTime) {
    this.processId = pId;
    this.processName = pName;
    this.processPIK = processPIK;
    this.processStartupTime = pStartupTime;
  }

  /**
   * Constructor.
   * @param pName process name
   * @param processPIK The process PIK
   * @param pStartupTime time when the process was started.
   * This startup time MUST MATCH the timestamp that was sent with the
   * ProcessConfigurationRequest message.
   */
  public ProcessDisconnectionRequest(final String pName, final Long processPIK, final long pStartupTime) {
    this.processName = pName;
    this.processPIK = processPIK;
    this.processStartupTime = pStartupTime;
  }
  
  /**
   * @return The process id
   */
  public final Long getProcessId() {
    return this.processId;
  }

  /**
   * @return The process name
   */
  public final String getProcessName() {
    return this.processName;
  }

  /**
   * @return The process startupTime
   */
  public final long getProcessStartupTime() {
    return this.processStartupTime;
  }
  
  /**
   * @return The process PIK
   */
  public final Long getProcessPIK() {
    return this.processPIK;
  }
  
  @Override
  public final String toString() {
    return ("Process Name: " + this.processName +  ", Process ID: " + this.processId + 
        ", Process PIK: " + this.processPIK + ", StartUp time: " + this.processStartupTime);
  }
}