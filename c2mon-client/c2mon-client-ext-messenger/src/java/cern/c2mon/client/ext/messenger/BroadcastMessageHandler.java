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
