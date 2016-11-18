package cern.c2mon.server.test.broker;

import org.apache.activemq.broker.BrokerService;
import org.junit.After;
import org.junit.Before;
import org.junit.rules.ExternalResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Justin Lewis Salmon
 */
@Component
public class EmbeddedBrokerRule extends ExternalResource {

  @Autowired
  private BrokerService broker;

  @Before
  public void before() {
    try {
      broker.start();
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }

  @After
  public void after() {
    try {
      broker.stop();
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }
}
