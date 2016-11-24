package cern.c2mon.server.daqcommunication.in.config;

import cern.c2mon.server.common.config.ServerConstants;
import cern.c2mon.server.daqcommunication.in.request.ProcessRequestHandlerImpl;
import cern.c2mon.shared.daq.datatag.DataTagValueUpdateConverter;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQQueue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.jms.listener.DefaultMessageListenerContainer;

/**
 * @author Justin Lewis Salmon
 */
public class DaqCommunicationInJmsConfig {

  @Autowired
  private DaqCommunicationInProperties properties;

  @Bean
  public ActiveMQConnectionFactory daqInConnectionFactory() {
    String url = properties.getJms().getUrl();
    ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(url);
    connectionFactory.setClientIDPrefix("C2MON-SERVER-DAQ-IN");
    connectionFactory.setWatchTopicAdvisories(false);
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
    container.setAutoStartup(false);
    container.setPhase(ServerConstants.PHASE_START_LAST);
    container.setBeanName("Process request JMS container");
    return container;
  }

  @Bean
  public DataTagValueUpdateConverter dataTagValueUpdateConverter() {
    return new DataTagValueUpdateConverter();
  }
}
