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
package cern.c2mon.client.ext.history.playback.schedule.event;

/**
 * 
 * Used by {@link TimerQueue} to inform about events
 * 
 * @author vdeila
 * 
 */
public interface TimerQueueListener {

  /**
   * Is invoked if the timer is behind schedule. This might happen if one
   * TimTimerTask takes too long to invoke
   * 
   * @param byTime
   *          The amount of time that it is behind schedule
   */
  void timerIsBehindSchedule(final long byTime);
  
  /**
   * Is invoked if the timer is back on schedule. (Within the threshold limits)
   */
  void timerIsOnSchedule();
}
