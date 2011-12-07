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
package cern.c2mon.client.jms.impl;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;

import cern.c2mon.client.common.admin.AdminMessage;
import cern.c2mon.client.common.admin.AdminMessageImpl;
import cern.c2mon.client.jms.AdminMessageListener;

/**
 * Wrapper JMS listener to register to the administrator messages topic. This
 * class then notifies all registered listeners.<br/>
 * <br/>
 * Is thread-safe: methods are synchronized to prevent concurrent calls to add,
 * remove and onMessage (which use the collection).
 * 
 * @author vdeila
 * 
 */
class AdminMessageListenerWrapper extends AbstractListenerWrapper<AdminMessageListener, AdminMessage> {

  @Override
  protected AdminMessage convertMessage(Message message) throws JMSException {
    return AdminMessageImpl.fromJson(((TextMessage) message).getText());
  }

  @Override
  protected void invokeListener(final AdminMessageListener listener, final AdminMessage event) {
    listener.onAdminMessageUpdate(event);
  }

}
