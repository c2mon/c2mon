package cern.c2mon.client.core.config.mock;

import cern.c2mon.client.core.jms.JmsProxy;
import org.easymock.EasyMock;
import org.springframework.context.annotation.Bean;

/**
 * @author Justin Lewis Salmon
 */
public class JmsProxyMock {

  @Bean
  public JmsProxy jmsProxy() {
    return EasyMock.createNiceMock(JmsProxy.class);
  }
}
