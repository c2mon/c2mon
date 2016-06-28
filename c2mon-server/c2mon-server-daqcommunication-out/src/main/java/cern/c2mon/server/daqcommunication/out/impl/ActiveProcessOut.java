/******************************************************************************
 * Copyright (C) 2010-2016 CERN. All rights not expressly granted are reserved.
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
package cern.c2mon.server.daqcommunication.out.impl;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TemporaryTopic;
import javax.jms.TextMessage;

import org.apache.activemq.command.ActiveMQQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.SessionCallback;
import org.springframework.stereotype.Service;

import cern.c2mon.server.daqcommunication.out.impl.JmsProcessOut;

/**
 * Implementation of the JmsProcessOut interface for ActiveMQ 
 * middleware.
 * 
 * @author Mark Brightwell
 *
 */
@Service("jmsProcessOut")
public class ActiveProcessOut implements JmsProcessOut {

  /**
   * Private class logger.
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(ActiveProcessOut.class);
  
  /**
   * Reference to the JmsTemplate, instantiated in the Spring XML.
   * Autowired using annotations to get the correct jmsTemplate
   * from the Spring container.
   */
  private JmsTemplate processOutJmsTemplate;    
  
  @Autowired
  public ActiveProcessOut(@Qualifier("processOutJmsTemplate") JmsTemplate processOutJmsTemplate) {
    super();
    this.processOutJmsTemplate = processOutJmsTemplate;
  }

  @Override
  public String sendTextMessage(final String text, final String jmsListenerQueue, final long timeout) {   
    String reply = (String) processOutJmsTemplate.execute(new SessionCallback<Object>() {
      @Override
    public Object doInJms(Session session) throws JMSException {
        String returnString = null;
        MessageConsumer consumer = null;
        MessageProducer messageProducer = null;
        TemporaryTopic replyQueue = null;
        
        try {
        
            replyQueue = session.createTemporaryTopic();
            
            //TemporaryTopic replyTopic = session.createTemporaryTopic();
            TextMessage textMessage = session.createTextMessage();
            textMessage.setText(text);
            textMessage.setJMSReplyTo(replyQueue);
            
            Destination requestDestination = new ActiveMQQueue(jmsListenerQueue);
            messageProducer = session.createProducer(requestDestination);
            messageProducer.setTimeToLive(2 * timeout);
            messageProducer.send(textMessage);
                                            
            //wait for reply                    
            consumer = session.createConsumer(replyQueue);           
            
            Message replyMessage = consumer.receive(timeout);
            if (replyMessage != null) {
              if (replyMessage instanceof TextMessage) {
                returnString = ((TextMessage) replyMessage).getText();
              } else {
                LOGGER.warn("Non-text message received as reply to SourceDataTagRequest - unable to process");
              }          
            }
        } finally {
            if (consumer != null) {
                try {consumer.close();}catch(JMSException ex) {//IGNORE
                    }
            }
            if (messageProducer != null) {
                try {messageProducer.close();}catch(JMSException ex) {//IGNORE
                    }
            }
            if (replyQueue != null) {
                try {replyQueue.delete();}catch(JMSException ex) {//IGNORE
                }
            }
        }
        return returnString;
      }      
    }, true);
    return reply;
  }

}
