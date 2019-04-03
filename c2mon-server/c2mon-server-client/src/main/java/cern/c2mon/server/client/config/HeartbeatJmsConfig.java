package cern.c2mon.server.client.config;

import org.apache.qpid.jms.JmsConnectionFactory;
import org.apache.qpid.jms.JmsTopic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.jms.connection.SingleConnectionFactory;
import org.springframework.jms.core.JmsTemplate;

import cern.c2mon.shared.util.jms.ActiveJmsSender;
import cern.c2mon.shared.util.jms.JmsSender;

/**
 * @author Justin Lewis Salmon
 */
public class HeartbeatJmsConfig {

  @Autowired
  private ClientProperties properties;

  @Bean
  public JmsConnectionFactory heartbeatConnectionFactory() {
    JmsConnectionFactory factory = new JmsConnectionFactory(properties.getAmqp().getUrl());
    factory.setClientIDPrefix("C2MON-SERVER-HEARTBEAT");
//    factory.setWatchTopicAdvisories(false);
    return factory;
  }

  @Bean
  public SingleConnectionFactory heartbeatSingleConnectionFactory() {
    return new SingleConnectionFactory(heartbeatConnectionFactory());
  }

  @Bean
  public JmsTemplate heartbeatJmsTemplate() {
    JmsTemplate jmsTemplate = new JmsTemplate(heartbeatSingleConnectionFactory());
    jmsTemplate.setDefaultDestination(new JmsTopic(properties.getJms().getHeartbeatTopic()));
    jmsTemplate.setExplicitQosEnabled(true);
    jmsTemplate.setPriority(7);
    jmsTemplate.setTimeToLive(5400000);
    jmsTemplate.setDeliveryPersistent(false);
    jmsTemplate.setSessionTransacted(false);
    return jmsTemplate;
  }

  @Bean
  public JmsSender heartbeatSender() {
    ActiveJmsSender sender = new ActiveJmsSender();
    sender.setJmsTemplate(heartbeatJmsTemplate());
    return sender;
  }
}
