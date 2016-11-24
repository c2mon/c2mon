package cern.c2mon.server.common.jms;

import cern.c2mon.server.common.config.ServerProperties;
import org.apache.activemq.broker.BrokerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Justin Lewis Salmon
 */
@Configuration
@ConditionalOnProperty(prefix = "c2mon.server.jms", name = "embedded", havingValue = "true", matchIfMissing = true)
public class EmbeddedBrokerAutoConfiguration {

  @Autowired
  private ServerProperties properties;

  @Bean(initMethod = "start", destroyMethod = "stop")
  public BrokerService brokerService() throws Exception {
    BrokerService brokerService = new BrokerService();
    brokerService.setPersistent(false);
    brokerService.setUseShutdownHook(false);
    brokerService.setUseJmx(false);
    brokerService.addConnector(properties.getJms().getUrl());
    return brokerService;
  }
}
