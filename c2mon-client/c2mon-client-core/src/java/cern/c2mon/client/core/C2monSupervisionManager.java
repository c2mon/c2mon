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
package cern.c2mon.client.core;

import cern.c2mon.client.core.listener.HeartbeatListener;
import cern.c2mon.client.jms.ClientHealthListener;
import cern.c2mon.client.jms.ConnectionListener;

/**
 * This interface describes the methods which are provided by
 * the C2MON supervision manager singleton. The supervision
 * manager allows registering listeners to get informed about
 * the connection state to the JMS brokers and the heartbeat
 * of the C2MON server.
 *
 * @deprecated Please use {@link SupervisionService} instead
 * @author Matthias Braeger
 */
@Deprecated
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
   * @return <code>true</code>, if the supervision manager is connected to the
   * server and was able to initialize correctly all <code>SupervisionEvent</code>
   * states.
   */
  boolean isServerConnectionWorking();
  
  /**
   * Register to be notified of detected problems with the processing
   * by the client application of incoming data from the server.
   * 
   * <p>In general, these notifications indicate a serious problem with
   * possible data loss, so the client should take some appropriate
   * action on receiving these callbacks (e.g. notify the user).
   * 
   * @param clientHealthListener the listener to notify
   */
  void addClientHealthListener(ClientHealthListener clientHealthListener);
}
