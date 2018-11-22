/******************************************************************************
 * Copyright (C) 2010-2018 CERN. All rights not expressly granted are reserved.
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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import cern.c2mon.server.cache.AlarmCache;
import cern.c2mon.server.cache.AlarmFacade;
import cern.c2mon.server.cache.alarm.config.OscillationProperties;
import cern.c2mon.server.common.alarm.AlarmCacheObject;
import cern.c2mon.server.common.tag.Tag;
import lombok.Data;


/**
 * 
 * @author Emiliano Piselli
 *
 */
@Component
@Data
public final class OscillationUpdater {

  @Autowired
  AlarmCache alarmCache;

  @Autowired
  OscillationProperties oscillationProperties;

  /**
   * Logic kept the same as in TIM1 (see {@link AlarmFacade}). The locking of
   * the objets is done in the public class. Notice, in this case the update()
   * method is putting the changes back into the cache.
   */
  public void update(final AlarmCacheObject alarmCacheObject, final Tag tag) {
    alarmCacheObject.setTimestamp(new Timestamp(System.currentTimeMillis()));

    // Evaluate oscillation
    boolean isAlarmConditionActive = alarmCacheObject.getCondition().evaluateState(tag.getValue());
    alarmCacheObject.setActive(isAlarmConditionActive);
    
    if (isAlarmConditionActive != alarmCacheObject.isLastActiveState()) {
      increaseOscillCounter(alarmCacheObject);
    }

    alarmCacheObject.setLastActiveState(isAlarmConditionActive);
  }

  public boolean checkOscillAlive(AlarmCacheObject alarmCacheObject) {
    return ((System.currentTimeMillis() - alarmCacheObject.getTimestamp().getTime()) < (oscillationProperties.getTimeOscillationAlive()*1000));
  }

  private void increaseOscillCounter(AlarmCacheObject alarmCacheObject) {
    alarmCacheObject.setCounterFault(alarmCacheObject.getCounterFault() + 1);
    if (alarmCacheObject.getCounterFault() == 1 || ((alarmCacheObject.getCounterFault() % oscillationProperties.getOscNumbers()) == 0)) {
      alarmCacheObject.setFirstOscTS(System.currentTimeMillis());
    }
    alarmCacheObject.setOscillating(checkOscillConditions(alarmCacheObject));
  }

  public void resetOscillCounter(AlarmCacheObject alarmCacheObject) {

    alarmCacheObject.setCounterFault(0);
    alarmCacheObject.setOscillating(false);
    alarmCacheObject.setFirstOscTS(0);
    alarmCacheObject.setLastActiveState(false);
  }

  /**
   * If the oscillation condition is met we still have to check, if the alarm is currently set to ACTIVE because we only want active
   * alarms being flagged as oscillating. Like that the user will only deal with ACTIVE oscillating alarms.
   * 
   * @param alarmCacheObject the alarm cache object containing already the new alarm state
   * @return true, if alarm shall be marked as oscillating
   */
  private boolean checkOscillConditions(AlarmCacheObject alarmCacheObject) {
    if ((alarmCacheObject.getCounterFault() >= oscillationProperties.getOscNumbers())
        && ((System.currentTimeMillis() - alarmCacheObject.getFirstOscTS()) <= oscillationProperties.getTimeRange() * 1000)) {
      
      return (alarmCacheObject.isOscillating() || (alarmCacheObject.isActive() && !alarmCacheObject.isOscillating()));
    }
    
    return false;
  }
}
