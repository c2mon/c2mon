package cern.c2mon.server.client.config;

import cern.c2mon.shared.util.jms.ActiveJmsSender;
import org.apache.activemq.command.ActiveMQTopic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.jms.connection.SingleConnectionFactory;
import org.springframework.jms.core.JmsTemplate;

/**
 * @author Justin Lewis Salmon
 */
@Configuration
public class AlarmJmsConfig {

  @Autowired
  private ClientProperties properties;

  @Autowired
  private SingleConnectionFactory clientSingleConnectionFactory;

  @Bean
  public JmsTemplate alarmTopicPublisherJmsTemplate() {
    JmsTemplate jmsTemplate = new JmsTemplate(clientSingleConnectionFactory);

    String alarmTopic = properties.getJms().getAlarmTopic();
    jmsTemplate.setDefaultDestination(new ActiveMQTopic(alarmTopic));

    jmsTemplate.setExplicitQosEnabled(true);
    jmsTemplate.setTimeToLive(5400000);
    jmsTemplate.setDeliveryPersistent(false);
    jmsTemplate.setSessionTransacted(false);
    return jmsTemplate;
  }

  @Bean
  public ActiveJmsSender alarmTopicPublisher() {
    ActiveJmsSender jmsSender = new ActiveJmsSender();
    jmsSender.setJmsTemplate(alarmTopicPublisherJmsTemplate());
    return jmsSender;
  }
}
