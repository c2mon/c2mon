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
package cern.c2mon.server.configuration.handler;

import java.util.Properties;

import cern.c2mon.shared.client.configuration.ConfigurationElement;
import cern.c2mon.shared.client.configuration.ConfigurationElementReport;

/**
 * Bean managing configuration updates to C2MON Alarms.
 *  
 * @author Mark Brightwell
 *
 */
public interface AlarmConfigHandler {

  /**
   * Creates a new Alarm in the C2MON server.
   * 
   * @param element element with configuration details
   * @throws IllegalAccessException
   */
  void createAlarm(ConfigurationElement element) throws IllegalAccessException;

  /**
   * Updates the Alarm object in the C2MON server.
   * @param alarmId id of Alarm to update
   * @param properties reconfiguration details
   */
  void updateAlarm(Long alarmId, Properties properties);

  /**
   * Removes an Alarm from C2MON server.
   * @param alarmId the Alarm id
   * @param alarmReport the report on this action (is passed as cascading
   *            actions may need to add subreports)
   */
  void removeAlarm(Long alarmId, ConfigurationElementReport alarmReport);

}
