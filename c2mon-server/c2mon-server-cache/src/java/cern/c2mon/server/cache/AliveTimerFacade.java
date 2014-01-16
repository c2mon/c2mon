/******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2005-2010 CERN.
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

import cern.c2mon.server.common.alive.AliveTimer;
import cern.c2mon.server.common.equipment.AbstractEquipment;
import cern.c2mon.server.common.process.Process;

/**
 * Interface to the bean that should be used to update and query
 * {@link AliveTimer} cache objects.
 * 
 * @author Mark Brightwell
 *
 */
public interface AliveTimerFacade {

  /**
   * Is the passed id the id of a known alive timer.
   * @param id the id to check
   * @return true if the alive timer is recognized
   * @throws NullPointerException if called with a null key
   */
  boolean isRegisteredAliveTimer(Long id);
  
  /**
   * Start this alive timer if not already active.
   * 
   * @param id of the alive timer to start
   */
  void start(Long id);

  /**
   * Stops the alive timer if active.
   * 
   * @param id of the alive timer to stop
   */
  void stop(Long id);

  /**
   * Starts all the alive timers in the cache that are not activated.
   */
  void startAllTimers();
  
  /**
   * Stops all the alive timers in the cache that are active.
   */
  void stopAllTimers();
  
  /**
  * Update alive timer with the new timestamp. Will only
  * update the timer if the passed timestamp is more recent
  * than the current one.
  * 
  * <p>Will start the alive timer if it is stopped.
  * 
  * @param aliveId id of the timer to update 
  */
  void update(Long aliveId);

  /**
   * Determine if the AliveTimer has expired. This is
   * the case if no alive message has arrived in
   * the last "alive interval" + "alive tolerance" seconds.
   * Currently allow "alive interval" + 1/3 "alive interval".
   * 
   * @param aliveTimerId the AliveTimer id of the cache object
   * @return true if the AliveTimer has expired
   */
  boolean hasExpired(Long aliveTimerId);

  /**
   * Generates the alive object and inserts it into the cache.
   * @param abstractEquipment for which the alive timer should be created in the cache 
   */
  void generateFromEquipment(AbstractEquipment abstractEquipment);


  /**
   * As above but for process.
   * @param subEquipment
   */
  void generateFromProcess(Process process);
}
