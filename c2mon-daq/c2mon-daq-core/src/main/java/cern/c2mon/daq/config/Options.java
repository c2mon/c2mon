/*******************************************************************************
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
 ******************************************************************************/
package cern.c2mon.daq.config;

/**
 * @author Justin Lewis Salmon
 */
public class Options {
  /**
   * Required option at startup to specify the DAQ process name.
   */
  public static final String C2MON_DAQ_NAME                = "c2mon.daq.name";
  /**
   * Set this system property to specify a file that overwrites the default configuration
   */
  public static final String C2MON_DAQ_PROPERTIES_LOCATION = "c2mon.daq.properties.location";
  /**
   * If set, the DAQ will load its configuration from the specified file rather than from the server.
   */
  public static final String LOCAL_CONFIG_FILE             = "c2mon.daq.config.local.file";
  /**
   * If set, the DAQ will save the received server configuration to the specified file location.
   */
  public static final String REMOTE_CONFIG_FILE            = "c2mon.daq.config.remote.file";
  /**
   * Possible values are
   * :<p/>
   * single = Default option to connect to a single broker cluster to send data to C2MON server layer<br/>
   * double = Allows defining a second (passive) broker cluster, which will receive in parallel all updates.<br/>
   *                Example usage: feed a test C2MON server with operational data <br/>
   * test = Allows to run the DAQ with a local configuration file without C2MON server
   */
  public static final String JMS_MODE                      = "c2mon.daq.jms.mode";
  /**
   * maximum capacity of the filter SynchroBuffer (FIFO thereafter: values added after this point will prompt the buffer to remove the oldest values). <br/>
   * Default value is 10000
   */
  public static final String FILTER_BUFFER_CAPACITY        = "c2mon.daq.filter.bufferCapacity";
  /**
   * Configuration setting for the dynamic time deadband to filter out oscillating values, if set to <code>true</code>
   */
  public static final String DYNAMIC_TIME_DEADBAND_ENABLED = "c2mon.daq.deadband.dynamic.enabled";
  /**
   * time in milliseconds which the DAQ waits for a server response
   */
  public static final String SERVER_REQUEST_TIMEOUT        = "c2mon.daq.serverRequestTimeout";
}
