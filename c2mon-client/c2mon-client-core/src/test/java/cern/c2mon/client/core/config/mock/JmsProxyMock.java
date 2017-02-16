package cern.c2mon.client.core.config.mock;

import org.easymock.EasyMock;
import org.springframework.context.annotation.Bean;

import cern.c2mon.client.core.jms.impl.JmsProxy;

/**
 * @author Justin Lewis Salmon
 */
public class JmsProxyMock {

  @Bean
  public JmsProxy jmsProxy() {
    return EasyMock.createNiceMock(JmsProxy.class);
  }
}
