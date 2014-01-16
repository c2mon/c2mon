/******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2005-2011 CERN.
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
