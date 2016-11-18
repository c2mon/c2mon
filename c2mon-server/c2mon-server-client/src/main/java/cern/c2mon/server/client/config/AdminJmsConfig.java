package cern.c2mon.server.client.config;

import cern.c2mon.server.client.request.ClientRequestDelegator;
import cern.c2mon.server.client.request.ClientRequestErrorHandler;
import cern.c2mon.server.common.config.ServerConstants;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.ActiveMQPrefetchPolicy;
import org.apache.activemq.command.ActiveMQQueue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.jms.connection.SingleConnectionFactory;
import org.springframework.jms.listener.DefaultMessageListenerContainer;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author Justin Lewis Salmon
 */
@Configuration
public class AdminJmsConfig {

  @Autowired
  private Environment environment;

  @Autowired
  private ThreadPoolExecutor clientExecutor;

  @Bean
  public ActiveMQConnectionFactory adminActiveMQConnectionFactory() {
    String url = environment.getRequiredProperty("c2mon.server.client.jms.url");

    ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(url);
    connectionFactory.setClientIDPrefix("C2MON-SERVER-CLIENT");
    connectionFactory.setWatchTopicAdvisories(false);

    ActiveMQPrefetchPolicy prefetchPolicy = new ActiveMQPrefetchPolicy();
    prefetchPolicy.setQueuePrefetch(0);
    connectionFactory.setPrefetchPolicy(prefetchPolicy);
    return connectionFactory;
  }

  @Bean
  public SingleConnectionFactory adminSingleConnectionFactory() {
    return new SingleConnectionFactory(adminActiveMQConnectionFactory());
  }

  @Bean
  public DefaultMessageListenerContainer adminRequestJmsContainer(ClientRequestDelegator delegator, ClientRequestErrorHandler errorHandler) {
    DefaultMessageListenerContainer container = new DefaultMessageListenerContainer();

    String adminRequestQueue = environment.getRequiredProperty("c2mon.server.client.jms.queue.admin");
    container.setDestination(new ActiveMQQueue(adminRequestQueue));

    container.setConnectionFactory(adminSingleConnectionFactory());
    container.setMessageListener(delegator);
    container.setConcurrentConsumers(1);
    container.setMaxConcurrentConsumers(2);
    container.setMaxMessagesPerTask(1);
    container.setReceiveTimeout(1000);
    container.setIdleTaskExecutionLimit(600);
    container.setSessionTransacted(false);
    container.setTaskExecutor(clientExecutor);
    container.setErrorHandler(errorHandler);
    container.setAutoStartup(false);
    container.setPhase(ServerConstants.PHASE_INTERMEDIATE);
    return container;
  }
}
