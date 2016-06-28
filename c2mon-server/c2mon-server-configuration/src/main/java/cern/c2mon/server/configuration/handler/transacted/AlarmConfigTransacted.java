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
package cern.c2mon.server.configuration.handler.transacted;

import java.util.Properties;

import cern.c2mon.shared.client.configuration.ConfigurationElement;
import cern.c2mon.shared.client.configuration.ConfigurationElementReport;

/**
 * Internal class with transacted methods.
 *  
 * @author Mark Brightwell
 *
 */
public interface AlarmConfigTransacted {

  /**
   * Creates the alarm in a transaction.
   * @param element creation details
   * @throws IllegalAccessException
   */
  void doCreateAlarm(ConfigurationElement element) throws IllegalAccessException;

  /**
   * Updates the alarm in a transaction.
   * @param alarmId id of the alarm
   * @param properties update properties
   */
  void doUpdateAlarm(Long alarmId, Properties properties);

  /**
   * Removes the alarm in a transaction.
   * @param alarmId id of alarm
   * @param alarmReport report on removal
   */
  void doRemoveAlarm(Long alarmId, ConfigurationElementReport alarmReport);

}
