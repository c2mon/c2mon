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


import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Timestamp;

import org.apache.log4j.Logger;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/**
 * This class is used by:
 *  - the DAQ core to send a Process Identification Key
 * request to C2MON
 *  - the C2MON to get the request from the DAQ core and
 *  work with it 
 * 
 * The class is serialized as XML before being sent and
 * deserialized as Object after being received.
 * 
 * * Simpleframework XML needs always a value
 * 
 * @author vilches
 */
@Root(name = "process-connection-request")
public class ProcessConnectionRequest implements ProcessRequest {
  
  /** Log4j instance */
  private static final Logger LOGGER = Logger.getLogger(ProcessConnectionRequest.class);
  
  /**
   * Constant of NO_PROCESS as default value for process name
   */
  public static final String NO_PROCESS = "NO_PROCESS";
  
  /**
   * Constant of NO_HOSTNAME as default value for process host name
   */
  public static final String NO_HOSTNAME = "NO_HOSTNAME";
  
  /**
   * Unique name of the Process that wishes to connect.
   */
  @Element
  protected String processName = NO_PROCESS;
  
  /**
   * Name of the host on which the process will be running.
   */
  @Element
  protected String processHostname = NO_HOSTNAME;

  /**
   * Time when the process was started.
   */
  @Element
  protected Timestamp processStartupTime = new Timestamp(System.currentTimeMillis());
  
  /**
   * Empty Constructor
   * 
   *  - processName is NO_PROCESS by default
   *  - processID is NO_HOSTNAME by default
   *  - processStartupTime is current time by default
   */
  public ProcessConnectionRequest() {}
  
  /**
   * Constructor
   * 
   * - processStartupTime is current time by default
   * 
   * @param processName name of the Process that wishes to connect
   */
  public ProcessConnectionRequest(final String processName) {
    this.processName = processName;

    try {
      this.processHostname = InetAddress.getLocalHost().getHostName();
    } catch (UnknownHostException e) {
      this.processHostname = "";
    }
  }
  
  /**
   * Constructor
   * @param processName name of the Process that wishes to connect
   * @param processStartupTime time when the Process was started
   */
  public ProcessConnectionRequest(final String processName, final Timestamp processStartupTime) {
    this(processName);
    this.processStartupTime = processStartupTime;
  }
  
  /**
   * Constructor
   * @param processName Name of the Process that wishes to connect
   * @param processHostname Name of the host where the DAQ is
   * @param processStartupTime Time when the Process was started
   */
  public ProcessConnectionRequest(final String processName, final String processHostname, final Timestamp processStartupTime) {
    this.processName = processName;
    this.processStartupTime = processStartupTime;
    this.processHostname = processHostname;
  }

  /**
   * @return The name of the process that wants to connect.
   */
  public final String getProcessName() {
    return this.processName;
  }
  
  /**
   * @return The name of the host on which the DAQ process is running.
   */
  public final String getProcessHostName() {
    return this.processHostname;
  }

  /**
   * @return The start-up time of the DAQ process.
   */
  public final Timestamp getProcessStartupTime() {
    return this.processStartupTime;
  }
  
  /**
   * Sets the Process Name
   * 
   * @param processName The name of the process that wants to connect.
   */
  public void setProcessName(final String processName) {
    this.processName = processName;
  }
  
  /**
   * Sets the Host Name
   * 
   * @param processHostname The name of the host on which the DAQ process is running.
   */
  public void setProcessHostName(final String processHostname) {
    this.processHostname = processHostname;
  }

  /**
   * Sets the Startup Time stamp
   * 
   * @param processStartupTime The start-up time of the DAQ process.
   */
  public void setProcessStartupTime(final Timestamp processStartupTime) {
    this.processStartupTime = processStartupTime;
  }
  
  @Override
  public final String toString() {
    return ("Process Name: " + this.processName + ", StartUp Time: " + this.processStartupTime + 
        ", HostName: " + this.processHostname);
  }

}
