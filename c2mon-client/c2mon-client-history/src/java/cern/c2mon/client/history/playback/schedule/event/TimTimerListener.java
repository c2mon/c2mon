/******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2004 - 2011 CERN This program is free software; you can
 * redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version. This program is distributed
 * in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received
 * a copy of the GNU General Public License along with this program; if not,
 * write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA 02111-1307, USA.
 * 
 * Author: TIM team, tim.support@cern.ch
 *****************************************************************************/
package cern.c2mon.client.history.playback.schedule.event;

/**
 * 
 * Used by {@link TimTimer} to inform about events
 * 
 * @author vdeila
 * 
 */
public interface TimTimerListener {

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
