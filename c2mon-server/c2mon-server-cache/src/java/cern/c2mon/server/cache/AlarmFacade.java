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
package cern.c2mon.server.cache;

import cern.c2mon.server.cache.common.ConfigurableCacheFacade;
import cern.c2mon.server.common.alarm.Alarm;
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
   * Returns the name of the JMS topic on which the Alarms
   * must be published.
   * @param alarm for which the topic is required
   * @return the topic name
   */
  String getTopicForAlarm(Alarm alarm);
  
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

}
