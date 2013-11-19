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
package cern.c2mon.shared.util.jms;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TemporaryTopic;
import javax.jms.TextMessage;

import org.apache.activemq.command.ActiveMQQueue;
import org.apache.activemq.command.ActiveMQTopic;
import org.apache.log4j.Logger;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.jms.core.SessionCallback;


/**
 * Implementation of the JmsSender for ActiveMQ.
 * 
 * <p>The connection details are set in a Spring JmsTemplate, which needs
 * instantiating elsewhere before this class can be used.
 * 
 * @author Mark Brightwell
 *
 */
public class ActiveJmsSender implements JmsSender {
  
  /**
   * Private class logger.
   */
  private static final Logger LOGGER = Logger.getLogger(ActiveJmsSender.class);
  
  /**
   * Reference to the JmsTemplate. Could be instantiated in a Spring XML.
   * Needs setting explicitly (no autowire annotation in source).
   */
  private JmsTemplate jmsTemplate;    
  
  /**
   * Sends the specified text to the listener queue and waits for a response (until timeout
   * reached). Expects a TextMessage reply back from the listener. The listener should
   * respond to the Message replyTo.
   */
  @SuppressWarnings("unchecked")
  @Override
  public String sendRequestToQueue(final String text, final String jmsListenerQueue, final long timeout) {
    if (text == null) {
      throw new NullPointerException("Attempting to send a null text message.");
    }
    String reply = (String) jmsTemplate.execute(new SessionCallback() {
      public Object doInJms(Session session) throws JMSException {
        String returnString = null;
        //TemporaryQueue replyQueue = session.createTemporaryQueue();
        TemporaryTopic replyTopic = session.createTemporaryTopic();
        TextMessage textMessage = session.createTextMessage();
        textMessage.setText(text);
        textMessage.setJMSReplyTo(replyTopic);
        
        Destination requestDestination = new ActiveMQQueue(jmsListenerQueue);
        MessageProducer messageProducer = session.createProducer(requestDestination);
        messageProducer.send(textMessage);
                                        
        //wait for reply (receive timeout is set in XML)                          
        MessageConsumer consumer = session.createConsumer(replyTopic);           
        
        Message replyMessage = consumer.receive(timeout);
        if (replyMessage != null) {
          if (replyMessage instanceof TextMessage) {
            returnString = ((TextMessage) replyMessage).getText();
          } else {
            LOGGER.warn("Non-text message received as JMS reply from ActiveMQ - unable to process");
          }          
        }
        return returnString;
      }      
    }, true);
    return reply;
  }

  /**
   * Setter method.
   * @param jmsTemplate the jmsTemplate to set
   */
  public void setJmsTemplate(JmsTemplate jmsTemplate) {
    this.jmsTemplate = jmsTemplate;
  }

  @Override
  public void sendToTopic(final String text, final String jmsTopicName) {  
    if (text == null) {
      throw new NullPointerException("Attempting to send a null text message.");
    }
    Destination topic = new ActiveMQTopic(jmsTopicName);
    jmsTemplate.send(topic, new MessageCreator() {
      
      @Override
      public Message createMessage(Session session) throws JMSException {
        return session.createTextMessage(text);        
      }
      
    });
  }
  
  @Override
  public void sendToQueue(final String text, final String jmsQueueName) {
    if (text == null) {
      throw new NullPointerException("Attempting to send a null text message.");
    }
    Destination queue = new ActiveMQQueue(jmsQueueName);
    jmsTemplate.send(queue, new MessageCreator() {
      
      @Override
      public Message createMessage(Session session) throws JMSException {
        return session.createTextMessage(text);        
      }
      
    });
  }

  /**
   * Sends a text message to the default destination, which 
   * must be set in the JmsTemplate (in Spring XML for instance).
   */
  @Override
  public void send(final String text) {
    if (text == null) {
      throw new NullPointerException("Attempting to send a null text message.");
    }
    jmsTemplate.send(new MessageCreator() {      
      
      @Override
      public Message createMessage(Session session) throws JMSException {
        return session.createTextMessage(text);
      }
      
    });
  }

}
