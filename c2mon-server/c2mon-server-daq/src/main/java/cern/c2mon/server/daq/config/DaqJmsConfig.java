package cern.c2mon.server.daq.config;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQQueue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.jms.connection.SingleConnectionFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.listener.DefaultMessageListenerContainer;

import cern.c2mon.server.common.config.ServerConstants;
import cern.c2mon.server.daq.request.ProcessRequestHandlerImpl;
import cern.c2mon.shared.daq.datatag.DataTagValueUpdateConverter;

/**
 * @author Justin Lewis Salmon
 */
public class DaqJmsConfig {

  @Autowired
  private DaqProperties properties;

  @Bean
  public ActiveMQConnectionFactory daqInConnectionFactory() {
    String url = properties.getJms().getUrl();
    ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(url);
    connectionFactory.setClientIDPrefix("C2MON-SERVER-DAQ-IN");
    return connectionFactory;
  }

  @Bean
  public DefaultMessageListenerContainer requestJmsContainer(ProcessRequestHandlerImpl processRequestHandler) {
    DefaultMessageListenerContainer container = new DefaultMessageListenerContainer();
    container.setConnectionFactory(daqInConnectionFactory());
    container.setDestination(new ActiveMQQueue(properties.getJms().getQueuePrefix() + ".request"));
    container.setMessageListener(processRequestHandler);
    container.setConcurrentConsumers(properties.getJms().getRequest().getInitialConsumers());
    container.setMaxConcurrentConsumers(properties.getJms().getRequest().getMaxConsumers());
    container.setSessionTransacted(properties.getJms().getRequest().isTransacted());
    container.setPhase(ServerConstants.PHASE_START_LAST);
    container.setBeanName("Process request JMS container");
    return container;
  }

  @Bean
  public DataTagValueUpdateConverter dataTagValueUpdateConverter() {
    return new DataTagValueUpdateConverter();
  }

  @Bean
  public JmsTemplate processOutJmsTemplate() {
    return new JmsTemplate(processOutConnectionFactory());
  }

  @Bean
  public SingleConnectionFactory processOutConnectionFactory() {
    return new SingleConnectionFactory(daqOutActiveMQConnectionFactory());
  }

  @Bean
  public ActiveMQConnectionFactory daqOutActiveMQConnectionFactory() {
    ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(properties.getJms().getUrl());

    connectionFactory.setClientIDPrefix("C2MON-DAQ-OUT");
    return connectionFactory;
  }
}
