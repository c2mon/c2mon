/*******************************************************************************
 * Copyright (C) 2010-2020 CERN. All rights not expressly granted are reserved.
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
package cern.c2mon.client.core.service;

import java.util.Collection;
import java.util.Optional;

import javax.jms.JMSException;

import cern.c2mon.client.core.jms.AlarmListener;
import cern.c2mon.shared.client.alarm.AlarmValue;

/**
 * The AlarmService allows subscribing to alarm updates as well as requesting
 * the list of all active alarms from the server.
 *  
 * @author Matthias Braeger
 */
public interface AlarmService {

  /**
   * Registers an <code>AlarmListener</code> to receive updates about alarm changes
   * @param listener The listener that shall be registered
   * @throws JMSException In case of JMS problems during the registration
   */
  void addAlarmListener(AlarmListener listener) throws JMSException;
  
  /**
   * Returns an {@link AlarmValue} object for every valid id on the list.
   * The values are fetched from the server.
   * However, in case of a connection error or an unknown alarm id the corresponding
   * tag might be missing.
   *
   * @param alarmIds A collection of alarm id's
   * @return A collection of all <code>AlarmValue</code> objects
   */
  Collection<AlarmValue> getAlarms(final Collection<Long> alarmIds);
  
  /**
   * Returns the {@link AlarmValue} object for the given alarm id.
   * The value is directly fetched from the server.
   *
   * @param alarmId The alarm id
   * @return The given alarm <code>AlarmValue</code>, if it exists
   */
  Optional<AlarmValue> getAlarm(final Long alarmId);
  
  /**
   * Returns an {@link AlarmValue} object for every active alarm found
   * in the server.
   *
   * @return A collection of all active <code>AlarmValue</code> objects
   */
  Collection<AlarmValue> getAllActiveAlarms();
  
  /**
   * Unregisters the given <code>AlarmListener</code> instance from receiving alarm updates.
   * @param listener The listener that shall be registered
   */
  void removeAlarmListener(AlarmListener listener);
}
