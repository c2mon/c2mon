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
package cern.c2mon.server.supervision;

import cern.c2mon.cache.api.exception.CacheElementNotFoundException;
import cern.c2mon.shared.common.datatag.SourceDataTagValue;

/**
 * Module interface of the bean in charge of the overall supervision
 * of the DAQ layer of the system.
 * 
 * <p>Synchronization takes place on the Process level: this means
 * that any supervision action on any level (Process, Equipment,
 * SubEquipment) is synchronized on the Process cache object.
 * 
 * <p>The public methods will catch and log all exceptions of type 
 * {@link CacheElementNotFoundException}, {@link NullPointerException} and
 * {@link IllegalArgumentException} - these may be thrown by private methods
 * if cache objects are not found, their Ids are set to null or cache methods
 * are called with a null parameter as key.
 * 
 * @author Mark Brightwell
 *
 */
public interface SupervisionManager {

  /**
   * Take the required supervision action when an alive timer expires.
   * 
   * <p>Is assumed to be called within a block synchronized on the AliveTimer
   * object. 
   * 
   * <p>Implements the synchronization and exception handling described
   * in the class documentation.
   * 
   * @param aliveTimerId Id of the alive timer that has expired
   */
  void onAliveTimerExpiration(Long aliveTimerId);

  /**
   * Take the necessary DAQ <b>supervision</b> steps on the reception of a Control tag
   * (this tag is saved in the cache elsewhere). 
   * 
   * <p>Notice currently all ensuing Supervision events and state tag modifications
   * are done using a new server timestamp (i.e. the source timestamp is not used here, 
   * although it still appears in the Tag cache).
   * 
   * <p>Synchronized on the Process level: this prevents the Supervision module
   * from undertaking multiple invalidations occurring at once due to Alive 
   * expirations, renewals or CommFault tags. ControlTag's should only arrive
   * occasionally, so this is not a performance issue.
   * 
   * @param sourceDataTagValue the incoming value of the ControlTag
   */
  void processControlTag(SourceDataTagValue sourceDataTagValue);
}
