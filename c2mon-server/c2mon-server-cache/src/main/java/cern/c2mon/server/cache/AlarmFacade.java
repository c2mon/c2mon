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
package cern.c2mon.server.cache;

import cern.c2mon.server.cache.alarm.AlarmAggregatorListener;
import cern.c2mon.server.cache.common.ConfigurableCacheFacade;
import cern.c2mon.server.common.alarm.Alarm;
import cern.c2mon.server.common.alarm.AlarmCacheObject;
import cern.c2mon.server.common.tag.Tag;

/**
 * The AlarmFacade is the bean that should be used to access
 * and modify Alarm objects residing in the cache. Making direct 
 * modifications to the Alarm object should be avoided (correct 
 * Terracotta synchronisation is necessary). 
 * 
 * <p>The methods are thread-safe and can be used to modify
 * objects living in the server cache.
 * 
 * <p>When the alarm object is changed, methods in this class
 * are responsible for notifying the cache listeners (beware
 * of making many small changes which will result in many
 * notifications! to make changes atomic to the listener, it
 * may be necessary to implement new methods wrapping calls
 * to private methods with no listener notification).
 * 
 * @author Mark Brightwell
 *
 */
public interface AlarmFacade extends ConfigurableCacheFacade<Alarm> {

  /**
   * Updates the Alarm in the cache using the passed Tag value.
   * Listeners will only be notified of an update if the Alarm state
   * or the Alarm info changes.
   * 
   * <p>Updates the state, timestamp and additional information of the 
   * Alarm if necessary. Three scenarios are possible:
   * <UL>
   * <LI>State update: if the new tag value has an impact on the alarm's
   * state (change ACTIVE -> TERMINATE or vice versa), the alarm's state 
   * and timestamp are updated. Listeners are notified.
   * </LI>  
   * <LI>Properties update: if the new tag value doesn't change the 
   * alarm's state but changes the additional information related to 
   * alarm, only the info field will be updated. Listeners are notified.
   * </LI>
   * <LI>No update: if the parameters neither change the alarm's state 
   * nor its additional information. No listeners are notified.
   * </LI>
   * </UL>
   * 
   * <p>Never returns null.
   * 
   * @param alarmId the id of the alarm to evaluate
   * @param tag the tag on which the alarm is defined
   * @return Reference of the current Alarm object (whether it changed or not);
   *          intended for publication with the tag itself; never returns null
   * @throws NullPointerException if called with null alarm or tag object
   */
  Alarm update(Long alarmId, Tag tag);

  /**
   * Evaluates this alarm. Is only updated in cache if the alarm status has changed.
   * @param alarmId id of the alarm
   */
  void evaluateAlarm(Long alarmId);

  /**
   * Notifies {@link AlarmCache} listeners and {@link AlarmAggregatorListener} about the removal
   * @param removedAlarm A copy of the alarm that got removed
   */
  void notifyOnAlarmRemoval(AlarmCacheObject removedAlarm);
  
  /**
   * Notifies {@link AlarmAggregatorListener} about the reset of an alarm oscillation on the
   * given tag. We assume that at this stage the required alarm cache update took already place. 
   * @param tag The Tag on which the alarm oscillation flag was reset
   */
  void notifyOnAlarmOscillationReset(final Tag tag);
}
