/*******************************************************************************
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
 ******************************************************************************/
package cern.c2mon.daq.config;

import java.util.HashMap;
import java.util.Map;

import javax.jms.ConnectionFactory;
import javax.jms.Destination;

import org.apache.activemq.command.ActiveMQQueue;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.QosSettings;

import cern.c2mon.shared.daq.datatag.DataTagValueUpdateConverter;

/**
 * Factory class the creates for every {@link QosSettings} configuration a dedicated {@link JmsTemplate} instance.
 * This assures note mixing configurations when sending messages in parallel.
 *  
 * @author Matthias Braeger
 */
public final class JmsUpdateQueueTemplateFactory {
  private final ConnectionFactory connectionFactory;
  private final Destination destination;
  private final Map<QosSettings, JmsTemplate> jmsTemplateMap = new HashMap<>();
  
  /**
   * The constructor is creating the name of the Destination queue from the DAQ properties 
   * @param connectionFactory JMS connection factory
   * @param properties the DAQ properties
   */
  public JmsUpdateQueueTemplateFactory(ConnectionFactory connectionFactory, DaqProperties properties) {
    this.connectionFactory = connectionFactory;
    destination = new ActiveMQQueue(properties.getJms().getQueuePrefix() + ".update." + properties.getName());
  }
  
  /**
   * Creates for the given settings the JMSTemplate instance, if not yet done.
   * @param settings The Quality-of-Service settings to use
   * @return the dedicated {@link JmsTemplate} for the given settings
   */
  public synchronized JmsTemplate getDataTagValueUpdateJmsTemplate(QosSettings settings) {
    if (!jmsTemplateMap.containsKey(settings)) { 
      createDataTagValueUpdateJmsTemplate(settings);
    }
    return jmsTemplateMap.get(settings);
  }
  
  /**
   * Note, that the {@link JmsTemplate#setQosSettings(QosSettings)} is implicitly calling 
   * {@link JmsTemplate#setExplicitQosEnabled(boolean)}
   * @param settings the Quality of Service settings for the template 
   */
  private void createDataTagValueUpdateJmsTemplate(QosSettings settings) {
    JmsTemplate jmsTemplate = new JmsTemplate(connectionFactory);
    jmsTemplate.setDefaultDestination(destination);
    jmsTemplate.setQosSettings(settings);
    jmsTemplate.setMessageConverter(new DataTagValueUpdateConverter());
    jmsTemplateMap.put(settings, jmsTemplate);
  }
}
