package cern.c2mon.client.core.amqp;

import java.net.URI;

import javax.jms.Connection;

import lombok.extern.slf4j.Slf4j;
import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.broker.TransportConnector;
import org.apache.qpid.jms.JmsConnectionFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import cern.c2mon.client.core.jms.EmbeddedBrokerTestSuite;

import static org.junit.Assert.assertNotNull;

@Slf4j
public class AmqpBasicBrokerTest extends EmbeddedBrokerTestSuite {

  @Test(timeout=30000)
  public void testCreateConnection() throws Exception {
    JmsConnectionFactory factory = new JmsConnectionFactory(new URI(URL));
    connection = factory.createConnection();
    assertNotNull(connection);
    connection.start();
    connection.close();
  }
}
