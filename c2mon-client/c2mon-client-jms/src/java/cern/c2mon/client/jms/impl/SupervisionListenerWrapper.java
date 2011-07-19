/******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2005-2011 CERN.
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
package cern.c2mon.client.jms.impl;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;

import cern.c2mon.client.jms.SupervisionListener;
import cern.c2mon.shared.client.supervision.SupervisionEvent;
import cern.c2mon.shared.client.supervision.SupervisionEventImpl;

/**
 * Wrapper JMS listener to register to the supervision topic.
 * This class then notifies all registered listeners.
 * 
 * <p>Is thread-safe: methods are synchronized to prevent concurrent calls
 * to add, remove and onMessage (which use the collection).
 * 
 * @author Mark Brightwell
 *
 */
class SupervisionListenerWrapper extends AbstractListenerWrapper<SupervisionListener, SupervisionEvent>{

  @Override
  protected SupervisionEvent convertMessage(Message message) throws JMSException {
    return SupervisionEventImpl.fromJson(((TextMessage) message).getText());
  }

  @Override
  protected void invokeListener(SupervisionListener listener, SupervisionEvent event) {
    listener.onSupervisionUpdate(event);
  }

}
