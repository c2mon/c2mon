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
package cern.c2mon.client.module.adminmessage.handler;

import cern.c2mon.client.common.admin.AdminMessage;
import cern.c2mon.client.common.admin.AdminMessageDeliveryException;
import cern.c2mon.client.jms.AdminMessageListener;

/**
 * This interface describes the methods necessary to listen to
 * {@link AdminMessage}, and to publish them
 * 
 * @author vdeila
 */
public interface AdminMessageHandler {

  /**
   * Register a listener to be notified of AdminMessage events received from the
   * server.
   * 
   * @param adminMessageListener
   *          the listener to register
   * @throws NullPointerException
   *           if argument is null
   * @throws IllegalStateException
   *           if
   */
  void registerAdminMessageListener(AdminMessageListener adminMessageListener);

  /**
   * Unregister the listener from receiving AdminMessage updates.
   * 
   * @param adminMessageListener
   *          the listener to remove
   * @throws NullPointerException
   *           if argument is null
   */
  void unregisterAdminMessageListener(AdminMessageListener adminMessageListener);

  /**
   * Sends the admin message to the admin message topic
   * 
   * @param adminMessage
   *          the admin message to send
   * @throws AdminMessageDeliveryException
   *           if it fails to deliver the admin message for any reason
   */
  void publishAdminMessage(AdminMessage adminMessage) throws AdminMessageDeliveryException;

}
