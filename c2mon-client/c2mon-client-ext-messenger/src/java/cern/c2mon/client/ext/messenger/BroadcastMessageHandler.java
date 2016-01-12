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
package cern.c2mon.client.ext.messenger;

import cern.c2mon.client.common.admin.BroadcastMessage;
import cern.c2mon.client.common.admin.BroadcastMessageDeliveryException;
import cern.c2mon.client.jms.BroadcastMessageListener;

/**
 * This interface describes the methods necessary to listen to
 * {@link BroadcastMessage}, and to publish them
 * 
 * @author vdeila
 */
public interface BroadcastMessageHandler {

  /**
   * Register a listener to be notified of BroadcastMessage events received from the
   * server.
   * 
   * @param broadcastMessageListener
   *          the listener to register
   * @throws NullPointerException
   *           if argument is null
   * @throws IllegalStateException
   *           if
   */
  void registerMessageListener(BroadcastMessageListener broadcastMessageListener);

  /**
   * Unregister the listener from receiving BroadcastMessage updates.
   * 
   * @param broadcastMessageListener
   *          the listener to remove
   * @throws NullPointerException
   *           if argument is null
   */
  void unregisterMessageListener(BroadcastMessageListener broadcastMessageListener);

  /**
   * Sends the broadcast message to the message topic
   * 
   * @param broadcastMessage
   *          the message to send
   * @throws BroadcastMessageDeliveryException
   *           if it fails to deliver the message for any reason
   */
  void publishMessage(BroadcastMessage broadcastMessage) throws BroadcastMessageDeliveryException;

}
