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

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQQueue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.jms.connection.SingleConnectionFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.listener.DefaultMessageListenerContainer;

import cern.c2mon.shared.daq.datatag.DataTagValueUpdateConverter;

/**
 * Creates all Beans required for the JMS communication
 * @author Justin Lewis Salmon
 */
public class JmsConfig {

  @Autowired
  private DaqProperties properties;

  @Bean
  public ActiveMQConnectionFactory activeMQConnectionFactory() {
    ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(properties.getJms().getUrl());
    factory.setClientIDPrefix("C2MON-DAQ-" + properties.getName());
    return factory;
  }

  @Bean
  public SingleConnectionFactory singleConnectionFactory() {
    return new SingleConnectionFactory(activeMQConnectionFactory());
  }

  @Bean
  public JmsUpdateQueueTemplateFactory sourceUpdateJmsTemplate() {
    return new JmsUpdateQueueTemplateFactory(singleConnectionFactory(), properties);
  }

  @Bean
  public JmsTemplate processRequestJmsTemplate() {
    JmsTemplate jmsTemplate = new JmsTemplate(singleConnectionFactory());
    addDefaultProcessRequestJmsTemplateProperties(jmsTemplate);
    return jmsTemplate;
  }

  @Bean
  public DefaultMessageListenerContainer serverRequestListenerContainer() {
    DefaultMessageListenerContainer container = new DefaultMessageListenerContainer();
    container.setConnectionFactory(singleConnectionFactory());
    container.setDestinationName("replaced-at-runtime");
    container.setSessionTransacted(false);
    container.setMaxMessagesPerTask(1);
    container.setReceiveTimeout(1000);
    container.setIdleTaskExecutionLimit(10);
    container.setAutoStartup(false);
    return container;
  }

  @Bean
  public ActiveMQConnectionFactory filterActiveMQConnectionFactory() {
    ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(properties.getFilter().getJms().getUrl());
    factory.setClientIDPrefix("C2MON_DAQ_FILTER");
    return factory;
  }

  @Bean
  public SingleConnectionFactory filterConnectionFactory() {
    return new SingleConnectionFactory(filterActiveMQConnectionFactory());
  }

  @Bean
  public JmsTemplate filterJmsTemplate() {
    String queueTrunk = properties.getJms().getQueuePrefix();
    JmsTemplate template = new JmsTemplate(filterConnectionFactory());
    template.setDefaultDestination(new ActiveMQQueue(queueTrunk + ".filter"));
    template.setExplicitQosEnabled(true);
    template.setDeliveryPersistent(false);
    template.setTimeToLive(60_000L);
    return template;
  }

  @Bean
  public ActiveMQConnectionFactory secondActiveMQConnectionFactory() {
    ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(properties.getJms().getSecondaryUrl());
    factory.setClientIDPrefix("C2MON-DAQ-" + properties.getName());
    return factory;
  }

  @Bean
  public SingleConnectionFactory secondSingleConnectionFactory() {
    return new SingleConnectionFactory(secondActiveMQConnectionFactory());
  }

  @Bean
  public JmsUpdateQueueTemplateFactory secondSourceUpdateJmsTemplate() {
    return new JmsUpdateQueueTemplateFactory(secondSingleConnectionFactory(), properties);
  }

  @Bean
  public JmsTemplate secondProcessRequestJmsTemplate() {
    JmsTemplate jmsTemplate = new JmsTemplate(secondSingleConnectionFactory());
    addDefaultProcessRequestJmsTemplateProperties(jmsTemplate);
    return jmsTemplate;
  }

  @Bean
  public DefaultMessageListenerContainer secondServerRequestListenerContainer() {
    DefaultMessageListenerContainer container = new DefaultMessageListenerContainer();
    container.setConnectionFactory(secondSingleConnectionFactory());
    container.setDestinationName("replaced-at-runtime");
    container.setSessionTransacted(false);
    container.setMaxMessagesPerTask(1);
    container.setReceiveTimeout(1000);
    container.setIdleTaskExecutionLimit(10);
    container.setAutoStartup(false);
    return container;
  }
  
  private void addDefaultUpdateJmsTemplateQueueProperties(JmsTemplate jmsTemplate) {
    jmsTemplate.setDefaultDestination(new ActiveMQQueue(properties.getJms().getQueuePrefix() + ".update." + properties.getName()));
    jmsTemplate.setExplicitQosEnabled(true);
    jmsTemplate.setMessageConverter(new DataTagValueUpdateConverter());
  }
  
  private void addDefaultProcessRequestJmsTemplateProperties(JmsTemplate jmsTemplate) {
    String queueTrunk = properties.getJms().getQueuePrefix();
    jmsTemplate.setDefaultDestination(new ActiveMQQueue(queueTrunk + ".request"));
    jmsTemplate.setExplicitQosEnabled(true);
    // timeToLive the message's lifetime (in milliseconds)
    long ttl = properties.getJms().getRequestMsgtimeToLive() * 1000L;
    jmsTemplate.setTimeToLive(ttl);
    jmsTemplate.setDeliveryPersistent(false);
  }
}
