/******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * <p/>
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
 * <p/>
 * Author: TIM team, tim.support@cern.ch
 *****************************************************************************/
package cern.c2mon.client.ext.messenger;

import cern.c2mon.client.common.admin.BroadcastMessage;
import cern.c2mon.client.common.admin.BroadcastMessageDeliveryException;
import cern.c2mon.client.common.service.SessionService;
import cern.c2mon.client.jms.BroadcastMessageListener;

/**
 * Service providing client broadcast message functionality.
 *
 * @author vdeila
 * @author Franz Ritter
 */
public interface BroadcastMessageService {
  /**
   * Registers a message listener in order to receive broadcast messages.
   *
   * @param listener
   *          the listener instance to register at the
   *          <code>HeartbeatManager</code>
   */
  void addMessageListener(final BroadcastMessageListener listener);

  /**
   * Removes a message listener.
   *
   * @param listener
   *          the listener instance to remove
   */
  void removeMessageListener(final BroadcastMessageListener listener);

  /**
   * Sends a broadcast message to all registered listeners
   *
   * @param userName
   *          the user that sends the message
   * @param type
   *          the type of message
   * @param message
   *          the message to send to all the clients
   * @throws BroadcastMessageDeliveryException
   *           if the given user is not logged in, or the user doesn't have the
   *           access to send messages, or if it fails to deliver the message.
   */
  void sendMessage(final String userName, final BroadcastMessage.BroadcastMessageType type, final String message) throws BroadcastMessageDeliveryException;

  /**
   * Checks if the given user is logged in and is allowed to send messages
   *
   * @param userName
   *          the user name to check
   * @return <code>true</code> if the given user is logged in and allowed to
   *         send a message
   */
  boolean isUserAllowedToSend(final String userName);

  /**
   * Register a SessionService to apply an authentication module to this service.
   *
   * @param sessionService the {@link SessionService} to use
   */
  void registerSessionService(SessionService sessionService);

}
