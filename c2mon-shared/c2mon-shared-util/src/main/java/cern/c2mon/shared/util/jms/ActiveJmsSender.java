/******************************************************************************
 * Copyright (C) 2010-2019 CERN. All rights not expressly granted are reserved.
 * 
 * This file is part of the CERN Control and Monitoring Platform 'C2MON'.
 * C2MON is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the license.
 * 
 * C2MON is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
 * more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with C2MON. If not, see <http://www.gnu.org/licenses/>.
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;


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
  private static final Logger LOGGER = LoggerFactory.getLogger(ActiveJmsSender.class);
  
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
    return jmsTemplate.execute(session -> {
      String returnString = null;
      TemporaryTopic replyTopic = null;
      MessageConsumer consumer = null;
      try {
        replyTopic = session.createTemporaryTopic();
        TextMessage textMessage = session.createTextMessage();
        textMessage.setText(text);
        textMessage.setJMSReplyTo(replyTopic);

        consumer = session.createConsumer(replyTopic);

        Destination requestDestination = new ActiveMQQueue(jmsListenerQueue);
        MessageProducer messageProducer = session.createProducer(requestDestination);
        messageProducer.send(textMessage);

        // Wait for reply
        Message replyMessage = consumer.receive(timeout);
        if (replyMessage != null) {
          if (replyMessage instanceof TextMessage) {
            returnString = ((TextMessage) replyMessage).getText();
          } else {
            LOGGER.warn("Non-text message received as JMS reply from ActiveMQ - unable to process");
          }
        }
      } finally {
        if (consumer != null) {
          consumer.close();
        } if (replyTopic != null) {
          replyTopic.delete();
        }
      }
      return returnString;
    }, true);
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
