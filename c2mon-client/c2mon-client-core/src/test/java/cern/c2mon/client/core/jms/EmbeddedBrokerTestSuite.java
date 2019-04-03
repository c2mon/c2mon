package cern.c2mon.client.core.jms;

import javax.jms.Connection;

import lombok.extern.slf4j.Slf4j;
import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.broker.TransportConnector;
import org.junit.After;
import org.junit.Before;

@Slf4j
public class EmbeddedBrokerTestSuite {

  protected static final String URL = "amqp://0.0.0.0:5672";

  private BrokerService brokerService;
  protected Connection connection;

  @Before
  public void setUp() throws Exception {
    if (brokerService != null && brokerService.isStarted()) {
      throw new IllegalStateException("Broker is already created.");
    }

    brokerService = createBroker("localhost", true);
    brokerService.start();
    brokerService.waitUntilStarted();
  }

  @After
  public void shutDown() throws Exception {
    log.info("========== tearDown ==========");
    Exception firstError = null;

    if (connection != null) {
      try {
        connection.close();
      } catch (Exception e) {
        log.warn("Error detected on connection close in tearDown: {}", e.getMessage());
        firstError = e;
      }
    }

    try {
      stopPrimaryBroker();
    } catch (Exception e) {
      log.warn("Error detected on close of broker in tearDown: {}", e.getMessage());
      firstError = e;
    }

    if (firstError != null) {
      throw firstError;
    }
  }

  protected BrokerService createBroker(String name, boolean deleteAllMessages) throws Exception {
    BrokerService brokerService = new BrokerService();

    brokerService.setBrokerName(name);
    brokerService.setDeleteAllMessagesOnStartup(deleteAllMessages);
    brokerService.setUseJmx(true);
    brokerService.getManagementContext().setCreateConnector(false);
    brokerService.setDataDirectory("target/" + name);
    brokerService.setKeepDurableSubsActive(false);
    brokerService.setPersistent(false);

    addAdditionalConnectors(brokerService);

    return brokerService;
  }

  protected void addAdditionalConnectors(BrokerService brokerService) throws Exception {
    TransportConnector connector = brokerService.addConnector(
        URL +
            "?transport.transformer=" + getAmqpTransformer() +
            "&transport.socketBufferSize=" + getSocketBufferSize() +
            "&transport.tcpNoDelay=true" +
            "&ioBufferSize=" + getIOBufferSize());
    connector.setName("amqp");
  }

  protected String getAmqpTransformer() {
    return "jms";
  }

  protected int getSocketBufferSize() {
    return 64 * 1024;
  }

  protected int getIOBufferSize() {
    return 8 * 1024;
  }

  public void stopPrimaryBroker() throws Exception {
    if (brokerService != null) {
      brokerService.stop();
      brokerService.waitUntilStopped();
    }
  }
}
