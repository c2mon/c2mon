package cern.c2mon.server.daqcommunication.out.config;

import org.apache.activemq.ActiveMQConnectionFactory;
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
public class DaqCommunicationOutJmsConfig {

  @Autowired
  private Environment environment;

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
    ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(
        environment.getRequiredProperty("c2mon.server.daqcommunication.jms.url"));

    connectionFactory.setClientIDPrefix("C2MON-DAQ-OUT");
    connectionFactory.setWatchTopicAdvisories(false);
    return connectionFactory;
  }
}
