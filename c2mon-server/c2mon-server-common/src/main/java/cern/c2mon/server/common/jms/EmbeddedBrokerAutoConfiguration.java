package cern.c2mon.server.common.jms;

import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.broker.TransportConnector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import cern.c2mon.server.common.config.ServerProperties;

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
    brokerService.setUseJmx(true);
    brokerService.getManagementContext().setCreateConnector(false);

    addAdditionalConnectors(brokerService);

    return brokerService;
  }

  private void addAdditionalConnectors(BrokerService brokerService) throws Exception {
    createJmsTransportConnector(brokerService);
    createAmqpTransportConnector(brokerService);
  }

  private void createAmqpTransportConnector(BrokerService brokerService) throws Exception {
    TransportConnector connector = brokerService.addConnector(properties.getAmqp().getUrl()
        + "?transport.transformer=jms"
        + "&transport.tcpNoDelay=true"
    );
    connector.setName("amqp");
  }

  private void createJmsTransportConnector(BrokerService brokerService) throws Exception {
    TransportConnector connector = brokerService.addConnector(properties.getJms().getUrl());
    connector.setName("jms");
  }
}
