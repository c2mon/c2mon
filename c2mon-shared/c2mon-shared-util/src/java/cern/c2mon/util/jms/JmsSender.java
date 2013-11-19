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
package cern.c2mon.util.jms;

import org.springframework.jms.JmsException;

/**
 * Bean providing for sending JMS messages and waiting for a response, 
 * without managing the connection.
 * 
 * <p>Implementations should provide a convenient way of setting the connection
 * details.
 * 
 * @author Mark Brightwell
 *
 */
public interface JmsSender {

  /**
   * Sends a text message to the specified JMS queue and waits for a
   * response. Expects a text message back from the receiver as the
   * response.
   * 
   * @param text the text message
   * @param jmsQueue the destination queue
   * @param timeout the timeout waiting for a response
   * @return a textual reply
   * @throws NullPointerException if text argument is null
   * @throws JmsException if problem encountered with JMS
   */
  String sendRequestToQueue(String text, String jmsQueue, long timeout);

  /**
   * Sends a text message to the specified topic. Does not expect any response.
   * @param text the message
   * @param jmsTopic the topic name
   * @throws NullPointerException if either argument is null
   * @throws JmsException if problem encountered with JMS
   */
  void sendToTopic(String text, String jmsTopic);
  
  /**
   * Sends a text message to a default destination (the implementation must provide
   * a way of setting this; using Spring this can be done on the JmsTemplate).
   * Does not expect any response. 
   * @param text the message
   * @throws NullPointerException if argument is null
   * @throws JmsException if problem encountered with JMS
   */
  void send(String text);

  /**
   * As for sendToTopic but for queues.
   * @param text the message
   * @param jmsQueueName the queue name
   * @throws NullPointerException if either argument is null
   * @throws JmsException if problem encountered with JMS
   */
  void sendToQueue(String text, String jmsQueueName);
  
}
