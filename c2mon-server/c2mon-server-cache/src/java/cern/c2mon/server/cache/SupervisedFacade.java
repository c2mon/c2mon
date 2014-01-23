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

import java.sql.Timestamp;

import cern.c2mon.shared.client.supervision.SupervisionEvent;
import cern.c2mon.server.cache.exception.CacheElementNotFoundException;
import cern.c2mon.server.common.supervision.Supervised;

/**
 * Implemented by Facade beans linked to Supervised cache
 * objects (so Process, Equipment and SubEquipment).
 * 
 * @author Mark Brightwell
 *
 * @param <T> the cache object type
 */
public interface SupervisedFacade<T extends Supervised> {

  /**
   * Returns the last supervision event that occured
   * for this supervised cache object (or a generated 
   * one if none have yet occured for this cache object).
   * 
   * @param id id of the supervised cache object
   * @return the last supervision event
   */
  SupervisionEvent getSupervisionStatus(Long id);

  /**
   * Notifies all registered listeners of the current supervision status
   * of the passed cache element. The timestamp of all events is refreshed
   * to the current time. Is used for example on server startup
   * to "refresh" all listeners in cache of server failure.
   * 
   * @param id id of the cache element
   */
  void refreshAndnotifyCurrentSupervisionStatus(Long id);
  
  /**
   * Sets the status of the Supervised object to STARTUP,
   * with associated message.
   * 
   * <p>Starts the alive timer if not already running.
   * 
   * @param id The cache id of the supervised object
   * @param timestamp time of the start
   */
  void start(Long id, Timestamp timestamp);
  
  /**
   * Sets the status of the Supervised object to DOWN,
   * with stop message. 
   * 
   * <p>Stops the alive timer for this object is running.
   * 
   * @param id The cache id of the supervised object
   * @param timestamp time of the stop
   */
  void stop(Long id, Timestamp timestamp);

  /**
   * Sets the status to running. Used when an alive comes
   * back in after an alive expiration, or when a first
   * alive arrives at Process startup.
   * 
   * @param id The cache id of the supervised object
   * @param timestamp time of the running event
   * @param message details of the event
   */
  void resume(Long id, Timestamp timestamp, String message);

  /**
   * Called when an alive expires or a commfault tag is received.
   * 
   * @param id The cache id of the supervised object
   * @param timestamp time of problem
   * @param message details
   */
  void suspend(Long id, Timestamp timestamp, String message);
  
  /**
   * Returns true if the object is either running or in 
   * the start up phase. And false if either DOWN or STOPPED, or
   * if the status is UNCERTAIN.
   * 
   * @param supervised the cache object to check
   * @return true if it is running (or starting up)
   */
  boolean isRunning(T supervised);
  

  /**
   * Returns true if the object is either running or in the
   * start up phase. And false if either DOWN or STOPPED, or
   * is UNCERTAIN.
   * 
   * @param id of the cache object
   * @return true if running
   */
  boolean isRunning(Long id);
  
  /**
   * Returns true only if the object is in UNCERTAIN status.
   * 
   * @param supervised the object to check
   * @return true if the status is uncertain
   */
  boolean isUncertain(T supervised);

  /**
   * Stops and removes the alive timer object from the cache.
   * No supervised lock should be held when calling this! (lock hierarchy)
   * 
   * @param id of supervised object
   * @throws CacheElementNotFoundException if the supervised object cannot be located in the corresponding cache
   */
  void removeAliveTimer(Long id);

  /**
   * Loads the alive timer into the cache and starts it.
   * Used on reconfiguration of a supervised object.
   * 
   * @param supervisedId id of supervised object
   */
  void loadAndStartAliveTag(Long supervisedId);

  /**
   * Stops and removes this alive by alive id. Should only be
   * used when it is no longer reference by a supervised object
   * (for instance on reconfiguration error recovery).
   * 
   * @param aliveId id of the alive
   */
  void removeAliveDirectly(Long aliveId);
  
  
}
