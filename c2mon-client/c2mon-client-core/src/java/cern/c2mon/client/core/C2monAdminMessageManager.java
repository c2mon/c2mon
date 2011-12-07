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
package cern.c2mon.client.core;

import cern.c2mon.client.common.admin.AdminMessage;
import cern.c2mon.client.common.admin.AdminMessageDeliveryException;
import cern.c2mon.client.jms.AdminMessageListener;

/**
 * This interface describes the methods which are provided by the C2MON admin
 * message manager singleton. The admin message manager allows registering
 * listeners to get informed about new administrator messages, and does also
 * have a method for sending admin messages.
 * 
 * @author vdeila
 */
public interface C2monAdminMessageManager {
  /**
   * Registers a admin message listener to the admin message manager in order to
   * receive messages sent by the administrators
   * 
   * @param listener
   *          the listerner instance to register at the
   *          <code>HeartbeatManager</code>
   */
  void addAdminMessageListener(final AdminMessageListener listener);

  /**
   * Removes a admin message listener from the admin message manager.
   * 
   * @param listener
   *          the listerner instance to remove from the
   *          <code>HeartbeatManager</code>
   */
  void removeAdminMessageListener(final AdminMessageListener listener);

  /**
   * Sends an admin message to everyone listening for admin messages
   * 
   * @param userName
   *          the user that sends the message
   * @param type
   *          the type of message
   * @param message
   *          the admin message to send to all the clients
   * @throws AdminMessageDeliveryException
   *           if the given user is not logged in, or the user doesn't have the
   *           access to send admin messages, or if it fails to deliver the
   *           admin message.
   */
  void sendAdminMessage(final String userName, final AdminMessage.AdminMessageType type, final String message) throws AdminMessageDeliveryException;

  /**
   * Checks if the given user is logged in and is allowed to send admin messages
   * 
   * @param userName
   *          the user name to check
   * @return <code>true</code> if the given user is logged in and allowed to
   *         send an admin message
   */
  boolean isUserAllowedToSend(final String userName);

}
