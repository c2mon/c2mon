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
package cern.c2mon.server.alarm.oscillation;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import cern.c2mon.server.alarm.config.OscillationProperties;
import cern.c2mon.server.cache.AlarmCache;
import cern.c2mon.server.cache.AlarmFacade;
import cern.c2mon.server.common.alarm.Alarm;
import cern.c2mon.server.common.alarm.AlarmCacheObject;
import cern.c2mon.server.common.tag.Tag;

@Component
@Slf4j
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
  public void update(final Alarm alarm, final Tag tag) {
    AlarmCacheObject alarmCacheObject = (AlarmCacheObject) alarm;

    // Evaluate oscillation
    final boolean isCurrentlyActive = alarm.isActive();
    if (isCurrentlyActive != alarmCacheObject.isLastActiveState()) {
      increaseOscillCounter(alarmCacheObject);
    } else {
      resetOscillCounter(alarmCacheObject);
    }
    alarmCacheObject.setLastActiveState(isCurrentlyActive);

  }

  private void increaseOscillCounter(AlarmCacheObject alarmCacheObject) {
    alarmCacheObject.setCounterFault(alarmCacheObject.getCounterFault() + 1);
    if (alarmCacheObject.getCounterFault() == 1) {
      alarmCacheObject.setFirstOscTS(System.currentTimeMillis());
    }
    alarmCacheObject.setOscillating(checkOscillConditions(alarmCacheObject));
  }

  private void resetOscillCounter(AlarmCacheObject alarmCacheObject) {
    alarmCacheObject.setCounterFault(0);
    alarmCacheObject.setOscillating(false);
  }

  private boolean checkOscillConditions(AlarmCacheObject alarmCacheObject) {
    return (alarmCacheObject.getCounterFault() >= oscillationProperties.getOscNumbers()
        && (System.currentTimeMillis() - alarmCacheObject.getFirstOscTS()) <= oscillationProperties.getTimeRange() * 1000);
  }

}
