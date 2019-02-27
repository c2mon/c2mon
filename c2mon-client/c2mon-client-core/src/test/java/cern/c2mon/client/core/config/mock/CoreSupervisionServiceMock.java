package cern.c2mon.client.core.config.mock;

import org.easymock.EasyMock;
import org.springframework.context.annotation.Bean;

import cern.c2mon.client.core.service.impl.SupervisionServiceImpl;

/**
 * @author Justin Lewis Salmon
 */
public class CoreSupervisionServiceMock {

  @Bean
  public SupervisionServiceImpl supervisionServiceImpl() {
    return EasyMock.createMock(SupervisionServiceImpl.class);
  }
}
