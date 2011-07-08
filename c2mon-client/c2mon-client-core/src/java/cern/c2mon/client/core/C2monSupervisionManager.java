/*******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2004 - 2011 CERN. This program is free software; you can
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
 ******************************************************************************/
package cern.c2mon.client.core;

import cern.c2mon.client.core.listener.HeartbeatListener;
import cern.c2mon.client.jms.ConnectionListener;

/**
 * This interface describes the methods which are provided by
 * the C2MON supervision manager singleton. The supervision
 * manager allows registering listeners to get informed about
 * the connection state to the JMS brokers and the heartbeat
 * of the C2MON server.
 *
 * @author Matthias Braeger
 */
public interface C2monSupervisionManager {
  /**
   * Registers a heartbeat listener in order to receive event notifications from
   * the heartbeat manager.
   * 
   * @param pListener the listerner instance to register at the
   *        <code>HeartbeatManager</code>
   */
  void addHeartbeatListener(final HeartbeatListener pListener);

  /**
   * Removes a heartbeat listener from the heartbeat manager.
   * 
   * @param pListener the listerner instance to remove from the
   *        <code>HeartbeatManager</code>
   */
  void removeHeartbeatListener(final HeartbeatListener pListener);
  
  /**
   * Registers the given connection listener at the <code>JmsProxy</code>
   * instance. <code>ConnectionListener</code> instances are notified about the
   * event of a disconnection from the JMS broker as well as when the connection
   * is reestablished.
   * 
   * @param pListener the listener instance to register
   */
  void addConnectionListener(final ConnectionListener pListener);

  /**
   * Removes the given connection listener from the <code>JmsProxy</code>
   * instance.
   * 
   * @param pListener the listener instance to removed
   */
  void removeConnectionListener(final ConnectionListener pListener);
}
