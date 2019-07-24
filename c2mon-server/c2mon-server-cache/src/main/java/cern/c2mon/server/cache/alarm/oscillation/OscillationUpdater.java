/******************************************************************************
 * Copyright (C) 2010-2019 CERN. All rights not expressly granted are reserved.
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
package cern.c2mon.server.cache.alarm.oscillation;

import java.sql.Timestamp;
import java.util.LinkedList;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import cern.c2mon.server.cache.AlarmCache;
import cern.c2mon.server.cache.alarm.config.OscillationProperties;
import cern.c2mon.server.common.alarm.AlarmCacheObject;

/**
 * Contains the logic for evaluating, if an alarm is oscillating
 *
 * @author Emiliano Piselli, Matthias Braeger
 */
@Component
@Data
@Slf4j
public final class OscillationUpdater {

    @Autowired
    AlarmCache alarmCache;

    @Autowired
    OscillationProperties oscillationProperties;

    /**
     * Check, if the alarm shall still keep its oscillation flag. This is the case, if the last alarm update was longer ago than the defined threshold.
     * @see OscillationProperties#getTimeOscillationAlive()
     * @param alarmCacheObject The current alarm object of the cache
     * @return true, if the alarm shall still keep its oscillation flag
     */
    public boolean checkOscillAlive(AlarmCacheObject alarmCacheObject) {
        long systemTime = System.currentTimeMillis();
        long alarmTs = alarmCacheObject.getTimestamp().getTime();
        if (log.isTraceEnabled()) {
            log.trace(" -> OscillationUpdater.checkOscillAlive()  Alarm #{} diff: {} systime : {} alarmts : {}",
                    alarmCacheObject.getId(), (systemTime - alarmTs), new Timestamp(systemTime),
                    alarmCacheObject.getTimestamp().toString());
        }
        return (systemTime - alarmTs) < (oscillationProperties.getTimeOscillationAlive() * 1000);
    }

    /**
     * Increases the oscillation counter and evaluates if the alarm is oscillating or not.
     * @param alarmCacheObject an updated alarm cache object with the new state
     */
    public void updateOscillationStatus(AlarmCacheObject alarmCacheObject) {

      updateOscTimestampList(alarmCacheObject);

      if (!alarmCacheObject.isOscillating() && isAlarmOscillating(alarmCacheObject)) {
        log.debug("Setting oscillation flag == true for alarm #{}", alarmCacheObject.getId());
      }
      alarmCacheObject.setOscillating(isAlarmOscillating(alarmCacheObject));
    }

    /**
     * Adds the current source timestamp to the oscillation FIFO list for comparison and makes sure that the list
     * of n previous source timestamps is never bigger than the configured OSC numbers.
     *
     * @param alarmCacheObject the alarm to treat
     */
    private void updateOscTimestampList(AlarmCacheObject alarmCacheObject) {
      if (alarmCacheObject.getFifoSourceTimestamps() == null) {
        alarmCacheObject.setFifoSourceTimestamps(new LinkedList<>());
      }

      while (alarmCacheObject.getFifoSourceTimestamps().size() > oscillationProperties.getOscNumbers()) {
        alarmCacheObject.getFifoSourceTimestamps().removeFirst();
      }
      alarmCacheObject.getFifoSourceTimestamps().add(alarmCacheObject.getSourceTimestamp().getTime());
    }

    /**
     * Check whether we have accumulated enough oscillations over the prescribed
     * period of time to declare we are actually oscillating.
     *
     * @param alarmCacheObject
     *            the alarm cache object containing already the new alarm state.
     * @return true, if alarm shall be marked as oscillating.
     */
    private boolean isAlarmOscillating(AlarmCacheObject alarmCacheObject) {
        return ((alarmCacheObject.getFifoSourceTimestamps().size() > oscillationProperties.getOscNumbers())
                && isInOscillationTimeTriggerRange(alarmCacheObject));
    }

    private boolean isInOscillationTimeTriggerRange(AlarmCacheObject alarmCacheObject) {
        if (log.isTraceEnabled()) {
          log.trace("isInOscillationTimeRange?: {} <= {}", alarmCacheObject.getSourceTimestamp().getTime() - alarmCacheObject.getFifoSourceTimestamps().getFirst(), oscillationProperties.getTimeRange() * 1000);
        }
        return (alarmCacheObject.getSourceTimestamp().getTime() - alarmCacheObject.getFifoSourceTimestamps().getFirst())
            <= (oscillationProperties.getTimeRange() * 1000);
    }
}
