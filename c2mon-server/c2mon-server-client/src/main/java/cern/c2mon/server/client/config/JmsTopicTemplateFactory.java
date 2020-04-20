/******************************************************************************
 * Copyright (C) 2010-2020 CERN. All rights not expressly granted are reserved.
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
package cern.c2mon.server.client.config;

import javax.jms.ConnectionFactory;

import org.springframework.jms.core.JmsTemplate;

/**
 * Factory used by all JMS Config classes to create {@link JmsTemplate} with standardized configuration 
 * 
 * @author Matthias Braeger
 */
final class JmsTopicTemplateFactory {
  
  private JmsTopicTemplateFactory() {}

  /**
   * Creates a new {@link JmsTemplate} with standardized configuration 
   * @param connectionFactory The JMS connection factory
   * @param clientTopicMsgTimeToLive Time-to-live in seconds
   * @return A new {@link JmsTemplate} instance with standardized configuration
   */
  static JmsTemplate createJmsTemplate(ConnectionFactory connectionFactory, int clientTopicMsgTimeToLive) {
    JmsTemplate jmsTemplate = new JmsTemplate(connectionFactory);

    jmsTemplate.setExplicitQosEnabled(true);
    
    // message time-to-live in ms
    long ttl = clientTopicMsgTimeToLive * 1000L;
    jmsTemplate.setTimeToLive(ttl);
    
    jmsTemplate.setDeliveryPersistent(false);
    jmsTemplate.setSessionTransacted(false);
    
    return jmsTemplate;
  }
}
