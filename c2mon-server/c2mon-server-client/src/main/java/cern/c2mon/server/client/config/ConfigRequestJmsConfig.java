package cern.c2mon.server.client.config;

import cern.c2mon.server.common.config.ServerConstants;
import cern.c2mon.server.configuration.impl.ConfigurationRequestHandler;
import org.apache.activemq.command.ActiveMQQueue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.jms.connection.SingleConnectionFactory;
import org.springframework.jms.listener.DefaultMessageListenerContainer;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author Justin Lewis Salmon
 */
public class ConfigRequestJmsConfig {

  @Autowired
  private ClientProperties properties;

  @Autowired
  private SingleConnectionFactory clientSingleConnectionFactory;

  @Autowired
  private ThreadPoolExecutor clientExecutor;

  @Bean
  public DefaultMessageListenerContainer configRequestJmsContainer(ConfigurationRequestHandler requestHandler) {
    DefaultMessageListenerContainer container = new DefaultMessageListenerContainer();

    String configRequestQueue = properties.getJms().getConfigRequestQueue();
    container.setDestination(new ActiveMQQueue(configRequestQueue));

    container.setConnectionFactory(clientSingleConnectionFactory);
    container.setMessageListener(requestHandler);
    container.setConcurrentConsumers(1);
    container.setMaxConcurrentConsumers(1);
    container.setSessionTransacted(false);
    container.setTaskExecutor(clientExecutor);
    container.setPhase(ServerConstants.PHASE_START_LAST);
    return container;
  }
}
