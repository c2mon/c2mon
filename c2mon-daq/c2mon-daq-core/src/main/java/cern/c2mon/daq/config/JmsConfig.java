package cern.c2mon.daq.config;

import cern.c2mon.shared.daq.datatag.DataTagValueUpdateConverter;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQQueue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.jms.connection.SingleConnectionFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.listener.DefaultMessageListenerContainer;

/**
 * @author Justin Lewis Salmon
 */
public class JmsConfig {

  @Autowired
  private Environment environment;

  @Bean
  public ActiveMQConnectionFactory activeMQConnectionFactory() {
    ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(environment.getRequiredProperty("jms.broker.url"));
    factory.setClientIDPrefix("C2MON-DAQ-" + environment.getRequiredProperty("c2mon.daq.name"));
    factory.setWatchTopicAdvisories(false);
    return factory;
  }

  @Bean
  public SingleConnectionFactory singleConnectionFactory() {
    return new SingleConnectionFactory(activeMQConnectionFactory());
  }

  @Bean
  public JmsTemplate sourceUpdateJmsTemplate() {
    String daqName = environment.getRequiredProperty("c2mon.daq.name");
    String queueTrunk = environment.getRequiredProperty("c2mon.jms.daq.queue.trunk");
    JmsTemplate template = new JmsTemplate(singleConnectionFactory());
    template.setDefaultDestination(new ActiveMQQueue(queueTrunk + ".update." + daqName));
    template.setMessageConverter(dataTagValueUpdateConverter());
    return template;
  }

  @Bean
  public DataTagValueUpdateConverter dataTagValueUpdateConverter() {
    return new DataTagValueUpdateConverter();
  }

  @Bean
  public JmsTemplate processRequestJmsTemplate() {
    String queueTrunk = environment.getRequiredProperty("c2mon.jms.daq.queue.trunk");
    JmsTemplate template = new JmsTemplate(singleConnectionFactory());
    template.setDefaultDestination(new ActiveMQQueue(queueTrunk + ".request"));
    return template;
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
    ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(environment.getRequiredProperty("jms.broker.url"));
    factory.setClientIDPrefix("C2MON_DAQ_FILTER");
    factory.setWatchTopicAdvisories(false);
    return factory;
  }

  @Bean
  public SingleConnectionFactory filterConnectionFactory() {
    return new SingleConnectionFactory(filterActiveMQConnectionFactory());
  }

  @Bean
  public JmsTemplate filterJmsTemplate() {
    String queueTrunk = environment.getRequiredProperty("c2mon.jms.daq.queue.trunk");
    JmsTemplate template = new JmsTemplate(filterConnectionFactory());
    template.setDefaultDestination(new ActiveMQQueue(queueTrunk + ".filter"));
    template.setDeliveryPersistent(false);
    template.setPriority(1);
    template.setTimeToLive(1800000);
    return template;
  }

  @Bean
  public ActiveMQConnectionFactory secondActiveMQConnectionFactory() {
    ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(environment.getRequiredProperty("jms.broker.url.2"));
    factory.setClientIDPrefix("C2MON-DAQ-" + environment.getRequiredProperty("c2mon.daq.name"));
    factory.setWatchTopicAdvisories(false);
    return factory;
  }

  @Bean
  public SingleConnectionFactory secondSingleConnectionFactory() {
    return new SingleConnectionFactory(secondActiveMQConnectionFactory());
  }

  @Bean
  public JmsTemplate secondSourceUpdateJmsTemplate() {
    String daqName = environment.getRequiredProperty("c2mon.daq.name");
    String queueTrunk = environment.getRequiredProperty("c2mon.jms.daq.queue.trunk");
    JmsTemplate template = new JmsTemplate(secondSingleConnectionFactory());
    template.setDefaultDestination(new ActiveMQQueue(queueTrunk + ".update." + daqName));
    template.setMessageConverter(dataTagValueUpdateConverter());
    return template;
  }

  @Bean
  public JmsTemplate secondProcessRequestJmsTemplate() {
    String queueTrunk = environment.getRequiredProperty("c2mon.jms.daq.queue.trunk");
    JmsTemplate template = new JmsTemplate(secondSingleConnectionFactory());
    template.setDefaultDestination(new ActiveMQQueue(queueTrunk + ".request"));
    return template;
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
}
