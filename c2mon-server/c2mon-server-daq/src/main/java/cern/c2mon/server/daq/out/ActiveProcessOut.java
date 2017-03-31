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
package cern.c2mon.server.daq.out;

import javax.jms.*;

import lombok.extern.slf4j.Slf4j;
import org.apache.activemq.command.ActiveMQQueue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.SessionCallback;
import org.springframework.stereotype.Service;

/**
 * Implementation of the JmsProcessOut interface for ActiveMQ
 * middleware.
 *
 * @author Mark Brightwell
 *
 */
@Slf4j
@Service("jmsProcessOut")
public class ActiveProcessOut implements JmsProcessOut {

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
        TemporaryTopic replyTopic = null;

        try {

            replyTopic = session.createTemporaryTopic();
            consumer = session.createConsumer(replyTopic);

            //TemporaryTopic replyTopic = session.createTemporaryTopic();
            TextMessage textMessage = session.createTextMessage();
            textMessage.setText(text);
            textMessage.setJMSReplyTo(replyTopic);

            Destination requestDestination = new ActiveMQQueue(jmsListenerQueue);
            messageProducer = session.createProducer(requestDestination);
            messageProducer.setTimeToLive(2 * timeout);
            messageProducer.send(textMessage);

            //wait for reply
            Message replyMessage = consumer.receive(timeout);
            if (replyMessage != null) {
              if (replyMessage instanceof TextMessage) {
                returnString = ((TextMessage) replyMessage).getText();
              } else {
                log.warn("Non-text message received as reply to SourceDataTagRequest - unable to process");
              }
            }
        } finally {
            if (consumer != null) {
                try {consumer.close();} catch (JMSException ex) {/** IGNORE */}
            }
            if (messageProducer != null) {
                try {messageProducer.close();} catch (JMSException ex) {/** IGNORE */}
            }
            if (replyTopic != null) {
                try {replyTopic.delete();} catch (JMSException ex) {/** IGNORE */}
            }
        }
        return returnString;
      }
    }, true);
    return reply;
  }

}
