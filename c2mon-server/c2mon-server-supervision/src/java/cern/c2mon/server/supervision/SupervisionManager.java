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
package cern.c2mon.server.supervision;

import cern.c2mon.server.cache.exception.CacheElementNotFoundException;
import cern.c2mon.server.common.alive.AliveTimer;
import cern.c2mon.shared.daq.datatag.SourceDataTagValue;
import cern.c2mon.shared.daq.process.ProcessConfigurationRequest;
import cern.c2mon.shared.daq.process.ProcessConnectionRequest;
import cern.c2mon.shared.daq.process.ProcessDisconnectionRequest;
import cern.c2mon.shared.daq.process.backward.ProcessDisconnectionBC;

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
   * Take the necessary action on the reception of a {@link ProcessDisconnectionRequest}
   * message sent by a DAQ.
   * 
   * <p>Implements the synchronization and exception handling described
   * in the class documentation.
   * 
   * @param ProcessDisconnectionRequest the disconnection message
   */
  void onProcessDisconnection(ProcessDisconnectionRequest processDisconnectionRequest);
  
  /**
   * Takes the necessary steps when a DAQ requests for configuration after connection, 
   * such as starting the alive timers, adjusting the state tag, and recording
   * the start up time. Returns the configuration XML to send to the
   * DAQ.
   * 
   * <p>Synchronizes on the Process object.
   * 
   * <p>This method catches ALL unexpected exceptions and rejects
   * the connection request in these cases.
   * 
   * @param processConfigurationRequest the configuration message
   * @return a reply XML string to send to the DAQ (is never null)
   */
  String onProcessConfiguration(ProcessConfigurationRequest processConfigurationRequest);
  
  /**
   * Takes the necessary steps when a DAQ requests to connect, 
   * such us retrieving the PIK. 
   * Returns the PIK XML to send to the DAQ.
   * 
   * <p>Synchronizes on the Process object.
   * 
   * <p>This method catches ALL unexpected exceptions and rejects
   * the connection request in these cases.
   * 
   * @param processConnectionRequest the PIK message
   * @return a reply XML string to send to the DAQ (is never null)
   */
  String onProcessConnection(ProcessConnectionRequest processConnectionRequest);

  /**
   * Take the required supervision action when an alive timer expires.
   * 
   * <p>Is assumed to be called within a block synchronized on the AliveTimer
   * object. 
   * 
   * <p>Implements the synchronization and exception handling described
   * in the class documentation.
   * 
   * @param pAliveTimer reference to the alive timer that has expired
   */
  void onAliveTimerExpiration(AliveTimer pAliveTimer);

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
  
  /**
   * TODO: Backward compatibility. remove after updating server
   */
  
  /**
   * Take the necessary action on the reception of a {@link ProcessDisconnectionBC}
   * message sent by a DAQ.
   * 
   * <p>Implements the synchronization and exception handling described
   * in the class documentation.
   * 
   * @param processDisconnection the disconnection message
   */
 void old_onProcessDisconnection(ProcessDisconnectionBC processDisconnection);
  
}
