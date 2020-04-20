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

import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQQueue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.connection.SingleConnectionFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.listener.DefaultMessageListenerContainer;

import cern.c2mon.server.client.request.ClientRequestDelegator;
import cern.c2mon.server.client.request.ClientRequestErrorHandler;
import cern.c2mon.server.common.config.ServerConstants;
import cern.c2mon.shared.util.jms.ActiveJmsSender;

/**
 * @author Justin Lewis Salmon
 */
@Configuration
public class ClientJmsConfig {

  @Autowired
  private ClientProperties properties;

  @Bean
  public ActiveMQConnectionFactory clientActiveMQConnectionFactory() {
    String url = properties.getJms().getUrl();
    ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(url);
    connectionFactory.setClientIDPrefix("C2MON-SERVER-CLIENT");
    connectionFactory.setTrustAllPackages(true);
    connectionFactory.setWatchTopicAdvisories(false);
    return connectionFactory;
  }

  @Bean
  public SingleConnectionFactory clientSingleConnectionFactory() {
    return new SingleConnectionFactory(clientActiveMQConnectionFactory());
  }

  @Bean
  public JmsTemplate clientTopicPublisherJmsTemplate() {
    int ttl = properties.getJms().getClientTopicMsgTimeToLive();
    return JmsTopicTemplateFactory.createJmsTemplate(clientSingleConnectionFactory(), ttl);
  }

  @Bean
  public ActiveJmsSender clientTopicPublisher() {
    ActiveJmsSender jmsSender = new ActiveJmsSender();
    jmsSender.setJmsTemplate(clientTopicPublisherJmsTemplate());
    return jmsSender;
  }

  @Bean
  public DefaultMessageListenerContainer clientRequestJmsContainer(ClientRequestDelegator delegator, ClientRequestErrorHandler errorHandler) {
    DefaultMessageListenerContainer container = new DefaultMessageListenerContainer();

    String clientRequestQueue = properties.getJms().getRequestQueue();
    container.setDestination(new ActiveMQQueue(clientRequestQueue));

    container.setConnectionFactory(clientSingleConnectionFactory());
    container.setMessageListener(delegator);
    container.setConcurrentConsumers(properties.getJms().getInitialConsumers());
    container.setMaxConcurrentConsumers(properties.getJms().getMaxConsumers());
    container.setMaxMessagesPerTask(1);
    container.setReceiveTimeout(1000);
    container.setIdleTaskExecutionLimit(600);
    container.setSessionTransacted(false);
    container.setTaskExecutor(clientExecutor());
    container.setErrorHandler(errorHandler);
    container.setAutoStartup(false);
    container.setPhase(ServerConstants.PHASE_INTERMEDIATE);
    return container;
  }

  @Bean
  public ThreadPoolExecutor clientExecutor() {
    return new ThreadPoolExecutor(1, 1000, 60000, TimeUnit.MILLISECONDS, new SynchronousQueue<>());
  }
}
