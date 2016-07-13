package cern.c2mon.client.core.config;

import cern.c2mon.client.core.jms.ClientHealthMonitor;
import cern.c2mon.client.core.jms.JmsProxy;
import cern.c2mon.client.core.jms.RequestHandler;
import cern.c2mon.client.core.jms.impl.ClientHealthMonitorImpl;
import cern.c2mon.client.core.jms.impl.JmsProxyImpl;
import cern.c2mon.client.core.jms.impl.RequestHandlerImpl;
import cern.c2mon.client.core.jms.impl.SlowConsumerListener;
import cern.c2mon.shared.util.jms.ActiveJmsSender;
import cern.c2mon.shared.util.jms.JmsSender;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQTopic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.core.JmsTemplate;

import javax.jms.Destination;

/**
 * @author Justin Lewis Salmon
 */
@Configuration
public class JmsConfig {

  @Autowired
  private C2monClientProperties properties;

  @Bean
  public ActiveMQConnectionFactory jmsConnectionFactory() {
    return new ActiveMQConnectionFactory(properties.getJms().getUrl());
  }

  @Bean
  public JmsProxy jmsProxy() {
    Destination supervisionTopic = new ActiveMQTopic(properties.getJms().getSupervisionTopic());
    Destination alarmTopic = new ActiveMQTopic(properties.getJms().getAlarmTopic());
    Destination heartbeatTopic = new ActiveMQTopic(properties.getJms().getHeartbeatTopic());
    return new JmsProxyImpl(jmsConnectionFactory(), supervisionTopic, alarmTopic, heartbeatTopic, (SlowConsumerListener) clientHealthMonitor());
  }

  @Bean
  public JmsSender jmsSender(JmsTemplate jmsTemplate) {
    ActiveJmsSender jmsSender = new ActiveJmsSender();
    jmsSender.setJmsTemplate(jmsTemplate);
    return jmsSender;
  }

  @Bean
  @ConditionalOnMissingBean(JmsTemplate.class)
  public JmsTemplate jmsTemplate() {
    return new JmsTemplate(jmsConnectionFactory());
  }

  @Bean
  public RequestHandler coreRequestHandler() {
    RequestHandlerImpl requestHandler = new RequestHandlerImpl(jmsProxy(), properties.getJms().getRequestQueue(), properties.getJms().getAdminQueue());
    requestHandler.setMaxRequestSize(properties.getMaxTagsPerRequest());
    requestHandler.setCorePoolSize(properties.getMaxRequestThreads());
    return requestHandler;
  }

  @Bean
  public ClientHealthMonitor clientHealthMonitor() {
    return new ClientHealthMonitorImpl();
  }
}
