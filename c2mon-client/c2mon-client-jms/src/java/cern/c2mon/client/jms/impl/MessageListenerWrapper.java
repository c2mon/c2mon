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

import java.util.HashMap;
import java.util.Map;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import org.apache.log4j.Logger;

import cern.c2mon.client.jms.ServerUpdateListener;
import cern.c2mon.shared.client.tag.TransferTagValue;
import cern.c2mon.shared.client.tag.TransferTagValueImpl;

/**
 * Class used locally to wrap Tag update listeners in a JMS message listener.
 * The JmsProxy subscribes a MessageListenerWrapper to receive TransferTagValue
 * objects on a given topic.
 * 
 * <p>Tag update listeners can register with this wrapper to receive updates
 * for a specific Tag (specified by id). The wrapper listens on a the given topic
 * and notifies {@link ServerUpdateListener}s when an update is received for
 * the corresponding Tag. Notice only one ServerUpdateListener will be registered
 * for a given id (the latest one added). In other words, this wrapper also
 * functions as a filter on the topic, with undesired messages being filtered
 * out.
 * 
 * @author Mark Brightwell
 *
 */
class MessageListenerWrapper implements MessageListener {
  
  /**
   * Class logger.
   */
  private static final Logger LOGGER = Logger.getLogger(MessageListenerWrapper.class);
  
  /**
   * Wrapped listener. Methods accessing this field are synchronized.
   */
  private Map<Long, ServerUpdateListener> listeners = new HashMap<Long, ServerUpdateListener>();
  
  /**
   * Constructor. Adds the listener to receive updates for the specified Tag id. 
   * 
   * @param tagId the ClientDataTag id
   * @param serverUpdateListener the listener that should be registered
   */
  public MessageListenerWrapper(final Long tagId, final ServerUpdateListener serverUpdateListener) { 
    addListener(serverUpdateListener, tagId);
  }

  /**
   * Registers the listener for update notifications for the specified Tag.
   * Assumes this object is registered as JMS listener on the correct topic
   * for the given Tag.
   * 
   * @param listener the listener to notify on update
   * @param tagId listens to updates for the ClientDataTag with this id
   */
  public synchronized void addListener(final ServerUpdateListener listener, final Long tagId) {    
    listeners.put(tagId, listener);    
  }
  
  /**
   * Removes any listener registered for update notifications for this Tag.
   * 
   * @param tagId the id of the Tag
   */
  public synchronized void removeListener(final Long tagId) {
    listeners.remove(tagId);
  }
  
  /**
   * Returns true if their are currently no listeners registered for
   * this topic (the wrapper class can then unsubscribe).
   * 
   * @return true if no listeners
   */
  public synchronized boolean isEmpty() {
    return listeners.isEmpty();
  }

  /**
   * Decodes the message into a TransferTagValue. If a listener is registered to
   * to received updates for this Tag, it will be notified. Otherwise the update
   * is filtered out.
   * 
   * <p>Notice this method is synchronized. This has no performance impact, since
   * a given message listener is notified on a single thread (since registered on
   * a given Session).
   * 
   * @param message the incoming JMS message
   */
  @Override
  public synchronized void onMessage(final Message message) { 
    try {
      if (message instanceof TextMessage) {
        TransferTagValue transferTagValue = TransferTagValueImpl.fromJson(((TextMessage) message).getText());
        if (listeners.containsKey(transferTagValue.getId())) {
          listeners.get(transferTagValue.getId()).onUpdate(transferTagValue);
        }
      } else {
        LOGGER.warn("Non-text message received on ");
      }
    } catch (JMSException e) {
      LOGGER.error("JMSException caught while receiving Tag update message.", e);
    }    
  }
  
}
