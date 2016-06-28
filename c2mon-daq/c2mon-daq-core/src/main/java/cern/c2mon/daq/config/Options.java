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
  public static final String C2MON_DAQ_NAME                = "c2mon.daq.name";
  public static final String C2MON_DAQ_PROPERTIES_LOCATION = "c2mon.daq.properties.location";
  public static final String LOCAL_CONFIG_FILE             = "c2mon.daq.config.local.file";
  public static final String REMOTE_CONFIG_FILE            = "c2mon.daq.config.remote.file";
  public static final String JMS_MODE                      = "c2mon.daq.jms.mode";
  public static final String FILTER_ENABLED                = "c2mon.daq.filter.enabled";
  public static final String FILTER_BUFFER_CAPACITY        = "c2mon.daq.filter.bufferCapacity";
  public static final String DYNAMIC_TIME_DEADBAND_ENABLED = "c2mon.daq.deadband.dynamic.enabled";
  public static final String SERVER_REQUEST_TIMEOUT        = "c2mon.daq.serverRequestTimeout";

}
