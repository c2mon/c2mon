package cern.c2mon.server.common.jms;

import org.apache.activemq.broker.BrokerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/**
 * @author Justin Lewis Salmon
 */
@Configuration
@ConditionalOnProperty(prefix = "c2mon.server.jms", name = "embedded", havingValue = "true", matchIfMissing = true)
public class EmbeddedBrokerAutoConfiguration {

  @Autowired
  private Environment environment;

  @Bean(initMethod = "start", destroyMethod = "stop")
  public BrokerService brokerService() throws Exception {
    BrokerService brokerService = new BrokerService();
    brokerService.setPersistent(false);
    brokerService.setUseShutdownHook(false);
    brokerService.setUseJmx(false);
    brokerService.addConnector(environment.getRequiredProperty("c2mon.server.jms.url"));
    return brokerService;
  }
}
