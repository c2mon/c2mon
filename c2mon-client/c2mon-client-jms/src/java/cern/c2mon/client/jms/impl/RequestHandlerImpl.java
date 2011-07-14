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

import java.util.Collection;

import javax.jms.JMSException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;

import cern.c2mon.client.jms.JmsProxy;
import cern.c2mon.client.jms.RequestHandler;
import cern.c2mon.shared.client.request.ClientRequestImpl;
import cern.c2mon.shared.client.supervision.SupervisionEvent;
import cern.c2mon.shared.client.tag.TagUpdate;
import cern.c2mon.shared.client.tag.TagValueUpdate;
import cern.tim.shared.client.command.CommandTagHandle;

/**
 * Implementation of the RequestHandler bean.
 * @see cern.c2mon.client.jms.RequestHandler  
 * @author Mark Brightwell
 *
 */
public class RequestHandlerImpl implements RequestHandler {

  /**
   * Ref to JmsProxy bean.
   */
  private JmsProxy jmsProxy;  
  
  /**
   * Name of request queue.
   */
  private String requestQueue;
  
  /**
   * Default request timeout for requests. NullPointerException is
   * thrown if timeout occurs.
   */
  private int requestTimeout;
  
  /**
   * Constructor.
   * @param jmsProxy the proxy bean
   */
  @Autowired
  public RequestHandlerImpl(final JmsProxy jmsProxy) {
    super();
    this.jmsProxy = jmsProxy;
  }

  @Override
  public Collection<CommandTagHandle> getCommandTagHandles(final Collection<Long> commandIds) {
   throw new UnsupportedOperationException("Not implemented yet!");
  }

  @Override
  public Collection<SupervisionEvent> getCurrentSupervisionStatus() throws JMSException {
    ClientRequestImpl<SupervisionEvent> clientRequest = new ClientRequestImpl<SupervisionEvent>(SupervisionEvent.class);    
    return jmsProxy.sendRequest(clientRequest, requestQueue, requestTimeout);
  }

  @Override
  public Collection<TagUpdate> requestTags(final Collection<Long> tagIds) throws JMSException {
    if (tagIds == null) {
      throw new NullPointerException("requestTags(..) method called with null parameter.");
    }
    ClientRequestImpl<TagUpdate> clientRequest = new ClientRequestImpl<TagUpdate>(TagUpdate.class);
    clientRequest.addTagIds(tagIds);
    return jmsProxy.sendRequest(clientRequest, requestQueue, requestTimeout);
  }
  
  @Override
  public Collection<TagValueUpdate> requestTagValues(final Collection<Long> tagIds) throws JMSException {
    if (tagIds == null) {
      throw new NullPointerException("requestTagValues(..) method called with null parameter.");
    }
    ClientRequestImpl<TagValueUpdate> clientRequest = new ClientRequestImpl<TagValueUpdate>(TagValueUpdate.class);
    clientRequest.addTagIds(tagIds);
    return jmsProxy.sendRequest(clientRequest, requestQueue, requestTimeout);
  }

  /**
   * Setter method.
   * @param requestQueue the requestQueue to set
   */
  @Required
  public void setRequestQueue(final String requestQueue) {
    this.requestQueue = requestQueue;
  }

  /**
   * Setter method.
   * @param requestTimeout the requestTimeout to set
   */
  @Required
  public void setRequestTimeout(final int requestTimeout) {
    this.requestTimeout = requestTimeout;
  }

}
