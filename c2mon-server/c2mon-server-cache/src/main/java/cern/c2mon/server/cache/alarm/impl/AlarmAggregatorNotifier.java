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
package cern.c2mon.server.cache.alarm.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

import cern.c2mon.server.cache.alarm.AlarmAggregatorRegistration;
import cern.c2mon.server.cache.alarm.AlarmAggregatorListener;
import cern.c2mon.server.common.alarm.Alarm;
import cern.c2mon.server.common.tag.Tag;

/**
 * This class implements the the logic of the {@link AlarmAggregatorRegistration} 
 * interface and notifies about tag and alarm changes. 
 * <p/>
 * It is also used internally by the {@link AlarmAggregatorImpl} to inform 
 * listeners about alarm changes related to supervision events.
 * 
 * @author Matthias Braeger
 */
@Slf4j
@Component
public class AlarmAggregatorNotifier implements AlarmAggregatorRegistration {

  private final List<AlarmAggregatorListener> listeners = new ArrayList<>();

  public void registerForTagUpdates(final AlarmAggregatorListener aggregatorListener) {
    listeners.add(aggregatorListener);
  }
  
  /**
   * Notify the listeners of a tag update with associated alarms.
   * 
   * @param tag
   *          the Tag that has been updated
   * @param alarms
   *          the associated list of evaluated alarms
   */
  public void notifyOnUpdate(final Tag tag, final List<Alarm> alarms) {
    for (AlarmAggregatorListener listener : listeners) {
      try {
        listener.notifyOnUpdate((Tag) tag.clone(), alarms);
      } catch (CloneNotSupportedException e) {
        log.error("Unexpected exception caught: clone should be implemented for this class! Alarm & tag listener was not notified: {}", listener.getClass().getSimpleName(), e);
      }
    }
  }
  
  public void notifyOnSupervisionChange(final Tag tag, final List<Alarm> alarms) {
    for (AlarmAggregatorListener listener : listeners) {
      try {
        listener.notifyOnSupervisionChange((Tag) tag.clone(), alarms);
      } catch (CloneNotSupportedException e) {
        log.error("Unexpected exception caught: clone should be implemented for this class! Alarm & tag listener was not notified: {}", listener.getClass().getSimpleName(), e);
      }
    }
  }
}